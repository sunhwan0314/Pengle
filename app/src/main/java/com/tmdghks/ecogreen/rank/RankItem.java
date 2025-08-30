// RankItem.java
package com.tmdghks.ecogreen.rank;

public class RankItem {
    private int rank; // 랭킹 번호
    private String userId; // 사용자의 UID
    private String nickname;
    private int totalPoint;
    private int level;
    private String profileImageUrl;

    public RankItem() {
        // Default constructor required for Firebase
    }

    public RankItem(int rank, String userId, String nickname, int totalPoint, int level, String profileImageUrl) {
        this.rank = rank;
        this.userId = userId;
        this.nickname = nickname;
        this.totalPoint = totalPoint;
        this.level = level;
        this.profileImageUrl = profileImageUrl;
    }

    // Getter methods
    public int getRank() {
        return rank;
    }

    public String getUserId() {
        return userId;
    }

    public String getNickname() {
        return nickname;
    }

    public int getTotalPoint() {
        return totalPoint;
    }

    public int getLevel() {
        return level;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    // Setter methods (필요시 추가)
    public void setRank(int rank) {
        this.rank = rank;
    }
    // ... 다른 setter도 필요시 추가
}