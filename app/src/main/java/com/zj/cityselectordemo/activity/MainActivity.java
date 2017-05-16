package com.zj.cityselectordemo.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.zj.cityselectordemo.R;

public class MainActivity extends AppCompatActivity {

    private Button btSelect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        setListener();
    }

    private void initView(){
        btSelect = (Button)findViewById(R.id.bt_select);
    }

    private void setListener(){
        btSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,CityListActivity.class);
                startActivity(intent);
            }
        });
    }
}
