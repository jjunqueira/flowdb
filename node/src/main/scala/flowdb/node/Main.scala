package flowdb.node

import java.nio.file.Paths
import java.time.{Instant, ZoneOffset, ZonedDateTime}

import akka.actor.typed.{ActorRef, ActorSystem}
import com.comcast.ip4s.IpAddress
import com.typesafe.config.ConfigFactory
import flowdb.common.command.Database.WriteProtocol
import flowdb.common.model.RawRecord
import flowdb.node.WriteManager._

import scala.concurrent.Await
import scala.concurrent.duration._

object Main {

  @SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
  def main(args: Array[String]): Unit = {
    val system: ActorSystem[WriteManager.WriteManagementProtocol] =
      ActorSystem(WriteManager.behavior(Map.empty[String, ActorRef[WriteProtocol]]), "hello", ConfigFactory.load())

    val instant = Instant.now()
    val time = ZonedDateTime.ofInstant(instant, ZoneOffset.UTC)
    val day = s"${time.getYear}${time.getMonthValue}${time.getDayOfMonth}"

    system ! WriteManager.Spawn(Paths.get("/tmp/testdb"), day)
    Thread.sleep(500)

    IpAddress("127.0.0.1").foreach { ip =>
      system ! Write(RawRecord(Instant.now().toEpochMilli, ip.toBytes, ip.toBytes))
    }

    scala.sys.addShutdownHook {
      system.log.info("Terminating...")
      system ! GracefulShutdown
      Await.result(system.whenTerminated, 30.seconds)
      ()
    }
    ()
  }

}
