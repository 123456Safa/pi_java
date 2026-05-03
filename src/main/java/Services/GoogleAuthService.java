package Services;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Userinfo;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;

/**
 * Service responsible for managing Google OAuth 2.0 authentication.
 * Uses the official Google Client Library for Java with a local Jetty
 * server receiver to handle the browser redirect callback.
 */
public class GoogleAuthService {

    private static final String APPLICATION_NAME = "PharmaX";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    /** Scopes required: email and basic profile information. */
    private static final List<String> SCOPES = Arrays.asList(
            "https://www.googleapis.com/auth/userinfo.email",
            "https://www.googleapis.com/auth/userinfo.profile"
    );

    /**
     * Simple POJO to hold the result of a Google authentication.
     */
    public static class GoogleUserInfo {
        private final String email;
        private final String firstName;
        private final String lastName;
        private final String picture;

        public GoogleUserInfo(String email, String firstName, String lastName, String picture) {
            this.email = email;
            this.firstName = firstName;
            this.lastName = lastName;
            this.picture = picture;
        }

        public String getEmail() { return email; }
        public String getFirstName() { return firstName; }
        public String getLastName() { return lastName; }
        public String getPicture() { return picture; }
    }

    /**
     * Runs the full Google OAuth 2.0 flow:
     * 1. Loads client secrets from credentials.json in resources.
     * 2. Starts a local Jetty server on a random port.
     * 3. Opens the default browser to the Google consent screen.
     * 4. Waits for the user to authenticate and consent.
     * 5. Exchanges the authorization code for an access token.
     * 6. Fetches the user's profile (email, first name, last name).
     *
     * @return a GoogleUserInfo containing the authenticated user's details.
     * @throws IOException if credentials.json is missing or network errors occur.
     * @throws GeneralSecurityException if TLS transport cannot be initialized.
     */
    public static GoogleUserInfo authenticate() throws IOException, GeneralSecurityException {
        // 1. Build the HTTP transport
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        // 2. Load client secrets from the bundled credentials.json
        InputStream credentialsStream = GoogleAuthService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (credentialsStream == null) {
            throw new IOException(
                "credentials.json not found in resources. " +
                "Please download it from Google Cloud Console and place it at src/main/resources/credentials.json"
            );
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(credentialsStream));

        // 3. Build the authorization code flow
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
                .setAccessType("offline")
                .build();

        // 4. Start a local server receiver and authorize
        //    This opens the user's default browser to the Google login page.
        //    Once the user completes login, the browser redirects to localhost
        //    where the Jetty server captures the authorization code.
        LocalServerReceiver receiver = new LocalServerReceiver.Builder()
                .setPort(0)  // random available port
                .build();

        Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");

        // 5. Use the credential to build the OAuth2 service and fetch user info
        Oauth2 oauth2Service = new Oauth2.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();

        Userinfo userinfo = oauth2Service.userinfo().get().execute();

        // 6. Map results to our POJO
        return new GoogleUserInfo(
                userinfo.getEmail(),
                userinfo.getGivenName() != null ? userinfo.getGivenName() : "",
                userinfo.getFamilyName() != null ? userinfo.getFamilyName() : "",
                userinfo.getPicture() != null ? userinfo.getPicture() : ""
        );
    }
}
