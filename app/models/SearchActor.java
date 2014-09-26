package models;

import akka.actor.UntypedActor;
import play.Logger;
import play.mvc.WebSocket;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Harald on 2014-09-23.
 */
public class SearchActor extends UntypedActor {
    private static List<WebSocket.Out<String>> connections = new ArrayList<WebSocket.Out<String>>();

    public static void start(WebSocket.In<String> in, WebSocket.Out<String> out) {
        Logger.debug("Add connection");
        connections.add(out);

        in.onMessage(event -> notifyAll(event));
        in.onClose(() -> Logger.debug("Connection closed"));
    }

    // Iterate connection list and write incoming message
    private static void notifyAll(String message) {
        for (WebSocket.Out<String> out : connections) {
            out.write(message);
        }
    }

    @Override
    public void onReceive(Object message) throws Exception {
        Logger.debug("onReceive" + message.toString());
    }
}
