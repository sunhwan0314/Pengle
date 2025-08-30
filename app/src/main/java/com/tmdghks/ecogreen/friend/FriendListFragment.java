package com.tmdghks.ecogreen.friend;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tmdghks.ecogreen.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FriendListFragment extends Fragment {

    private RecyclerView recyclerViewFriendList;
    private TextView textViewEmptyFriendList;
    private Button buttonGoToSearch;
    private FriendSearchAdapter adapter;
    private List<FriendItem> friendList;
    private FirebaseFirestore db;
    private String currentUserId; // 현재 사용자 ID

    private Button buttonGoToRequest;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.friend_list, container, false);

        recyclerViewFriendList = view.findViewById(R.id.recyclerViewFriendList);
        textViewEmptyFriendList = view.findViewById(R.id.textViewEmptyFriendList);
        buttonGoToSearch = view.findViewById(R.id.buttonGoToSearch);
        buttonGoToRequest = view.findViewById(R.id.buttonGoToRequest);

        recyclerViewFriendList.setLayoutManager(new LinearLayoutManager(getContext()));
        friendList = new ArrayList<>();
        adapter = new FriendSearchAdapter(friendList); // FriendSearchAdapter 재활용
        recyclerViewFriendList.setAdapter(adapter);



        db = FirebaseFirestore.getInstance();
        currentUserId = getCurrentUserId(); // 현재 사용자 ID 가져오는 메서드 (구현 필요)

        buttonGoToRequest.setOnClickListener(v -> {
            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new FriendRequestFragment());
            transaction.addToBackStack(null);
            transaction.commit();

        });
        // "친구 찾기" 버튼 클릭 리스너
        buttonGoToSearch.setOnClickListener(v -> {
            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new FriendSearchFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        });

        getFriendListFromFirestore(); // 친구 목록 가져오기

        adapter.setOnFriendAddClickListener(friendId -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext()); // ✅ Fragment에서 Context 가져오기
            builder.setTitle("친구 삭제")
                    .setMessage("친구 목록에서 지우시겠습니까?")
                    .setPositiveButton("확인", (dialog, which) -> {
                        removeFriendFromFirestore(friendId); // Firestore에 친구 추가하는 메서드 호출

                        Toast.makeText(requireContext(), "이제 이 친구를 볼 수 없어요!", Toast.LENGTH_SHORT).show();
                        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.fragment_container, new FriendListFragment());
                        transaction.commit();
                    })
                    .setNegativeButton("취소", (dialog, which) -> dialog.dismiss());

            AlertDialog dialog = builder.create();
            dialog.show();

        });
        return view;
    }

    private String getCurrentUserId() {
        // TODO: 현재 사용자 ID를 가져오는 실제 로직을 구현 (예: FirebaseAuth 사용)
        return "someUserId";  // 임시로 하드코딩, 실제 구현 필요
    }

    private void getFriendListFromFirestore() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null || currentUser.getEmail() == null) {
            Log.e("FriendListFragment", "Current user not logged in or email is null.");
            textViewEmptyFriendList.setText("로그인 정보가 없습니다.");
            textViewEmptyFriendList.setVisibility(View.VISIBLE);
            recyclerViewFriendList.setVisibility(View.GONE);
            return;
        }
        String currentUserEmail = currentUser.getEmail();

        db.collection("users").document(currentUserEmail).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // 'friend' 필드가 Map 타입이라고 가정
                        Map<String, Object> userData = documentSnapshot.getData();
                        if (userData != null && userData.containsKey("friend")) {
                            Object friendFieldObject = userData.get("friend");
                            if (friendFieldObject instanceof Map) {
                                @SuppressWarnings("unchecked") // 타입 캐스팅 경고 억제
                                Map<String, Object> friendMap = (Map<String, Object>) friendFieldObject;
                                List<String> friendEmailKeys = new ArrayList<>();

                                for (Map.Entry<String, Object> entry : friendMap.entrySet()) {
                                    // 값이 Boolean 타입이고 true인 경우만 필터링
                                    if (entry.getValue() instanceof Boolean && (Boolean) entry.getValue()) {
                                        // 키는 "user_email_com" 형식이므로 원래 이메일로 복원 (선택적)
                                        String originalEmail = entry.getKey().replace("_", ".");
                                        friendEmailKeys.add(originalEmail);
                                    }
                                }

                                if (!friendEmailKeys.isEmpty()) {
                                    textViewEmptyFriendList.setVisibility(View.GONE);
                                    recyclerViewFriendList.setVisibility(View.VISIBLE);
                                    fetchFriendDetails(friendEmailKeys); // 실제 친구 정보를 가져오는 메서드 호출
                                } else {
                                    Log.d("Firestore", "친구 목록이 비어 있습니다 (값이 true인 친구 없음).");
                                    friendList.clear(); // 기존 목록 비우기
                                    adapter.notifyDataSetChanged(); // 어댑터에 알림
                                    textViewEmptyFriendList.setText("친구가 없습니다.");
                                    textViewEmptyFriendList.setVisibility(View.VISIBLE);
                                    recyclerViewFriendList.setVisibility(View.GONE);
                                }
                            } else {
                                Log.d("Firestore", "'friend' 필드가 Map 타입이 아닙니다.");
                                friendList.clear();
                                adapter.notifyDataSetChanged();
                                textViewEmptyFriendList.setText("친구 목록 형식이 올바르지 않습니다.");
                                textViewEmptyFriendList.setVisibility(View.VISIBLE);
                                recyclerViewFriendList.setVisibility(View.GONE);
                            }
                        } else {
                            Log.d("Firestore", "'friend' 필드가 존재하지 않습니다.");
                            friendList.clear();
                            adapter.notifyDataSetChanged();
                            textViewEmptyFriendList.setText("친구 목록이 없습니다.");
                            textViewEmptyFriendList.setVisibility(View.VISIBLE);
                            recyclerViewFriendList.setVisibility(View.GONE);
                        }
                    } else {
                        Log.d("Firestore", "현재 사용자 문서 없음: " + currentUserEmail);
                        friendList.clear();
                        adapter.notifyDataSetChanged();
                        textViewEmptyFriendList.setText("사용자 정보를 찾을 수 없습니다.");
                        textViewEmptyFriendList.setVisibility(View.VISIBLE);
                        recyclerViewFriendList.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "친구 목록 가져오기 실패", e);
                    textViewEmptyFriendList.setText("친구 목록을 가져오지 못했습니다.");
                    textViewEmptyFriendList.setVisibility(View.VISIBLE);
                    recyclerViewFriendList.setVisibility(View.GONE);
                });
    }

    // `users` 컬렉션에서 친구 정보 가져오기
    private void fetchFriendDetails(List<String> friendEmails) {
        friendList.clear(); // 새 목록을 가져오기 전에 기존 목록 비우기
        if (friendEmails.isEmpty()) {
            adapter.notifyDataSetChanged(); // 어댑터에 빈 목록 알림
            textViewEmptyFriendList.setText("친구가 없습니다.");
            textViewEmptyFriendList.setVisibility(View.VISIBLE);
            recyclerViewFriendList.setVisibility(View.GONE);
            return;
        }

        for (String friendEmail : friendEmails) {
            // friendEmail은 실제 이메일 주소여야 함 (문서 ID로 사용)
            db.collection("users").document(friendEmail).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            // Firestore에 저장된 필드명이 "mail" 또는 "email"인지 확인
                            String emailFromDoc = documentSnapshot.getString("mail"); // 또는 documentSnapshot.getId() 사용 가능 (문서 ID가 이메일인 경우)
                            if (emailFromDoc == null) {
                                emailFromDoc = documentSnapshot.getId(); // 문서 ID를 이메일로 사용
                            }

                            Long pointLong = documentSnapshot.getLong("totalPoint");
                            Long levelLong = documentSnapshot.getLong("level");
                            Integer levelInt = levelLong != null ? levelLong.intValue() : 0; // 기본값 설정
                            Integer pointInt = pointLong != null ? pointLong.intValue() : 0; // 기본값 설정

                            // 프로필 이미지 URL 생성 (문서 ID가 이메일이라고 가정)
                            String profileImageUrl = "profile_images/" + emailFromDoc + ".jpg";

                            if (name != null) { // 이메일은 이미 friendEmail로 가지고 있음
                                friendList.add(new FriendItem(emailFromDoc, name, pointInt, levelInt, profileImageUrl));
                                adapter.notifyItemInserted(friendList.size() - 1); // 개별 아이템 추가 알림
                                if (friendList.isEmpty()) {
                                    textViewEmptyFriendList.setVisibility(View.VISIBLE);
                                    recyclerViewFriendList.setVisibility(View.GONE);
                                } else {
                                    textViewEmptyFriendList.setVisibility(View.GONE);
                                    recyclerViewFriendList.setVisibility(View.VISIBLE);
                                }
                            } else {
                                Log.w("Firestore", "사용자 정보 일부 누락: " + friendEmail);
                            }
                        } else {
                            Log.w("Firestore", "친구 정보 없음 (문서 ID): " + friendEmail);
                        }
                    })
                    .addOnFailureListener(e -> Log.e("Firestore", "친구 (" + friendEmail + ") 정보 가져오기 실패", e));
        }
        // 모든 비동기 호출이 완료된 후 어댑터를 업데이트하려면 CountDownLatch 또는 다른 동기화 메커니즘이 필요할 수 있습니다.
        // 간단하게는, 마지막 호출 후 또는 일정 시간 후 전체를 notifyDataSetChanged() 할 수 있지만, 개별 추가가 더 효율적입니다.
    }

    // Firestore에서 친구의 키-값 쌍을 삭제하는 메서드
    private void removeFriendFromFirestore(String friendEmailToRemove) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null || currentUser.getEmail() == null) {
            Log.e("FriendListFragment", "Current user not logged in or email is null. Cannot remove friend.");
            Toast.makeText(requireContext(), "로그인 정보가 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        String currentUserEmail = currentUser.getEmail();

        // 삭제할 친구의 이메일을 Firestore 필드 키 형식으로 변환 ('.' -> '_')
        String friendKeyToRemove = friendEmailToRemove.replace(".", "_");

        // users/{currentUserEmail} 문서의 'friend' 맵에서 'friendKeyToRemove' 필드를 삭제
        String fieldPathToRemove = "friend." + friendKeyToRemove;

        db.collection("users").document(currentUserEmail)
                .update(fieldPathToRemove, FieldValue.delete())
                .addOnSuccessListener(aVoid -> {
                    Log.d("FriendListFragment", "Friend " + friendEmailToRemove + " removed from " + currentUserEmail + "'s friend list.");
                    Toast.makeText(requireContext(), friendEmailToRemove + "님을 친구 목록에서 삭제했습니다.", Toast.LENGTH_SHORT).show();

                    // UI 새로고침 또는 목록에서 해당 아이템 제거
                    // 방법 1: 목록 전체 새로고침
//                    getFriendListFromFirestore(); // 친구 목록을 다시 불러옴

                    // 방법 2: 로컬 리스트에서 해당 아이템 직접 제거 및 어댑터 알림 (더 효율적일 수 있음)
                     int indexToRemove = -1;
                     for (int i = 0; i < friendList.size(); i++) {
                        if (friendList.get(i).getProfileImageUrl().equals(friendEmailToRemove)) {
                            indexToRemove = i;
                            break;
                        }
                     }
                     if (indexToRemove != -1) {
                        friendList.remove(indexToRemove);
                        adapter.notifyItemRemoved(indexToRemove);
                        adapter.notifyItemRangeChanged(indexToRemove, friendList.size());
                        if (friendList.isEmpty()) {
                            textViewEmptyFriendList.setText("친구가 없습니다.");
                            textViewEmptyFriendList.setVisibility(View.VISIBLE);
                            recyclerViewFriendList.setVisibility(View.GONE);
                        }
                     }

                    // (선택 사항) 양방향 관계인 경우, 상대방의 친구 목록에서도 현재 사용자를 삭제하는 로직 추가
                     String currentUserKeyForFriend = currentUserEmail.replace(".", "_");
                     String friendSideFieldPathToRemove = "friend." + currentUserKeyForFriend;
                     db.collection("users").document(friendEmailToRemove) // 상대방 이메일로 문서 접근
                            .update(friendSideFieldPathToRemove, FieldValue.delete())
                            .addOnSuccessListener(aVoid1 -> Log.d("FriendListFragment", currentUserEmail + " removed from " + friendEmailToRemove + "'s list."))
                            .addOnFailureListener(e1 -> Log.e("FriendListFragment", "Error removing " + currentUserEmail + " from " + friendEmailToRemove + "'s list.", e1));

                })
                .addOnFailureListener(e -> {
                    Log.e("FriendListFragment", "Error removing friend " + friendEmailToRemove + " from " + currentUserEmail + "'s list", e);
                    Toast.makeText(requireContext(), "친구 삭제에 실패했습니다.", Toast.LENGTH_SHORT).show();
                });
    }
    public void addFriend(FriendItem friend) {
        friendList.add(friend);
        adapter.setFriendList(friendList);
        textViewEmptyFriendList.setVisibility(View.GONE);
    }
}