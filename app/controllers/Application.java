/*
 * Copyright (c) 2014.
 */
package controllers;

import com.wrapper.spotify.models.*;
import models.SearchActor;
import org.apache.commons.lang3.StringEscapeUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;

import java.util.List;

/**
 * This is the main controller class
 * All application routes are defined in the files "routes"
 *
 * Controllers act as mapper between the HTTP world (that is stateless, and request/response based)
 * and the Model layer that is fully object oriented.
 *
 * @author Harald Eilert
 */
public class Application extends Controller {

    @SuppressWarnings("unchecked")
    public static Result index() {
        return ok(views.html.searchartist.render());
    }

    /**
     * To view a personal playlist an authorization is needed
     */
    public static Result personalPlaylist() {
        String url = SpotifyWebApi.getAuthorizeURL();
        return redirect(url);
    }

    /**
     * Callback from the authorization
     */
    public static Result callback(String code) {
        String accessToken = SpotifyWebApi.getAccessToken(code);
        return ok(views.html.playlists.render(accessToken));
    }


    /**
     * Get artist info
     *
     * @param artistId
     * @return JSON response
     */
    public static Result artistInfo(String artistId) {
        //Main Json Object
        JSONObject jsonToReturn = new JSONObject();

        //ARTIST INFO
        Artist artistJson = SpotifyWebApi.getArtist(artistId);
        List<Image> imagesList = artistJson.getImages();
        for (Image image : imagesList) {//Choose an image not larger than 400
            if (image.getHeight() < 400) {
                jsonToReturn.put("url", image.getUrl());
                jsonToReturn.put("height", image.getHeight());
                jsonToReturn.put("width", image.getWidth());
                break;
            }
        }

        //TRACK INFO
        JSONArray jsonArrayTracks = new JSONArray();
        List<Track> artistsTracksList = SpotifyWebApi.getTopTracks(artistId, "SE");
        for (Track track : artistsTracksList) {
            JSONObject jsonObjectTrack = new JSONObject();
            jsonObjectTrack.put("track", track.getName());
            jsonObjectTrack.put("spotifyurl", track.getExternalUrls().get("spotify"));

            jsonArrayTracks.add(jsonObjectTrack);
        }
        jsonToReturn.put("tracks", jsonArrayTracks);

        //RELATED ARTIST INFO
        JSONArray jsonArrayRelatedArtists = new JSONArray();
        List<Artist> relatedArtistList = SpotifyWebApi.getArtistRelatedArtists(artistId);
        for (Artist artist : relatedArtistList) {
            JSONObject jsonObjectArtist = new JSONObject();
            jsonObjectArtist.put("artist", artist.getName());
            jsonObjectArtist.put("artistEncoded", StringEscapeUtils.escapeJava(artist.getName()));
            jsonObjectArtist.put("id", artist.getId());
            jsonArrayRelatedArtists.add(jsonObjectArtist);
        }
        jsonToReturn.put("relatedartists", jsonArrayRelatedArtists);

        //ALBUM INFO
        JSONArray jsonArrayAlbums = new JSONArray();
        Page<SimpleAlbum> artistsAlbum = SpotifyWebApi.getAlbumsForArtist(artistId);
        for (SimpleAlbum simpleAlbum : artistsAlbum.getItems()) {
            JSONObject jsonObjectAlbum = new JSONObject();
            jsonObjectAlbum.put("album", simpleAlbum.getName());
            jsonArrayAlbums.add(jsonObjectAlbum);
        }
        jsonToReturn.put("albums", jsonArrayAlbums);

        return ok(jsonToReturn.toJSONString());
    }

    /**
     * Search for artists
     *
     * @param query
     * @return JSON response
     */
    public static Result searchArtists(String query) {
        JSONArray artistListArray = new JSONArray();

        Page<Artist> artistPage = SpotifyWebApi.searchArtists(query);

        for (Artist artist : artistPage.getItems()) {
            JSONObject artistJson = new JSONObject();
            artistJson.put("name", artist.getName());
            artistJson.put("id", artist.getId());
            artistListArray.add(artistJson);
        }
        return ok(artistListArray.toJSONString());
    }

    /**
     * Fetch all personal playlist
     *
     * @param accessToken
     * @return JSON response
     */
    public static Result getPlayList(String accessToken) {
        User user = SpotifyWebApi.getCurrentUser(accessToken);
        Page<SimplePlaylist> playlistPage = SpotifyWebApi.getPlaylistsForUser(user.getId(), accessToken);
        JSONArray mainArray = new JSONArray();

        for (SimplePlaylist playlist : playlistPage.getItems()) {//loop through all playlist
            Page<PlaylistTrack> page = SpotifyWebApi.getTracksFromPlayList(user.getId(), playlist.getId(), accessToken);
            if (page != null) {
                try {
                    List<PlaylistTrack> playlistTracks = page.getItems();
                    for (PlaylistTrack playlistTrack : playlistTracks) {//loop through tracks in playlist
                        List<SimpleArtist> simpleArtistList = playlistTrack.getTrack().getArtists();
                        String artists = "";
                        //Loop through the artist list and make a string
                        for (SimpleArtist simpleArtist : simpleArtistList) {//loop artists on the track
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
            } else {
                Logger.info("Track for playlist " + playlist.getName() + " cannot be fetched since it's collaborative.");
                JSONArray playListArray = new JSONArray();
                playListArray.add("collaborative-list456");
                playListArray.add("");
                playListArray.add("");
                playListArray.add(playlist.getName());
                mainArray.add(playListArray);
            }
        }
        JSONObject data = new JSONObject();
        data.put("data", mainArray);
        return ok(data.toJSONString());
    }

    // Websocket interface
    public static WebSocket<String> wsInterface() {
        return new WebSocket<String>() {
            // called when websocket handshake is done
            public void onReady(WebSocket.In<String> in, WebSocket.Out<String> out) {
                SearchActor.start(in, out);
            }
        };
    }
}
