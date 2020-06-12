package com.example.honyarexample;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.honyarexample.view.CmdTestActivity;
import com.example.honyarexample.view.EsptouchDemoActivity;
import com.google.gson.Gson;
import com.honyar.SDKHonyarSupport;
import com.honyar.bean.BaseReqBean;
import com.honyar.bean.DeviceData102;
import com.honyar.contract.ClearDeviceListener;
import com.honyar.contract.DeviceListCallBack;
import com.honyar.contract.DisConnectListener;
import com.honyar.contract.DeviceUpDataListener;
import com.honyar.contract.DoFindDeviceResult;
import com.honyar.contract.NewDeviceConnectListener;
import com.honyar.contract.SendDataListener;
import com.qw.soul.permission.SoulPermission;
import com.qw.soul.permission.bean.Permission;
import com.qw.soul.permission.bean.Permissions;
import com.qw.soul.permission.callbcak.CheckRequestPermissionsListener;

import java.util.List;

public class MainActivity extends Activity implements DisConnectListener,DeviceUpDataListener, NewDeviceConnectListener {
    private String TAG = this.getClass().getSimpleName();
    private Gson gson = new Gson();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initData();
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onDestroy(){
        removeListener();
        super.onDestroy();
    }
    private void initData(){
        SDKHonyarSupport.getInstance().addDeviceDisConnectListener(this);
        SDKHonyarSupport.getInstance().addDeviceUpDataListener(this);
        SDKHonyarSupport.getInstance().addNewDeviceConnectListener(this);
    }

    private void removeListener(){
        SDKHonyarSupport.getInstance().removeDeviceDisConnectListener(this);
        SDKHonyarSupport.getInstance().removeDeviceUpDataListener(this);
        SDKHonyarSupport.getInstance().removeNewDeviceConnectListener(this);
    }
    public void EspTouchConfig(View v){
        toEspTouchActivity();
    }

    public void tocmdTest(View v){
        tocmdTestActivity();
    }

    public void getDeviceList(View v){
        SDKHonyarSupport.getInstance().getDeviceList(new DeviceListCallBack() {
            @Override
            public void deviceList(List<DeviceData102> devices) {
                Log.i(TAG,gson.toJson(devices));
            }
        });
    }

    public void sendData(View v){
        SDKHonyarSupport.getInstance().sendData("68c63a8c156a", "{\"cmd\":\"202\",\"data\":{\"relay\":\"255\"}}", new SendDataListener() {
            @Override
            public void sendFaild(int errorCode, String message) {

            }

            @Override
            public void sendSuccess() {

            }
        });
    }

    public void clearDeviceList(View v){
        SDKHonyarSupport.getInstance().clearDeviceList(new ClearDeviceListener() {
            @Override
            public void clearSuccess() {
                Log.i(TAG,"clear success");
            }

            @Override
            public void clearFaild() {

            }
        });
    }
    public void stopFindDevice(View v){
        SDKHonyarSupport.getInstance().stopFindDevice(new DoFindDeviceResult() {
            @Override
            public void doSuccess() {
                Log.i(TAG,"stopFindDevice success");
            }

            @Override
            public void doFaild(int i, String s) {
                Log.i(TAG,"startFindDevice doFaild"+s);
            }
        });
    }

    public void startFindDevice(View v){
        SDKHonyarSupport.getInstance().startFindDevice(new DoFindDeviceResult() {
            @Override
            public void doSuccess() {
                Log.i(TAG,"startFindDevice success");
            }

            @Override
            public void doFaild(int i, String s) {
                Log.i(TAG,"startFindDevice doFaild"+s);
            }
        });
    }
    private void tocmdTestActivity(){
        DeviceData102 deviceData102 = new DeviceData102();
        deviceData102.deviceMac = "68c63a8c156a"; //修改成实际设备的mac地址，或者传入真正的DeviceData102
        Intent intent = new Intent();
        intent.putExtra("device",deviceData102);
        intent.setClass(MainActivity.this, CmdTestActivity.class);
        startActivity(intent);
    }
    private void toEspTouchActivity(){
        SoulPermission.getInstance().checkAndRequestPermissions(Permissions.build(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION),
                new CheckRequestPermissionsListener() {
                    @Override
                    public void onAllPermissionOk(Permission[] allPermissions) {
                        Log.i(TAG,"permiss ok");
                        toActivity(MainActivity.this,EsptouchDemoActivity .class);
                    }

                    @Override
                    public void onPermissionDenied(final Permission[] refusedPermissions) {
                        // see CheckPermissionWithRationaleAdapter
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.i(TAG,"permission:"+refusedPermissions[0].shouldRationale());
                                if (refusedPermissions[0].shouldRationale()) {
                                    Toast.makeText(MainActivity.this, "请打开手机定位权限后再操作", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(MainActivity.this, "请打开手机定位权限后再操作", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    }
                });

    }
    public void toActivity(Context mcontext, Class toActivity){
        Intent intent = new Intent();
        intent.setClass(mcontext, toActivity);
        startActivity(intent);
    }

    @Override
    public void deviceUpData(String data) {
        Log.i(TAG,"device deviceUpData:"+data);
    }

    @Override
    public void newDeviceConnected(DeviceData102 deviceData102) {
        Log.i(TAG,"got new data:"+gson.toJson(deviceData102));
    }

    @Override
    public void disConnect(String s) {
        Log.i(TAG,"device disconnected");
    }
}
