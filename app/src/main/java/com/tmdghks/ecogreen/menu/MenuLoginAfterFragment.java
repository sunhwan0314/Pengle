package com.tmdghks.ecogreen.menu;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.tmdghks.ecogreen.friend.FriendListFragment;
import com.tmdghks.ecogreen.mission.MissionFragment;
import com.tmdghks.ecogreen.R;
import com.tmdghks.ecogreen.rank.RankingFragment;
import com.tmdghks.ecogreen.setting.SettingsFragment;
import com.tmdghks.ecogreen.home.TreeDetailActivity;

public class MenuLoginAfterFragment extends Fragment {

    private LinearLayout menuRank, menuSetting, menuFriend, menuMission, menuRecord;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_menu_login_after, container, false);

        // 메뉴 클릭 리스너 연결
        menuFriend = view.findViewById(R.id.menu_friend);
        menuFriend.setOnClickListener(v -> replaceFragment(new FriendListFragment()));

        menuRank = view.findViewById(R.id.menu_tier);
        menuRank.setOnClickListener(v -> replaceFragment(new RankingFragment()));

        menuSetting = view.findViewById(R.id.menu_settings);
        menuSetting.setOnClickListener(v -> replaceFragment(new SettingsFragment()));

        menuMission = view.findViewById(R.id.menu_mission);
        menuMission.setOnClickListener(v -> replaceFragment(new MissionFragment()));

        menuRecord = view.findViewById(R.id.menu_record);
        menuRecord.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), TreeDetailActivity.class);
            startActivity(intent);
        });

        // 로그인 사용자 정보
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        ImageView profileImageView = view.findViewById(R.id.profile_image);
        TextView nameTextView = view.findViewById(R.id.userNameTextView);

        if (currentUser != null && currentUser.getEmail() != null) {
            String userEmail = currentUser.getEmail();
            String imagePath = "profile_images/" + userEmail + ".jpg";

            StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(imagePath);

            storageRef.getDownloadUrl()
                    .addOnSuccessListener(uri -> {
                        if (!isAdded()) return;
                        Glide.with(requireContext())
                                .load(uri)
                                .placeholder(R.drawable.userdefault)
                                .error(R.drawable.userdefault)
                                .transform(new CircleCrop())
                                .into(profileImageView);
                        Log.d("ProfileImage", "프로필 이미지 로드 성공: " + uri);
                    })
                    .addOnFailureListener(e -> {
                        if (!isAdded()) return;
                        profileImageView.setImageResource(R.drawable.userdefault);
                        Log.e("ProfileImage", "프로필 이미지 로드 실패: " + imagePath, e);
                    });

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference userRef = db.collection("users").document(userEmail);

            userRef.get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (!isAdded()) return;
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            nameTextView.setText(name != null && !name.isEmpty() ? name : "사용자 이름 없음");
                        } else {
                            nameTextView.setText("사용자 정보 없음");
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (!isAdded()) return;
                        nameTextView.setText("데이터 불러오기 오류");
                        Log.e("Firestore", "Firestore에서 사용자 이름 불러오기 실패", e);
                    });
        } else {
            profileImageView.setImageResource(R.drawable.userdefault);
            nameTextView.setText("로그인 정보 없음");
        }

        return view;
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}