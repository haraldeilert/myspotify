package controllers;

import com.wrapper.spotify.models.*;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.List;

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

        String jsonStr = "{\"data\": [";
        boolean first = true;
        for (SimplePlaylist playlist : playlistPage.getItems()) {

            Page<PlaylistTrack> page = CurrentUser.getTracksFromPlayList(user.getId(), playlist.getId(), accessToken);

            if (page != null) {

                try {
                    final List<PlaylistTrack> playlistTracks = page.getItems();

                    for (PlaylistTrack playlistTrack : playlistTracks) {
                        List<SimpleArtist> simpleArtistList = playlistTrack.getTrack().getArtists();
                        String artists = "";


                        for (SimpleArtist simpleArtist : simpleArtistList) {
                            if (!"".equals(artists))
                                artists += ", ";
                            if (simpleArtist.getName() != null && !"".equals(simpleArtist.getName()))
                                artists += simpleArtist.getName();
                        }
                        if (!first)
                            jsonStr += ", ";

                        jsonStr += "[" +
                                "      \"" + playlistTrack.getTrack().getName() + "\"," +
                                "      \"" + artists + "\"," +
                                "      \"" + playlistTrack.getTrack().getAlbum().getName() + "\"," +
                                "      \"" + playlist.getName() +
                                "\"   ]";

                        first = false;
                    }

                } catch (Exception e) {
                    System.out.println("Something went wrong!" + e.getMessage());
                }
            } else {
                System.out.println("*****page is null: " + playlist.getName());
            }
        }


        jsonStr += "]" +
                "}";

        return ok(jsonStr);

    }
}
