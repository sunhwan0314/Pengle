package com.tmdghks.ecogreen.mission;

public class Mission {
    private final String title;
    final int rewardPoints;
    private boolean isCompleted;
    private boolean isConditionMet;  // 추가: 조건 충족 여부 표시
    private final MissionType type;
    final int requiredChecklistCount;
    final int requiredDailyCompletions;

    public Mission(String title, int rewardPoints, MissionType type, int requiredChecklistCount, int requiredDailyCompletions) {
        this.title = title;
        this.rewardPoints = rewardPoints;
        this.type = type;
        this.requiredChecklistCount = requiredChecklistCount;
        this.requiredDailyCompletions = requiredDailyCompletions;
        this.isCompleted = false;
        this.isConditionMet = false;  // 초기값
    }

    // getters & setters
    public String getTitle() { return title; }
    public int getRewardPoints() { return rewardPoints; }
    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }
    public MissionType getType() { return type; }
    public int getRequiredChecklistCount() { return requiredChecklistCount; }
    public int getRequiredDailyCompletions() { return requiredDailyCompletions; }

    // 조건 충족 여부 getter/setter
    public boolean isConditionMet() { return isConditionMet; }
    public void setConditionMet(boolean conditionMet) { isConditionMet = conditionMet; }
}
