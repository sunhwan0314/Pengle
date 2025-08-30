package com.tmdghks.ecogreen.login;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tmdghks.ecogreen.R;
import com.tmdghks.ecogreen.menu.MenuLoginAfterFragment;

import java.util.HashMap;
import java.util.Map;

public class nameCreateFragment extends Fragment {

    private FirebaseAuth mAuth; // Firebase Authentication 인스턴스

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Fragment가 생성될 때 FirebaseAuth 인스턴스를 초기화합니다.
        mAuth = FirebaseAuth.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_name, container, false);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // onCreateView 시점에는 mAuth가 초기화되었을 것입니다.
        FirebaseUser user = mAuth.getCurrentUser();

        EditText etName = view.findViewById(R.id.etName);
        Button btnName = view.findViewById(R.id.btnName);

        btnName.setOnClickListener(v -> {
            if (user != null && user.getEmail() != null) { // 사용자 & 이메일 null 체크 필수
                String email = user.getEmail(); // 사용자 이메일 가져오기
                DocumentReference userRef = db.collection("users").document(email); // 이메일을 문서 ID로 사용
                Map<String, Object> updatedData = new HashMap<>();
                updatedData.put("name", etName.getText().toString()); // getText() 후 toString() 호출
                userRef.update(updatedData)
                        .addOnSuccessListener(aVoid -> Log.d("Firestore", "데이터 업데이트 성공!"))
                        .addOnFailureListener(e -> Log.w("Firestore", "Firestore 업데이트 실패", e));
                //firestore에 이미지 저장

                replaceFragment(new MenuLoginAfterFragment());
            } else {
                Log.e("Firestore", "FirebaseUser 또는 이메일이 null입니다. 로그인 여부 확인 필요!");
            }
        });

        return view;
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment); // 컨테이너 ID에 Fragment 추가
        transaction.addToBackStack(null); // 뒤로가기 버튼 처리 가능
        transaction.commit();
    }
}