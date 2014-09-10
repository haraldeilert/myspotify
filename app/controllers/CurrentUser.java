package controllers;

import com.wrapper.spotify.Api;
import com.wrapper.spotify.exceptions.WebApiException;
import com.wrapper.spotify.methods.PlaylistTracksRequest;
import com.wrapper.spotify.methods.UserPlaylistsRequest;
import com.wrapper.spotify.models.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * This example shows how to get information about the user that is 'connected' to the
 * access token. The methods used (api.authorizationCodeGrant and api.getMe) are synchronous, but are
 * available asynchronously as well. The scopes necessary for this example are 'user-read-private'
 * and 'user-read-email'.
 * <p>
 * The authorization flow used is documented in detail at
 * https://developer.spotify.com/spotify-web-api/authorization-guide/#authorization_code_flow
 * <p>
 * Details about requesting the current user's information is documented at
 * https://developer.spotify.com/spotify-web-api/get-users-profile/ in the
 * "Authorization Code" section.
 */
public class CurrentUser {

    /* Application details necessary to get an access token */
    private static final String clientId = "49d41b67c05f4be1980721d19f86ecb2";
    private static final String clientSecret = "7d642cbd53794d58b8b5c93856c2d0d2";
    private static final String redirectUri = "http://myspotify.herokuapp.com/callback";

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

    public static Page<PlaylistTrack> getTracksFromPlayList(String userId, String playListId, String accessToken){
        api.setAccessToken(accessToken);
        PlaylistTracksRequest request = api.getPlaylistTracks(userId, playListId).build();
        System.out.println("******************************request.toStringWithQuery(): " + request.toStringWithQueryParameters());

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

      /* Retrieve information about the user.
      * The amount of information that is set on the User object depends on the scopes that
      * the user has allowed the application to read.
      * Read about which scopes that are available on
      * https://developer.spotify.com/spotify-web-api/get-users-profile/
      */
            currentUser = api.getMe().accessToken(accessToken).build().get();

      /* Use the information about the user */
            System.out.println("URI to currently logged in user is: " + currentUser.getUri());
            System.out.println("The currently logged in user comes from: " + currentUser.getCountry());
            System.out.println("You can reach this user at: " + currentUser.getEmail());


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
        String state = "someExpectedStateString";

        return api.createAuthorizeURL(scopes, state);
    }
}