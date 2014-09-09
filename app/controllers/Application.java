package controllers;

import com.wrapper.spotify.models.Page;
import com.wrapper.spotify.models.PlaylistTracksInformation;
import com.wrapper.spotify.models.SimplePlaylist;
import com.wrapper.spotify.models.User;
import play.mvc.Controller;
import play.mvc.Result;

public class Application extends Controller {

    public static Result index() {
        String url = CurrentUser.getAuthorizeURL();
        return redirect(url);
    }

    public static Result callback(String code) {
        String accessToken = CurrentUser.getAccessToken(code);
        return ok(views.html.playlists.render(accessToken));
    }

    public static Result getPlayList(String accessToken) {
        User user = CurrentUser.getCurrentUser(accessToken);
        Page<SimplePlaylist> playlistPage = CurrentUser.getPlaylistsForUser(user.getId(), accessToken);

        String test = "{\"data\": [";
        boolean first = false;
        for (SimplePlaylist playlist : playlistPage.getItems()) {

            if (first)
                test += ",";

            PlaylistTracksInformation playlistTracksInformation = playlist.getTracks();

            test += "[\n" +
                    "      \"" + playlist.getOwner() + "\",\n" +
                    "      \"" + playlistTracksInformation.getTotal() + "\",\n" +
                    "      \"" + playlist.getName() + "\"\n" +
                    "    ]";


            first = true;
        }


        test += "]\n" +
                "}";

        return ok(test);

    }
}
