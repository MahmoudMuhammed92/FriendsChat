package com.example.sahil.friendschat;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * Created by SAHIL on 4/7/2017.
 */

public class SplashScreen extends Activity {

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Window window = getWindow();
        window.setFormat(PixelFormat.RGBA_8888);
    }

    Thread splashThread;

    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_splashscreen);
        StartAnimation();

        progressBar();
    }

    private void progressBar() {

        Handler handel = new Handler();
        handel.postDelayed(new Runnable() {
            @Override
            public void run() {
            }
        },600);
    }

    private void StartAnimation() {

        Animation anim = AnimationUtils.loadAnimation(this, R.anim.alpha);
        anim.reset();
        LinearLayout l = (LinearLayout) findViewById(R.id.lin_lay);
        l.clearAnimation();
        l.startAnimation(anim);

        anim = AnimationUtils.loadAnimation(this, R.anim.translate);
        anim.reset();

        ImageView iv = (ImageView) findViewById(R.id.splash);
        iv.clearAnimation();
        iv.startAnimation(anim);

        splashThread = new Thread() {
            @Override
            public void run() {
                try {
                    int wait = 0;
                    while (wait < 6000) {
                        sleep(80);
                        wait += 50;
                    }
                    Intent intent = new Intent(SplashScreen.this, MainActivity.class);
                    intent.setFlags(intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                    SplashScreen.this.finish();
                } catch (InterruptedException e) {
                    //do nothing
                }finally {
                    SplashScreen.this.finish();
                }
            }
        };
        splashThread.start();
    }
}

