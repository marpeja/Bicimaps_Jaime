package com.example.osmdroid;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.osmdroid.Extras.Tutorial.TutorialActivity;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private static int SPLASH_SCREEN = 1500;

    TextView name, year;
    ImageView logo;

    Animation top_animation, bottom_animation;


    List<AuthUI.IdpConfig> providers = Arrays.asList(
            new AuthUI.IdpConfig.EmailBuilder().build(),
            new AuthUI.IdpConfig.GoogleBuilder().build());
    private static int RC_SIGN_IN = 44;
    private String user;
    private boolean first_access;
    private SharedPreferences.Editor editor;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_home);

        name = findViewById(R.id.name);
        year = findViewById(R.id.year);
        logo = findViewById(R.id.logo);
        top_animation = AnimationUtils.loadAnimation(this, R.anim.top_animation);
        bottom_animation = AnimationUtils.loadAnimation(this, R.anim.bottom_animation);

        name.setAnimation(bottom_animation);
        year.setAnimation(bottom_animation);
        logo.setAnimation(top_animation);

        SharedPreferences config = getPreferences(Context.MODE_PRIVATE);
        editor = config.edit();
        first_access = config.getBoolean("first_access", true);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.useAppLanguage();
        if(auth.getCurrentUser() == null){

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    showSignInOptions();
                }
            }, SPLASH_SCREEN);

        } else {
            user = auth.getCurrentUser().getEmail();
            Log.i("FIREBASE_AUTH", user);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }, SPLASH_SCREEN);
        }
    }

    public void showSignInOptions() {
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setTheme(R.style.AppTheme_NoActionBar_Background)
                        .setLogo(R.drawable.ic_bicimap)
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                Log.i("FIREBASE_AUTH", user.getDisplayName());
                Log.i("FIREBASE_AUTH", user.getEmail());

                if(first_access) {
                    editor.putBoolean("first_access", false);
                    editor.commit();
                    Intent intent = new Intent(HomeActivity.this, TutorialActivity.class);
                    intent.putExtra("from_home", true);
                    startActivity(intent);
                    finish();

                } else {
                    Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();

                }

            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                finish();
            }
        }
    }
}
