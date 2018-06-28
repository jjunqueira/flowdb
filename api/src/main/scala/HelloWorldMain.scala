import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

object HelloWorldMain {

  final case class Start(name: String)

  val main: Behavior[Start] =
    Behaviors.setup { context ⇒
      @SuppressWarnings(Array("org.wartremover.warts.Nothing"))
      val greeter = context.spawn(HelloWorld.greeter, "greeter")

      Behaviors.receiveMessage { msg ⇒
        @SuppressWarnings(Array("org.wartremover.warts.Nothing"))
        val replyTo = context.spawn(HelloWorldBot.bot(greetingCounter = 0, max = 3), msg.name)
        greeter ! HelloWorld.Greet(msg.name, replyTo)
        Behaviors.same
      }
    }
}
