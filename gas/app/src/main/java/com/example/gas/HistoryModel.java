package com.example.gas;

public class HistoryModel {
    public String room;
    public String time;
    public String level;

    // Bắt buộc phải có Constructor rỗng để Firebase đọc dữ liệu
    public HistoryModel() { }

    public HistoryModel(String room, String time, String level) {
        this.room = room;
        this.time = time;
        this.level = level;
    }

    // Hàm này giúp hiển thị đẹp khi hiện lên danh sách
    @Override
    public String toString() {
        return "[" + time + "] " + room + " - " + level;
    }
}