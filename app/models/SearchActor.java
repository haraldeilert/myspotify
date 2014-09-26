package models;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import play.Logger;
import play.libs.Akka;
import play.mvc.WebSocket;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Harald on 2014-09-23.
 */
public class SearchActor extends UntypedActor {

    // Default room.
    static ActorRef defaultActor = Akka.system().actorOf(Props.create(SearchActor.class));

    // Create a Robot, just for fun.
    static {
        new Robot(defaultActor);
    }

    private static List<WebSocket.Out<String>> connections = new ArrayList<WebSocket.Out<String>>();

    public static void start(WebSocket.In<String> in, WebSocket.Out<String> out) {
        Logger.debug("Add connection");
        connections.add(out);

        in.onMessage(event -> notifyAll(event));
        in.onClose(() -> Logger.debug("Connection closed"));
    }

    // Iterate connection list and write incoming message
    public static void notifyAll(String message) {
        for (WebSocket.Out<String> out : connections) {
            out.write(message);
        }
    }

    @Override
    public void onReceive(Object message) throws Exception {
        notifyAll("Lady gaga");
    }

    public static class Talk {
        public Talk() {}
    }
}
