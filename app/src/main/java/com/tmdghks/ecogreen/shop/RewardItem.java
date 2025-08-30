package com.tmdghks.ecogreen.shop;

public class RewardItem {
    String name;
    String points;
    int imageResourceId;
    String description; // 아이템 설명 추가

    public RewardItem(String name, String points, int imageResourceId, String description) {
        this.name = name;
        this.points = points;
        this.imageResourceId = imageResourceId;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getPoints() {
        return points;
    }

    public int getImageResourceId() {
        return imageResourceId;
    }

    public String getDescription() {
        return description;
    }
}