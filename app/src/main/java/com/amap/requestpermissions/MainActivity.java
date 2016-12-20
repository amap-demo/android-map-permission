package com.amap.requestpermissions;

import android.Manifest;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.preference.DialogPreference;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;

public class MainActivity extends FragmentActivity implements AMapLocationListener, View.OnClickListener, HintDialogFragment.DialogFragmentCallback{
    private AMap amap;
    private MapView mapView;
    private AMapLocationClient mlocationClient;
    private AMapLocationClientOption mLocationOption;
    private static final int LOCATION_PERMISSION_CODE = 100;
    private static final int STORAGE_PERMISSION_CODE = 101;
    private Button locbtn;
    private Button stobtn;
    private Marker locMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initLocation();//初始化定位参数
        checkStoragePermission();//初始化请求权限，存储权限

        //添加地图
        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);// 此方法必须重写
        if (amap == null)
            amap = mapView.getMap();
        init();
    }

    private void init() {
        locbtn = (Button)findViewById(R.id.locbtn);
        stobtn = (Button)findViewById(R.id.stobtn);
        locbtn.setOnClickListener(this);
        stobtn.setOnClickListener(this);
    }

    private void checkLocationPermission() {
        // 检查是否有定位权限
        // 检查权限的方法: ContextCompat.checkSelfPermission()两个参数分别是Context和权限名.
        // 返回PERMISSION_GRANTED是有权限，PERMISSION_DENIED没有权限
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            //没有权限，向系统申请该权限。
            Log.i("MY","没有权限");
            requestPermission(LOCATION_PERMISSION_CODE);
        } else {
            //已经获得权限，则执行定位请求。
            Toast.makeText(MainActivity.this, "已获取定位权限",Toast.LENGTH_SHORT).show();

            startLocation();

        }
    }

    private void checkStoragePermission() {
        // 检查是否有存储的读写权限
        // 检查权限的方法: ContextCompat.checkSelfPermission()两个参数分别是Context和权限名.
        // 返回PERMISSION_GRANTED是有权限，PERMISSION_DENIED没有权限
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            //没有权限，向系统申请该权限。
            Log.i("MY","没有权限");
            requestPermission(STORAGE_PERMISSION_CODE);
        } else {
            //同组的权限，只要有一个已经授权，则系统会自动授权同一组的所有权限，比如WRITE_EXTERNAL_STORAGE和READ_EXTERNAL_STORAGE
            Toast.makeText(MainActivity.this, "已获取存储的读写权限",Toast.LENGTH_SHORT).show();
        }
    }

    private void requestPermission(int permissioncode) {
        String permission = getPermissionString(permissioncode);
        if (!IsEmptyOrNullString(permission)){
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    permission)) {
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                if(permissioncode == LOCATION_PERMISSION_CODE) {
                    DialogFragment newFragment = HintDialogFragment.newInstance(R.string.location_description_title,
                            R.string.location_description_why_we_need_the_permission,
                            permissioncode);
                    newFragment.show(getFragmentManager(), HintDialogFragment.class.getSimpleName());
                } else if (permissioncode == STORAGE_PERMISSION_CODE) {
                    DialogFragment newFragment = HintDialogFragment.newInstance(R.string.storage_description_title,
                            R.string.storage_description_why_we_need_the_permission,
                            permissioncode);
                    newFragment.show(getFragmentManager(), HintDialogFragment.class.getSimpleName());
                }


            } else {
                Log.i("MY","返回false 不需要解释为啥要权限，可能是第一次请求，也可能是勾选了不再询问");
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{permission}, permissioncode);
            }
        }
    }

    //定位
    private void initLocation() {
        //初始化client
        mlocationClient = new AMapLocationClient(this.getApplicationContext());
        // 设置定位监听
        mlocationClient.setLocationListener(this);
        //定位参数
        mLocationOption = new AMapLocationClientOption();
        //设置为高精度定位模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //设置为单次定位
        mLocationOption.setOnceLocation(true);
        //设置定位参数
        mlocationClient.setLocationOption(mLocationOption);
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (aMapLocation != null
                && aMapLocation.getErrorCode() == 0) {
            double longitude = aMapLocation.getLongitude();
            double latitude = aMapLocation.getLatitude();
            LatLng location = new LatLng(latitude, longitude);
            changeLocation(location);
        } else {
            String errText = "定位失败," + aMapLocation.getErrorCode() + ": " + aMapLocation.getErrorInfo();
            Log.e("AmapErr", errText);
            Toast.makeText(MainActivity.this, errText, Toast.LENGTH_LONG).show();
        }
    }

    private void changeLocation(LatLng location) {
        if (locMarker == null){
            locMarker = amap.addMarker(new MarkerOptions().position(location));
        }else{
            locMarker.setPosition(location);
        }
        amap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
    }

    /**
     * 开始定位
     */
    private void startLocation(){
        // 启动定位
        mlocationClient.startLocation();
        Log.i("MY","startLocation");
    }
    /**
     * 停止定位
     */
    private void stopLocation() {
        // 停止定位
        mlocationClient.stopLocation();
    }

    /**
     * 销毁定位
     */
    private void destroyLocation() {
        if (null != mlocationClient) {
            mlocationClient.stopLocation();
            /**
             * 如果AMapLocationClient是在当前Activity实例化的，
             * 在Activity的onDestroy中一定要执行AMapLocationClient的onDestroy
             */
            mlocationClient.onDestroy();
            mlocationClient = null;
            mlocationClient = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mapView != null) {
            mapView.onSaveInstanceState(outState);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroyLocation();
        if (mapView != null) {
            mapView.onDestroy();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this,"定位权限已获取",Toast.LENGTH_SHORT).show();
                    Log.i("MY","定位权限已获取");
                    startLocation();
                } else {
                    Toast.makeText(MainActivity.this,"定位权限被拒绝",Toast.LENGTH_SHORT).show();
                    Log.i("MY","定位权限被拒绝");
                    if(!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){
                        DialogFragment newFragment = HintDialogFragment.newInstance(R.string.location_description_title,
                                R.string.location_description_why_we_need_the_permission,
                                requestCode);
                        newFragment.show(getFragmentManager(), HintDialogFragment.class.getSimpleName());
                        Log.i("MY","false 勾选了不再询问，并引导用户去设置中手动设置");

                        return;
                    }
                }
                return;
            }
            case STORAGE_PERMISSION_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this,"存储权限已获取",Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this,"存储权限被拒绝",Toast.LENGTH_SHORT).show();
                    Log.i("MY","定位权限被拒绝");
                    if(!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        DialogFragment newFragment = HintDialogFragment.newInstance(R.string.storage_description_title,
                                R.string.storage_description_why_we_need_the_permission,
                                requestCode);
                        newFragment.show(getFragmentManager(), HintDialogFragment.class.getSimpleName());
                        Log.i("MY","false 勾选了不再询问，并引导用户去设置中手动设置");
                    }
                    return;
                }
            }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case  R.id.locbtn:
                checkLocationPermission();
                break;
//            case R.id.stobtn:
//                checkStoragePermission();
//                break;
        }

    }

    public static boolean IsEmptyOrNullString(String s) {
        return (s == null) || (s.trim().length() == 0);
    }

    @Override
    public void doPositiveClick(int requestCode) {
        String permission = getPermissionString(requestCode);
        if (!IsEmptyOrNullString(permission)){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{permission},
                        requestCode);
            }else{
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        }
    }

    @Override
    public void doNegativeClick(int requestCode) {

    }

    private String getPermissionString(int requestCode){
        String permission = "";
        switch (requestCode){
            case LOCATION_PERMISSION_CODE:
                permission = Manifest.permission.ACCESS_FINE_LOCATION;
                break;
            case STORAGE_PERMISSION_CODE:
                permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
                break;
        }
        return permission;
    }

}
