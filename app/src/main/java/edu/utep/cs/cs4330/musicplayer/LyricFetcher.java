package edu.utep.cs.cs4330.musicplayer;

import com.github.scribejava.apis.GeniusApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class LyricFetcher {
    public String doThing(){
        String clientId = "A6TUo5x_o84rgmnegSeME_toVmfj8QzV8TruDKeL0hAbPnB1TahmnIiXspVUs4W4";
        String clientSecret = "1ViW0RoSWsIgB247kz8JY9XzCVvRdpJiRwmtRlxL6Svw6bUYpP9G5f2OIFuwkCx4x4hRi0TsIv7XL34CKv1V5A";
        String secretState = "1";
        final OAuth20Service service = new ServiceBuilder(clientId)
                .apiSecret(clientSecret)
                .scope("me")
                .state(secretState)
                .callback("http://example.com/")
                .build(GeniusApi.instance());
        String auth = service.getAuthorizationUrl();
        return auth;
    }

}
