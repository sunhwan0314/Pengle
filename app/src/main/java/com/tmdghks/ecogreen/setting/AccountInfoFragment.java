package com.tmdghks.ecogreen.setting;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.tmdghks.ecogreen.R;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class AccountInfoFragment extends Fragment {

    private static final String TAG = "AccountInfoFragment";

    // UI 요소 선언
    private ImageView imageViewProfilePicture;
    private ImageView imageViewChangeProfilePicture;
    private EditText editTextNickname;
    private TextView textViewEmail;
    // private Button buttonChangePassword; // <-- 이 줄 제거
    private Button buttonSaveProfile;

    // Firebase 인스턴스
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private FirebaseUser currentUser;

    // 이미지 URI 저장
    private Uri selectedImageUri; // 갤러리/카메라에서 선택된 이미지 URI

    // ActivityResultLauncher for picking image from gallery
    private ActivityResultLauncher<Intent> pickImageLauncher;
    // ActivityResultLauncher for taking picture from camera
    private ActivityResultLauncher<Intent> takePictureLauncher;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // ActivityResultLauncher는 onAttach에서 초기화하는 것이 좋습니다.
        // 갤러리에서 이미지 가져오기
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            // ★ 추가된 부분: MIME 타입 확인 ★
                            String mimeType = requireContext().getContentResolver().getType(selectedImageUri);
                            if (mimeType != null && mimeType.startsWith("image/")) {
                                Glide.with(this)
                                        .load(selectedImageUri)
                                        .circleCrop()
                                        .into(imageViewProfilePicture);
                                Log.d(TAG, "갤러리에서 이미지 선택 완료: " + selectedImageUri.toString() + ", MIME: " + mimeType);
                            } else {
                                selectedImageUri = null; // 이미지 파일이 아니므로 초기화
                                Toast.makeText(getContext(), "이미지 파일만 선택할 수 있습니다.", Toast.LENGTH_LONG).show();
                                Log.w(TAG, "선택된 파일이 이미지가 아님: " + (mimeType != null ? mimeType : "알 수 없음"));
                            }
                        }
                    } else {
                        Log.d(TAG, "갤러리에서 이미지 선택 취소 또는 실패");
                    }
                });

        // 카메라로 사진 촬영
        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Bundle extras = result.getData().getExtras();
                        Bitmap imageBitmap = (Bitmap) extras.get("data");
                        if (imageBitmap != null) {
                            // 비트맵을 URI로 변환 (Storage 업로드를 위해)
                            selectedImageUri = getImageUri(requireContext(), imageBitmap);
                            if (selectedImageUri != null) {
                                Glide.with(this)
                                        .load(imageBitmap) // 비트맵 직접 로드
                                        .circleCrop()
                                        .into(imageViewProfilePicture);
                                Log.d(TAG, "카메라로 사진 촬영 완료, URI: " + selectedImageUri.toString());
                            } else {
                                Log.e(TAG, "비트맵을 URI로 변환 실패");
                                Toast.makeText(getContext(), "사진을 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        Log.d(TAG, "사진 촬영 취소 또는 실패");
                    }
                });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_account_info, container, false);

        // UI 요소 초기화
        imageViewProfilePicture = view.findViewById(R.id.imageViewProfilePicture);
        imageViewChangeProfilePicture = view.findViewById(R.id.imageViewChangeProfilePicture);
        editTextNickname = view.findViewById(R.id.editTextNickname);
        buttonSaveProfile = view.findViewById(R.id.buttonSaveProfile);

        // Firebase 인스턴스 초기화
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(getContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            // 로그인 화면으로 이동 또는 처리 로직 추가
            return view; // 더 이상 진행하지 않음
        }

        // 현재 사용자 정보 로드 및 UI 업데이트
        loadUserProfile();

        // 프로필 사진 변경 버튼 클릭 리스너
        imageViewChangeProfilePicture.setOnClickListener(v -> showImagePickerDialog());


        // 변경 사항 저장 버튼 클릭 리스너
        buttonSaveProfile.setOnClickListener(v ->
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(requireContext()); // ✅ Fragment에서 Context 가져오기
                    builder.setTitle("프로필 저장")
                            .setMessage("변경된 사항을 저장 하시겠습니까?")
                            .setPositiveButton("확인", (dialog, which) -> {
                                saveUserProfile();
                                Toast.makeText(getContext(), "변경된 사항을 저장 중입니다...", Toast.LENGTH_SHORT).show();

                                FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                                transaction.replace(R.id.fragment_container, new SettingsFragment());
                                transaction.commit();
                            })
                            .setNegativeButton("취소", (dialog, which) -> dialog.dismiss());

                    AlertDialog dialog = builder.create();
                    dialog.show();

                });

        return view;
    }

    private void loadUserProfile() {
        if (currentUser == null) return;

        // 이메일 표시

        // Firestore에서 사용자 닉네임 및 프로필 이미지 URL 로드
        db.collection("users").document(currentUser.getEmail()) // 이메일을 문서 ID로 사용
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String nickname = documentSnapshot.getString("name"); // "name" 필드에서 닉네임 가져오기
                        String profileImageUrl = documentSnapshot.getString("profileImageUrl");

                        if (nickname != null) {
                            editTextNickname.setText(nickname);
                        }

                        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                            // Glide로 프로필 이미지 로드
                            Glide.with(this)
                                    .load(profileImageUrl)
                                    .circleCrop()
                                    .placeholder(R.drawable.userdefault) // 로딩 중 표시할 이미지
                                    .error(R.drawable.userdefault)      // 로드 실패 시 표시할 이미지
                                    .listener(new RequestListener<Drawable>() {
                                        @Override
                                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                            Log.e(TAG, "프로필 이미지 로드 실패: " + e.getMessage(), e);
                                            return false; // 오류 처리 Glide에 맡김
                                        }

                                        @Override
                                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                            Log.d(TAG, "프로필 이미지 로드 성공");
                                            return false;
                                        }
                                    })
                                    .into(imageViewProfilePicture);
                        } else {
                            // 프로필 이미지가 없으면 기본 이미지 설정
                            imageViewProfilePicture.setImageResource(R.drawable.userdefault);
                        }
                    } else {
                        Log.d(TAG, "사용자 문서 없음: " + currentUser.getEmail());
                        Toast.makeText(getContext(), "사용자 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
                        imageViewProfilePicture.setImageResource(R.drawable.userdefault);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "사용자 정보 로드 실패", e);
                    Toast.makeText(getContext(), "사용자 정보를 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
                    imageViewProfilePicture.setImageResource(R.drawable.userdefault);
                });
    }

    private void showImagePickerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("프로필 사진 설정");
        builder.setItems(new CharSequence[]{"사진 촬영", "갤러리에서 선택"}, (dialog, which) -> {
            switch (which) {
                case 0: // 사진 촬영
                    if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
                    } else {
                        dispatchTakePictureIntent();
                    }
                    break;
                case 1: // 갤러리에서 선택
                    dispatchPickImageIntent();
                    break;
            }
        });
        builder.show();
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            takePictureLauncher.launch(takePictureIntent);
        } else {
            Toast.makeText(getContext(), "카메라 앱을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private void dispatchPickImageIntent() {
        Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        // MIME 타입을 "image/*"로 설정하여 이미지 파일만 선택 가능하도록 제한합니다.
        pickPhotoIntent.setType("image/*");
        pickImageLauncher.launch(pickPhotoIntent);
        Log.d(TAG, "갤러리에서 이미지 선택 인텐트 시작");
    }

    // Bitmap을 Uri로 변환하는 헬퍼 메서드 (카메라에서 찍은 사진을 위해)
    private Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            } else {
                Toast.makeText(getContext(), "카메라 권한이 거부되었습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveUserProfile() {
        if (currentUser == null) {
            Toast.makeText(getContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        String newNickname = editTextNickname.getText().toString().trim();
        if (newNickname.isEmpty()) {
            Toast.makeText(getContext(), "닉네임을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Firestore에 업데이트할 데이터 맵
        Map<String, Object> userUpdates = new HashMap<>();
        userUpdates.put("name", newNickname); // "name" 필드 업데이트

        // 프로필 사진이 새로 선택된 경우 Storage에 업로드
        if (selectedImageUri != null) {
            String imageFileName = currentUser.getEmail() + ".jpg"; // 사용자 이메일을 파일 이름으로 사용
            StorageReference profileImageRef = storageRef.child("profile_images/" + imageFileName); // "profile_images" 폴더에 저장

            UploadTask uploadTask = profileImageRef.putFile(selectedImageUri);

            uploadTask.addOnProgressListener(snapshot -> {
                double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                Log.d(TAG, "업로드 진행률: " + progress + "%");
                // TODO: 프로그레스 다이얼로그 등을 표시할 수 있음
            }).addOnFailureListener(exception -> {
                Log.e(TAG, "프로필 이미지 업로드 실패", exception);
                Toast.makeText(getContext(), "프로필 사진 업로드에 실패했습니다.", Toast.LENGTH_SHORT).show();
            }).addOnSuccessListener(taskSnapshot -> {
                profileImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String downloadUrl = uri.toString();
                    userUpdates.put("profileImageUrl", downloadUrl); // Firestore에 다운로드 URL 저장
                    Log.d(TAG, "프로필 이미지 업로드 성공, 다운로드 URL: " + downloadUrl);

                    // Firestore 업데이트 (이미지 URL 포함)
                    updateFirestoreUserDocument(userUpdates);

                }).addOnFailureListener(exception -> {
                    Log.e(TAG, "다운로드 URL 가져오기 실패", exception);
                    Toast.makeText(getContext(), "프로필 사진 URL을 가져오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
                });
            });
        } else {
            // 프로필 사진 변경 없이 닉네임만 업데이트
            updateFirestoreUserDocument(userUpdates);
        }
    }

    private void updateFirestoreUserDocument(Map<String, Object> updates) {
        if (currentUser.getEmail() == null) {
            Toast.makeText(getContext(), "사용자 이메일을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        DocumentReference userDocRef = db.collection("users").document(currentUser.getEmail()); // 이메일을 문서 ID로 사용

        userDocRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "프로필이 성공적으로 업데이트되었습니다.", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Firestore 사용자 문서 업데이트 성공: " + updates.toString());
                    // 업데이트 후 UI 갱신 (선택 사항)
                    // loadUserProfile();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "프로필 업데이트에 실패했습니다: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Firestore 사용자 문서 업데이트 실패", e);
                });
    }

    // Fragment가 더 이상 사용되지 않을 때 리소스 해제
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        selectedImageUri = null; // URI 초기화
    }
}