package com.tmdghks.ecogreen.friend;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tmdghks.ecogreen.R;

import java.util.List;

public class FriendRequestAdapter extends RecyclerView.Adapter<FriendRequestViewHolder> {

    private List<FriendRequestItem> friendRequestList;
    private FriendRequestFragment.OnFriendRequestListener listener; // Fragment에서 구현한 리스너

    public FriendRequestAdapter(List<FriendRequestItem> friendRequestList, FriendRequestFragment.OnFriendRequestListener listener) {
        this.friendRequestList = friendRequestList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FriendRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend_request, parent, false);
        // ViewHolder 생성 시 리스너 전달
        return new FriendRequestViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendRequestViewHolder holder, int position) {
        FriendRequestItem currentItem = friendRequestList.get(position);
        holder.bind(currentItem); // ViewHolder의 bind 메서드를 사용하여 데이터와 아이템 자체를 전달
    }

    @Override
    public int getItemCount() {
        return friendRequestList.size();
    }
}