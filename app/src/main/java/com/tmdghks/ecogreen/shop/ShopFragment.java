package com.tmdghks.ecogreen.shop;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tmdghks.ecogreen.R;

public class ShopFragment extends Fragment {

    private RecyclerView rewardRecyclerView;
    private RewardAdapter rewardAdapter;
    private ArrayList<RewardItem> rewardItemList;
    private TextView pointBox;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_shop, container, false);

        // RecyclerView 설정
        rewardRecyclerView = inflate.findViewById(R.id.reward_recycler_view);
        rewardRecyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        pointBox = inflate.findViewById(R.id.pointBox);

        // 보상 아이템 초기화
        rewardItemList = new ArrayList<>();
        rewardItemList.add(new RewardItem("배경 화면", "10P", R.drawable.background_image, "기존 배경이 질리셨나요? 자신만의 배경화면을 만들어 보세요!"));
        rewardItemList.add(new RewardItem("배경 음악", "10,000P", R.drawable.music_icon, "남들과 다른 개성있는 음악을 원하시나요? 그렇다면 구매하세요!"));
        rewardItemList.add(new RewardItem("이름 변경권", "15,000P", R.drawable.name_change, "무심코 지은 이름 후회 되시나요? 괜찮습니다 포인트만 지불한다면요!"));
        rewardItemList.add(new RewardItem("이름색 변경권", "10,000P", R.drawable.name_change, "식상한 이름 멋지게 꾸며 보세요!"));
        rewardItemList.add(new RewardItem("꾸미기 랜덤 박스", "9,999P", R.drawable.random_box, "나무를 꾸며 주세요 확률은 비밀 입니다!"));

        // 어댑터 설정
        rewardAdapter = new RewardAdapter(rewardItemList, getContext(), getChildFragmentManager());

        // 🔄 포인트 변경 콜백 설정
        rewardAdapter.setOnPointUpdateListener(new RewardAdapter.OnPointUpdateListener() {
            @Override
            public void onPointUpdated(int newPoint) {
                DecimalFormat decimalFormat = new DecimalFormat("#,###");
                pointBox.setText(decimalFormat.format(newPoint) + "P");
            }
        });

        rewardRecyclerView.setAdapter(rewardAdapter);

        // 현재 포인트 표시
        SharedPreferences prefs = requireContext().getSharedPreferences("ChecklistPrefs", Context.MODE_PRIVATE);
        int point = prefs.getInt("totalPoint", 0);
        DecimalFormat decimalFormat = new DecimalFormat("#,###");
        String formattedPoint = decimalFormat.format(point);
        pointBox.setText(formattedPoint + "P");

        return inflate;
    }
}
