package com.tmdghks.ecogreen.login;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.tmdghks.ecogreen.R;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class LoginFragment extends Fragment {

    private FirebaseAuth mAuth; // Firebase Authentication 인스턴스
    private GoogleSignInClient mGoogleSignInClient; // Google 로그인 클라이언트
    private static final int RC_SIGN_IN = 100; // Google 로그인 요청 코드

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);



        // Firebase 초기화
        mAuth = FirebaseAuth.getInstance();
        // Google 로그인 옵션 설정
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(requireContext().getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Google 로그인 클라이언트 생성
        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);

        // XML 요소 연결
        //5월 2주차
//        EditText etEmail = view.findViewById(R.id.etEmail);
//        EditText etPassword = view.findViewById(R.id.etPassword);
//        Button btnLogin = view.findViewById(R.id.btnLogin);
//        Button btnSignup = view.findViewById(R.id.btnSignup);
        //5월 2주차

        SignInButton btnGoogleSignIn = view.findViewById(R.id.btnGoogleSignIn);
        //5월 2주차

        // 이메일 로그인 버튼 클릭 리스너
//        btnLogin.setOnClickListener(v -> {
//            String email = etEmail.getText().toString().trim();
//            String password = etPassword.getText().toString().trim();
//
//            // 유효성 검사
//            if (email.isEmpty()) {
//                etEmail.setError("이메일을 입력하세요.");
//                return;
//            }
//            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
//                etEmail.setError("유효한 이메일 주소를 입력하세요.");
//                return;
//            }
//            if (password.isEmpty()) {
//                etPassword.setError("비밀번호를 입력하세요.");
//                return;
//            }

//        });
//
//        // 회원가입 버튼 클릭 리스너
//        btnSignup.setOnClickListener(v -> replaceFragment(new SignupFragment()));
        //5월 2주차

        // Google 로그인 버튼 클릭 리스너
        btnGoogleSignIn.setOnClickListener(v -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });

        return view;
    }
    //5월 2주차
//
//    private void loginUser(String email, String password) {
//        mAuth.signInWithEmailAndPassword(email, password)
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        FirebaseUser user = mAuth.getCurrentUser();
//                        Toast.makeText(getContext(), "로그인 성공: " + user.getEmail(), Toast.LENGTH_SHORT).show();
//                        replaceFragment(new MenuLoginAfterFragment());
//                    } else {
//                        Toast.makeText(getContext(), "로그인 실패: " + task.getException().getMessage(),
//                                Toast.LENGTH_LONG).show();
//                    }
//                });
//    }

    //5월 2주차

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Toast.makeText(getContext(), "Google 로그인 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        SharedPreferences prefs = requireContext().getSharedPreferences("ChecklistPrefs", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = prefs.edit();

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {


                        FirebaseUser user = mAuth.getCurrentUser();
                        String email = user.getEmail(); // 사용자 이메일 가져오기
                        String Gname = user.getDisplayName();//사용자 구글 이름 가져오
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        DocumentReference userRef = db.collection("users").document(email); // 이메일을 문서 ID로 사용
                        //firestore에 이미지 저장
                        StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                                .child("profile_images/" + user.getEmail() + ".jpg"); // 사용자 이메일 기반 파일명 설정

                        Glide.with(this)
                                .asBitmap()
                                .load(user.getPhotoUrl()) // Google 프로필 이미지 가져오기
                                .into(new CustomTarget<Bitmap>() {
                                    @Override
                                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                        resource.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                        byte[] imageData = baos.toByteArray();

                                        UploadTask uploadTask = storageRef.putBytes(imageData); // Firebase Storage에 업로드
                                        uploadTask.addOnSuccessListener(taskSnapshot -> Log.d("Firebase", "이미지 업로드 성공!"))
                                                .addOnFailureListener(e -> Log.e("Firebase", "업로드 실패", e));
                                    }

                                    @Override
                                    public void onLoadCleared(@Nullable Drawable placeholder) {}
                                });

                        userRef.get().addOnSuccessListener(documentSnapshot -> {
                            if (!documentSnapshot.exists()) { // 문서가 없으면 생성
                                Map<String, Object> newUser = new HashMap<>();
                                newUser.put("totalPoint", 0);
                                newUser.put("level", 1);
                                newUser.put("name","이름없음");
                                newUser.put("exp",0);
                                newUser.put("mail",email);
                                newUser.put("friend", new HashMap<>()); //
                                userRef.set(newUser) // 문서 저장
                                        .addOnSuccessListener(aVoid -> Log.d(TAG, "Firestore 문서 생성 완료: " + email))
                                        .addOnFailureListener(e -> Log.w(TAG, "Firestore 문서 생성 오류", e));


                            }
                            else{//문서가 있다면 값 초기화

                                editor.putInt("exp", Objects.requireNonNull(documentSnapshot.getLong("exp")).intValue() );
                                editor.putInt("level", Objects.requireNonNull(documentSnapshot.getLong("level")).intValue() );
                                editor.putInt("totalPoint", Objects.requireNonNull(documentSnapshot.getLong("totalPoint")).intValue());

                                editor.apply();

                            }
                        });
//5월 2주차 
                        Toast.makeText(getContext(), "Google 로그인 성공: " + email, Toast.LENGTH_SHORT).show();
                        editor.putBoolean("isLogin",true);
                        editor.apply(); // 변경 사항 적용 (필수)
                        replaceFragment(new nameCreateFragment());
                    } else {

                        Toast.makeText(getContext(), "Firebase 인증 실패: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
//5월 2주차
    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment); // 컨테이너 ID 확인
        transaction.addToBackStack(null); // 뒤로가기 가능
        transaction.commit();
    }
}