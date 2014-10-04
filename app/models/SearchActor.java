package models;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import play.Logger;
import play.libs.Akka;
import play.mvc.WebSocket;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Created by Harald on 2014-09-23.
 */
public class SearchActor extends UntypedActor {

    //To keep the websocket alive we have a robot that is pretending to search with this strings
    private static final List<String> robotList = new ArrayList<String>(Arrays.asList("Kendrick Lamar", "Big L", "Schoolboy Q", "Mos def", "EPMD", "Nas", "Jay-Rock", "Petter", "Kent", "Bruce Springsteen", "Gang Starr", "Robyn"));

    // Default room.
    private static ActorRef defaultActor = Akka.system().actorOf(Props.create(SearchActor.class));

    private static Robot myLittleSearcher = new Robot(defaultActor);

    private static List<WebSocket.Out<String>> connections = new ArrayList<WebSocket.Out<String>>();

    public static void start(WebSocket.In<String> in, WebSocket.Out<String> out) {
        Logger.debug("Add connection");
        connections.add(out);
        if (myLittleSearcher == null) {
            Logger.debug("***recreate Robot");
            myLittleSearcher = new Robot(defaultActor);
        }

        in.onMessage(event -> notifyAll(event));
        in.onClose(() -> closeConnection());
    }

    private static void closeConnection() {
        Logger.debug("One client has closed a connection, remove from list.");
        try {
            connections.remove(connections.size() - 1);
        }catch (Exception e){
            Logger.error("Failed to remove client from connection");
        }
    }

    // Iterate connection list and write incoming message
    public static void notifyAll(String message) {
        Logger.debug("***connections: " + connections.size());
        if (connections == null || connections.size() < 1) {
            myLittleSearcher.cancelMe();
            myLittleSearcher = null;
        }

        for (WebSocket.Out<String> out : connections) {
            out.write(message);
        }
    }

    @Override
    public void onReceive(Object message) throws Exception {
        Logger.debug("Robot keeping websocket alive.");
        //Randomize the robot search or hide it..
        Random randomizer = new Random();
        String random = robotList.get(randomizer.nextInt(robotList.size()));

        notifyAll(random);
    }

    public static class RobotActor {
        public RobotActor() {
        }
    }
}
