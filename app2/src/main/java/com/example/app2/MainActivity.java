package com.example.app2;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    Button btnInit, btnInput, btnEdit, btnSelect, btnDelete;
    TextView nameAppear, numAppear;
    EditText edtName, edtNum;
    Helper helper;
    SQLiteDatabase sqLiteDatabase;
    String nameInput, numInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnInit = findViewById(R.id.btnInit);
        btnInput = findViewById(R.id.btnInput);
        btnSelect = findViewById(R.id.btnSelect);
        btnDelete = findViewById(R.id.btnDelete);
        btnEdit = findViewById(R.id.btnEdit);
        edtName = findViewById(R.id.edtName);
        edtNum = findViewById(R.id.edtNumber);
        nameAppear = findViewById(R.id.nameAppear);
        numAppear = findViewById(R.id.numAppear);

        helper = new Helper(this);
        btnInit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sqLiteDatabase = helper.getReadableDatabase();
                helper.onUpgrade(sqLiteDatabase, 1, 2);
                sqLiteDatabase.close();
                showToast("deleted all!");
            }
        });

        btnInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sqLiteDatabase = helper.getWritableDatabase();
                nameInput = edtName.getText().toString();
                numInput = edtNum.getText().toString();
                try {
                    sqLiteDatabase.execSQL("INSERT INTO groupTBL VALUES('" + nameInput + "','" + numInput + "');");
                    showToast("saved!");
                    btnSelect.callOnClick();
                } catch (SQLiteConstraintException e) {
                    if (nameInput.isEmpty() || numInput.isEmpty()) {
                        showToast("empty!");
                    } else {
                        showToast("same record exists!");
                    }
                }
                sqLiteDatabase.close();
                edtName.setText("");
                edtNum.setText("");
            }
        });

        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nameInput = edtName.getText().toString();
                numInput = edtNum.getText().toString();
                if (nameInput.isEmpty() || numInput.isEmpty()) {
                    showToast("empty!");
                } else {
                    sqLiteDatabase = helper.getReadableDatabase();
                    sqLiteDatabase.execSQL("UPDATE groupTBL SET Numbers='" + numInput + "' WHERE Name ='" + nameInput + "';");
                    sqLiteDatabase.close();
                    showToast("updated!");
                    btnSelect.callOnClick();
                    edtName.setText("");
                    edtNum.setText("");
                }
            }
        });
        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nameInput = edtName.getText().toString();
                sqLiteDatabase = helper.getReadableDatabase();
                Cursor cursor;
                cursor = sqLiteDatabase.rawQuery("SELECT * from groupTBL where Name like '" + nameInput + "%';", null);
                String strNames = "그룹이름\n----------\n";
                String strNumbers = "인원\n-----------\n";
                while (cursor.moveToNext()) {
                    strNames += cursor.getString(0) + "\n";
                    strNumbers += cursor.getString(1) + "\n";
                    nameAppear.setText(strNames);
                    numAppear.setText(strNumbers);
                    showToast("void");
                }
                cursor.close();
                sqLiteDatabase.close();
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nameInput = edtName.getText().toString();
                numInput = edtNum.getText().toString();
                sqLiteDatabase = helper.getWritableDatabase();
                if(numInput.isEmpty()|| nameInput.isEmpty()){
                    showToast("name is empty!");
                }else {
                    sqLiteDatabase.execSQL("DELETE from groupTBL WHERE Name ='"+nameInput+"'");
                    sqLiteDatabase.close();
                }
            }
        });
    }

    void showToast(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    public class Helper extends SQLiteOpenHelper {

        public Helper(@Nullable Context context) {
            super(context, "groupDB", null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE groupTBL(Name TEXT PRIMARY KEY, Numbers integer);");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS groupTBL");
            onCreate(db);
        }
    }
}
