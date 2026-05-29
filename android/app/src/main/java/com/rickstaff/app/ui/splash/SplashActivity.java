package com.rickstaff.app.ui.splash;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import com.rickstaff.app.MainActivity;
import com.rickstaff.app.R;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        LinearLayout container = findViewById(R.id.splashContainer);
        startAnimation(container);
    }

    private void startAnimation(LinearLayout container) {
        container.setScaleX(0.5f);
        container.setScaleY(0.5f);
        container.setAlpha(0f);

        container.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(700)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(this::navigateNext)
                .start();
    }

    private void navigateNext() {
        new android.os.Handler(getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(this, MainActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }, 300);
    }
}