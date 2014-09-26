package models;

import akka.actor.ActorRef;
import play.libs.Akka;
import scala.concurrent.duration.Duration;

import static java.util.concurrent.TimeUnit.SECONDS;

public class Robot {

    public Robot(ActorRef searchActor) {
        // Make the robot talk every 30 seconds
        Akka.system().scheduler().schedule(
                Duration.create(30, SECONDS),
                Duration.create(30, SECONDS),
                searchActor,
                new SearchActor.Talk(),
                Akka.system().dispatcher(),
                /** sender **/null
        );
    }
}