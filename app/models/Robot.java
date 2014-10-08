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
         * Schedules a Robot to keep the websocket open.
         * Otherwise it will be closed after 60s..
         */
        cancellable = Akka.system().scheduler().schedule(
                Duration.create(55, SECONDS),
                Duration.create(55, SECONDS),
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