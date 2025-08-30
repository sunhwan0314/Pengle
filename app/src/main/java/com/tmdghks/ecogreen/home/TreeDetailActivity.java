package com.tmdghks.ecogreen.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CalendarView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.tmdghks.ecogreen.IntegerValueFormatter;
import com.tmdghks.ecogreen.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class TreeDetailActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private TextView selectedDateText;
    private RecyclerView recyclerViewCheckedList;
    private LineChart lineChart;

    private SharedPreferences prefs;
    private ArrayList<String> itemList = new ArrayList<>();
    private CheckListAdapter adapter;
    private ArrayList<String> currentCheckedItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tree_detail);

        calendarView = findViewById(R.id.calendarView);
        selectedDateText = findViewById(R.id.selectedDateText);
        recyclerViewCheckedList = findViewById(R.id.recyclerViewCheckedList);
        lineChart = findViewById(R.id.lineChart);

        prefs = getSharedPreferences("ChecklistPrefs", Context.MODE_PRIVATE);
        itemList = loadChecklistItemsFromJson();

        adapter = new CheckListAdapter(currentCheckedItems);
        recyclerViewCheckedList.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewCheckedList.setAdapter(adapter);

        // 기본 오늘 날짜로 표시 및 데이터 로딩
        Calendar today = Calendar.getInstance();
        String todayKey = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(today.getTime());
        selectedDateText.setText("선택한 날짜: " + todayKey);
        loadCheckedList(todayKey);
        setupLineChart(today); // 오늘 날짜 기준 최근 7일 그래프 그리기

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar selectedCal = Calendar.getInstance();
            selectedCal.set(year, month, dayOfMonth);
            String dateKey = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedCal.getTime());

            selectedDateText.setText("선택한 날짜: " + dateKey);
            loadCheckedList(dateKey);
            setupLineChart(selectedCal); // 선택 날짜 기준 최근 7일 그래프 갱신
        });
    }

    private ArrayList<String> loadChecklistItemsFromJson() {
        ArrayList<String> list = new ArrayList<>();
        String json = prefs.getString("checklist_json", null);
        if (json != null) {
            try {
                JSONArray jsonArray = new JSONArray(json);
                for (int i = 0; i < jsonArray.length(); i++) {
                    list.add(jsonArray.getString(i));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    private void loadCheckedList(String dateKey) {
        currentCheckedItems.clear();
        for (String item : itemList) {
            boolean isChecked = prefs.getBoolean("check_" + item + "_" + dateKey, false);
            if (isChecked) {
                currentCheckedItems.add(item);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void setupLineChart(Calendar baseDate) {
        ArrayList<Entry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        Calendar calendar = (Calendar) baseDate.clone();

        // baseDate 기준으로 6일 전부터 오늘까지 7일간
        calendar.add(Calendar.DAY_OF_MONTH, -6);

        for (int i = 0; i < 7; i++) {
            String dateKey = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());
            int count = 0;
            for (String item : itemList) {
                if (prefs.getBoolean("check_" + item + "_" + dateKey, false)) {
                    count++;
                }
            }

            entries.add(new Entry(i, count));
            labels.add(new SimpleDateFormat("MM/dd", Locale.getDefault()).format(calendar.getTime()));
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        LineDataSet dataSet = new LineDataSet(entries, "체크 개수");
        dataSet.setCircleRadius(5f);
        dataSet.setLineWidth(2f);
        dataSet.setColor(getResources().getColor(R.color.purple_500, null));
        dataSet.setCircleColor(getResources().getColor(R.color.teal_700, null));
        dataSet.setValueTextSize(12f);
        dataSet.setValueFormatter(new IntegerValueFormatter());

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);

        YAxis leftAxis = lineChart.getAxisLeft();
        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setEnabled(false);

        // 정수 값 표시 포맷터 (필요하면 IntegerValueFormatter 클래스 구현 필요)
        leftAxis.setValueFormatter(new IntegerValueFormatter());  // 정수 포맷터 적용
        leftAxis.setGranularity(1f); // 최소 단위 1
        leftAxis.setAxisMinimum(0f);

        lineChart.getDescription().setEnabled(false);
        lineChart.invalidate();
    }
}
