package com.example.wndud.everylifelogger;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Parcelable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    SQLiteDatabase db;
    String dbName = "idList.db"; // name of Database;
    String tableName = "idListTable"; // name of Table;
    int dbMode = Context.MODE_PRIVATE;

    // layout object
    EditText mEtName;
    Button mBtInsert;
    Button mBtDelete;
    Button mBtEdit;
    Button mBtStats;


    Intent send;
    ArrayAdapter<String> baseAdapter;
    ArrayList<String> nameList;
    Spinner spinner;//spinner을 사용하기 위한 변수
    ArrayAdapter<CharSequence> adapter;
    TextView logView;//디버깅을 위해 log를 사용하여 쓰기위한 변수
    LatLng place;//위도와 경도를 받은 변수
    private GoogleMap map;
    LocationManager locationManager;
    LocationListener locationListener;
    boolean isGPSEnabled;
    boolean isNetworkEnabled;
    String eventSort;
    Marker deleteHere;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // // Database 생성 및 열기
        db = openOrCreateDatabase(dbName, dbMode, null);
        // 테이블 생성
        createTable();
        //layout에 연결
        mEtName = (EditText) findViewById(R.id.et_event);
        mBtInsert = (Button) findViewById(R.id.bt_insert);
        mBtDelete = (Button) findViewById(R.id.bt_delete);
        mBtEdit = (Button) findViewById(R.id.bt_edit);
        mBtStats = (Button) findViewById(R.id.bt_stats);

        send = new Intent(this, StatsActivity.class);//인텐트 사용
        spinner = (Spinner) findViewById(R.id.spinner);//스피너 사용
        adapter = ArrayAdapter.createFromResource(this, R.array.event_sorts, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        logView = (TextView) findViewById(R.id.log);
        logView.setText("GPS 가 잡혀야 좌표가 구해짐");
        // Acquire a reference to the system Location Manager
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // GPS 프로바이더 사용가능여부
        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // 네트워크 프로바이더 사용가능여부
        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        Log.d("Main", "isGPSEnabled=" + isGPSEnabled);
        Log.d("Main", "isNetworkEnabled=" + isNetworkEnabled);
        //스피너를 사용하기 위한 리스너
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                eventSort = String.valueOf(parent.getItemAtPosition(position));
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        //insert
        mBtInsert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mEtName.getText().toString().equals("")) {
                    Toast.makeText(MainActivity.this.getApplicationContext(), "아무것도 입력하지 않았습니다.", Toast.LENGTH_LONG).show();
                } else {
                    locationListener = new LocationListener() {
                        public void onLocationChanged(Location location) {
                            double lat = location.getLatitude();
                            double lng = location.getLongitude();
                            insertData(mEtName.getText().toString(), lat, lng);
                            place = new LatLng(lat, lng);
                            Marker here = map.addMarker(new MarkerOptions().position(place).title(eventSort).snippet(mEtName.getText().toString()));
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(place, 15));
                            logView.setText("위치가 확인되었습니다.");
                            Toast.makeText(MainActivity.this.getApplicationContext(), "위치 들어감", Toast.LENGTH_LONG).show();
                            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                return;
                            }
                            locationManager.removeUpdates(locationListener);
                        }

                        public void onStatusChanged(String provider, int status, Bundle extras) {
                            logView.setText("GPS를 켜주세요");
                        }

                        public void onProviderEnabled(String provider) {
                            logView.setText("GPS를 켜주세요");
                        }

                        public void onProviderDisabled(String provider) {
                            logView.setText("GPS를 켜주세요");
                        }
                    };
                    //퍼미션 확인을 위하여 사용
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                }
            }
        });
        //마크확인
        mBtEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewMark();
            }
        });
        //Delete
        mBtDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEtName.getText().toString().equals("")) {
                    Toast.makeText(MainActivity.this.getApplicationContext(), "아무것도 입력하지 않았습니다.", Toast.LENGTH_LONG).show();
                }
                else {
                    removeData(mEtName.getText().toString(), eventSort);
                    viewMark();
                }
            }
        });
        //통계를 보여줌
        mBtStats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nameList.clear();
                selectAll();
            }
        });
        // Create listview
        nameList = new ArrayList<String>();
        baseAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, nameList);
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
    }
    // Table 생성
    public void createTable() {
        try {
            String sql = "create table " + tableName + "(id integer primary key autoincrement, " + "eventSort text not null, " + "name text not null, " + "lat double not null, " + "lng double not null)";
            db.execSQL(sql);
        } catch (android.database.sqlite.SQLiteException e) {
            Log.d("Lab sqlite", "error: " + e);
        }
    }
    // Table 삭제
    public void removeTable() {
        String sql = "drop table " + tableName;
        db.execSQL(sql);
    }
    // Data 추가
    public void insertData(String name, double lat, double lng) {
        String sql = "insert into " + tableName + " values(NULL, '" + eventSort + "' , '" + name + "' , '" + lat + "' , '" + lng + "');";
        Log.d("lab_sqlite", " sort= " + eventSort + " name=" + name + " lat=" + lat + " lng" + lng);
        db.execSQL(sql);
    }
    // Data 업데이트
    public void updateData(int index, String name) {
        String sql = "update " + tableName + " set name = '" + name + "' where id = " + index + ";";
        db.execSQL(sql);
    }
    //Mark확인
    public void viewMark() {
        String search_sql = "select * from " + tableName + ";";
        Cursor results = db.rawQuery(search_sql, null);
        results.moveToFirst();
        //전체 DB에 저장된 내용을 출력해준다.
        while (!results.isAfterLast()) {
            int id = results.getInt(0);
            String sort = results.getString(1);
            String name = results.getString(2);
            double lat = results.getDouble(3);
            double lng = results.getDouble(4);
            LatLng place = new LatLng(lat, lng);
            Marker here = map.addMarker(new MarkerOptions().position(place).title(sort).snippet(name));
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(place, 15));//지정된 위도와 경도로 확대해서 보여준다.
            results.moveToNext();//DB Row 이동
        }
        results.close();
    }
    // Data 삭제
    public void removeData(String compare_name,String compare_sort) {
        String search_sql = "select * from " + tableName + ";";
        int result_id = 0;
        Cursor results = db.rawQuery(search_sql, null);
        results.moveToFirst();
        Log.d("lab_sqlite", " name=" + compare_name);
        while (!results.isAfterLast()) {
            int id = results.getInt(0);
            String sort = results.getString(1);
            String name = results.getString(2);
            double lat = results.getDouble(3);
            double lng = results.getDouble(4);
            LatLng deletePlace = new LatLng(lat, lng);
            if (compare_name.equals(name) && compare_sort.equals(sort)) {
                result_id = id;
                Log.d("lab_sqlite", " sort= " + sort + " name=" + name + " lat=" + lat + " lng" + lng);
                deleteHere = map.addMarker(new MarkerOptions().position(deletePlace).title(sort).snippet(name));
            }
            results.moveToNext();
        }
        results.close();
        deleteHere.remove();
        map.clear();//삭제후 맵을 클리어
        String sql = "delete from " + tableName + " where id = " + result_id + ";";
        db.execSQL(sql);
    }
    // 모든 Data 읽기
    public void selectAll() {
        String sql = "select * from " + tableName + ";";
        Cursor results = db.rawQuery(sql, null);
        results.moveToFirst();
        while (!results.isAfterLast()) {
            int id = results.getInt(0);
            String sort = results.getString(1);
            String name = results.getString(2);
            double lat = results.getDouble(3);
            double lng = results.getDouble(4);
            Log.d("lab_sqlite", "index= " + id + " sort= " + eventSort + " name=" + name + " lat=" + lat + " lng" + lng);
            nameList.add("종류:" + sort + ", 사건:" + name);
            results.moveToNext();
        }
        send.putExtra("List", nameList);//List에 nameList의 값을 넣어서 인텐트로 넌겨줌
        startActivity(send);//통계함수에서 읽고 바로 인텐트로 지정된 화면으로 넘어감
        results.close();//DB를 열고 확인한후에 닫아줌
    }
}

