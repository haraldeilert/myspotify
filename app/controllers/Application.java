package controllers;

import com.wrapper.spotify.models.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.List;

public class Application extends Controller {

    public static final String DATA = "data";

    public static Result index() {
        String url = SpotifyWebApi.getAuthorizeURL();
        return redirect(url);
    }

    public static Result callback(String code) {
        String accessToken = SpotifyWebApi.getAccessToken(code);
        return ok(views.html.playlists.render(accessToken));
    }

    public static Result getPlayList(String accessToken) {
        User user = SpotifyWebApi.getCurrentUser(accessToken);
        Page<SimplePlaylist> playlistPage = SpotifyWebApi.getPlaylistsForUser(user.getId(), accessToken);
        JSONArray mainArray = new JSONArray();

        for (SimplePlaylist playlist : playlistPage.getItems()) {
            Page<PlaylistTrack> page = SpotifyWebApi.getTracksFromPlayList(user.getId(), playlist.getId(), accessToken);
            if (page != null) {
                try {
                    List<PlaylistTrack> playlistTracks = page.getItems();
                    for (PlaylistTrack playlistTrack : playlistTracks) {
                        List<SimpleArtist> simpleArtistList = playlistTrack.getTrack().getArtists();
                        String artists = "";
                        //Loop through the artist list and make a string
                        for (SimpleArtist simpleArtist : simpleArtistList) {
                            if (!"".equals(artists))
                                artists += ", ";
                            if (simpleArtist.getName() != null && !"".equals(simpleArtist.getName()))
                                artists += simpleArtist.getName();
                        }
                        JSONArray playListArray = new JSONArray();
                        playListArray.add(playlistTrack.getTrack().getName());
                        playListArray.add(artists);
                        playListArray.add(playlistTrack.getTrack().getAlbum().getName());
                        playListArray.add(playlist.getName());
                        mainArray.add(playListArray);
                    }

                } catch (Exception e) {
                   Logger.error(e.getMessage());
                }
            }else{
                Logger.info("Track for playlist " + playlist.getName() +" cannot be fetched since it's collaborative.");
                JSONArray playListArray = new JSONArray();
                playListArray.add("collaborative-list456");
                playListArray.add("");
                playListArray.add("");
                playListArray.add(playlist.getName());
                mainArray.add(playListArray);
            }
        }
        JSONObject data = new JSONObject();
        data.put(DATA, mainArray);
        return ok(data.toJSONString());
    }
}