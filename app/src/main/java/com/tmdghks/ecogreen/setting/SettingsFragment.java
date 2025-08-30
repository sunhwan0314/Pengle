package com.tmdghks.ecogreen.setting; // 실제 패키지명에 맞게 변경

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton; // Switch 리스너에 필요
import android.widget.LinearLayout;
import android.widget.Switch; // Switch 위젯 사용
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

// Firebase 관련 임포트 (로그아웃, 회원탈퇴 시 필요)
import com.google.firebase.auth.FirebaseAuth;
import com.tmdghks.ecogreen.R;
import com.tmdghks.ecogreen.menu.MenuFragment;

public class SettingsFragment extends Fragment {

    // UI 요소 선언 (XML의 ID와 일치해야 함)
    private Switch switchPushNotification;
    private Switch switchDarkMode;
    private LinearLayout settingAccountInfo; // 계정 정보 클릭 리스너를 위해 추가
    private TextView textAppVersion;
    private Button buttonLogout;
    private Button buttonDeleteAccount;

    // 설정 상태 저장을 위한 SharedPreferences (가장 흔한 방법)
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "AppSettings";
    private static final String KEY_PUSH_NOTIFICATION = "push_notification_enabled";
    private static final String KEY_DARK_MODE = "dark_mode_enabled";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false); // 이전에 만든 XML 파일 사용

        // UI 요소 초기화 (findViewById)
        switchPushNotification = view.findViewById(R.id.switch_push_notification);
        switchDarkMode = view.findViewById(R.id.switch_dark_mode);
        settingAccountInfo = view.findViewById(R.id.setting_account_info); // 계정 정보 레이아웃
        textAppVersion = view.findViewById(R.id.text_app_version);
        buttonLogout = view.findViewById(R.id.button_logout);
        buttonDeleteAccount = view.findViewById(R.id.button_delete_account);

        // SharedPreferences 초기화
        sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // 1. 저장된 설정 상태 불러와서 스위치 초기 상태 설정
        loadSettings();

        // 2. 스위치 상태 변경 리스너 설정
        switchPushNotification.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // 푸시 알림 상태 변경 시 로직
                saveSetting(KEY_PUSH_NOTIFICATION, isChecked);
                if (isChecked) {
                    Toast.makeText(getContext(), "푸시 알림이 켜졌습니다.", Toast.LENGTH_SHORT).show();
                    // TODO: 푸시 알림 활성화 로직 (예: Firebase Cloud Messaging 구독)
                } else {
                    Toast.makeText(getContext(), "푸시 알림이 꺼졌습니다.", Toast.LENGTH_SHORT).show();
                    // TODO: 푸시 알림 비활성화 로직 (예: Firebase Cloud Messaging 구독 해제)
                }
            }
        });

        switchDarkMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // 다크 모드 상태 변경 시 로직
                saveSetting(KEY_DARK_MODE, isChecked);
                if (isChecked) {
                    Toast.makeText(getContext(), "다크 모드가 켜졌습니다.", Toast.LENGTH_SHORT).show();
                    // TODO: 앱 테마를 다크 모드로 변경하는 로직 (예: AppCompatDelegate.setDefaultNightMode)
                } else {
                    Toast.makeText(getContext(), "다크 모드가 꺼졌습니다.", Toast.LENGTH_SHORT).show();
                    // TODO: 앱 테마를 라이트 모드로 변경하는 로직
                }
                // 테마 변경은 앱 재시작이 필요할 수 있습니다.
            }
        });

        // 3. 계정 정보 항목 클릭 리스너
        settingAccountInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "계정 정보 페이지로 이동합니다.", Toast.LENGTH_SHORT).show();
                // TODO: 계정 정보 Fragment/Activity로 전환하는 코드
                FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, new AccountInfoFragment());
                transaction.addToBackStack(null); // 뒤로가기 가능

                transaction.commit();

                // 예: getParentFragmentManager().beginTransaction().replace(R.id.fragment_container, new AccountInfoFragment()).addToBackStack(null).commit();
            }
        });

        // 4. 앱 버전 정보 설정 (PackageInfo 사용)
        try {
            String versionName = requireContext().getPackageManager().getPackageInfo(requireContext().getPackageName(), 0).versionName;
            textAppVersion.setText(versionName);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("SettingsFragment", "앱 버전 정보 로드 실패", e);
            textAppVersion.setText("N/A");
        }


        // 5. 로그아웃 버튼 클릭 리스너
        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences pre = requireContext().getSharedPreferences("ChecklistPrefs",Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = pre.edit();

                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext()); // ✅ Fragment에서 Context 가져오기
                builder.setTitle("로그아웃")
                        .setMessage("로그아웃 하시겠습니까?")
                        .setPositiveButton("확인", (dialog, which) -> {
                            //5월 1주차 추가
                            // Firebase 로그아웃
                            FirebaseAuth.getInstance().signOut();
                            //로그아웃 후 저장소 초기화
                            editor.putBoolean("isLogin",false);
                            editor.putInt("exp", 0);
                            editor.putInt("level", 1);
                            editor.putInt("totalPoint", 0);
                            editor.apply();

                            Toast.makeText(requireContext(), "로그아웃 하였어요!", Toast.LENGTH_SHORT).show();
                            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                            transaction.replace(R.id.fragment_container, new MenuFragment());
                            transaction.commit();
                        })
                        .setNegativeButton("취소", (dialog, which) -> dialog.dismiss());

                AlertDialog dialog = builder.create();
                dialog.show();

            }
        });

        // 6. 회원 탈퇴 버튼 클릭 리스너
        buttonDeleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: 회원 탈퇴 확인 다이얼로그 띄우기
                // TODO: Firebase Authentication에서 사용자 삭제 (매우 민감한 작업이므로 신중하게 구현)
                // TODO: Firestore, Storage 등 사용자 관련 데이터 삭제
                Toast.makeText(getContext(), "회원 탈퇴 기능 구현 예정입니다.", Toast.LENGTH_SHORT).show();
            }
        });


        return view;
    }

    // SharedPreferences에 설정 상태 저장하는 헬퍼 메서드
    private void saveSetting(String key, boolean value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply(); // 비동기 저장
        Log.d("SettingsFragment", "설정 저장: " + key + " = " + value);
    }

    // SharedPreferences에서 설정 상태 불러오는 헬퍼 메서드
    private void loadSettings() {
        boolean pushNotificationEnabled = sharedPreferences.getBoolean(KEY_PUSH_NOTIFICATION, true); // 기본값 true
        boolean darkModeEnabled = sharedPreferences.getBoolean(KEY_DARK_MODE, false); // 기본값 false

        switchPushNotification.setChecked(pushNotificationEnabled);
        switchDarkMode.setChecked(darkModeEnabled);

        Log.d("SettingsFragment", "설정 로드: 푸시 알림 = " + pushNotificationEnabled + ", 다크 모드 = " + darkModeEnabled);
    }
}