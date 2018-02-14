package com.example.mohamed.store;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;


public class SplashActivity extends AppCompatActivity{

    /** Duration of wait **/
    private final int SPLASH_DISPLAY_LENGTH = 3000;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if (savedInstanceState == null){
            /*
         *  New Handler to start the Menu-Activity
         *  and close this Splash-Screen after some seconds.
         */
            new Handler().postDelayed(new Runnable(){

                @Override
                public void run() {

                /* Create an Intent that will start the Main-Activity. */
                    Intent intent = new Intent(SplashActivity.this,MainActivity.class);
                    startActivity(intent);

                }

            }, SPLASH_DISPLAY_LENGTH);
        }


    }
}
