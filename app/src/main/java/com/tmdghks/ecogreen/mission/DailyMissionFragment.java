package com.tmdghks.ecogreen.mission;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tmdghks.ecogreen.R;

public class DailyMissionFragment extends Fragment implements MissionAdapter.OnMissionCompleteListener {

    private RecyclerView recyclerView;
    private MissionAdapter adapter;
    private MissionManager missionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_daily_mission, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        missionManager = new MissionManager(getContext());
        recyclerView = view.findViewById(R.id.recyclerDailyMissions);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MissionAdapter(getContext(), missionManager.getDailyMissions(), this, missionManager);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onMissionCompleted(Mission mission) {
        missionManager.addPoints(mission.getRewardPoints());
        missionManager.markDailyMissionCompleted();
        // TODO: 필요시 홈 프래그먼트에 포인트 변경 알림
    }
}
