package com.tmdghks.ecogreen.shop;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.tmdghks.ecogreen.R;

public class RewardDialogFragment extends DialogFragment {

    private String itemName;
    private String itemPoints;
    private String itemDescription;

    public interface OnPurchaseListener {
        void onPurchase(int cost);
    }

    private OnPurchaseListener listener;

    public void setOnPurchaseListener(OnPurchaseListener listener) {
        this.listener = listener;
    }
    public static RewardDialogFragment newInstance(String itemName, String itemPoints, String itemDescription) {
        RewardDialogFragment fragment = new RewardDialogFragment();
        Bundle args = new Bundle();
        args.putString("itemName", itemName);
        args.putString("itemPoints", itemPoints);
        args.putString("itemDescription", itemDescription);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            itemName = getArguments().getString("itemName");
            itemPoints = getArguments().getString("itemPoints");
            itemDescription = getArguments().getString("itemDescription");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_reward, null);
        builder.setView(view);

        TextView rewardDescriptionText = view.findViewById(R.id.reward_description_text);
        TextView rewardPointsTextView = view.findViewById(R.id.reward_points);
        TextView rewardItemDescription = view.findViewById(R.id.reward_item_description);
        Button btnCancel = view.findViewById(R.id.btn_cancel);
        Button btnAdd = view.findViewById(R.id.btn_add);

        rewardDescriptionText.setText(itemName);
        rewardPointsTextView.setText(itemPoints);
        rewardItemDescription.setText(itemDescription);

        btnCancel.setOnClickListener(v -> dismiss());

        btnAdd.setOnClickListener(v -> {
            if (listener != null) {
                int cost = Integer.parseInt(itemPoints.replace(",", "").replace("P", ""));
                listener.onPurchase(cost);
            }

            //Toast.makeText(getContext(), itemName + " 구매 완료!", Toast.LENGTH_SHORT).show();
            dismiss();
        });
        return builder.create();
    }
}