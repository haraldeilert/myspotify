package controllers;

import com.wrapper.spotify.models.User;
import play.libs.EventSource;
import play.libs.F;
import play.libs.WS;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;

import java.util.concurrent.TimeUnit;

public class Application extends Controller {

    public static Result index() {
        return ok(views.html.index.render("Connect to Spotify"));
    }

    public static Result loginSpotify() {
        String url = CurrentUser.getAuthorizeURL();
        return redirect(url);
    }

    public static F.Promise<Result> callback(String code) {
        String accessToken = CurrentUser.getAccessToken(code);
        User user = CurrentUser.getCurrentUser(accessToken);
        CurrentUser.getPlaylistsForUser(user.getId(), accessToken);
        return F.Promise.promise(() -> ok("User name: " + user.getId()));
    }
}
