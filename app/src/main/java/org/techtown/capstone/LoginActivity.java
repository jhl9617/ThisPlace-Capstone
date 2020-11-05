package org.techtown.capstone;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Notification;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.google.firebase.iid.FirebaseInstanceId;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class LoginActivity extends AppCompatActivity{
    //어플 맨 처음 실행 시 실행되는 엑티비티입니다.

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        showPermissionDialog();//어플 실행시 gps사용 권한 확인 함수입니다.
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        setContentView(R.layout.activity_login);
        GpsTracker gpsTracker = new GpsTracker(LoginActivity.this);
        final double longtitude =gpsTracker.getLongitude();
        final double latitude = gpsTracker.getLatitude();
        //사용자의 gps정보를 얻어와서 위도와 경도를 변수에 저장합니다.

        final EditText new_id=(EditText)findViewById(R.id.new_id);
        final EditText new_pw=(EditText)findViewById(R.id.new_pw);
        final Button loginbtn = (Button)findViewById(R.id.button3);
        final TextView SignUpbtn=(TextView)findViewById(R.id.button4);

        SignUpbtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent SignUpIntent = new Intent(LoginActivity.this,SignUpActivity.class);
                LoginActivity.this.startActivity(SignUpIntent);
                //회원가입이 되어있지 않다면 회원가입 버튼을 눌러 회원가입 엑티비티로 넘어갑니다.
            }
        });

        loginbtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                final String u_id = new_id.getText().toString();
                final String u_pw = new_pw.getText().toString();
                final String longtitude1 = Double.toString(longtitude);
                final String latitude1 = Double.toString(latitude);
                final String Token = FirebaseInstanceId.getInstance().getToken();
                final String address=getCurrentAddress(latitude,longtitude);
                //회원가입이 되어있다면 아이디와 비밀번호를 입력하면 로그인버튼 클릭시 아이디와 비밀번호,위도,경도, 푸쉬알람을 보내기위한 토큰, 그리고 위도,경도에 해당되는 주소를 변수에 저장합니다.
                Response.Listener<String> responseListener=new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try{
                            JSONObject jsonResponse = new JSONObject(response);
                            boolean success = jsonResponse.getBoolean("success");
                            Toast.makeText(getApplicationContext(),"success"+success,Toast.LENGTH_SHORT).show();

                            if(success){
                                String u_id = jsonResponse.getString("u_id");
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                intent.putExtra("u_id",u_id);
                                LoginActivity.this.startActivity(intent);
                                //로그인 성공시 사용자의 id를 MainActivity로 전달합니다.
                                //intent.putExtra("u_pw",u_pw);
                            }else{
                                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                                builder.setMessage("Login failed")

                                        .setNegativeButton("retry",null)
                                        .create()
                                        .show();
                                //로그인 실패시 뜨는 알람입니다.
                            }
                        }catch(JSONException e){
                            e.printStackTrace();
                        }
                    }
                };
                LoginRequest loginRequest = new LoginRequest(u_id,u_pw,longtitude1,latitude1,Token,address,responseListener);
                RequestQueue queue = Volley.newRequestQueue(LoginActivity.this);
                queue.add(loginRequest);
                //변수에 저장된값들을 서버에 전달하기위해 volley라이브러리와 LoginRequest class를 이용하여 서버에 전달할 것입니다.
            }
        });
    }
    //지오코더 GPS를 주소로 변환하는 함수입니다.
    public String getCurrentAddress(double latitude,double longtitude){
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(
                    latitude,
                    longtitude,
                    100);
        }catch(IOException ioException){
            Toast.makeText(this, "지오코더 서비스 사용불가",Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        }catch (IllegalArgumentException illegalArgumentException){
            Toast.makeText(this,"잘못된 GPS좌표",Toast.LENGTH_LONG).show();
            return "주소 미발견";
        }
        Address address = addresses.get(0);
        return address.getAddressLine(0).toString()+"\n";
    }

    //어플 실행시 gps권한을 확인하는 함수입니다.
    private void showPermissionDialog(){
        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                //Toast.makeText(LoginActivity.this,"Permission Granted.",Toast.LENGTH_LONG).show();
            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                //Toast.makeText(LoginActivity.this,"Permission Denied:"+deniedPermissions.get(0),Toast.LENGTH_LONG).show();
            }
        };
        new TedPermission(this)
                .setPermissionListener(permissionlistener)
                .setPermissions(Manifest.permission.ACCESS_COARSE_LOCATION)
                .setPermissions(Manifest.permission.ACCESS_FINE_LOCATION)
                .check();
    }
}