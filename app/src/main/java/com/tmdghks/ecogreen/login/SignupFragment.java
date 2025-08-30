package com.tmdghks.ecogreen.login;

import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.firebase.auth.FirebaseAuth;
import com.tmdghks.ecogreen.R;

//회원가입 화면
public class SignupFragment extends Fragment {

    private FirebaseAuth mAuth; // Firebase Authentication 인스턴스

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // 회원가입 화면 레이아웃 설정
        View view = inflater.inflate(R.layout.fragment_signup, container, false);

        // FirebaseAuth 초기화
        mAuth = FirebaseAuth.getInstance();

        // UI 요소 연결
        EditText etEmail = view.findViewById(R.id.etUsername);
        EditText etPassword = view.findViewById(R.id.etPassword);
        EditText etConfirmPassword = view.findViewById(R.id.etConfirmPassword);
        Button btnSignup = view.findViewById(R.id.btnSignup);
        Button btnBackToLogin = view.findViewById(R.id.btnBackToLogin);

        // 회원가입 버튼 클릭 리스너
        btnSignup.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            // 유효성 검사
            if (email.isEmpty()) {
                etEmail.setError("이메일을 입력하세요.");
                return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.setError("유효한 이메일 주소를 입력하세요.");
                return;
            }
            if (password.isEmpty()) {
                etPassword.setError("비밀번호를 입력하세요.");
                return;
            }
            if (password.length() < 6) {
                etPassword.setError("비밀번호는 최소 6자리여야 합니다.");
                return;
            }
            if (!password.equals(confirmPassword)) {
                etConfirmPassword.setError("비밀번호가 일치하지 않습니다.");
                return;
            }

            // Firebase 회원가입 처리
            registerUser(email, password);
        });

        // 뒤로가기 버튼 클릭 리스너
        btnBackToLogin.setOnClickListener(v -> replaceFragment(new LoginFragment()));

        return view;
    }

    private void registerUser(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // 회원가입 성공
                        Toast.makeText(getContext(), "회원가입 성공!", Toast.LENGTH_SHORT).show();
                        // 성공 시 LoginFragment로 이동
                        replaceFragment(new LoginFragment());
                    } else {
                        // 회원가입 실패
                        Toast.makeText(getContext(), "회원가입 실패: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment); // 컨테이너 ID에 Fragment 추가
        transaction.addToBackStack(null); // 뒤로가기 버튼 처리 가능
        transaction.commit();
    }
}