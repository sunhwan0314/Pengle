package com.tmdghks.ecogreen;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import com.airbnb.lottie.LottieAnimationView;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        LottieAnimationView animationView = findViewById(R.id.lottie);
        animationView.setAnimation(R.raw.penguinsplash); // 애니메이션 설정
        animationView.playAnimation(); // 자동 재생

        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish(); // 스플래시 종료 후 메인으로 이동
        }, 2000); // 2초 후 화면 전환
    }
}