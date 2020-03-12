package de.ur.mi.android.excercises.starter.SplashScreen;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import de.ur.mi.android.excercises.starter.Constants.Constants;
import de.ur.mi.android.excercises.starter.ListPage;
import de.ur.mi.android.excercises.starter.R;

/**
 * Created by Sabrina Hartl on 11.08.2017.
 */

public class SplashScreen extends Activity {

    //Einbelung eines Willkommen Screens

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
       Thread myThread = new Thread(){
           @Override
            public void run(){
                try{
                    sleep(Constants.SPLASH_TIME_OUT);
                    Intent i = new Intent(getApplicationContext(), ListPage.class);
                    startActivity(i);
                    finish();

                } catch (InterruptedException e) {
                    e.printStackTrace();
                } ;
            }
        };
        myThread.start();

    }
}
