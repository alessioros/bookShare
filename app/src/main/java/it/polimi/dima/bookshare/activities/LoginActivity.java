package it.polimi.dima.bookshare.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

import it.polimi.dima.bookshare.amazon.CognitoSyncClientManager;
import it.polimi.dima.bookshare.amazon.DynamoDBManagerTask;
import it.polimi.dima.bookshare.amazon.DynamoDBManagerType;
import it.polimi.dima.bookshare.R;
import it.polimi.dima.bookshare.tables.User;

public class LoginActivity extends AppCompatActivity {

    private CallbackManager callbackManager;
    private User user;
    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sp=PreferenceManager.getDefaultSharedPreferences(this);
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        /**
         * Initializes the sync client. This must be call before you can use it.
         */
        CognitoSyncClientManager.init(this);


        //If access token is already here, set fb session
        final AccessToken fbAccessToken = AccessToken.getCurrentAccessToken();
        if (fbAccessToken != null) {
            setFacebookSession(fbAccessToken);
            Intent i = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(i);
            finish();
        }

        TextView title = (TextView) findViewById(R.id.logo);
        EditText email = (EditText) findViewById(R.id.email);
        EditText password = (EditText) findViewById(R.id.password);
        TextView register = (TextView) findViewById(R.id.register);
        Button login = (Button) findViewById(R.id.login_button);
        Button loginFB = (Button) findViewById(R.id.login_fb);

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

                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
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

                        new GetFbName(loginResult).execute();
                        setFacebookSession(loginResult.getAccessToken());
                        Intent i = new Intent(LoginActivity.this, MainActivity.class);
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

        if(!sp.getBoolean("Registered",false)) {

            user=new User();

            try {

                user.setUserID(Profile.getCurrentProfile().getId());
                user.setName(Profile.getCurrentProfile().getFirstName());
                user.setSurname(Profile.getCurrentProfile().getLastName());
                user.setImgURL(Profile.getCurrentProfile().getProfilePictureUri(200, 200).toString());
                user.setCity("Milano");
                new DynamoDBManagerTask(LoginActivity.this, null, user).execute(DynamoDBManagerType.INSERT_USER);
                sp.edit().putBoolean("Registered", true).apply();

            }catch (Exception e){
                e.printStackTrace();
            }
        }
        new SaveCredentials().execute();
    }

    private class GetFbName extends AsyncTask<Void, Void, String> {
        private final LoginResult loginResult;
        private ProgressDialog dialog;

        public GetFbName(LoginResult loginResult) {
            this.loginResult = loginResult;
        }

        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(LoginActivity.this, "Wait", "Getting user name");
        }

        @Override
        protected String doInBackground(Void... params) {
            GraphRequest request = GraphRequest.newMeRequest(
                    loginResult.getAccessToken(),
                    new GraphRequest.GraphJSONObjectCallback() {
                        @Override
                        public void onCompleted(
                                JSONObject object,
                                GraphResponse response) {
                            // Application code
                            Log.v("LoginActivity", response.toString());
                        }
                    });
            Bundle parameters = new Bundle();
            parameters.putString("fields", "name");
            request.setParameters(parameters);
            GraphResponse graphResponse = request.executeAndWait();
            try {
                return graphResponse.getJSONObject().getString("name");
            } catch (JSONException e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String response) {

            if (response != null) {
                Toast.makeText(LoginActivity.this, "Hello " + response, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(LoginActivity.this, "Unable to get user name from Facebook",
                        Toast.LENGTH_LONG).show();
            }
        }
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
