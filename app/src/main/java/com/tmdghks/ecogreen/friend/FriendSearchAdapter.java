package com.tmdghks.ecogreen.friend;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tmdghks.ecogreen.R;

import java.util.List;

public class FriendSearchAdapter extends RecyclerView.Adapter<FriendViewHolder> {

    private List<FriendItem> friendList;
    private FriendViewHolder.OnFriendAddClickListener onFriendAddClickListener;

    public FriendSearchAdapter(List<FriendItem> friendList) {
        this.friendList = friendList;
    }

    public void setFriendList(List<FriendItem> friendList) {
        this.friendList = friendList;
        notifyDataSetChanged();
    }

    public void setOnFriendAddClickListener(FriendViewHolder.OnFriendAddClickListener listener) {
        this.onFriendAddClickListener = listener;
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_friend_search_result, parent, false);
        return new FriendViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        FriendItem friend = friendList.get(position);
        holder.bind(friend);
        holder.setOnFriendAddClickListener(onFriendAddClickListener);
    }

    @Override
    public int getItemCount() {
        return friendList == null ? 0 : friendList.size();
    }
}
