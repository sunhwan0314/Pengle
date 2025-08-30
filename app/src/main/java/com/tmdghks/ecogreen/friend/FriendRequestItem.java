package com.tmdghks.ecogreen.friend;

// ... (기존 import문 유지) ...

public class FriendRequestItem {
    private String userId; // 요청 보낸 사용자의 UID (또는 필드 키 이름)
    private String name;   // 표시될 이름 ("name 님이 친구를 원합니다")
    private String profileImageUrl; // Storage 내 프로필 이미지 경로 또는 이름

    // Firestore data mapping을 위한 기본 생성자 추가
    public FriendRequestItem() {
    }

    public FriendRequestItem(String userId, String name,  String profileImageUrl) {
        this.name = name;
        this.profileImageUrl = profileImageUrl; // Storage에서 이미지를 가져올 때 사용할 키 (예: requesting_user_uid_1)
        this.userId = userId; // users 컬렉션에서 이름을 조회할 때 사용할 키
    }

    public String getName() {
        return name;
    }

    public String getProfileImageUrl() { // 이 메서드는 Storage에서 이미지를 로드할 때 사용될 키를 반환
        return profileImageUrl;
    }

    public String getUserId() { // 이 메서드는 users 컬렉션에서 사용자 정보를 조회할 때 사용될 키를 반환
        return userId;
    }

    // 필요에 따라 Setter 메서드 추가
    public void setName(String name) {
        this.name = name;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}