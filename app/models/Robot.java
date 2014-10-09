package models;

import akka.actor.ActorRef;
import akka.actor.Cancellable;
import play.Logger;
import play.libs.Akka;
import scala.concurrent.duration.Duration;

import static java.util.concurrent.TimeUnit.SECONDS;

public class Robot {

    private static Cancellable cancellable;

    public Robot(ActorRef searchActor) {
        Logger.debug("Initaliazing Robot");
        /**
         * On Heroku the websocket will be closed after 55s if no message is sent in either direction.
         * Read more here <a href="https://devcenter.heroku.com/articles/http-routing#timeouts">here</a>.
         *
         * Therefore we need to schedule a Robot to keep the websocket alive.
         */
        cancellable = Akka.system().scheduler().schedule(
                Duration.create(50, SECONDS),
                Duration.create(50, SECONDS),
                searchActor,
                "",
                Akka.system().dispatcher(),
                /** sender **/null
        );
    }

    public static void cancel(){
        Logger.debug("No more clients connected, cancel Robot instance.");
        boolean cancelled = false;
        if(cancellable != null)
            cancelled = cancellable.cancel();

        Logger.debug("Cancel success: " + cancelled);
    }
}