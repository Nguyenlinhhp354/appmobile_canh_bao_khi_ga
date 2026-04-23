package com.example.gas;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    // Khai báo View
    private TextView tvPhong1Status, tvPhong2Status, tvCurrentLimit;
    private EditText edtPpmLimit;
    private Button btnMute1, btnMute2, btnHistory, btnSavePpm;

    // Firebase
    private DatabaseReference mDatabase;

    // Danh sách lịch sử
    private ArrayList<HistoryModel> historyList = new ArrayList<>();

    // Biến lưu trạng thái Mute hiện tại để xử lý Bật/Tắt
    private boolean isMutedP1 = false;
    private boolean isMutedP2 = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // --- 1. ÁNH XẠ VIEW ---
        tvPhong1Status = findViewById(R.id.tv_phong1_status);
        tvPhong2Status = findViewById(R.id.tv_phong2_status);
        tvCurrentLimit = findViewById(R.id.tv_current_limit);

        edtPpmLimit = findViewById(R.id.edt_ppm_limit);
        btnSavePpm = findViewById(R.id.btn_save_ppm);

        btnMute1 = findViewById(R.id.btn_mute_1);
        btnMute2 = findViewById(R.id.btn_mute_2);
        btnHistory = findViewById(R.id.btn_history);

        // Kết nối Firebase
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // --- 2. LẮNG NGHE TRẠNG THÁI CẢM BIẾN ---
        mDatabase.child("status").child("phong1").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String st = snapshot.getValue(String.class);
                updateUI(tvPhong1Status, "Phòng 1", st);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        mDatabase.child("status").child("phong2").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String st = snapshot.getValue(String.class);
                updateUI(tvPhong2Status, "Phòng 2", st);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // --- 3. LẮNG NGHE CẤU HÌNH (PPM & NÚT MUTE) ---

        // 3.1. Lắng nghe ngưỡng PPM
        mDatabase.child("config").child("limit_ppm").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String currentVal = String.valueOf(snapshot.getValue());
                    tvCurrentLimit.setText("Ngưỡng báo động: " + currentVal + " PPM");
                } else {
                    tvCurrentLimit.setText("Ngưỡng báo động: Chưa cài");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // 3.2. Lắng nghe trạng thái Mute Phòng 1 (Để cập nhật nút)
        mDatabase.child("control").child("mute_p1").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    // Cập nhật biến trạng thái
                    Boolean val = snapshot.getValue(Boolean.class);
                    isMutedP1 = (val != null) ? val : false;

                    // Cập nhật giao diện nút bấm
                    updateMuteButtonUI(btnMute1, isMutedP1, "P1");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // 3.3. Lắng nghe trạng thái Mute Phòng 2
        mDatabase.child("control").child("mute_p2").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    Boolean val = snapshot.getValue(Boolean.class);
                    isMutedP2 = (val != null) ? val : false;
                    updateMuteButtonUI(btnMute2, isMutedP2, "P2");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // --- 4. LẮNG NGHE LỊCH SỬ ---
        mDatabase.child("history").limitToLast(20).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                historyList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    HistoryModel item = data.getValue(HistoryModel.class);
                    if (item != null) historyList.add(item);
                }
                Collections.reverse(historyList);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // --- 5. XỬ LÝ NÚT BẤM (LOGIC ĐẢO TRẠNG THÁI) ---

        // Lưu PPM
        btnSavePpm.setOnClickListener(v -> {
            String input = edtPpmLimit.getText().toString().trim();
            if (!input.isEmpty()) {
                try {
                    int ppm = Integer.parseInt(input);
                    mDatabase.child("config").child("limit_ppm").setValue(ppm);
                    Toast.makeText(MainActivity.this, "Đã lưu ngưỡng: " + ppm, Toast.LENGTH_SHORT).show();
                    edtPpmLimit.setText("");
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Chỉ nhập số!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Nút Mute P1: Gửi giá trị ngược lại với trạng thái hiện tại (!isMutedP1)
        btnMute1.setOnClickListener(v -> {
            mDatabase.child("control").child("mute_p1").setValue(!isMutedP1);
            // Không cần Toast ở đây vì nút sẽ tự đổi màu khi Firebase phản hồi
        });

        // Nút Mute P2: Tương tự
        btnMute2.setOnClickListener(v -> {
            mDatabase.child("control").child("mute_p2").setValue(!isMutedP2);
        });

        // Xem lịch sử
        btnHistory.setOnClickListener(v -> showHistoryDialog());
    }

    // --- CÁC HÀM CẬP NHẬT GIAO DIỆN ---

    // Hàm đổi màu nút Mute dựa trên trạng thái
    private void updateMuteButtonUI(Button btn, boolean isMuted, String roomName) {
        if (isMuted) {
            // Đang bị tắt -> Hiện nút xanh để bật lại
            btn.setText("🔔 BẬT CÒI " + roomName);
            btn.setBackgroundColor(Color.parseColor("#4CAF50")); // Màu xanh lá
        } else {
            // Đang bật -> Hiện nút đỏ để tắt
            btn.setText("🔕 TẮT CÒI " + roomName);
            btn.setBackgroundColor(Color.parseColor("#D32F2F")); // Màu đỏ
        }
    }

    private void updateUI(TextView tv, String roomName, String status) {
        if (status == null) status = "...";
        tv.setText(roomName + ": " + status);

        if (status.contains("DANGER") || status.contains("CHAY")) {
            tv.setTextColor(Color.WHITE);
            tv.setBackgroundColor(Color.RED);
        } else if (status.contains("WARNING")) {
            tv.setTextColor(Color.BLACK);
            tv.setBackgroundColor(Color.YELLOW);
        } else {
            tv.setTextColor(Color.GREEN);
            tv.setBackgroundColor(Color.parseColor("#333333"));
        }
    }

    private void showHistoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("🕒 Lịch sử cảnh báo");
        if (historyList.isEmpty()) {
            builder.setMessage("Chưa có dữ liệu.");
        } else {
            String[] displayArray = new String[historyList.size()];
            for (int i = 0; i < historyList.size(); i++) {
                displayArray[i] = historyList.get(i).toString();
            }
            builder.setItems(displayArray, null);
        }
        builder.setPositiveButton("Đóng", null);
        builder.show();
    }
}