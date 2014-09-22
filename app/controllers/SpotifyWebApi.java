package controllers;

import com.wrapper.spotify.Api;
import com.wrapper.spotify.exceptions.WebApiException;
import com.wrapper.spotify.methods.*;
import com.wrapper.spotify.models.*;
import play.Logger;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class SpotifyWebApi {

    /* Application details necessary to get an access token */
    private static final String clientId = "49d41b67c05f4be1980721d19f86ecb2";
    private static final String clientSecret = "7d642cbd53794d58b8b5c93856c2d0d2";
    private static final String redirectUri = "http://myspotify.herokuapp.com/callback";
    //private static final String redirectUri = "http://localhost:9000/callback";

    /* Create a default API instance that will be used to make requests to Spotify */
    private static Api api = Api.builder().clientId(clientId).clientSecret(clientSecret).redirectURI(redirectUri).build();

    /**
     * Retrieve an access token.
     * The token response contains a refresh token, an accesstoken, and some other things.
     * We need the access token to retrieve the user's information.
     *
     * @param code from Spotify Web Api
     * @return access token
     */
    public static String getAccessToken(String code) {
        AuthorizationCodeCredentials authorizationCodeCredentials = null;
        try {
            authorizationCodeCredentials = api.authorizationCodeGrant(code).build().get();
        } catch (IOException e) {
            Logger.error(e.getMessage());
        } catch (WebApiException e) {
            Logger.error(e.getMessage());
        }
        return authorizationCodeCredentials.getAccessToken();
    }

    /**
     * Get full details of the tracks of a playlist owned by a Spotify user.
     *
     * @param userId
     * @param playListId
     * @param accessToken
     * @return Page<PlaylistTrack>
     */
    public static Page<PlaylistTrack> getTracksFromPlayList(String userId, String playListId, String accessToken) {
        api.setAccessToken(accessToken);
        PlaylistTracksRequest request = api.getPlaylistTracks(userId, playListId).build();
        try {
            return request.get();
        } catch (Exception e) {
            Logger.error(e.getMessage());
        }
        return null;
    }

    /**
     * Get all playlists owned by a Spotify user.
     * <p>
     * Note: only non-collaborative playlists are currently returned by the Web API.
     *
     * @param userId
     * @param accessToken
     * @return Page<SimplePlaylist>
     */
    public static Page<SimplePlaylist> getPlaylistsForUser(String userId, String accessToken) {
        api.setAccessToken(accessToken);
        UserPlaylistsRequest request = api.getPlaylistsForUser(userId).build();
        Page<SimplePlaylist> playlistsPage = null;
        try {
            playlistsPage = request.get();
        } catch (Exception e) {
            Logger.error(e.getMessage());
        }
        return playlistsPage;
    }

    /**
     * Get public profile information about a Spotify user.
     *
     * @param accessToken
     * @return User
     */
    public static User getCurrentUser(String accessToken) {
        User currentUser = null;
        try {
            currentUser = api.getMe().accessToken(accessToken).build().get();
        } catch (Exception e) {
            Logger.error(e.getMessage());
        }
        return currentUser;
    }

    public static String getAuthorizeURL() {
        /* Set the necessary scopes that the applicaiton will need from the user */
        List<String> scopes = Arrays.asList("user-read-private", "user-read-email");
        /* Set a state. This is used to prevent cross site request forgeries. */
        String state = "prod";
        return api.createAuthorizeURL(scopes, state);
    }

    public static List<Track> getTopTracks(String artistId, String countryCode){
        TopTracksRequest topTracksRequest = api.getTopTracksForArtist(artistId, countryCode).build();

        try {
            return topTracksRequest.get();
        } catch (Exception e) {
            Logger.error(e.getMessage());
        }
        return null;
    }

    public static List<Artist> getArtistRelatedArtists(String artistId){
        RelatedArtistsRequest relatedArtistsRequest = api.getArtistRelatedArtists(artistId).build();

        try {
            return relatedArtistsRequest.get();
        } catch (Exception e) {
            Logger.error(e.getMessage());
        }
        return null;
    }

    public static Artist getArtist(String artist) {
        Logger.debug("***Get artist: " + artist);
        ArtistRequest artistRequest = api.getArtist(artist).build();
        try {
            return artistRequest.get();
        } catch (Exception e) {
            Logger.error(e.getMessage());
        }
        return null;
    }

    public static Page<Artist> searchArtists(String query) {
        ArtistSearchRequest artistSearchRequest = api.searchArtists(query).build();
        try {
            return artistSearchRequest.get();
        } catch (Exception e) {
            Logger.error(e.getMessage());
        }
        return null;
    }
}