package it.polimi.dima.bookshare.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import java.util.Arrays;

import it.polimi.dima.bookshare.R;
import it.polimi.dima.bookshare.amazon.CognitoSyncClientManager;
import it.polimi.dima.bookshare.services.RegistrationIntentService;

public class LoginActivity extends AppCompatActivity {

    private CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        CognitoSyncClientManager.init(this);


        //If access token is already here, set fb session
        final AccessToken fbAccessToken = AccessToken.getCurrentAccessToken();
        if (fbAccessToken != null) {
            setFacebookSession(fbAccessToken);
            Intent i = new Intent(LoginActivity.this, SplashScreen.class);
            startActivity(i);
            finish();
        }

        TextView title = (TextView) findViewById(R.id.logo);
        EditText email = (EditText) findViewById(R.id.email);
        EditText password = (EditText) findViewById(R.id.password);
        TextView register = (TextView) findViewById(R.id.register);
        Button login = (Button) findViewById(R.id.login_button);
        Button loginFB = (Button) findViewById(R.id.login_fb);

        /* ------- TEMPORARY INVISIBLE */
        ImageView emailImg = (ImageView) findViewById(R.id.email_icon);
        ImageView passImg = (ImageView) findViewById(R.id.password_icon);

        emailImg.setVisibility(View.INVISIBLE);
        passImg.setVisibility(View.INVISIBLE);
        login.setVisibility(View.INVISIBLE);
        password.setVisibility(View.INVISIBLE);
        email.setVisibility(View.INVISIBLE);
        register.setVisibility(View.INVISIBLE);

        /* ------- */

        Typeface zaguatica = Typeface.createFromAsset(getAssets(), "fonts/zaguatica-Bold.otf");
        Typeface aller = Typeface.createFromAsset(getAssets(), "fonts/Aller_Rg.ttf");

        title.setTypeface(zaguatica);
        email.setTypeface(aller);
        password.setTypeface(aller);
        register.setTypeface(aller);
        login.setTypeface(aller);
        loginFB.setTypeface(aller);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(LoginActivity.this, SplashScreen.class);
                startActivity(intent);

                LoginActivity.this.finish();
            }
        });

        loginFB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // start Facebook Login
                LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this, Arrays.asList("public_profile"));
                LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {

                        setFacebookSession(loginResult.getAccessToken());
                        Intent i = new Intent(LoginActivity.this, SplashScreen.class);

                        startActivity(i);
                        finish();
                    }

                    @Override
                    public void onCancel() {
                        Toast.makeText(LoginActivity.this, "Facebook login cancelled",
                                Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(FacebookException error) {
                        Toast.makeText(LoginActivity.this, "Error in Facebook login " +
                                error.getMessage(), Toast.LENGTH_LONG).show();
                        if (error instanceof FacebookAuthorizationException) {
                            if (AccessToken.getCurrentAccessToken() != null) {
                                LoginManager.getInstance().logOut();
                            }
                        }
                    }
                });
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void setFacebookSession(AccessToken accessToken) {

        Log.i("Token", "facebook token: " + accessToken.getToken());

        CognitoSyncClientManager.addLogins("graph.facebook.com", accessToken.getToken());
        new SaveCredentials().execute();

    }

    private class SaveCredentials extends AsyncTask<Void, Void, String> {

        public SaveCredentials() {

        }

        @Override
        protected String doInBackground(Void... params) {
            CognitoSyncClientManager.getCredentialsProvider().getCredentials();
            return "done";
        }

        @Override
        protected void onPostExecute(String response) {

        }
    }


}
