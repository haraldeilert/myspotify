/*
 * Copyright (c) 2014.
 */
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
 * The Actor Model provides a higher level of abstraction for writing concurrent and distributed systems.
 * It alleviates the developer from having to deal with explicit locking and thread management,
 * making it easier to write correct concurrent and parallel systems.
 *
 * @author Harald Eilert
 */
public class SearchActor extends UntypedActor {

    //To keep the websocket alive we have a robot that is pretending to search with these strings
    private static final List<String> robotList = new ArrayList<String>(Arrays.asList("Kendrick Lamar", "Big L", "Schoolboy Q", "Mos def", "EPMD", "Nas", "Jay-Rock", "Petter", "Kent", "Bruce Springsteen", "Gang Starr", "Robyn"));

    private static ActorRef searchActor = Akka.system().actorOf(Props.create(SearchActor.class));

    private static Robot robotKeepingWebSocketAlive = new Robot(searchActor);

    private static List<WebSocket.Out<String>> connectedClients = new ArrayList<WebSocket.Out<String>>();

    public static void start(WebSocket.In<String> in, WebSocket.Out<String> out) {
        Logger.debug("Add connection");
        connectedClients.add(out);
        if (robotKeepingWebSocketAlive == null)
            robotKeepingWebSocketAlive = new Robot(searchActor);

        in.onMessage(message -> notifyAll(message, false));
        in.onClose(() -> closeConnection());

        Logger.debug("Number of connected clients: " + connectedClients.size());
    }

    private static void closeConnection() {
        Logger.debug("One client has closed a connection, remove from list.");
        try {
            connectedClients.remove(connectedClients.size() - 1);
        } catch (Exception e) {
            Logger.error("Failed to remove client from connection");
        }
    }

    // Iterate connection list and write incoming message
    public static void notifyAll(String message, boolean robot) {
        //If there are no connected clients, no need to keep the Robot alive..
        if (connectedClients == null || connectedClients.size() < 1) {
            robotKeepingWebSocketAlive.cancel();
            robotKeepingWebSocketAlive = null;
        } else {

            if (robot)
                Logger.debug("Robot pretending to make a search but basically just makes sure the websocket isn't closed!");
            else
                Logger.debug("Hey a live user searching on: " + message + ". Lets push it out to all " + connectedClients.size() + " clients!");

            //Push out message to all clients
            for (WebSocket.Out<String> out : connectedClients) {
                out.write(message);
            }
        }
    }

    @Override
    public void onReceive(Object message) throws Exception {
        Random randomizer = new Random();
        String random = robotList.get(randomizer.nextInt(robotList.size()));
        notifyAll(random, true);
    }
}
