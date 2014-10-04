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
        // Make the robot talk every 30 seconds
        cancellable = Akka.system().scheduler().schedule(
                Duration.create(30, SECONDS),
                Duration.create(30, SECONDS),
                searchActor,
                new SearchActor.RobotActor(),
                Akka.system().dispatcher(),
                /** sender **/null
        );
    }

    public static void cancelMe(){
        Logger.debug("***cancel me!!");
        if(cancellable != null)
            cancellable.cancel();
    }
}