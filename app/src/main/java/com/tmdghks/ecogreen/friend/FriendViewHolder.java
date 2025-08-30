package com.tmdghks.ecogreen.friend;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.tmdghks.ecogreen.R;

public class FriendViewHolder extends RecyclerView.ViewHolder {

    ImageView imageViewProfile;
    TextView textViewFriendName;
    TextView textViewFriendPoint;
    TextView textViewFriendLevel;
    Button buttonAddFriend;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public FriendViewHolder(@NonNull View itemView) {
        super(itemView);
        imageViewProfile = itemView.findViewById(R.id.imageViewProfile);
        textViewFriendName = itemView.findViewById(R.id.textViewFriendName);
        textViewFriendPoint = itemView.findViewById(R.id.textViewFriendPoint);
        textViewFriendLevel = itemView.findViewById(R.id.textViewFriendLevel);
        buttonAddFriend = itemView.findViewById(R.id.buttonAddFriend);
    }

    public void bind(FriendItem friend) {
        textViewFriendName.setText(friend.getName());
        textViewFriendPoint.setText(String.format("%d P", friend.getPoint()));
        textViewFriendLevel.setText(String.valueOf(friend.getLevel()));

        String imagePath = friend.getProfileImageUrl();
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(imagePath);

        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            Glide.with(itemView.getContext())
                    .load(uri)
                    .placeholder(R.drawable.loginbig)
                    .error(R.drawable.loginbig)
                    .circleCrop()
                    .into(imageViewProfile);
        }).addOnFailureListener(exception -> {
            imageViewProfile.setImageResource(R.drawable.loginbig);
            Log.e("FriendViewHolder", "프로필 이미지 로드 실패 (Storage): " + imagePath, exception);
        });

        // 추가: 친구 추가 버튼 클릭 리스너 설정
        buttonAddFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onFriendAddClickListener != null) {
                    onFriendAddClickListener.onFriendAddClick(friend.getUserId());
                } else {
                    Log.e("FriendViewHolder", "OnFriendAddClickListener is null!");
                }
            }
        });
    }

    // 친구 추가 버튼 클릭 리스너 인터페이스
    public interface OnFriendAddClickListener {
        void onFriendAddClick(String friendId);

    }

    private OnFriendAddClickListener onFriendAddClickListener;


    public void setOnFriendAddClickListener(OnFriendAddClickListener listener) {
        this.onFriendAddClickListener = listener;
    }
}