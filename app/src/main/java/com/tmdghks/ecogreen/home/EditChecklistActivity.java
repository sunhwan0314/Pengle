package com.tmdghks.ecogreen.home;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.tmdghks.ecogreen.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class EditChecklistActivity extends AppCompatActivity {

    ArrayList<String> checklistItems;
    ArrayAdapter<String> adapter;
    ListView listView;
    EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_checklist);

        checklistItems = loadChecklistItems();
        adapter = new ArrayAdapter<>(this, R.layout.list_item_multiline, R.id.textItem, checklistItems);

        listView = findViewById(R.id.listViewChecklist);
        editText = findViewById(R.id.editTextChecklistItem);
        listView.setAdapter(adapter);



        // ➕ 직접 입력 추가 버튼
        findViewById(R.id.btnAddChecklistItem).setOnClickListener(v -> {
            String item = editText.getText().toString().trim();
            if (!item.isEmpty()) {
                checklistItems.add(item);
                adapter.notifyDataSetChanged();
                editText.setText("");
                saveChecklistItems();
            } else {
                Toast.makeText(this, "항목을 입력하세요", Toast.LENGTH_SHORT).show();
            }
        });


        Button btnRecom1 = findViewById(R.id.btnRecom1);
        Button btnRecom2 = findViewById(R.id.btnRecom2);
        Button btnRecom3 = findViewById(R.id.btnRecom3);

        View.OnClickListener recomClickListener = v -> {
            String item = ((Button) v).getText().toString();
            if (!checklistItems.contains(item)) {
                checklistItems.add(item);
                adapter.notifyDataSetChanged(); // 리스트 업데이트
                saveChecklistItems();
                listView.setAdapter(adapter);   // ✅ 리스트뷰에 다시 적용
            } else {
                Toast.makeText(this, "이미 추가된 항목입니다", Toast.LENGTH_SHORT).show();
            }
        };

        btnRecom1.setOnClickListener(recomClickListener);
        btnRecom2.setOnClickListener(recomClickListener);
        btnRecom3.setOnClickListener(recomClickListener);

        // ✂️ 삭제
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            checklistItems.remove(position);
            adapter.notifyDataSetChanged();
            saveChecklistItems();
            return true;
        });

        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            String selectedItem = checklistItems.get(position);

            new AlertDialog.Builder(EditChecklistActivity.this)
                    .setTitle("항목 선택")
                    .setItems(new CharSequence[]{"수정", "삭제"}, (dialog, which) -> {
                        if (which == 0) {
                            // 수정: 선택 항목을 EditText에 넣고 리스트에서 제거
                            editText.setText(selectedItem);
                            checklistItems.remove(position);
                            adapter.notifyDataSetChanged();
                            saveChecklistItems();
                        } else if (which == 1) {
                            // 삭제
                            checklistItems.remove(position);
                            adapter.notifyDataSetChanged();
                            saveChecklistItems();
                        }
                    })
                    .show();

            return true;
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        saveChecklistItems();
    }

    private void saveChecklistItems() {
        SharedPreferences prefs = getSharedPreferences("ChecklistPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        JSONArray jsonArray = new JSONArray(checklistItems);
        editor.putString("checklist_json", jsonArray.toString());
        editor.apply();
    }

    private ArrayList<String> loadChecklistItems() {
        SharedPreferences prefs = getSharedPreferences("ChecklistPrefs", MODE_PRIVATE);
        String json = prefs.getString("checklist_json", null);
        ArrayList<String> list = new ArrayList<>();
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
}