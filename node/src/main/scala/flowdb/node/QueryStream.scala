package flowdb.node

import java.nio.file.{Files, Path}

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import akka.stream._
import akka.stream.scaladsl.{Keep, Source, StreamRefs}
import flowdb.common.command.Reads.Query
import flowdb.common.model.RawRecord
import org.rocksdb.{Options, RocksDB}

import scala.collection.JavaConverters._
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@SuppressWarnings(
  Array("org.wartremover.warts.ToString", "org.wartremover.warts.ImplicitParameter", "org.wartremover.warts.Any"))
object QueryStream {

  sealed trait QueryRequestorProtocol
  final case class Opened(sourceRef: SourceRef[RawRecord]) extends QueryRequestorProtocol
  final case object StreamClosed extends QueryRequestorProtocol
  final case class StreamError(msg: String) extends QueryRequestorProtocol

  sealed trait QueryStreamProtocol
  final case class StartStream(query: Query, replyTo: ActorRef[QueryRequestorProtocol]) extends QueryStreamProtocol
  final case object CloseStream extends QueryStreamProtocol

  def behavior(basePath: Path)(implicit mat: ActorMaterializer, ec: ExecutionContext): Behavior[QueryStreamProtocol] = {
    val opts = new Options().setCreateIfMissing(true)
    val paths = Files.list(basePath).iterator().asScala
    val dbHandles = paths.map(p => RocksDB.open(opts, p.toString)).toSeq
    start(dbHandles)
  }

  def start(databaseHandles: Seq[RocksDB])(implicit mat: ActorMaterializer,
                                           ec: ExecutionContext): Behavior[QueryStreamProtocol] =
    Behaviors.receive[QueryStreamProtocol] { (ctx, msg) =>
      msg match {

        case StartStream(query, sender) =>
          ctx.log.info(s"Starting query stream for ${sender.path}")
          val source = constructSource(query, databaseHandles)
          source._2.onComplete {
            case Success(opened) => sender ! opened
            case Failure(e)      => ctx.log.error(e.getLocalizedMessage)
          }
          opened(databaseHandles, source)

        case CloseStream =>
          databaseHandles.foreach(_.close())
          Behaviors.stopped

      }
    }

  def opened(databaseHandles: Seq[RocksDB], source: (KillSwitch, Future[Opened])): Behavior[QueryStreamProtocol] =
    Behaviors.receive[QueryStreamProtocol] { (ctx, msg) =>
      msg match {

        case StartStream(_, sender) =>
          ctx.log.info(s"${sender.path} requested stream but it was already opened. Ignoring")
          Behaviors.same

        case CloseStream =>
          source._1.shutdown()
          databaseHandles.foreach(_.close())
          Behaviors.stopped

      }
    }

  def constructSource(query: Query, dbs: Seq[RocksDB])(implicit mat: ActorMaterializer,
                                                       ec: ExecutionContext): (KillSwitch, Future[Opened]) = {
    dbs.foreach(db => println(db.getLatestSequenceNumber))
    println(query)
    val testRecords = Seq(RawRecord(0L, "Hello".getBytes, "World".getBytes)).iterator
    val countingSrc = Source
      .fromIterator(() => testRecords)
      .delay(1.second, DelayOverflowStrategy.backpressure)
    val (killSwitch, last) = countingSrc
      .viaMat(KillSwitches.single)(Keep.right)
      .toMat(StreamRefs.sourceRef())(Keep.both)
      .run()
    (killSwitch, last.map(Opened))
  }

}
