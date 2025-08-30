// RankingFragment.java
package com.tmdghks.ecogreen.rank;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.tmdghks.ecogreen.R;

import java.util.ArrayList;
import java.util.List;

public class RankingFragment extends Fragment {

    private RecyclerView recyclerViewRanking;
    private TextView textViewEmptyRanking;
    private RankingAdapter adapter;
    private List<RankItem> rankingList; // RankItem은 새로 만들 모델 클래스
    private FirebaseFirestore db;

    //하단 내 랭킹
    private ImageView myRankImage;
    private TextView myTextViewRank;
    private TextView myTextViewName;
    private TextView myTextViewPoint;
    private TextView myTextViewLevel;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ranking, container, false); // 새 레이아웃 파일 생성 필요

        recyclerViewRanking = view.findViewById(R.id.recyclerViewRanking);
        textViewEmptyRanking = view.findViewById(R.id.textViewEmptyRanking);

        recyclerViewRanking.setLayoutManager(new LinearLayoutManager(getContext()));
        rankingList = new ArrayList<>();
        adapter = new RankingAdapter(rankingList, getContext()); // 새 어댑터 생성 필요
        recyclerViewRanking.setAdapter(adapter);

        myRankImage = view.findViewById(R.id.myimageViewProfile);
        myTextViewRank = view.findViewById(R.id.mytextViewRank);
        myTextViewName = view.findViewById(R.id.mytextViewNickname);
        myTextViewPoint = view.findViewById(R.id.mytextViewPoint);
        myTextViewLevel = view.findViewById(R.id.mytextViewLevel);


        db = FirebaseFirestore.getInstance();

        loadRanking();
        myRank();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // 프래그먼트가 다시 활성화될 때마다 랭킹을 새로고침
        loadRanking();
        Log.d("RankingFragment", "onResume: 랭킹 새로고침.");
    }

    // RankingFragment.java 의 loadRanking() 메서드 내에 추가
    private void loadRanking() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserEmail = (currentUser != null) ? currentUser.getEmail() : null;

        Log.d("RankingFragment", "loadRanking() 호출됨. Firestore 데이터 로드 시작.");


        db.collection("users")
                .orderBy("totalPoint", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    rankingList.clear();
                    int rank = 1;

                    if (queryDocumentSnapshots.isEmpty()) {
                        textViewEmptyRanking.setVisibility(View.VISIBLE);
                        textViewEmptyRanking.setText("랭킹 데이터가 없습니다.");
                        Log.d("RankingFragment", "Firestore에 랭킹 데이터가 없습니다.");
                    } else {
                        textViewEmptyRanking.setVisibility(View.GONE);
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String userId = document.getId();
                            String nickname = document.getString("name");
                            Long totalPointLong = document.getLong("totalPoint");
                            Long levelLong = document.getLong("level");
                            String profileImageUrl = "profile_images/" + document.getId() + ".jpg";

                            int totalPoint = (totalPointLong != null) ? totalPointLong.intValue() : 0;
                            int level = (levelLong != null) ? levelLong.intValue() : 0;

                            if (nickname != null) {
                                rankingList.add(new RankItem(rank, userId, nickname, totalPoint, level, profileImageUrl));


                                rank++;
                                Log.d("RankingFragment", "랭킹 아이템 추가: " + nickname + " (점수: " + totalPoint + ")");
                            } else {
                                Log.w("RankingFragment", "닉네임이 없는 사용자 문서 발견: " + userId);
                            }
                        }
                        Log.d("RankingFragment", "총 " + rankingList.size() + "개의 랭킹 아이템 로드 완료.");
                    }

                    // ✅ 현재 사용자 랭킹을 찾지 못한 경우 "순위 없음" 표시
//                    if (!isCurrentUserRanked) {
//                        myTextViewRank.setText("순위 없음");
//                    }

                    adapter.setRankingList(rankingList); // 어댑터에 갱신된 리스트 전달
                })
                .addOnFailureListener(e -> {
                    Log.e("RankingFragment", "랭킹 로드 실패", e);
                    Toast.makeText(getContext(), "랭킹을 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
                    textViewEmptyRanking.setVisibility(View.VISIBLE);
                    textViewEmptyRanking.setText("랭킹 로드 중 오류가 발생했습니다.");
                });
    }
    private void myRank(){
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();



        if (currentUser != null && currentUser.getEmail() != null) {
            String userEmail = currentUser.getEmail();
            String imagePath = "profile_images/" + userEmail + ".jpg"; // Firebase Storage 경로

            StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(imagePath);

            storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                Glide.with(requireContext()) // Fragment에서는 requireContext() 사용
                        .load(uri) // Firebase Storage 다운로드 URL 사용
                        .placeholder(R.drawable.userdefault) // 기본 이미지 설정
                        .error(R.drawable.userdefault) // 오류 발생 시 기본 이미지 사용
                        .transform(new CircleCrop())
                        .into(myRankImage);
                Log.d("ProfileImage", "프로필 이미지 로드 성공: " + uri.toString());
            }).addOnFailureListener(exception -> {
                // 이미지를 찾을 수 없거나 로드에 실패한 경우
                myRankImage.setImageResource(R.drawable.userdefault); // 기본 이미지 설정
                Log.e("ProfileImage", "프로필 이미지 로드 실패 (Storage): " + imagePath, exception);
            });
        } else {
            // 현재 사용자가 로그인되어 있지 않거나 이메일이 없는 경우
            myRankImage.setImageResource(R.drawable.userdefault); // 기본 이미지 설정
            Log.w("ProfileImage", "현재 사용자가 로그인되어 있지 않거나 이메일이 없습니다.");
        }
        // 사용자 이름 설정

        if (currentUser != null) {
            String userEmail = currentUser.getEmail(); // 사용자 이메일 가져오기

            if (userEmail != null && !userEmail.isEmpty()) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                DocumentReference userRef = db.collection("users").document(userEmail);


                userRef.get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                String name = documentSnapshot.getString("name"); // Firestore의 `name` 값 가져오기
                                Long point = documentSnapshot.getLong("totalPoint"); // Firestore의 `totalPoint` 값 가져오기
                                Long level = documentSnapshot.getLong("level"); // Firestore의 `level` 값 가져오기


                                // 특정 컬렉션에서 조건에 맞는 문서 찾기
                                db.collection("users")
                                        .whereGreaterThan("totalPoint", point)
                                        .get()
                                        .addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                int count = task.getResult().size();
                                                myTextViewRank.setText(String.valueOf(count+1));

                                                Log.d("Firestore", "총 " + count + "개의 문서가 조건을 만족합니다.");
                                            } else {
                                                Log.e("Firestore", "문서를 가져오는데 실패했습니다.", task.getException());
                                            }
                                        });

                                if (name != null && !name.isEmpty()) {
                                    myTextViewName.setText(name); // 사용자 이름 설정
                                    myTextViewPoint.setText(point.toString());
                                    myTextViewLevel.setText(level.toString());

                                } else {
                                    myTextViewName.setText("사용자 이름 없음"); // `name` 필드가 존재하지 않거나 비어 있는 경우
                                }
                            } else {
                                myTextViewName.setText("사용자 정보 없음"); // 문서가 존재하지 않는 경우
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e("Firestore", "Firestore에서 데이터 가져오기 실패", e);
                            myTextViewName.setText("데이터 불러오기 오류"); // Firestore 조회 실패 시 처리
                        });
            } else {
                myTextViewName.setText("이메일 정보 없음"); // 이메일이 `null`인 경우
            }
        } else {
            myTextViewName.setText("로그인 정보 없음"); // 로그인하지 않은 경우
        }

    }
}