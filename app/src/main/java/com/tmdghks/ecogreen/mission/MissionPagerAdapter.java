package com.tmdghks.ecogreen.mission;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class MissionPagerAdapter extends FragmentStateAdapter {

    public MissionPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new DailyMissionFragment();
        } else {
            return new WeeklyMissionFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
