package com.example.wndud.everylifelogger;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class StatsActivity extends AppCompatActivity {
    ListView mList;
    ArrayList<String> nameList;
    ArrayAdapter<String> baseAdapter;
    Button bt_comeback;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);
        bt_comeback = (Button)findViewById(R.id.bt_comeback);//메인으로 돌아가기위한 버튼변수
        Intent recv = this.getIntent();//인텐트 값을 받음
        mList = (ListView) findViewById(R.id.list1);
        nameList = new ArrayList<String>();
        nameList=recv.getStringArrayListExtra("List");//인텐트로 ArrayList를 받음
        baseAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, nameList);

        mList.setAdapter(baseAdapter);
        baseAdapter.notifyDataSetChanged();
        //돌아가기위한 리스너
        bt_comeback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
