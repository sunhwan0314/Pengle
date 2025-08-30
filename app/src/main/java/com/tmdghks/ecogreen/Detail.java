package com.tmdghks.ecogreen;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class Detail extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail); // activity_detail.xml 레이아웃 파일 생성 필요
        // 인텐트로부터 전달된 데이터 처리 (예: itemName, itemPoints)
        String itemName = getIntent().getStringExtra("itemName");
        String itemPoints = getIntent().getStringExtra("itemPoints");
        TextView nameText = findViewById(R.id.detail_item_name);
        TextView pointText = findViewById(R.id.detail_item_points);
        nameText.setText(itemName);
        pointText.setText(itemPoints);

        // UI에 데이터 표시하는 로직 (TextView 등 사용)
    }

}