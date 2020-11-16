package com.example.dtatabaseex;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    EditText edtName, edtNumber;
    TextView tvName, tvNum;
    Button btnInit, btnInsert, btnSelect, btnUpdate, btnDelete;
    MyDBHelper myDBHelper; // 사용하기 위해 선언
    SQLiteDatabase sqlDB; // 4대쿼리 (인스턴스 생성안함)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnInit = findViewById(R.id.btnInit);
        btnInsert = findViewById(R.id.btnInsert);
        btnSelect = findViewById(R.id.btnSelect);
        btnDelete = findViewById(R.id.btnDelete);
        btnUpdate = findViewById(R.id.btnUpdate);
        edtName = findViewById(R.id.edtName);
        edtNumber = findViewById(R.id.edtNumber);
        tvName = findViewById(R.id.tvName);
        tvNum = findViewById(R.id.tvNum);
        myDBHelper = new MyDBHelper(this); //인스턴스 만들자마자 생성자 호출-> db and table created
        btnInit.setOnClickListener(new View.OnClickListener() {//초기화 작업하기(실제로는 잘 안쓰지만 )
            @Override
            public void onClick(View v) {
                //   sqlDB = myDBHelper.getReadableDatabase() // 이미 데이터베이스가 존재할 때
                sqlDB = myDBHelper.getWritableDatabase(); // 읽어오고
                myDBHelper.onUpgrade(sqlDB, 1, 2);//upgrade 메소드 수행
                sqlDB.close();

            }
        });

        btnInsert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nameInput = edtName.getText().toString();
                String numInput = edtNumber.getText().toString();

                //그룹이름,인원 비워둔채 입력하면 토스트 메시지
                if (nameInput.isEmpty() || numInput.isEmpty()) {
                    showToast("자료를 입력하세요.");
                } /*else if (nameInput.equals()) {//이미 등록된 그룹이면 메시지 보이기--> gName에서 찾기..?
                    showToast("이미 등록된 그룹입니다. \n수정하거나 새로운 그룹을 입력하세요");
                }*/ else {
                    sqlDB = myDBHelper.getWritableDatabase();// 값 넣을 거니까..
                    //문자니까 홑따옴표! , 홑따옴표 없는 숫자는 바로 던져주면 숫자로 받음
                    sqlDB.execSQL("insert into grouptbl values ('" + nameInput + "'," + numInput + ");");//insert, delete, update는 이 명령어 이용 select만 rawQuery 명령어 이용

                    sqlDB.close();
                    showToast("자료가 저장되었습니다.");
                    edtName.setText("");
                    edtNumber.setText(""); //입력하고 나면 없어지게
                }
            }
        });
        //그룹이름을 입력하여 인원 업뎃
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nameInput = edtName.getText().toString();
                String numInput = edtNumber.getText().toString();
                sqlDB = myDBHelper.getWritableDatabase();
                sqlDB.execSQL("update grouptbl set gNumber='" + numInput + "' where gName ='" + nameInput + "';");
                sqlDB.close();
                edtNumber.setText("");
            }
        });
        //그룹이름을 입력하여 그룹 삭제
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nameInput = edtName.getText().toString();
                sqlDB = myDBHelper.getWritableDatabase();
                sqlDB.execSQL("delete from grouptbl where gName='" + nameInput + "';");
            }
        });
        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nameInput = edtName.getText().toString();
                sqlDB = myDBHelper.getReadableDatabase();//조회는 읽어오는 거니까
                Cursor cursor;//인터페이스--> db를 원하는 위치에 오게 해준다. 조회하는 위치에 가져다준다.
                if (!nameInput.isEmpty()) { // 일부만 입력하면 해당 레코드만 조회

                } else {//비어있으면 전체조회
                    cursor = sqlDB.rawQuery("select * from grouptbl;", null);//select는 이 메소드
                    String strNames = "그룹이름\n----------\n";
                    String strNumbers = "인원\n-----------\n";

                    //database에서 while많이 사용 -> 데이터베이스가 하나씩 접근 (레코드 개수를 모르니까) ,
                    while (cursor.moveToNext()) { //끝에오면 다음으로 갈 수 없으니 빠져나오게 됨
                        //누적
                        strNames += cursor.getString(0) + "\n"; //가져온거에서 순서 붙음 우리는 모두 가져옴.
                        strNumbers += cursor.getInt(1) + "\n";//real(실수)라면 getDouble
                    }
                    tvName.setText(strNames);
                    tvNum.setText(strNumbers);
                    cursor.close();
                    sqlDB.close();
                }
            }
        });
    }

    void showToast(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    //데이터베이스 클래스 만듦(사용전)
    public class MyDBHelper extends SQLiteOpenHelper {
        //생성자 -> DB 생성
        public MyDBHelper(@Nullable Context context) {
            //디비 생성장소, 디비 이름(확장자 포함 미포함 OK), factory는 null , 데이터 처음버전은 1
            super(context, "groupDB", null, 1);
        }

        //테이블 생성
        @Override
        public void onCreate(SQLiteDatabase db) {//db변수로부터 메소드를 받음
            db.execSQL("create table grouptbl(gName text primary key, gNumber integer);");//따옴표 안에있는 디비를 생성해서 sql한테 던져줌
        }

        //테이블 삭제후 다시 생성 (초기화 버튼에서 할일)
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("drop table if exists grouptbl;");
            onCreate(db);//호출해서 재 생성
        }
    }
}