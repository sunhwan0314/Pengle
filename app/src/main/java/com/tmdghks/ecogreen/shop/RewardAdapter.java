package com.tmdghks.ecogreen.shop;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tmdghks.ecogreen.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RewardAdapter extends RecyclerView.Adapter<RewardAdapter.RewardViewHolder> {
    private FirebaseAuth mAuth; // Firebase Authentication 인스턴스

    private final ArrayList<RewardItem> rewardItems;
    private final Context context;
    private final FragmentManager fragmentManager;

    /**
     * ShopFragment(UI) 쪽으로 포인트 변경을 전파하기 위한 콜백.
     */
    public interface OnPointUpdateListener {
        void onPointUpdated(int newPoint);
    }

    private OnPointUpdateListener pointUpdateListener;

    public void setOnPointUpdateListener(OnPointUpdateListener listener) {
        this.pointUpdateListener = listener;
    }

    public RewardAdapter(ArrayList<RewardItem> rewardItems, Context context, FragmentManager fragmentManager) {
        this.rewardItems = rewardItems;
        this.context = context;
        this.fragmentManager = fragmentManager;
    }

    @NonNull
    @Override
    public RewardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reward, parent, false);
        return new RewardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RewardViewHolder holder, int position) {
        RewardItem item = rewardItems.get(position);
        holder.rewardNameTextView.setText(item.getName());
        holder.rewardPointsTextView.setText(item.getPoints());
        holder.rewardImageView.setImageResource(item.getImageResourceId());
        mAuth = FirebaseAuth.getInstance(); // FirebaseAuth 초기화

        // "구매" 버튼(+) 클릭 리스너
        holder.addButton.setOnClickListener(v -> {
            // 다이얼로그를 생성 & 콜백 연결
            RewardDialogFragment dialogFragment = RewardDialogFragment.newInstance(
                    item.getName(),
                    item.getPoints(),
                    item.getDescription()
            );

            dialogFragment.setOnPurchaseListener(cost -> {
                // 1. 현재 보유 포인트 조회
                SharedPreferences prefs = context.getSharedPreferences("ChecklistPrefs", Context.MODE_PRIVATE);
                int currentPoint = prefs.getInt("totalPoint", 0);

                // 2. 포인트 충분한지 확인
                if (currentPoint < cost) {
                    Toast.makeText(context, "포인트가 부족합니다!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 3. 포인트 차감 및 저장
                int newPoint = currentPoint - cost;
                prefs.edit().putInt("totalPoint", newPoint).apply();


                FirebaseUser user = mAuth.getCurrentUser(); // 현재 로그인된 사용자 가져오기

                if (user != null && user.getEmail() != null) { // 사용자 & 이메일 null 체크 필수
                    String email = user.getEmail(); // 사용자 이메일 가져오기

                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    DocumentReference userRef = db.collection("users").document(email); // 이메일을 문서 ID로 사용

                    // 한 번에 데이터 업데이트 (최적화)
                    Map<String, Object> updatedData = new HashMap<>();
                    updatedData.put("totalPoint", Long.valueOf(newPoint));

                    userRef.update(updatedData)
                            .addOnSuccessListener(aVoid -> Log.d("Firestore", "데이터 업데이트 성공!"))
                            .addOnFailureListener(e -> Log.w("Firestore", "Firestore 업데이트 실패", e));
                } else {
                    Log.e("Firestore", "FirebaseUser 또는 이메일이 null입니다. 로그인 여부 확인 필요!");
                }



                // 4. ShopFragment UI 갱신
                if (pointUpdateListener != null) {
                    pointUpdateListener.onPointUpdated(newPoint);
                }

                // 5. 완료 알림
                Toast.makeText(context, "아이템을 구매했습니다!", Toast.LENGTH_SHORT).show();
            });

            dialogFragment.show(fragmentManager, "reward_dialog");
        });
    }

    @Override
    public int getItemCount() {
        return rewardItems.size();
    }

    public static class RewardViewHolder extends RecyclerView.ViewHolder {
        TextView rewardNameTextView;
        TextView rewardPointsTextView;
        ImageView rewardImageView;
        ImageView addButton;

        public RewardViewHolder(@NonNull View itemView) {
            super(itemView);
            rewardNameTextView = itemView.findViewById(R.id.reward_name);
            rewardPointsTextView = itemView.findViewById(R.id.point);
            rewardImageView = itemView.findViewById(R.id.reward_image);
            addButton = itemView.findViewById(R.id.add_button);
        }
    }
}
