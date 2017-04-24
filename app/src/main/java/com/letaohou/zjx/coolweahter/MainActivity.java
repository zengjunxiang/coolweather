package com.letaohou.zjx.coolweahter;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.letaohou.zjx.coolweahter.db.Province;
import com.letaohou.zjx.coolweahter.util.HttpUtil;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPreferences= PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPreferences.getString("weather",null)!=null){
            Intent intent=new Intent(this,WeaherActivity.class);
            startActivity(intent);
            finish();
        }


    }






    }

