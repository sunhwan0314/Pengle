package com.tmdghks.ecogreen.friend;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.tmdghks.ecogreen.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FriendRequestFragment extends Fragment {

    private RecyclerView recyclerView;
    private FriendRequestAdapter adapter;
    private List<FriendRequestItem> friendRequestList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    // 삭제/추가 버튼 리스너 인터페이스 (Adapter에서 ViewHolder로 전달)
    public interface OnFriendRequestListener {
        void onAccept(FriendRequestItem item);
        void onDecline(FriendRequestItem item);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        friendRequestList = new ArrayList<>();

        // Adapter에 리스너 전달 (Fragment에서 직접 처리)
        adapter = new FriendRequestAdapter(friendRequestList, new OnFriendRequestListener() {
            @Override
            public void onAccept(FriendRequestItem item) {
                if (currentUser == null || currentUser.getEmail() == null) {
                    Log.w("FriendRequestFragment", "User not logged in or email is null. Cannot accept friend request.");
                    return;
                }

                final String currentUserEmail = currentUser.getEmail();
                // FriendRequestItem에서 요청자 이메일 가져오기 (item.getUserId()가 이메일이라고 가정)
                final String requestingUserEmail = item.getUserId();

                if (requestingUserEmail == null || requestingUserEmail.isEmpty()) {
                    Log.w("FriendRequestFragment", "Requesting user email is null or empty. Cannot accept friend request.");
                    return;
                }

                // Firestore에서 이메일 주소를 필드 키로 사용할 때 '.' 문자는 허용되지 않으므로, 대체 문자로 변경합니다.
                // 일반적으로 '_'를 사용하지만, 다른 문자를 선택할 수도 있습니다.
                // 저장할 때와 읽을 때 동일한 로직을 사용해야 합니다.
                final String currentUserEmailKey = currentUserEmail.replace(".", "_");
                final String requestingUserEmailKey = requestingUserEmail.replace(".", "_");

                // 1. 현재 사용자의 users 문서 업데이트: 요청 보낸 사용자를 친구로 추가
                //    users/{currentUserEmail}/friend/{requestingUserEmailKey} = true
                db.collection("users").document(currentUserEmail)
                        .update("friend." + requestingUserEmailKey, true)
                        .addOnSuccessListener(aVoid -> {
                            Log.d("FriendRequestFragment", requestingUserEmail + " added to " + currentUserEmail + "'s friend list.");

                            // 2. 요청 보낸 사용자의 users 문서 업데이트: 현재 사용자를 친구로 추가 (양방향)
                            //    users/{requestingUserEmail}/friend/{currentUserEmailKey} = true
                            db.collection("users").document(requestingUserEmail)
                                    .update("friend." + currentUserEmailKey, true)
                                    .addOnSuccessListener(aVoid2 -> {
                                        Log.d("FriendRequestFragment", currentUserEmail + " added to " + requestingUserEmail + "'s friend list.");

                                        // 3. (선택 사항) friend_requests 문서에서 해당 요청 상태 변경 또는 삭제
                                        //    이 부분은 friend_requests 컬렉션을 더 이상 사용하지 않는다면 제거해도 됩니다.
                                        //    만약 users/{currentUserEmail}/friend/{requestingUserEmailKey}의 값이
                                        //    false (요청 상태)에서 true (수락 상태)로 바뀌는 것이라면,
                                        //    이전 로직(friend_requests 컬렉션 업데이트)은 필요 없을 수 있습니다.
                                        //    여기서는 단순히 UI에서만 제거하는 것으로 가정합니다.

                                        // 4. UI 업데이트: 리스트에서 해당 아이템 제거
                                        int position = friendRequestList.indexOf(item);
                                        if (position != -1) {
                                            friendRequestList.remove(position);
                                            adapter.notifyItemRemoved(position);
                                            // 아이템 제거 후 다음 아이템부터 끝까지의 위치가 변경되었음을 알림
                                            adapter.notifyItemRangeChanged(position, friendRequestList.size());
                                        }
                                    })
                                    .addOnFailureListener(e -> Log.e("FriendRequestFragment", "Error adding " + currentUserEmail + " to " + requestingUserEmail + "'s friend list", e));
                        })
                        .addOnFailureListener(e -> Log.e("FriendRequestFragment", "Error adding " + requestingUserEmail + " to " + currentUserEmail + "'s friend list", e));
            }
            @Override
            public void onDecline(FriendRequestItem item) {
                if (currentUser == null || currentUser.getEmail() == null) {
                    Log.w("FriendRequestFragment", "User not logged in or email is null. Cannot decline friend request.");
                    return;
                }
                String currentUserEmail = currentUser.getEmail();
                String requestingUserEmail = item.getUserId(); // FriendRequestItem에서 요청자 ID 가져오기

                if (requestingUserEmail == null || requestingUserEmail.isEmpty()) {
                    Log.w("FriendRequestFragment", "Requesting user email is null or empty. Cannot decline friend request.");
                    return;
                }

                final String requestingUserEmailKey = requestingUserEmail.replace(".", "_");

                // users/{currentUserEmail} 문서에서 friend.{requestingUserEmailKey} 필드를 삭제하거나 false로 설정
                // 여기서는 요청을 보낸 사람의 'friend' 맵에서 현재 사용자의 항목을 false로 변경하거나 삭제하는 로직은 없습니다.
                // 단방향으로 현재 사용자의 friend 목록에서만 해당 요청을 '무시'하는 개념으로,
                // users/{currentUserEmail}/friend/{requestingUserEmailKey} 필드를 false로 업데이트하거나 삭제합니다.
                // 여기서는 false로 업데이트하여 '요청 거절' 상태를 명시적으로 표시합니다.
                // 또는 FieldValue.delete()를 사용하여 필드 자체를 삭제할 수도 있습니다.
                db.collection("users").document(currentUserEmail)
                        .update("friend." + requestingUserEmailKey, FieldValue.delete()) // 필드 삭제
                        .addOnSuccessListener(aVoid -> {
                            Log.d("FriendRequestFragment", "Friend request from " + requestingUserEmail + " declined and removed from " + currentUserEmail + "'s friend map.");
                            // UI 업데이트: 리스트에서 해당 아이템 제거
                            int position = friendRequestList.indexOf(item);
                            if (position != -1) {
                                friendRequestList.remove(position);
                                adapter.notifyItemRemoved(position);
                                adapter.notifyItemRangeChanged(position, friendRequestList.size());
                            }
                        })
                        .addOnFailureListener(e -> Log.e("FriendRequestFragment", "Error declining friend request from " + requestingUserEmail, e));
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friend_request, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewFriendListRequest); // fragment_friend_request.xml의 RecyclerView ID
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);


        if (currentUser != null && currentUser.getEmail() != null) {
            loadFriendRequests(currentUser.getEmail());
        } else {
            Log.w("FriendRequestFragment", "User not logged in or email is null.");
            // 사용자에게 로그인 필요 알림 등 처리
        }
        return view;
    }

    // FriendRequestFragment.java 내의 메서드들

    private void loadFriendRequests(String currentUserEmail) {
        // 현재 사용자의 users 문서에서 friend 맵 가져오기
        db.collection("users").document(currentUserEmail)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        friendRequestList.clear(); // 기존 목록 초기화

                        if (document != null && document.exists()) {
                            Map<String, Object> userData = document.getData();
                            if (userData != null && userData.containsKey("friend")) {
                                Object friendFieldObject = userData.get("friend");
                                if (friendFieldObject instanceof Map) {
                                    @SuppressWarnings("unchecked")
                                    Map<String, Object> friendMap = (Map<String, Object>) friendFieldObject;
                                    boolean foundRequests = false;
                                    for (Map.Entry<String, Object> entry : friendMap.entrySet()) {
                                        // 값이 Boolean이고 false인 경우에만 처리
                                        if (entry.getValue() instanceof Boolean && !(Boolean) entry.getValue()) {
                                            // 키는 "요청자이메일_키" 형태이므로, 실제 이메일로 변환
                                            String requestingUserEmailKey = entry.getKey();
                                            String requestingUserEmail = requestingUserEmailKey.replace("_", ".");
                                            fetchUserDetailsAndCreateItem(requestingUserEmail);
                                            foundRequests = true;
                                        }
                                    }
                                    if (!foundRequests) {
                                        Log.d("FriendRequestFragment", "No pending friend requests (value is false) found in friend map for: " + currentUserEmail);
                                        // 어댑터에 변경 사항 알림 (목록이 비었음을 확실히 하기 위해)
                                        adapter.notifyDataSetChanged();
                                        // TODO: 친구 요청이 없는 경우 UI 처리 (예: 텍스트 표시)
                                    }
                                } else {
                                    Log.d("FriendRequestFragment", "'friend' field is not a Map for: " + currentUserEmail);
                                    adapter.notifyDataSetChanged();
                                }
                            } else {
                                Log.d("FriendRequestFragment", "No 'friend' field found for: " + currentUserEmail);
                                adapter.notifyDataSetChanged();
                                // TODO: 친구 요청이 없는 경우 UI 처리
                            }
                        } else {
                            Log.d("FriendRequestFragment", "No user document found for: " + currentUserEmail);
                            adapter.notifyDataSetChanged();
                            // TODO: 친구 요청이 없는 경우 UI 처리
                        }
                    } else {
                        Log.e("FriendRequestFragment", "Error getting user document for friend requests: ", task.getException());
                        friendRequestList.clear();
                        adapter.notifyDataSetChanged();
                        // TODO: 오류 발생 시 UI 처리
                    }
                });
    }

    private void fetchUserDetailsAndCreateItem(String requestingUserEmail) {
        // 'users' 컬렉션에서 requestingUserEmail (요청자 이메일)을 문서 ID로 사용하여 사용자 정보 조회
        db.collection("users").document(requestingUserEmail)
                .get()
                .addOnSuccessListener(userDocument -> {
                    if (userDocument.exists()) {
                        String userName = userDocument.getString("name"); // 'users' 컬렉션의 'name' 필드

                        // Firebase Storage에서 이미지 가져오기
                        StorageReference storageRef = FirebaseStorage.getInstance().getReference();

                        // Storage에 "profile_images/user@example.com.jpg" 형태로 저장되어 있다고 가정
                        // requestingUserEmail 자체가 "user@example.com" 이므로, 여기에 ".jpg"만 붙입니다.
                        String imageFileNameInStorage = requestingUserEmail + ".jpg";
                        StorageReference profileImageRef = storageRef.child("profile_images/" + imageFileNameInStorage);

                        profileImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            // 이미지 다운로드 URL 성공적으로 가져옴
                            String profileImageUrl = uri.toString();
                            String displayText = (userName != null ? userName : requestingUserEmail) + " 님이 친구 요청을 보냈습니다.";
                            FriendRequestItem item = new FriendRequestItem(requestingUserEmail, displayText, profileImageUrl);
                            friendRequestList.add(item);
                            adapter.notifyItemInserted(friendRequestList.size() - 1);

                        }).addOnFailureListener(exception -> {
                            // 이미지 다운로드 URL 가져오기 실패 (예: 파일이 없거나 권한 문제)
                            // 여기서 404 오류가 발생하면, imageFileNameInStorage 경로를 다시 한번 확인해야 합니다.
                            Log.w("FriendRequestFragment", "Profile image download URL fetch failed for " + imageFileNameInStorage + ". Error: " + exception.getMessage());
                            // 이미지가 없는 경우 기본 이미지 URL 또는 null 처리
                            String defaultProfileImageUrl = null; // 또는 기본 이미지 경로 "drawable/default_profile.png" 등
                            String displayText = (userName != null ? userName : requestingUserEmail) + " 님이 친구 요청을 보냈습니다.";
                            FriendRequestItem item = new FriendRequestItem(requestingUserEmail, displayText, defaultProfileImageUrl);
                            friendRequestList.add(item);
                            adapter.notifyItemInserted(friendRequestList.size() - 1);
                        });

                    } else {
                        Log.w("FriendRequestFragment", "User document not found for email: " + requestingUserEmail + ". Cannot create friend request item.");
                    }
                })
                .addOnFailureListener(e -> Log.e("FriendRequestFragment", "Error fetching user data for email: " + requestingUserEmail, e));

    }
}
