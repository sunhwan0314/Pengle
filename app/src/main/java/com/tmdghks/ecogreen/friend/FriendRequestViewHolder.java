package com.tmdghks.ecogreen.friend;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide; // Glide 사용 예시
import com.tmdghks.ecogreen.R;

public class FriendRequestViewHolder extends RecyclerView.ViewHolder {
    public ImageView profileImageView;
    public TextView nameTextView;
    public Button acceptButton;
    public Button declineButton;

    private FriendRequestFragment.OnFriendRequestListener listener;

    public FriendRequestViewHolder(@NonNull View itemView, FriendRequestFragment.OnFriendRequestListener listener) {
        super(itemView);
        this.listener = listener; // 리스너 초기화

        profileImageView = itemView.findViewById(R.id.iv_requester_profile_picture_notif); // item_friend_request.xml의 ImageView ID
        nameTextView = itemView.findViewById(R.id.tv_request_notification_text);         // item_friend_request.xml의 TextView ID
        acceptButton = itemView.findViewById(R.id.buttonAddRequest);       // item_friend_request.xml의 수락 버튼 ID
        declineButton = itemView.findViewById(R.id.buttonRemoveRequest);     // item_friend_request.xml의 거절 버튼 ID
    }
    public void bind(final FriendRequestItem item) {
        nameTextView.setText(item.getName()); // 또는 item.getDisplayText() 등, 이름 표시 로직 확인

        // item.getProfileImageUrl()이 이미 완전한 다운로드 URL이라고 가정
        if (item.getProfileImageUrl() != null && !item.getProfileImageUrl().isEmpty()) {
            Log.d("ViewHolder", "Loading image with direct URL: " + item.getProfileImageUrl());
            if (itemView.getContext() != null) {
                Glide.with(itemView.getContext())
                        .load(item.getProfileImageUrl()) // StorageReference를 다시 만들지 않고 URL 직접 사용
                        .placeholder(R.drawable.loginbig) // 기본 이미지
                        .error(R.drawable.loginbig)       // 에러 시 이미지
                        .circleCrop()                     // 원형으로 이미지 표시 (선택 사항)
                        .into(profileImageView);
            }
        } else {
            // 프로필 이미지 URL이 없는 경우 기본 이미지 설정
            Log.d("ViewHolder", "ProfileImageUrl is null or empty, loading default.");
            if (itemView.getContext() != null) {
                Glide.with(itemView.getContext())
                        .load(R.drawable.loginbig)
                        .circleCrop()
                        .into(profileImageView);
            }
        }


        // 수락 버튼 클릭 리스너 (일단 기능은 비워둠)
        acceptButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAccept(item);
            }
        });

        // 거절 버튼 클릭 리스너 (일단 기능은 비워둠)
        declineButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDecline(item);
            }
        });
    }
}