package com.tmdghks.ecogreen.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tmdghks.ecogreen.R;

import java.util.ArrayList;

public class CheckListAdapter extends RecyclerView.Adapter<CheckListAdapter.ViewHolder> {

    private final ArrayList<String> checkedItems;

    public CheckListAdapter(ArrayList<String> checkedItems) {
        this.checkedItems = checkedItems;
    }

    @NonNull
    @Override
    public CheckListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_checked_list, parent, false); // 여기 변경됨
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CheckListAdapter.ViewHolder holder, int position) {
        holder.textView.setText("- " + checkedItems.get(position)); // 보기 좋게 '-' 추가
    }

    @Override
    public int getItemCount() {
        return checkedItems.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        ViewHolder(View view) {
            super(view);
            textView = view.findViewById(R.id.checkedItemText); // 여기 변경됨
        }
    }
}
