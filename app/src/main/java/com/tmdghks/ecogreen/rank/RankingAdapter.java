// RankingAdapter.java
package com.tmdghks.ecogreen.rank;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.tmdghks.ecogreen.R;

import java.util.List;

public class RankingAdapter extends RecyclerView.Adapter<RankingAdapter.RankingViewHolder> {

    private List<RankItem> rankingList;
    private Context context;

    public RankingAdapter(List<RankItem> rankingList, Context context) {
        this.rankingList = rankingList;
        this.context = context;
    }

    public void setRankingList(List<RankItem> rankingList) {
        this.rankingList = rankingList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RankingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ranking, parent, false); // 새 아이템 레이아웃 파일 생성 필요
        return new RankingViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RankingViewHolder holder, int position) {
        RankItem currentItem = rankingList.get(position);

        holder.textViewRank.setText(String.valueOf(currentItem.getRank()));
        holder.textViewNickname.setText(currentItem.getNickname());
        holder.textViewPoint.setText(String.format("%d P", currentItem.getTotalPoint()));
        holder.textViewLevel.setText(String.valueOf(currentItem.getLevel()));

        String imagePath = currentItem.getProfileImageUrl();
        if (imagePath != null && !imagePath.isEmpty()) {
            if (imagePath.startsWith("https://firebasestorage.googleapis.com/")) {
                Glide.with(context)
                        .load(imagePath)
                        .placeholder(R.drawable.userdefault) // 기본 이미지 설정
                        .error(R.drawable.userdefault) // 에러 발생 시 이미지 설정
                        .transform(new CircleCrop()) // 원형으로 자르기
                        .into(holder.imageViewProfile);
            } else {
                // Firebase Storage 경로인 경우
                StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(imagePath);
                storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    Glide.with(context)
                            .load(uri)
                            .placeholder(R.drawable.userdefault)
                            .error(R.drawable.userdefault)
                            .transform(new CircleCrop())
                            .into(holder.imageViewProfile);
                }).addOnFailureListener(exception -> {
                    holder.imageViewProfile.setImageResource(R.drawable.userdefault);
                    Log.e("RankingAdapter", "프로필 이미지 로드 실패 (Storage): " + imagePath, exception);
                });
            }
        } else {
            holder.imageViewProfile.setImageResource(R.drawable.userdefault); // 이미지 경로가 없으면 기본 이미지
        }
    }

    @Override
    public int getItemCount() {
        return rankingList.size();
    }

    public static class RankingViewHolder extends RecyclerView.ViewHolder {
        TextView textViewRank;
        ImageView imageViewProfile;
        TextView textViewNickname;
        TextView textViewPoint;
        TextView textViewLevel;

        public RankingViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewRank = itemView.findViewById(R.id.textViewRank);
            imageViewProfile = itemView.findViewById(R.id.imageViewProfile);
            textViewNickname = itemView.findViewById(R.id.textViewNickname);
            textViewPoint = itemView.findViewById(R.id.textViewPoint);
            textViewLevel = itemView.findViewById(R.id.textViewLevel);

        }
    }
}