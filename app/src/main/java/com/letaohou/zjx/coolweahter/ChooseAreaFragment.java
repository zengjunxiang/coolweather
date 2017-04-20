package com.letaohou.zjx.coolweahter;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.letaohou.zjx.coolweahter.db.City;
import com.letaohou.zjx.coolweahter.db.County;
import com.letaohou.zjx.coolweahter.db.Province;
import com.letaohou.zjx.coolweahter.util.HttpUtil;
import com.letaohou.zjx.coolweahter.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by ZJX on 2017/4/18.
 */

public class ChooseAreaFragment extends Fragment{

    public static final int LEVEL_PROVINCE=0;
    public static final int LEVEL_CITY=1;
    public static final int LEVEL_COUNT=2;

    private TextView titleTV;
    private Button backBtn;
    private ListView lv;
    private ProgressDialog progressDialog;

    private List<String>  dataList=new ArrayList<>();

    private List<Province> provinceList;

    private List<City>  cityList;

    private List<County>  countyList;

    private Province selectedProvince;

    private City selectedCity;

    private int currentLevel;

    private ArrayAdapter<String> adapter;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view=inflater.inflate(R.layout.choose_area,container,false);

        titleTV= (TextView) view.findViewById(R.id.choose_area_title_tv);
        backBtn= (Button) view.findViewById(R.id.choose_area_back_btn);
        lv= (ListView) view.findViewById(R.id.choose_area_lv);
        adapter=new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_1,dataList);
        lv.setAdapter(adapter);

        return view;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        queryProvices();

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel==LEVEL_PROVINCE){
                    selectedProvince=provinceList.get(position);
                    queryCity();
                }else if (currentLevel==LEVEL_COUNT){
                    selectedCity=cityList.get(position);
                    queryCounty();
                }
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentLevel==LEVEL_CITY){
                    queryProvices();
                }else if (currentLevel==LEVEL_COUNT){
                    queryCity();
                }
            }
        });
    }


    private void showProgressDialog(){

        if (progressDialog==null){
            progressDialog=new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载。。。。");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
        Log.e("showProgressDialog","显示进度条。。。。");
    };


    private void colseProgressDialog(){

        if (progressDialog!=null){
            progressDialog.dismiss();
        }
        Log.e("colseProgressDialog","关闭进度条。。。。");
    }


    private void queryFromServer(String url,final String type){
         showProgressDialog();
          Log.e("queryFromServer",url);
        HttpUtil.sendOkHttpRequest(url, new Callback() {

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                 boolean result=false;
                String responseText=response.body().string();
                Log.e("onResponse",responseText);

                if ("province".equals(type)){
                    result= Utility.handleProvinceResponse(responseText);
                    Log.e("onResponse province ",result+"");

                } else if ("city".equals(type)){
                    result=Utility.handleCityResponse(responseText,selectedProvince.getId());


                }else if ("county".equals(type)){
                    result=Utility.handleCountyResponse(responseText,selectedCity.getId());

                }else {
                    Log.e("queryFromServer","传入查询参数错误");
                }

                if (result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            colseProgressDialog();

                            if ("provice".equals(type)){
                                queryProvices();
                                Log.e("！","查询省份信息");

                            }else if ("city".equals(type)){
                                queryCity();

                            } else if (("county".equals(type))){
                                 queryCounty();
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call call, final IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        colseProgressDialog();
                        Toast.makeText(getActivity(),"加载失败"+e,Toast.LENGTH_LONG).show();
                        Log.e("onFailure","加载失败"+e);
                    }
                });
            }
        });

    }


    private void queryProvices() {

        titleTV.setText("中国");
        backBtn.setVisibility(View.GONE);
        provinceList= DataSupport.findAll(Province.class);
        if (provinceList.size()>0){
            dataList.clear();
            for (Province province:provinceList){
                dataList.add(province.getProvinceName());
            }
            Log.e("queryProvices","城市的个数"+dataList.size());
            adapter.notifyDataSetChanged();
            lv.setSelection(0);
            currentLevel=LEVEL_PROVINCE;

        }else {
            String address="http://guolin.tech/api/china";
            queryFromServer(address,"province");
        }

    }

    private void queryCity(){
        titleTV.setText(selectedProvince.getProvinceName());
        backBtn.setVisibility(View.VISIBLE);
        cityList=DataSupport.where("provinceid=?",String.valueOf(selectedProvince.getId())).find(City.class);

        if (cityList.size()>0){

            dataList.clear();
            for (City city:cityList){
                dataList.add(city.getCityName());
            }

            adapter.notifyDataSetChanged();
            lv.setSelection(0);
            currentLevel=LEVEL_CITY;

        }else {
            int provinceCode=selectedProvince.getProvinceCode();
            String address="http://guolin.tech/api/china/"+provinceCode;
            queryFromServer(address,"city");
        }
    }

    private void queryCounty(){
        titleTV.setText(selectedCity.getCityName());
        backBtn.setVisibility(View.VISIBLE);
        countyList=DataSupport.where("cityid=?",String.valueOf(selectedCity.getId())).find(County.class);

        if (countyList.size()>0){
            dataList.clear();
            for (County county:countyList){

                dataList.add(county.getCountyName());
            }

            adapter.notifyDataSetChanged();
            lv.setSelection(0);
            currentLevel=LEVEL_COUNT;

        }else {
            int provinceCode=selectedProvince.getProvinceCode();
            int cityCode=selectedCity.getCityCode();
            String address="http://guolin.tech/china/"+provinceCode+"/"+cityCode;
            queryFromServer(address,"county");
        }

    }
}
