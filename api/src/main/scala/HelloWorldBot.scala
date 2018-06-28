import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import cats.implicits._

object HelloWorldBot {

  @SuppressWarnings(Array("org.wartremover.warts.Recursion"))
  def bot(greetingCounter: Int, max: Int): Behavior[HelloWorld.Greeted] =
    Behaviors.receive { (ctx, msg) ⇒
      val n = greetingCounter + 1
      ctx.log.info("Greeting {} for {}", n, msg.whom)
      if (n === max) {
        Behaviors.stopped
      } else {
        msg.from ! HelloWorld.Greet(msg.whom, ctx.self)
        bot(n, max)
      }
    }
}
