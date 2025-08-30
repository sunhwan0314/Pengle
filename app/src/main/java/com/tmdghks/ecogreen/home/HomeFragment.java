package com.tmdghks.ecogreen.home;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.airbnb.lottie.LottieAnimationView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tmdghks.ecogreen.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class HomeFragment extends Fragment {

    private FirebaseAuth mAuth; // Firebase Authentication 인스턴스

    private static final int LOCATION_PERMISSION_CODE = 1001;
    private final String API_KEY = "c4fcb9e7e15778489c59240fc7a7daa3";

    private TextView textPoint, textCheckStatus, textLevelInfo;
    private ProgressBar progressBar;
    private TextView weatherText;
    private ImageView weatherIcon;
    private LinearLayout leafProgressLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        mAuth = FirebaseAuth.getInstance(); // FirebaseAuth 초기화

        leafProgressLayout = view.findViewById(R.id.leafProgressLayout);

        weatherText = view.findViewById(R.id.weatherText);
        weatherIcon = view.findViewById(R.id.weatherIcon);

        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_CODE);
        } else {
            fetchLocationAndWeather();
        }

        ImageButton editBtn = view.findViewById(R.id.btn_edit_checklist);
        editBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), EditChecklistActivity.class);
            startActivity(intent);
        });

        textPoint = view.findViewById(R.id.textPoint);
        textCheckStatus = view.findViewById(R.id.textCheckStatus);
        textLevelInfo = view.findViewById(R.id.textLevelInfo);

        updatePointText();
        updateLevelInfo(view);
        loadChecklist(view);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getView() != null) {
            loadChecklist(getView());
            updatePointText();
            updateLevelInfo(getView());
        }
    }

    public void loadChecklist(View view) {
        SharedPreferences prefs = requireContext().getSharedPreferences("ChecklistPrefs", Context.MODE_PRIVATE);
        String json = prefs.getString("checklist_json", null);
        ArrayList<String> list = new ArrayList<>();
        if (json != null) {
            try {
                JSONArray jsonArray = new JSONArray(json);
                for (int i = 0; i < jsonArray.length(); i++) {
                    list.add(jsonArray.getString(i));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        LinearLayout container = view.findViewById(R.id.checklistContainer);
        container.removeAllViews();
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        final int[] checkedTodayCount = {0};

        for (String item : list) {
            CheckBox checkBox = new CheckBox(getContext());

            // 표시용 텍스트에 이모지 붙이기
            String displayText;
            if (item.contains("형광등") || item.contains("조명")) displayText = "💡 " + item;
            else if (item.contains("콘센트")) displayText = "🔌 " + item;
            else if (item.contains("TV") || item.contains("티비")) displayText = "📺 " + item;
            else if (item.contains("모니터")) displayText = "🖥️ " + item;
            else displayText = "🌿 " + item;

            checkBox.setText(displayText);

            // 저장/불러올 때는 이모지 없는 item 사용
            boolean checkedToday = prefs.getBoolean("check_" + item + "_" + today, false);
            checkBox.setChecked(checkedToday);
            if (checkedToday) {
                checkBox.setEnabled(false);
                checkedTodayCount[0]++;
            }

            String originalItem = item; // 이모지 없는 원본 문자열

            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                SharedPreferences.Editor editor = prefs.edit();
                int point = prefs.getInt("totalPoint", 0);
                int exp = prefs.getInt("exp", 0);
                int level = prefs.getInt("level", 1);
                int maxExp = level * 100;

                if (isChecked && !prefs.getBoolean("check_" + originalItem + "_" + today, false)) {
                    exp += 10;
                    if (checkedTodayCount[0] < list.size()) {
                        checkedTodayCount[0]++;
                        if (checkedTodayCount[0] <= 6) {
                            point += 50;
                        }
                        Snackbar.make(getActivity().findViewById(android.R.id.content), "체크 완료!", Snackbar.LENGTH_SHORT).show();
                    }

                    if (exp >= maxExp) {
                        exp -= maxExp;
                        level++;
                    }

                    editor.putInt("exp", exp);
                    editor.putInt("level", level);
                    editor.putInt("totalPoint", point);
                    editor.putBoolean("check_" + originalItem + "_" + today, true);
                    editor.apply();

                    FirebaseUser user = mAuth.getCurrentUser(); // 현재 로그인된 사용자 가져오기

                    if (user != null && user.getEmail() != null) { // 사용자 & 이메일 null 체크 필수
                        String email = user.getEmail(); // 사용자 이메일 가져오기
                        String Gname = user.getDisplayName(); // 사용자 구글 이름 가져오기

                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        DocumentReference userRef = db.collection("users").document(email); // 이메일을 문서 ID로 사용

                        // 한 번에 데이터 업데이트 (최적화)
                        Map<String, Object> updatedData = new HashMap<>();
                        updatedData.put("totalPoint", point);
                        updatedData.put("level", level);
                        updatedData.put("exp", exp);

                        userRef.update(updatedData)
                                .addOnSuccessListener(aVoid -> Log.d("Firestore", "데이터 업데이트 성공!"))
                                .addOnFailureListener(e -> Log.w("Firestore", "Firestore 업데이트 실패", e));
                    } else {
                        Log.e("Firestore", "FirebaseUser 또는 이메일이 null입니다. 로그인 여부 확인 필요!");
                    }

                    checkBox.setEnabled(false);
                    updatePointText();
                    updateLevelInfo(view);
                    updateCheckProgress(checkedTodayCount[0], list.size());
                }
            });

            container.addView(checkBox);
        }

        updateCheckProgress(checkedTodayCount[0], list.size());
    }

    public void updatePointText() {
        SharedPreferences prefs = requireContext().getSharedPreferences("ChecklistPrefs", Context.MODE_PRIVATE);
        int point = prefs.getInt("totalPoint", 0);
        textPoint.setText("보유 포인트: " + point + "P");
    }

    public void updateLevelInfo(View view) {
        SharedPreferences prefs = requireContext().getSharedPreferences("ChecklistPrefs", Context.MODE_PRIVATE);
        int exp = prefs.getInt("exp", 0);
        int level = prefs.getInt("level", 1);
        int maxExp = level * 100;

        textLevelInfo.setText("Lv." + level + " (" + exp + "/" + maxExp + " EXP)");

        LottieAnimationView expLottie = view.findViewById(R.id.expLottie);
        float progress = (float) exp / maxExp; // 0.0 ~ 1.0
        expLottie.setProgress(progress);
    }

    public void updateCheckProgress(int checked, int total) {
        textCheckStatus.setText("오늘 체크: " + checked + "/" + total);

        int leafCount = 5;
        int filledLeaves = (total == 0) ? 0 : Math.round((checked / (float) total) * leafCount);

        leafProgressLayout.removeAllViews();

        for (int i = 0; i < leafCount; i++) {
            TextView leaf = new TextView(getContext());
            leaf.setText(i < filledLeaves ? "\uD83E\uDDCA" : "⚪");
            leaf.setTextSize(20);
            leaf.setPadding(4, 0, 4, 0);
            leafProgressLayout.addView(leaf);
        }
    }

    private void fetchLocationAndWeather() {
        LocationManager locationManager = (LocationManager) requireActivity().getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) return;

        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (location != null) {
            double lat = location.getLatitude();
            double lon = location.getLongitude();
            fetchWeatherData(lat, lon);
        }
    }

    private void fetchWeatherData(double lat, double lon) {
        new Thread(() -> {
            try {
                String apiUrl = "https://api.openweathermap.org/data/2.5/weather?lat=" + lat +
                        "&lon=" + lon + "&units=metric&appid=" + API_KEY + "&lang=kr";
                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) response.append(line);
                reader.close();

                JSONObject jsonObject = new JSONObject(response.toString());
                String rawDesc = jsonObject.getJSONArray("weather").getJSONObject(0).getString("description");
                int temp = (int) jsonObject.getJSONObject("main").getDouble("temp");
                String city = jsonObject.getString("name");

                String translated = translateWeather(rawDesc);

                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        weatherText.setText("   " + city + " · " + translated + " " + temp + "℃");
                        weatherIcon.setImageResource(R.drawable.ic_weather_default);
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private String translateWeather(String desc) {
        if (desc.contains("흐림") || desc.contains("온흐림") || desc.contains("구름") || desc.contains("연무")) return "흐림";
        else if (desc.contains("맑음")) return "맑음";
        else if (desc.contains("비")) return "비";
        else if (desc.contains("눈")) return "눈";
        else return desc;
    }
}
