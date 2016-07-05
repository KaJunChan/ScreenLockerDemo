package com.example.demo.screenlocker.modeldemo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/7/5.
 */
public class WeatherInfo {
//        {
//        "cond":
//
//        {
//            "code":"300", "txt":"阵雨"
//        }
//
//        ,"fl":"38", "hum":"66", "pcpn":"0", "pres":"1006", "tmp":"31", "vis":"10", "wind":
//
//        {
//            "deg":"140", "dir":"南风", "sc":"4-5", "spd":"21"
//        }
//    }
    public Cond cond;
    public int fl;
    public int hum;
    public double pcpn;
    public int pres;
    public int tmp;
    public int vis;
    public Wind wind;

    public class Cond {
        public int code;
        public String txt;
    }

    public class Wind {
        public int deg;
        public String dir;
        public String sc;
        public int spd;
    }
}
