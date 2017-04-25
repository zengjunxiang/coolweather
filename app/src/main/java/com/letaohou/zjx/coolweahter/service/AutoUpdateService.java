package com.letaohou.zjx.coolweahter.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

import com.letaohou.zjx.coolweahter.gson.Weather;
import com.letaohou.zjx.coolweahter.util.HttpUtil;
import com.letaohou.zjx.coolweahter.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AutoUpdateService extends Service {
    public AutoUpdateService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw null;
}


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.e("Service","onStartCommand......");
        updateBingPic();
        updateWeather();

        AlarmManager manager= (AlarmManager) getSystemService(ALARM_SERVICE);

        int anHour=8*60*60*1000;
        long triggerAtTime= SystemClock.elapsedRealtime()+anHour;
        Intent i=new Intent(this,AutoUpdateService.class);
        PendingIntent pi=PendingIntent.getService(this,0,i,0);
                     manager.cancel(pi);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pi);

        return super.onStartCommand(intent, flags, startId);
    }

    private  void updateWeather(){
        SharedPreferences sharedPreferences= PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString=sharedPreferences.getString("weahter",null);

        if (weatherString!=null){
            Weather weather= Utility.handleWetherResponse(weatherString);
            String weatherId=weather.basic.weatherId;

            String weatherURL="http://guolin.tech/api/weather?cityid="+weatherId+"&key=9e4a30d6f8cc4e3b91ceb768f6e73396";
            HttpUtil.sendOkHttpRequest(weatherURL, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                    final String responseText=response.body().string();
                    final Weather weather=Utility.handleWetherResponse(responseText);

                    if (weather!=null &&"ok".equals(weather.status)){
                        SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                        editor.putString("weahter",responseText);
                        editor.apply();
                    }

                }
            });
        }

    }

    private void updateBingPic(){
        String requestBingPic="http://guolin.tech.api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String bingPic=response.body().string();
                SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                editor.putString("bing_pic",bingPic);
                editor.apply();

            }
        });
    }


}
