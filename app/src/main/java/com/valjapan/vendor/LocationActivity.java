package com.valjapan.vendor;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

public class LocationActivity extends AppCompatActivity implements OnMapReadyCallback {
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference reference = database.getReference();


    //    Fused Location Provider API.
    private FusedLocationProviderClient fusedLocationClient;

    //    Location Settings APIs.
    private SettingsClient settingsClient;
    private LocationSettingsRequest locationSettingsRequest;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private Location location;

    private GoogleMap mMap;


    private String lastUpdateTime;
    private Boolean requestingLocationUpdates;
    private static final int REQUEST_CHECK_SETTINGS = 0x1;
    private int priority = 0;
    private String textLog;

    //    GoogleMaps
    private LatLng latlng;
    double locateX, locateY;
    private Boolean checkFiretLocation = false;

    HashMap<String, Marker> hashMapMarker = new HashMap<>();
    private String myPlace = "MyPlace";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        settingsClient = LocationServices.getSettingsClient(this);

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnap) {
                for (DataSnapshot dataSnapshot : dataSnap.getChildren()) {
                    String key = (String) dataSnapshot.child("fireBaseKey").getValue();
                    String kind = (String) dataSnapshot.child("vendingKind").getValue();
                    String content = (String) dataSnapshot.child("content").getValue();
                    String locateX = (String) dataSnapshot.child("locateX").getValue();
                    String locateY = (String) dataSnapshot.child("locateY").getValue();
                    // このforループで、取得できるはず！

                    setFireBaseDataIcon(key, kind, content, locateX, locateY);
//                    マーカーを置くメソッドに行く
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("LocationActivity", "onCancelled()");
            }
        });
//
//        reference.addChildEventListener(new ChildEventListener() {
//            @Override
//            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//                for (DataSnapshot dataS : dataSnapshot.getChildren()) {
//                    String kind = (String) dataS.child("vendingKind").getValue();
//                    String content = (String) dataS.child("content").getValue();
//                    String locateX = (String) dataS.child("locateX").getValue();
//                    String locateY = (String) dataS.child("locateY").getValue();
//
//                    setFireBaseDataIcon(kind, content, locateX, locateY);
//                }
////                保存した情報を用いた描画処理を記載している。
//            }
//
//            @Override
//            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//                PlaceData result = dataSnapshot.getValue(PlaceData.class);
////                アイテムのリストを取得するか、アイテムのリストへの追加がないかリッスンします。
//                if (result == null) return;
//
//                for (DataSnapshot dataS : dataSnapshot.getChildren()) {
//                    String kind = (String) dataS.child("vendingKind").getValue();
//                    String content = (String) dataS.child("content").getValue();
//                    String locateX = (String) dataS.child("locateX").getValue();
//                    String locateY = (String) dataS.child("locateY").getValue();
//
//                    setFireBaseDataIcon(kind, content, locateX, locateY);
//                }
//            }
//
//            @Override
//            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
//
//            }
//
//            @Override
//            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });

        priority = 0;

        createLocationCallback();
        createLocationRequest();
        buildLocationSettingRequest();

        textLog = "onCreate()\n";
        Log.d("LocationActivity", "onCreate()");

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

//        測位開始
        startLocationUpdates();
    }

    /*
    位置情報を受け取るクラス
     */
//    locationのコールバックを受け取る

    private void createLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                location = locationResult.getLastLocation();
                lastUpdateTime = DateFormat.getTimeInstance().format(new Date());
                updateLocationUI();
            }
        };
    }

    private void updateLocationUI() {
//        getLastLocation()からの情報がある場合のみ
        if (location != null) {
            String fusedName[] = {
                    "Latitude", "Longitude", "Accuracy", "Altitude", "Speed", "Bearing"
            };

            double fusedData[] = {
                    location.getLatitude(),
                    location.getLongitude(),
                    location.getAccuracy(),
                    location.getAltitude(),
                    location.getSpeed(),
                    location.getBearing()
            };

            locateX = location.getLatitude();
            locateY = location.getLongitude();


            setIcon(locateX, locateY);
//            TODO 現在位置を更新する

            if (!checkFiretLocation) {
                Log.d("LocationActivity", "初回の読み込みをしました");
                setNowPlace(locateX, locateY);
                checkFiretLocation = true;
            }

            StringBuilder stuBuf = new StringBuilder("---------- UpdateLocation ---------- \n");

            for (int i = 0; i < fusedName.length; i++) {
                stuBuf.append(fusedName[i]);
                stuBuf.append(" = ");
                stuBuf.append(String.valueOf(fusedData[i]));
                stuBuf.append("\n");
            }


            stuBuf.append("Time");
            stuBuf.append(" = ");
            stuBuf.append(lastUpdateTime);
            stuBuf.append("\n");

            textLog += stuBuf;

            Log.d("LocationActivity", textLog);

        }
    }

    private void createLocationRequest() {
        locationRequest = new LocationRequest();

        if (priority == 0) {
            /**
             *
             * 高い精度の位置情報を取得したい場合、
             * インターバルを例えば5000msecに設定すれば、
             * マップアプリのようなリアルタイム即位となる。
             * 主に精度重視のためのGPSが優先的に使われる。
             */

            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        } else if (priority == 1) {
            /**
             * バッテリー消費を抑えたい場合、精度は100mと悪くなる。
             * 主にwifi,電話網での位置情報が主となる
             * この設定の例としては、setInterval(1時間)、setFastInterval(1分)
             */
            locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        } else if (priority == 2) {
            /**
             * バッテリー消費を抑えたい場合、精度は10kmと悪くなる
             */
            locationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);

        } else {
            /**
             * 受身的な位置情報取得でアプリが自ら即位せず、
             * 他のアプリで得られた位置情報は入手できる
             */
            locationRequest.setPriority(LocationRequest.PRIORITY_NO_POWER);

        }

        /**
         * アップデートのインターバル期間設定
         * このインターバルは即位データがない場合はアップデートしません
         * また状況によってはこの時間よりも長くなることもあり、
         * 必ずしも正確な時間ではありません
         *
         *他に同様のアプリが短いインターバルでアップデートしていると
         * それに影響されインターバルが短くなることがあります
         * 単位:msec
         */
        locationRequest.setInterval(6000);
        /**
         * このインターバル時間は正確です。これより早いアップデートはしません。
         * 単位:msec
         */
        locationRequest.setFastestInterval(5000);

    }

    //    端末で測位できるか状態を確認する。wifi,GPSなどがOffになっているとエラー情報のダイアログが出る。
    private void buildLocationSettingRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();

        builder.addLocationRequest(locationRequest);
        locationSettingsRequest = builder.build();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.i("debug", "User agreed to make required location settings changes.");
                        // Nothing to do. startLocationupdates() gets called in onResume again.
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i("debug", "User chose not to make required location settings changes.");
                        requestingLocationUpdates = false;
                        break;
                }
                break;
        }
    }

    //    FusedLocationApiによるlocation updatesをリクエスト
    private void startLocationUpdates() {
        // Begin by checking if the device has the necessary location settings.
        settingsClient.checkLocationSettings(locationSettingsRequest)
                .addOnSuccessListener(this,
                        new OnSuccessListener<LocationSettingsResponse>() {
                            @Override
                            public void onSuccess(
                                    LocationSettingsResponse locationSettingsResponse) {
                                Log.i("debug", "All location settings are satisfied.");

                                // パーミッションの確認
                                if (ActivityCompat.checkSelfPermission(
                                        LocationActivity.this,
                                        android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                                        PackageManager.PERMISSION_GRANTED
                                        && ActivityCompat.checkSelfPermission(
                                        LocationActivity.this,
                                        Manifest.permission.ACCESS_COARSE_LOCATION) !=
                                        PackageManager.PERMISSION_GRANTED) {

                                    // TODO: Consider calling
                                    //    ActivityCompat#requestPermissions
                                    // here to request the missing permissions, and then overriding
                                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                    //                                          int[] grantResults)
                                    // to handle the case where the user grants the permission. See the documentation
                                    // for ActivityCompat#requestPermissions for more details.
                                    return;
                                }
                                fusedLocationClient.requestLocationUpdates(
                                        locationRequest, locationCallback, Looper.myLooper());

                            }
                        })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                Log.i("debug", "Location settings are not satisfied. Attempting to upgrade " +
                                        "location settings ");
                                try {
                                    // Show the dialog by calling startResolutionForResult(), and check the
                                    // result in onActivityResult().
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(
                                            LocationActivity.this,
                                            REQUEST_CHECK_SETTINGS);

                                } catch (IntentSender.SendIntentException sie) {
                                    Log.i("debug", "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
                                Log.e("debug", errorMessage);
                                Toast.makeText(LocationActivity.this,
                                        errorMessage, Toast.LENGTH_LONG).show();

                                requestingLocationUpdates = false;
                        }

                    }
                });

        requestingLocationUpdates = true;
    }


    private void stopLocationUpdates() {
        textLog += "onStop()\n";

        if (!requestingLocationUpdates) {
            Log.d("debug", "stopLocationUpdates: " +
                    "updates never requested, no-op.");

            return;
        }

        fusedLocationClient.removeLocationUpdates(locationCallback)
                .addOnCompleteListener(this,
                        new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                requestingLocationUpdates = false;
                            }
                        });
    }


    /*
    GoogleMapApiを利用したクラス
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        // Add a marker in Tokyo and move the camera
        latlng = new LatLng(35.6847212, 139.7504106);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));

        Marker marker = mMap.addMarker(new MarkerOptions()
                .position(latlng)
        );
        hashMapMarker.put(myPlace, marker);


    }

    public void searchMyPlace(View v) {
        setNowPlace(locateX, locateY);
    }


    public void addVendor(View v) {
        Intent intent = new Intent(getApplication(), AddActivity.class);
        intent.putExtra("LocateX", locateX);
        intent.putExtra("LocateY", locateY);
        startActivity(intent);
    }


    public void setNowPlace(double locateX, double locateY) {
        mMap.getCameraPosition();
        CameraUpdate cUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(locateX, locateY), 16);
        mMap.moveCamera(cUpdate);
    }

    private void setIcon(double latitude, double longitude) {

        Drawable circleDrawable = getResources().getDrawable(R.drawable.ic_my_location);
        BitmapDescriptor markerIcon = getMarkerIconFromDrawable(circleDrawable);

//        mMap.clear();


        Marker deleteMarker = hashMapMarker.get(myPlace);

        deleteMarker.remove();

        hashMapMarker.remove(myPlace);


        Marker marker = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(latitude, longitude))
                .icon(markerIcon)
        );

        hashMapMarker.put(myPlace, marker);


    }

    private void setFireBaseDataIcon(String key, String kind, String content, String latitude, String longitude) {
//        Firebaseから取得した座標をここでマーカーを置く
        Log.d("LocationActivity", key + " " + kind + " " + content + " " + latitude + " " + longitude);
//        Drawable circleDrawable = getResources().getDrawable(R.drawable.ic_person_pin_circle);
//        BitmapDescriptor markerIcon = getMarkerIconFromDrawable(circleDrawable);
//        TODO とりあえずマーカーはデフォルトで

        double locateX = Double.parseDouble(latitude);
        double locateY = Double.parseDouble(longitude);


        LatLng point = new LatLng(locateX, locateY);
        Marker marker = mMap.addMarker(new MarkerOptions()
                .position(point)
                .title(kind)
                .snippet(content));

        hashMapMarker.put(key, marker);

//        MarkerOptions options = new MarkerOptions();//ピンの設定
//        options.position(latLng);//ピンの場所を指定
//        options.title(kind + "(" + content + ")");//マーカーの吹き出しの設定
//        mMap.addMarker(options);//ピンの設置

    }


    private BitmapDescriptor getMarkerIconFromDrawable(Drawable drawable) {
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // バッテリー消費を抑えてLocation requestを止める
        stopLocationUpdates();
    }

}