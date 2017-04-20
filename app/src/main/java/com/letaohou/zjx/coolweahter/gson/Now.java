package com.letaohou.zjx.coolweahter.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ZJX on 2017/4/20.
 */

public class Now {

    @SerializedName("tmp")
    public String tmperature;

    @SerializedName("cond")
    public  More more;


    public class More{

           @SerializedName("txt")
          public String info;
    }
}
