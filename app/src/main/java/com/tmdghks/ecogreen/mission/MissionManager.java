package com.tmdghks.ecogreen.mission;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MissionManager {
    private Context context;
    private SharedPreferences prefs;
    private static final String PREFS_NAME = "MissionPrefs";

    public MissionManager(Context context) {
        this.context = context;
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // 오늘 날짜 (yyyy-MM-dd)
    private String getTodayDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().getTime());
    }

    // 주 시작일 (월요일 기준)
    private String getStartOfWeek() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.getTime());
    }

    // 오늘 체크리스트 완료 개수
    public int getTodayCheckedCount() {
        SharedPreferences prefs = context.getSharedPreferences("ChecklistPrefs", Context.MODE_PRIVATE);

        String json = prefs.getString("checklist_json", null);
        if (json == null) return 0;

        ArrayList<String> checklistItems = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                checklistItems.add(jsonArray.getString(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return 0;
        }

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        int count = 0;
        for (String item : checklistItems) {
            String key = "check_" + item + "_" + today;
            if (prefs.getBoolean(key, false)) {
                count++;
            }
        }
        return count;
    }

    // 이번 주에 일일 미션 완료한 날짜 수
    public int getWeeklyDailyMissionCompletedCount() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        List<String> dates = new ArrayList<>();

        for (int i = 0; i <= Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY; i++) {
            String dateStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.getTime());
            dates.add(dateStr);
            cal.add(Calendar.DATE, 1);
        }

        int count = 0;
        for (String date : dates) {
            if (prefs.getBoolean("dailyMissionCompleted_" + date, false)) {
                count++;
            }
        }
        return count;
    }

    // 포인트 추가
    public void addPoints(int points) {
        SharedPreferences checklistPrefs = context.getSharedPreferences("ChecklistPrefs", Context.MODE_PRIVATE);
        int currentPoints = checklistPrefs.getInt("totalPoint", 0);
        checklistPrefs.edit().putInt("totalPoint", currentPoints + points).apply();
    }

    // 일일 미션 완료 기록
    public void markDailyMissionCompleted() {
        prefs.edit().putBoolean("dailyMissionCompleted_" + getTodayDate(), true).apply();
    }

    // 미션 완료 저장
    public void markMissionCompleted(Mission mission) {
        String key = "missionCompleted_" + mission.getTitle() + "_" + getTodayDate();
        prefs.edit().putBoolean(key, true).apply();
    }

    // 미션 완료 여부 확인
    public boolean isMissionCompleted(Mission mission) {
        String key = "missionCompleted_" + mission.getTitle() + "_" + getTodayDate();
        return prefs.getBoolean(key, false);
    }

    // 미션 완료 처리
    public void onMissionCompleted(Mission mission) {
        if (!isMissionCompleted(mission)) {
            addPoints(mission.getRewardPoints());
            markMissionCompleted(mission);
            if (mission.getType() == MissionType.DAILY) {
                markDailyMissionCompleted();
            }
        }
    }

    // 일일 미션 반환
    public List<Mission> getDailyMissions() {
        List<Mission> dailyMissions = new ArrayList<>();
        int todayCount = getTodayCheckedCount();

        Mission mission = new Mission("오늘 체크리스트 3개 완료하기", 50, MissionType.DAILY, 3, 0);

        // 조건 충족 여부 판단
        boolean conditionMet = todayCount >= mission.getRequiredChecklistCount();
        mission.setConditionMet(conditionMet);

        // 완료 상태는 저장된 값에 의존
        boolean isCompleted = isMissionCompleted(mission);
        mission.setCompleted(isCompleted);

        dailyMissions.add(mission);
        return dailyMissions;
    }


    // 주간 미션 반환
    public List<Mission> getWeeklyMissions() {
        List<Mission> weeklyMissions = new ArrayList<>();
        int weeklyCount = getWeeklyDailyMissionCompletedCount();

        Mission mission = new Mission("이번 주 일일 미션 5일 이상 완료", 150, MissionType.WEEKLY, 0, 5);
        boolean isCompleted = weeklyCount >= mission.getRequiredDailyCompletions() || isMissionCompleted(mission);
        mission.setCompleted(isCompleted);

        weeklyMissions.add(mission);
        return weeklyMissions;
    }
}
