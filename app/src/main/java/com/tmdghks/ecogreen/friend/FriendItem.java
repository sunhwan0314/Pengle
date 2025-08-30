package com.tmdghks.ecogreen.friend;

public class FriendItem {
    private String userId;
    private String name;
    private int point;
    private int level;
    private String profileImageUrl;

    public FriendItem() {
        // Default constructor required for Firebase
    }

    public FriendItem(String userId, String name, int point, int level, String profileImageUrl) {
        this.userId = userId;
        this.name = name;
        this.point = point;
        this.level = level;
        this.profileImageUrl = profileImageUrl;
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public int getPoint() {
        return point;
    }

    public int getLevel() {
        return level;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    // 필요에 따라 Setter 메서드 추가
}