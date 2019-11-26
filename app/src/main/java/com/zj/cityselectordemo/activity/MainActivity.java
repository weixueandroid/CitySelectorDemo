package com.zj.cityselectordemo.activity;

import android.Manifest;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.zj.cityselectordemo.R;

import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    private Button btSelect;
    private static final String[] PERMS_HOME = {Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE};
    private static final int CODE_PERM = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        setListener();
    }

    private void initView(){
        getPermission();
        btSelect = (Button)findViewById(R.id.bt_select);
    }

    private void setListener(){
        btSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,CityListActivity.class);
                startActivityForResult(intent,201);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 201 && resultCode == RESULT_OK){
            String name = data.getStringExtra("CityName");
            Toast.makeText(this,name,Toast.LENGTH_SHORT).show();
        }
    }

    @AfterPermissionGranted(CODE_PERM)
    private void getPermission(){
        if (hasGetPermission()){

        }else {
            EasyPermissions.requestPermissions(
                    this,
                    "需要设备的存储权限",
                    CODE_PERM,
                    PERMS_HOME);
        }
    }

    private boolean hasGetPermission(){
        return EasyPermissions.hasPermissions(this,PERMS_HOME);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this)
                    .setTitle("权限设置")
                    .setRationale("需要设备的存储权限，否则无法正常使用，是否打开设置")
                    .setPositiveButton("是")
                    .setNegativeButton("否").build().show();
        }
    }
}