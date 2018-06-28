import akka.actor.typed.ActorSystem
import com.typesafe.config.ConfigFactory

object Main {

  def main(args: Array[String]): Unit = {
    startup(Seq("2551", "2552"))
  }

  def startup(ports: Seq[String]): Unit = {
    ports foreach { port =>
      val config = ConfigFactory.parseString(s"""
        akka.remote.artery.canonical.port=$port
        """).withFallback(ConfigFactory.load())

      val system: ActorSystem[HelloWorldMain.Start] =
        ActorSystem(HelloWorldMain.main, "hello", config)

      system ! HelloWorldMain.Start("World")
      system ! HelloWorldMain.Start("Akka")
    }
  }

}
