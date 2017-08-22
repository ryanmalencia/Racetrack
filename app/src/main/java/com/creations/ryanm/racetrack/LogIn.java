package com.creations.ryanm.racetrack;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import java.io.BufferedReader;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class LogIn extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener,View.OnClickListener{
    private GoogleApiClient mGoogleApiClient;
    private int RC_SIGN_IN = 1024;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this , this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        SignInButton signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_WIDE);
        findViewById(R.id.sign_in_button).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        signIn();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
    }

    private void signIn() {
        System.out.println("signing in");
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        System.out.println("got result");
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            GoogleSignInAccount acct = result.getSignInAccount();
            if(acct != null) {
                String id = acct.getId();
                String name = acct.getDisplayName();
                System.out.println("Signed in: " + name + " " + id);
                try {
                    FileOutputStream fos = openFileOutput("auth", MODE_PRIVATE);
                    OutputStreamWriter osw = new OutputStreamWriter(fos);
                    String output = "";
                    if(id != null){
                        output = output + id + "!";
                    }
                    if(name != null){
                        output = output + name;
                    }
                    osw.write(output);
                    osw.close();
                    fos.close();
                }catch (IOException e){
                    System.out.println("File not written");
                }
                /*if(id != null) {
                    new AddUser().execute(id);
                }*/
            }
        }
        else {
            System.out.println("Failed to sign in");
        }
    }
}
