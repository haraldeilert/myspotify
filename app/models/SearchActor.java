package models;

import akka.actor.UntypedActor;
import play.libs.F;
import play.mvc.WebSocket;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Diversify on 2014-09-23.
 */
public class SearchActor extends UntypedActor {
    // collect all websockets here
    private static List<WebSocket.Out<String>> connections = new ArrayList<WebSocket.Out<String>>();

    public static void start(WebSocket.In<String> in, WebSocket.Out<String> out) {

        connections.add(out);

        in.onMessage(new F.Callback<String>() {
            public void invoke(String event) {
                SearchActor.notifyAll(event);
            }
        });

        in.onClose(new F.Callback0() {
            public void invoke() {
                SearchActor.notifyAll("A connection closed");
            }
        });
    }

    // Iterate connection list and write incoming message
    public static void notifyAll(String message) {
        for (WebSocket.Out<String> out : connections) {
            out.write(message);
        }
    }

    @Override
    public void onReceive(Object message) throws Exception {

    }
}
