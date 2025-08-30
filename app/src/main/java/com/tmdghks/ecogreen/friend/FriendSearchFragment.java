package com.tmdghks.ecogreen.friend;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth; // FirebaseAuth 추가
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.tmdghks.ecogreen.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;   // Map 추가
import java.util.Set;

public class FriendSearchFragment extends Fragment {

    private EditText editTextSearchFriend;
    private Button buttonSearchFriend;
    private RecyclerView recyclerViewSearchResults;
    private TextView textViewNoResults;
    private FriendSearchAdapter adapter;
    private List<FriendItem> searchResultsList;
    private FirebaseFirestore db;

    // 친구 목록 Fragment 참조 (추가)
    private FriendListFragment friendListFragment;
    private FirebaseUser currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.friend_fragment, container, false);

        editTextSearchFriend = view.findViewById(R.id.editTextSearchFriend);
        buttonSearchFriend = view.findViewById(R.id.buttonSearchFriend);
        recyclerViewSearchResults = view.findViewById(R.id.recyclerViewSearchResults);
        textViewNoResults = view.findViewById(R.id.textViewNoResults);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
        searchResultsList = new ArrayList<>();
        adapter = new FriendSearchAdapter(searchResultsList);
        recyclerViewSearchResults.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewSearchResults.setAdapter(adapter);

        buttonSearchFriend.setOnClickListener(v -> {
            String query = editTextSearchFriend.getText().toString().trim();
            if (!query.isEmpty()) {
                if (currentUser != null && currentUser.getEmail() != null) {
                    searchFriends(query, currentUser.getEmail());
                } else {
                    Toast.makeText(getContext(), "로그인 정보를 확인할 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 어댑터에 친구 추가 클릭 리스너 설정
        adapter.setOnFriendAddClickListener(friendId -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext()); // ✅ Fragment에서 Context 가져오기
            builder.setTitle("친구 추가")
                    .setMessage("친구 목록에 추가하시겠습니까?")
                    .setPositiveButton("확인", (dialog, which) -> {
                        addFriendToFirestore(friendId); // Firestore에 친구 추가하는 메서드 호출

                        Toast.makeText(requireContext(), "정상적으로 추가됐습니다!", Toast.LENGTH_SHORT).show();
                        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.fragment_container, new FriendSearchFragment());
                        transaction.commit();
                    })
                    .setNegativeButton("취소", (dialog, which) -> dialog.dismiss());

            AlertDialog dialog = builder.create();
            dialog.show();

        });

        return view;
    }

    // FriendSearchFragment.java 내의 searchFriends 메서드

    private void searchFriends(String query, final String currentUserEmail) {
        // 1. 현재 사용자의 'friend' 맵에 있는 친구들의 이메일(문서 ID로 사용되는) 목록 가져오기
        db.collection("users").document(currentUserEmail).get()
                .addOnSuccessListener(documentSnapshot -> {
                    Set<String> idsToExclude = new HashSet<>();
                    // 항상 자기 자신은 제외 목록에 추가
                    idsToExclude.add(currentUserEmail);

                    if (documentSnapshot.exists()) {
                        Map<String, Object> userData = documentSnapshot.getData();
                        if (userData != null && userData.containsKey("friend")) {
                            Object friendFieldObject = userData.get("friend");
                            if (friendFieldObject instanceof Map) {
                                @SuppressWarnings("unchecked")
                                Map<String, Object> friendMap = (Map<String, Object>) friendFieldObject;
                                // friendMap의 키는 "user_email_com" 같은 형식이므로,
                                // 이를 실제 이메일 주소(문서 ID로 사용되는)로 변환하여 제외 목록에 추가
                                for (String friendKeyInMap : friendMap.keySet()) {
                                    idsToExclude.add(friendKeyInMap.replace("_", "."));
                                }
                            }
                        }
                    }

                    // 2. 사용자 검색 쿼리 실행
                    db.collection("users")
                            .orderBy("name") // 이름으로 검색 (또는 이메일 필드 'mail' 등)
                            .startAt(query)
                            .endAt(query + "\uf8ff")
                            .limit(20) // 필터링 전 충분한 수의 문서를 가져옴 (필요에 따라 조정)
                            .get()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    searchResultsList.clear(); // 이전 검색 결과 초기화
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        String searchedUserDocumentId = document.getId(); // 검색된 사용자의 문서 ID (이메일이라고 가정)

                                        // 조건: 제외 목록(idsToExclude)에 없는 사용자만 추가
                                        if (!idsToExclude.contains(searchedUserDocumentId)) {
                                            String name = document.getString("name");
                                            // 'mail' 필드가 있다면 그것을 사용, 없다면 문서 ID를 이메일로 간주
                                            // String email = document.getString("mail") != null ? document.getString("mail") : searchedUserDocumentId;
                                            Long pointLong = document.getLong("totalPoint");
                                            Long levelLong = document.getLong("level");

                                            Integer points = (pointLong != null) ? pointLong.intValue() : 0;
                                            Integer level = (levelLong != null) ? levelLong.intValue() : 0;
                                            // 프로필 이미지 URL (문서 ID가 이미지 파일명과 관련있다고 가정)
                                            String profileImageUrl = "profile_images/" + searchedUserDocumentId + ".jpg";

                                            if (name != null) {
                                                searchResultsList.add(new FriendItem(searchedUserDocumentId, name, points, level, profileImageUrl));
                                            }
                                        }
                                    }
                                    adapter.setFriendList(searchResultsList); // 어댑터에 최종 필터링된 결과 설정
                                    if (searchResultsList.isEmpty()) {
                                        textViewNoResults.setText("검색 결과가 없습니다.");
                                        textViewNoResults.setVisibility(View.VISIBLE);
                                    } else {
                                        textViewNoResults.setVisibility(View.GONE);
                                    }
                                } else {
                                    Log.e("FriendSearch", "친구 검색 쿼리 실패", task.getException());
                                    textViewNoResults.setText("검색에 실패했습니다.");
                                    textViewNoResults.setVisibility(View.VISIBLE);
                                    searchResultsList.clear(); // 실패 시 목록 비우기
                                    adapter.notifyDataSetChanged();
                                }
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("FriendSearch", "현재 사용자 친구 정보 가져오기 실패", e);
                    Toast.makeText(getContext(), "친구 정보를 가져오는 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                    // 이 경우, 필터링 없이 검색을 진행하거나 사용자에게 알릴 수 있습니다.
                    // 여기서는 간단히 빈 결과를 표시하도록 처리합니다.
                    searchResultsList.clear();
                    adapter.notifyDataSetChanged();
                    textViewNoResults.setText("정보 조회 오류로 검색할 수 없습니다.");
                    textViewNoResults.setVisibility(View.VISIBLE);
                });
    }

    // 친구 목록 Fragment 설정 (Activity에서 호출)
    public void setFriendListFragment(FriendListFragment fragment) {
        this.friendListFragment = fragment;
    }

    // Firestore에 친구 관계를 추가하는 메서드
    private void addFriendToFirestore(String friendDocumentId) { // friendDocumentId는 검색된 사용자의 문서 ID
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            final String currentUserEmail = currentUser.getEmail(); // 요청을 보내는 현재 사용자의 이메일
            if (currentUserEmail != null) {
                // friendDocumentId는 검색된 사용자(요청을 받을 사용자)의 문서 ID.
                // 이 사용자의 문서에 현재 사용자의 이메일을 키로, false를 값으로 추가.

                // 현재 사용자의 이메일을 Firestore 필드 키로 사용하기 위해 '.' 대체
                String currentUserEmailKeyForMap = currentUserEmail.replace(".", "_");

                // 업데이트할 필드의 정확한 경로 지정: "friend.현재사용자이메일키"
                // 이 경로는 'friendDocumentId'로 식별되는 문서 내부에 적용됨
                String fieldPathInFriendDoc = "friend." + currentUserEmailKeyForMap;

                db.collection("users").document(friendDocumentId) // 요청을 받는 친구의 문서 ID로 접근
                        .update(fieldPathInFriendDoc, false) // 'friend.현재사용자이메일키' 필드의 값을 false로 설정
                        .addOnSuccessListener(aVoid -> {
                            Log.d("FriendSearch", "Friend request from " + currentUserEmail + " sent to user " + friendDocumentId + ". Status set to false in their 'friend' map.");
                            // 사용자에게는 "친구 요청을 보냈습니다" 와 같이 표시하는 것이 적절
                            Toast.makeText(requireContext(), "친구 요청을 보냈습니다.", Toast.LENGTH_SHORT).show();

                            // (선택 사항) 현재 사용자의 문서에도 이 요청 상태를 기록할 수 있음
                            // 예: users/{currentUserEmail}/sent_requests/{friendDocumentId_or_Email} = true (또는 타임스탬프)
                            // String friendEmailForMyDoc = ""; // friendDocumentId로 이메일을 조회해야 할 수 있음
                            // db.collection("users").document(friendDocumentId).get().addOnSuccessListener(snapshot -> {
                            //    if(snapshot.exists()){
                            //        friendEmailForMyDoc = snapshot.getString("mail");
                            //        if(friendEmailForMyDoc != null){
                            //             String mySentRequestKey = friendEmailForMyDoc.replace(".", "_");
                            //             db.collection("users").document(currentUserEmail)
                            //                .update("sent_requests." + mySentRequestKey, true);
                            //        }
                            //    }
                            // });


                            // UI 새로고침 또는 다른 작업
                            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                            transaction.replace(R.id.fragment_container, new FriendSearchFragment()); // 현재 프래그먼트 새로고침
                            transaction.commit();
                        })
                        .addOnFailureListener(e -> {
                            Log.e("FriendSearch", "Error sending friend request to user " + friendDocumentId, e);
                            Toast.makeText(requireContext(), "친구 요청에 실패했습니다.", Toast.LENGTH_SHORT).show();
                        });
            } else {
                Log.e("FriendSearch", "Current user email is null.");
                Toast.makeText(requireContext(), "로그인 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e("FriendSearch", "Current user is not logged in.");
            Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
        }
    }

    // 친구를 목록에 추가하는 메서드 (기존 코드 유지)
    private void addFriendToList(String friendId) {
        if (friendListFragment != null) {
            // 검색 결과에서 선택한 친구 정보를 찾음
            for (FriendItem friend : searchResultsList) {
                if (friend.getUserId().equals(friendId)) {
                    friendListFragment.addFriend(friend); // FriendListFragment에 친구 추가
                    break;
                }
            }
        } else {
            Log.e("FriendSearch", "FriendListFragment가 설정되지 않았습니다.");
        }
    }
}