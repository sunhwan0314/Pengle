package com.tmdghks.ecogreen.menu;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.tmdghks.ecogreen.login.LoginFragment;
import com.tmdghks.ecogreen.mission.MissionFragment;
import com.tmdghks.ecogreen.R;
import com.tmdghks.ecogreen.home.TreeDetailActivity;

public class MenuFragment extends Fragment {

    //5월 1주차 추가
    private LinearLayout menuTier;
    //5월 1주차 추가
    private LinearLayout menuMission;
    private LinearLayout menuLogin;
    private LinearLayout menuRecord;  // ✅ 기록 보기 메뉴 추가


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_menu, container, false);

        // 미션 이동
        menuMission = view.findViewById(R.id.menu_mission);
        menuMission.setOnClickListener(v -> replaceFragment(new MissionFragment()));

        //5월 1주차 추가
        menuTier = view.findViewById(R.id.menu_tier);
        menuTier.setOnClickListener(v -> {
            Toast.makeText(getContext(), "로그인 후에 이용해주세요.", Toast.LENGTH_SHORT).show();
        });
        //5월 1주차 추가
        // 로그인 이동
        menuLogin = view.findViewById(R.id.menu_login);
        menuLogin.setOnClickListener(v -> replaceFragment(new LoginFragment()));

        // ✅ 기록 보기 클릭 시 TreeDetailActivity로 이동
        menuRecord = view.findViewById(R.id.menu_record);
        menuRecord.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), TreeDetailActivity.class);
            startActivity(intent);
        });

        return view;
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }
}