package flowdb.node

import java.nio.file.{Path, Paths}
import java.time.{Instant, ZoneOffset, ZonedDateTime}

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior, PostStop}
import flowdb.common.command.Database.{CloseDatabase, WriteProtocol, WriteToDatabase}
import flowdb.common.model.RawRecord

object WriteManager {

  sealed trait WriteManagementProtocol
  final case class Spawn(basePath: Path, date: String) extends WriteManagementProtocol
  final case class Write(record: RawRecord) extends WriteManagementProtocol
  final case object GracefulShutdown extends WriteManagementProtocol

  @SuppressWarnings(
    Array("org.wartremover.warts.Recursion", "org.wartremover.warts.Nothing", "org.wartremover.warts.ToString"))
  def behavior(instances: Map[String, ActorRef[WriteProtocol]]): Behavior[WriteManagementProtocol] =
    Behaviors
      .receive[WriteManagementProtocol] { (ctx, msg) =>
        msg match {
          case Spawn(path, day) =>
            ctx.log.debug(s"Spawning database actor with key $day")
            val ref = ctx.spawn(Database.behavior(Paths.get(path.toString, day)), "database")
            behavior(instances.updated(day, ref))

          case Write(record) =>
            ctx.log.debug("Received write message")
            val key = dateFromEpoch(record.timestamp)
            ctx.log.debug(s"Writing to database $key")
            instances.get(key).foreach { db =>
              db ! WriteToDatabase(record)
            }
            Behaviors.same

          case GracefulShutdown =>
            instances.foreach(_._2 ! CloseDatabase)
            Behaviors.stopped {
              Behaviors.receiveSignal {
                case (context, PostStop) =>
                  context.log.info("Closing database handles")
                  Behaviors.same
              }
            }

        }
      }
      .receiveSignal {
        case (ctx, PostStop) =>
          ctx.log.info("Database manager stopped")
          Behaviors.same
      }

  private def dateFromEpoch(epoch: Long): String = {
    val instant = Instant.ofEpochMilli(epoch)
    val time = ZonedDateTime.ofInstant(instant, ZoneOffset.UTC)
    s"${time.getYear}${time.getMonthValue}${time.getDayOfMonth}"
  }

}
