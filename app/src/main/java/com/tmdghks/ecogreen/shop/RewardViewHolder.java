package com.tmdghks.ecogreen.shop;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.tmdghks.ecogreen.R;

public class RewardViewHolder extends RecyclerView.ViewHolder {
    TextView rewardNameTextView;
    TextView rewardPointsTextView;
    ImageView addButton;

    public RewardViewHolder(View itemView) {
        super(itemView);
        rewardNameTextView = itemView.findViewById(R.id.reward_name);
        rewardPointsTextView = itemView.findViewById(R.id.point);
        addButton = itemView.findViewById(R.id.add_button);
    }
}
