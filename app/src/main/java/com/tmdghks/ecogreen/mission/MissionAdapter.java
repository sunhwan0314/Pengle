package com.tmdghks.ecogreen.mission;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tmdghks.ecogreen.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MissionAdapter extends RecyclerView.Adapter<MissionAdapter.MissionViewHolder> {

    public interface OnMissionCompleteListener {
        void onMissionCompleted(Mission mission);
    }

    private List<Mission> missions;
    private Context context;
    private OnMissionCompleteListener listener;
    private MissionManager missionManager;

    public MissionAdapter(Context context, List<Mission> missions, OnMissionCompleteListener listener, MissionManager missionManager) {
        this.context = context;
        this.missions = missions;
        this.listener = listener;
        this.missionManager = missionManager;
    }

    @NonNull
    @Override
    public MissionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_mission, parent, false);
        return new MissionViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull MissionViewHolder holder, int position) {
        Mission mission = missions.get(position);
        holder.titleText.setText(mission.getTitle());
        holder.rewardText.setText("보상: " + mission.getRewardPoints() + "P");

        // SharedPreferences에서 미션 완료 여부 확인
        SharedPreferences prefs = context.getSharedPreferences("MissionPrefs", Context.MODE_PRIVATE);
        final String key;

        if (mission.getType() == MissionType.DAILY) {
            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            key = "dailyMissionCompleted_" + today;
        } else {
            String weekKey = new SimpleDateFormat("yyyy-ww", Locale.getDefault()).format(new Date());
            key = "weeklyMissionCompleted_" + weekKey;
        }

        boolean isCompleted = prefs.getBoolean(key, false);
        mission.setCompleted(isCompleted); // SharedPreferences에서 가져온 값으로 상태 설정

        holder.completeBtn.setEnabled(true);
        holder.completeBtn.setText(mission.isCompleted() ? "완료" : "완료하기");

        holder.completeBtn.setOnClickListener(v -> {
            if (mission.isCompleted()) {
                // 이미 완료된 미션일 때
                Toast.makeText(context, "이미 완료된 미션입니다.", Toast.LENGTH_SHORT).show();
            } else {
                // 미션 미완료 상태에서만 완료 처리 시도
                boolean canComplete = false;
                if (mission.getType() == MissionType.DAILY) {
                    int checklistCount = missionManager.getTodayCheckedCount();
                    canComplete = checklistCount >= mission.getRequiredChecklistCount();
                } else if (mission.getType() == MissionType.WEEKLY) {
                    int weeklyCount = missionManager.getWeeklyDailyMissionCompletedCount();
                    canComplete = weeklyCount >= mission.getRequiredDailyCompletions();
                }

                if (canComplete) {
                    mission.setCompleted(true);
                    notifyItemChanged(position);
                    Toast.makeText(context, "미션 완료! 포인트 +" + mission.getRewardPoints() + "P", Toast.LENGTH_SHORT).show();
                    if (listener != null) listener.onMissionCompleted(mission);
                } else {
                    Toast.makeText(context, "아직 미션 조건이 충족되지 않았습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return missions.size();
    }

    static class MissionViewHolder extends RecyclerView.ViewHolder {
        TextView titleText, rewardText;
        Button completeBtn;

        public MissionViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.missionTitle);
            rewardText = itemView.findViewById(R.id.missionReward);
            completeBtn = itemView.findViewById(R.id.btnCompleteMission);
        }
    }
}
