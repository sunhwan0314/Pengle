package com.tmdghks.ecogreen;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.tmdghks.ecogreen.home.HomeFragment;
import com.tmdghks.ecogreen.menu.MenuFragment;
import com.tmdghks.ecogreen.menu.MenuLoginAfterFragment;
import com.tmdghks.ecogreen.mission.MissionFragment;
import com.tmdghks.ecogreen.shop.RewardAdapter;
import com.tmdghks.ecogreen.shop.RewardItem;
import com.tmdghks.ecogreen.shop.ShopFragment;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    private RecyclerView rewardRecyclerView;
    private RewardAdapter rewardAdapter;
    private ArrayList<RewardItem> rewardItemList;
    private MissionFragment missionFragment; // MissionFragment 참조 추가


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav);

        SharedPreferences prefs = getSharedPreferences("ChecklistPrefs", Context.MODE_PRIVATE);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            if (item.getItemId() == R.id.menu_menu) {
                if(prefs.getBoolean("isLogin",false)==false){
                    selectedFragment = new MenuFragment();

                }
                else{
                    selectedFragment = new MenuLoginAfterFragment();

                }

            } else if (item.getItemId() == R.id.menu_shop) {
                selectedFragment = new ShopFragment();
            } else if (item.getItemId() == R.id.menu_home) {
                selectedFragment = new HomeFragment();
            }


            if (selectedFragment != null) {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, selectedFragment);
                transaction.commit();
            }
            return true;
        });


        // 홈 화면 기본으로 설정
        bottomNavigationView.setSelectedItemId(R.id.menu_home);
        bottomNavigationView.setItemIconTintList(null);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }
    }


    // MissionFragment 참조 설정 (MenuFragment에서 MissionFragment로 이동할 때 호출)
    public void setMissionFragment(MissionFragment fragment) {
        Log.d("MainActivity", "setMissionFragment called");
        this.missionFragment = fragment;
    }


    // MissionFragment의 addPoints 메서드 호출
    public void addPointsToMissionFragment(int points, boolean isDaily) {
        Log.d("MainActivity", "addPointsToMissionFragment called. Points: " + points + ", isDaily: " + isDaily);
        if (missionFragment != null) {
            missionFragment.addPoints(points, isDaily);
        } else {
            Log.e("MainActivity", "missionFragment is null");
        }
    }
}