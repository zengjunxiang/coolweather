package com.letaohou.zjx.coolweahter;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.letaohou.zjx.coolweahter.gson.Forecast;
import com.letaohou.zjx.coolweahter.gson.Weather;
import com.letaohou.zjx.coolweahter.service.AutoUpdateService;
import com.letaohou.zjx.coolweahter.util.HttpUtil;
import com.letaohou.zjx.coolweahter.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by ZJX on 2017/4/20.
 */

public class WeaherActivity extends AppCompatActivity {

    private ScrollView weatherLayout;
    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView wetherInfoText;
    private LinearLayout forecastLayout;
    private TextView aqiText;
    private TextView pmText;
    private TextView comfortText;
    private TextView carWashText;
    private TextView sporText;
    private ImageView bingPicImg;
    public SwipeRefreshLayout swipeRefresh;
    public DrawerLayout drawerLayout;
    private Button navBtn;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


         //背景图和状态栏融合,仅对5.0以上（含）系统有效
        if (Build.VERSION.SDK_INT>=21){
            View decorView=getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        setContentView(R.layout.activity_weather);

        weatherLayout= (ScrollView) findViewById(R.id.weather_layout);
        titleCity= (TextView) findViewById(R.id.title_city);
        titleUpdateTime= (TextView) findViewById(R.id.title_update_time);
        degreeText= (TextView) findViewById(R.id.degree_text);
        wetherInfoText= (TextView) findViewById(R.id.weather_info_text);
        forecastLayout= (LinearLayout) findViewById(R.id.forecast_layout);
        aqiText= (TextView) findViewById(R.id.aqi_text);
        pmText= (TextView) findViewById(R.id.pm25_text);
        comfortText= (TextView) findViewById(R.id.comfort_text);
        carWashText= (TextView) findViewById(R.id.car_wash_text);
        sporText= (TextView) findViewById(R.id.sport_text);
        bingPicImg= (ImageView) findViewById(R.id.bing_pic_img);
        swipeRefresh= (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        drawerLayout= (DrawerLayout) findViewById(R.id.drawer_layout);
        navBtn= (Button) findViewById(R.id.nav_button);


        SharedPreferences preferences= PreferenceManager.getDefaultSharedPreferences(this);

        String weatherString=preferences.getString("weather",null);
        final String weatherId;
        if (weatherString!=null){

            Weather weather= Utility.handleWetherResponse(weatherString);
             weatherId=weather.basic.weatherId;
            showWeatherInfo(weather);

        }else {


            weatherLayout.setVisibility(View.INVISIBLE);

            weatherId=getIntent().getStringExtra("weather_id");
            requsetWeather(weatherId);
        }


        String bingPic=preferences.getString("bing_pic",null);
        if (bingPic!=null){
            Glide.with(this).load(bingPic).into(bingPicImg);

        }else {
            loadBingPic();
        }


        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requsetWeather(weatherId);
            }
        });

        navBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("navBtn","侧滑按钮被点击了。。。。");
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

    }




    public void requsetWeather(final String weatherId){

        String weatherUrl="http://guolin.tech/api/weather?cityid="+weatherId+"&key=9e4a30d6f8cc4e3b91ceb768f6e73396";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeaherActivity.this,"获取天气信息失败1",Toast.LENGTH_LONG).show();
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                  final String responseText=response.body().string();
                  final Weather weather=Utility.handleWetherResponse(responseText);

                   runOnUiThread(new Runnable() {
                       @Override
                       public void run() {
                           if (weather!=null&&"ok".equals(weather.status)){

                               SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(WeaherActivity.this).edit();
                               editor.putString("weather",responseText);
                               editor.apply();
                               showWeatherInfo(weather);

                               loadBingPic();

                               swipeRefresh.setRefreshing(false);

                           }else {
                               Toast.makeText(WeaherActivity.this,"获取天气信息失败2",Toast.LENGTH_LONG).show();
                               swipeRefresh.setRefreshing(false);
                           }
                       }
                   });

            }
        });

    }


    public void loadBingPic(){

        String requestBingPic="http://guolin.tech/api/bing_pic";
       HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
           @Override
           public void onFailure(Call call, IOException e) {

           }

           @Override
           public void onResponse(Call call, Response response) throws IOException {

               final String bingPic=response.body().string();

               SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(WeaherActivity.this).edit();
                editor.putString("bing_pic",bingPic);
                editor.apply();
               runOnUiThread(new Runnable() {
                   @Override
                   public void run() {
                       Glide.with(WeaherActivity.this).load(bingPic).into(bingPicImg);
                   }
               });


           }
       });
        }



    public void showWeatherInfo(Weather weather){

        String cityName=weather.basic.cityName;
        String updateTime=weather.basic.update.updateTime.split("")[1];
        String degree=weather.now.tmperature+" C";
        String weatherInfo=weather.now.more.info;

        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        wetherInfoText.setText(weatherInfo);

        forecastLayout.removeAllViews();

        if (weather!=null&&"ok".equals(weather.status)){
            Intent intent=new Intent(this, AutoUpdateService.class);
            startService(intent);
        }else {
            Toast.makeText(WeaherActivity.this,"获取天气信息失败3",Toast.LENGTH_LONG).show();
        }


        for (Forecast forecast:weather.forecastList){
            View view= LayoutInflater.from(this).inflate(R.layout.forecast_item,forecastLayout,false);

            TextView dateText= (TextView) view.findViewById(R.id.date_text);
            TextView infoText= (TextView) view.findViewById(R.id.info_text);
            TextView maxText= (TextView) view.findViewById(R.id.max_text);
            TextView minText= (TextView) view.findViewById(R.id.min_text);

            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);

            forecastLayout.addView(view);
        }

        if (weather.aqi!=null){
            aqiText.setText(weather.aqi.city.aqi);
            pmText.setText(weather.aqi.city.pm25);
        }

        String comfort="舒适度："+weather.suggestion.comfort.info;
        String carWash="洗车指数："+weather.suggestion.carWash.info;
        String soprt="运动建议:"+weather.suggestion.sport.info;
        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sporText.setText(soprt);
        weatherLayout.setVisibility(View.VISIBLE);
    }

}
