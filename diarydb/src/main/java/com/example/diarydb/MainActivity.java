package com.example.diarydb;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    DatePicker dPicker1;
    EditText edtDiary;
    Button btnSave;
    Helper helper;
    SQLiteDatabase liteDatabase;
    String date;
    int cYear, cMonth, cDay;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Calendar cal = Calendar.getInstance();
        cYear = cal.get(Calendar.YEAR);
        cMonth = cal.get(Calendar.MONTH);
        cDay = cal.get(Calendar.DAY_OF_MONTH);
        dPicker1 = findViewById(R.id.dPicker1);
        edtDiary = findViewById(R.id.edtDiary);
        btnSave = findViewById(R.id.btnSave);
        helper = new Helper(this);
        date = cYear + "_" + (cMonth + 1) + "_" + cDay;
        edtDiary.setText(readDiary(date));
                    //처음엔 오늘 날짜로 초기화
        dPicker1.init(cYear, cMonth, cDay, new DatePicker.OnDateChangedListener() { // 날짜 바꿀 때
            @Override
            public void onDateChanged(DatePicker datePicker, int i, int i1, int i2) {
                date =i + "_" + (i1 + 1) + "_" + i2;
                edtDiary.setText(readDiary(date));
 }
        });
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                liteDatabase = helper.getWritableDatabase();
                if(btnSave.getText().toString().equals("새로 저장하기")){
                    liteDatabase.execSQL("INSERT INTO DIARYTBL VALUES('"+date+"','"+edtDiary.getText().toString()+"');");
                    showToast("일기가 저장되었습니다.");
                }else {
                    liteDatabase.execSQL("UPDATE DIARYTBL SET CONTENTS ='"+edtDiary.getText().toString()+"' WHERE DATE ='"+date+"';");
                    showToast("일기가 수정되었습니다.");

                }
                btnSave.setText("수정하기");
                liteDatabase.close();
            }
        });
    }
    //Toast method
    void showToast(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }
    String readDiary(String date) {
        String diaryStr = null; //일기 내용 담을 변수
        liteDatabase = helper.getReadableDatabase();
        Cursor cursor;
        cursor = liteDatabase.rawQuery("SELECT * FROM DIARYTBL WHERE DATE = '"+date+"';",null);
        if(cursor.moveToFirst()){ /* 자료가 있을 때만 true */
            diaryStr = cursor.getString(1);
            btnSave.setText("수정하기");
        }else {
            edtDiary.setHint("일기 없음");
            btnSave.setText("새로 저장하기");
        }

        /*   if(cursor ==null){
            //일기 처음 실행했을 때 (아무것도 없을 때)
            edtDiary.setHint("일기 없음");
            btnSave.setText("새로 저장하기");
        }else if(cursor.moveToFirst()){ *//* 자료가 있을 때만 true *//*
            diaryStr = cursor.getString(1);
            btnSave.setText("수정하기");
        }else {
            edtDiary.setHint("일기 없음");
            btnSave.setText("새로 저장하기");
        }*/
        liteDatabase.close();
        cursor.close();

        return diaryStr;
    }

    public class Helper extends SQLiteOpenHelper {

        public Helper(@Nullable Context context) {
            super(context, "diaryDB", null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE DIARYTBL(DATE TEXT PRIMARY KEY, CONTENTS TEXT);");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS DIARYTBL ");
            onCreate(db);
        }
    }
}
