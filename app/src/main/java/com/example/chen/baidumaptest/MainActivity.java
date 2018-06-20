package com.example.chen.baidumaptest;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private MapView mMapView = null;
    private BaiduMap mBaiduMap = null;
    boolean isFirstLoc = true; // 是否首次定位
    LocationClient mLocClient;
    private Marker marker;
    private Button btn_search;

    private static final int REQUEST_CODE_ACCESS_COARSE_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        SDKInitializer.initialize(getApplicationContext());

        setContentView(R.layout.activity_main);

        req();
        btn_search = findViewById(R.id.search);
        btn_search.setOnClickListener(this);

        mMapView = findViewById(R.id.bmapview);
        mBaiduMap = mMapView.getMap();

        //设置类型
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);

         //开启定位图层
        mBaiduMap.setMyLocationEnabled(true);

        mLocClient = new LocationClient(getApplicationContext());
        mLocClient.registerLocationListener(listener);
        LocationClientOption option = new LocationClientOption();
        option.setIsNeedAddress(true);//是否要地址
        option.setOpenGps(true);
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000);//可选，默认0，即仅定位一次，设置发起连续定位请求的间隔需要大于等于1000ms才是有效的
        mLocClient.setLocOption(option);
        mLocClient.start();


    }


    BDAbstractLocationListener listener = new BDAbstractLocationListener() {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            Log.i("bdmap","定位类型:"+bdLocation.getLocTypeDescription()+"\n"
                    +"纬度:"+bdLocation.getLatitude()+"\n"
                    +"经度:"+bdLocation.getLongitude()+"\n"
                    +"详细地址:"+bdLocation.getAddrStr()+"\n"
                    +"卫星数目"+bdLocation.getSatelliteNumber());
            if (isFirstLoc)
            {
                isFirstLoc = false;
                //点
                LatLng ll = new LatLng(bdLocation.getLatitude(),bdLocation.getLongitude());
                //创建Marker图标
                BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.mipmap.marker);

                //构建MarkerOption 用于地图上添加Marker
                OverlayOptions option = new MarkerOptions()
                        .position(ll)   //marker出现的位置
                        .icon(bitmap)
                        .draggable(true);   //可拖动
                //在地图上田间Marker并显示
                marker = (Marker) mBaiduMap.addOverlay(option);

                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(ll).zoom(17.0f);
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));

            }

        }

    };

    //动态申请权限
    private void req(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//如果 API level 是大于等于 23(Android 6.0) 时
            //判断是否具有权限
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //判断是否需要向用户解释为什么需要申请该权限
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION)) {
                   Toast.makeText(MainActivity.this,
                           "自Android 6.0开始需要打开位置权限才可以搜索到Ble设备",Toast.LENGTH_LONG).show();
                }
                //请求权限
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        REQUEST_CODE_ACCESS_COARSE_LOCATION);
            }
        }
    }
    @Override
    protected void onDestroy() {
        //退出时销毁定位
        mLocClient.stop();
        //关闭定位图层
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;
        super.onDestroy();

    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onClick(View v) {

        MapStatusUpdate move = MapStatusUpdateFactory.newLatLng(new LatLng(
                39.9899560000, 116.3230660000));// 中关村地铁经纬度坐标
        // 移动
        mBaiduMap.animateMapStatus(move);
    }
}