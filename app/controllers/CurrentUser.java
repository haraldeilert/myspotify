package controllers;

import com.wrapper.spotify.Api;
import com.wrapper.spotify.exceptions.WebApiException;
import com.wrapper.spotify.methods.PlaylistTracksRequest;
import com.wrapper.spotify.methods.UserPlaylistsRequest;
import com.wrapper.spotify.models.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class CurrentUser {

    /* Application details necessary to get an access token */
    private static final String clientId = "49d41b67c05f4be1980721d19f86ecb2";
    private static final String clientSecret = "7d642cbd53794d58b8b5c93856c2d0d2";
    private static final String redirectUri = "http://myspotify.herokuapp.com/callback";
    //private static final String redirectUri = "http://localhost:9000/callback";

    /* Create a default API instance that will be used to make requests to Spotify */
    private static Api api = Api.builder().clientId(clientId).clientSecret(clientSecret).redirectURI(redirectUri).build();

    /* Retrieve an access token.
     * The token response contains a refresh token, an accesstoken, and some other things.
     * We need the access token to retrieve the user's information.
     */
    public static String getAccessToken(String code) {
        AuthorizationCodeCredentials authorizationCodeCredentials = null;
        try {
            authorizationCodeCredentials = api.authorizationCodeGrant(code).build().get();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (WebApiException e) {
            e.printStackTrace();
        }
        return authorizationCodeCredentials.getAccessToken();

    }

    public static Page<PlaylistTrack> getTracksFromPlayList(String userId, String playListId, String accessToken) {
        api.setAccessToken(accessToken);
        PlaylistTracksRequest request = api.getPlaylistTracks(userId, playListId).build();
        try {
            return request.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Page<SimplePlaylist> getPlaylistsForUser(String userId, String accessToken) {
        api.setAccessToken(accessToken);
        UserPlaylistsRequest request = api.getPlaylistsForUser(userId).build();
        Page<SimplePlaylist> playlistsPage = null;
        try {
            playlistsPage = request.get();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Something went wrong!" + e.getMessage());
        }
        return playlistsPage;
    }

    public static User getCurrentUser(String accessToken) {
        User currentUser = null;
        try {
            currentUser = api.getMe().accessToken(accessToken).build().get();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Something went wrong." + e.getMessage());
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
}