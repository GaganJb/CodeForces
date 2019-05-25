package com.example.ridetracker;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Looper;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.location.LocationServices;

import android.location.Location;
import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.math.MathUtils;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static java.lang.StrictMath.abs;

public class MapsActivity extends FragmentActivity
        implements OnMapReadyCallback {

    GoogleMap mGoogleMap;
    SupportMapFragment mapFrag;
    LocationRequest mLocationRequest;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    FusedLocationProviderClient mFusedLocationClient;
 //   LocationCallback mLocationCallback;
    Button button;
    TextView km,speed1;
    Chronometer chronometer;
    boolean running;
    private double kms=0.00d, finspeed=0.00d;
    private long pause;
    private Float temp=0.00f;
    private String gg ="0";
    private String ggg="0";
    private String jj="0.00";
    private String durations=null;
    private long durationTime, c=0,i=1;
    private int findur=0,findur1=0;
    SharedPreferences prefs;
    List<Double> locs;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        locs = new ArrayList<Double>();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFrag.getMapAsync(this);
        button = findViewById(R.id.button);
        chronometer = findViewById(R.id.duration);
        km = findViewById(R.id.km);
        speed1= findViewById(R.id.speed);

        prefs = getApplicationContext().getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString("lat", "0");
        edit.putString("lng", "0");
        edit.commit();
    }

    public void start(View view){
            button.setText("STOP");
            if (!running) {
                c++;
                chronometer.setBase(SystemClock.elapsedRealtime() - pause);
                chronometer.start();
                running = true;

            } else {
                chronometer.stop();
                pause = SystemClock.elapsedRealtime() - chronometer.getBase();
                running = false;
                durationTime = SystemClock.elapsedRealtime();
                Toast.makeText(this, "Your duration is " + chronometer.getText() , Toast.LENGTH_SHORT).show();

            }

    }

    @Override
    public void onPause() {
        super.onPause();

        //stop location updates when Activity is no longer active
        if (mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
      //  mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000); // 10 sec interval
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                mGoogleMap.setMyLocationEnabled(true);
            } else {
                //Request Location Permission
                checkLocationPermission();
            }
        }
        else {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            mGoogleMap.setMyLocationEnabled(true);
        }
    }
    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            List<Location> locationList = locationResult.getLocations();
            if (locationList.size() > 0) {
                //The last location in the list is the newest
                Location location = locationList.get(locationList.size() - 1);
                Log.i("MapsActivity", "Location: " + location.getLatitude() + " " + location.getLongitude());

                Toast.makeText(MapsActivity.this, location.getLatitude() + " " + location.getLongitude(), Toast.LENGTH_SHORT).show();
                mLastLocation = location;
                if (mCurrLocationMarker != null) {
                    mCurrLocationMarker.remove();
                }

                //float speed = location.getSpeed();

               // Log.i("speed", String.valueOf(speed));

                //Place current location marker
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                List<LatLng> latLngs;
                double lat = location.getLatitude();
                double lng = location.getLongitude();
                gg = prefs.getString("lat", null);
                ggg = prefs.getString("lng", null);
                double lat1 = 0;
                double lng1 = 0;
                if (gg != null)
                    lat1 = Double.parseDouble(gg);
                if (ggg != null)
                    lng1 = Double.parseDouble(ggg);
                Log.i("hee", String.valueOf(button.getText())+ lat1 + lng1 + lat + lng);

                if (lat1 != 0 && running && GetDistanceFromLatLonInKm(lat1, lng1, lat, lng)>0.001){
                    Log.i("opp", String.valueOf(GetDistanceFromLatLonInKm(lat1, lng1, lat, lng)));
                    kms += GetDistanceFromLatLonInKm(lat1, lng1, lat, lng);
               //    Toast.makeText(MapsActivity.this, "Your speed: " +'\n' + GetDistanceFromLatLonInKm(lat1, lng1, lat, lng)*3600+  '\n' +GetDistanceFromLatLonInKm(lat1, lng1, lat, lng), Toast.LENGTH_LONG).show();
                    finspeed= (GetDistanceFromLatLonInKm(lat1, lng1, lat, lng)*3600);
                 //   Toast.makeText(MapsActivity.this, "jj" +finspeed, Toast.LENGTH_SHORT).show();
                    //finspeed/=i;
//                    double finspeed1=0.00d;
//                    if(i!=4)
//                    {
//                        locs.add(finspeed);
//                        i++;
//
//                        for(int j = 0; j < locs.size(); j++)
//                            finspeed1 += locs.get(j);
//                        finspeed1/=(i-1);
//                    }
//                    else
//                    {
//                        for(int j = 0; j < locs.size(); j++)
//                            finspeed1 += locs.get(j);
//                        finspeed1/=3;
//                        finspeed1 = (double)Math.round(finspeed1 * 100d) / 100d;
//                        speed1.setText(finspeed1 + " kmph");
//                        locs.clear();
//                        i=0;
//                    }
//                    for (Double num : locs) {
//                        Log.i("kk", String.valueOf(num));
//                    }
//                    Log.i("sppp", String.valueOf(finspeed));
//                    double kk = finspeed ;
//                    kk = (double)Math.round(kk * 100d) / 100d;
//                    speed1.setText(kk + " kmph");

                    durations= (String) chronometer.getText();
                    char dd3 =  durations.charAt(durations.length()-4);
                    char d1 =  durations.charAt(durations.length()-2);
                    char d2 = durations.charAt(durations.length()-1);
                    int d3= (int)d1-48;
                    int d4=(int)d2-48;
                    int dd33 = (int)dd3-48;
                    //durations = d1+d2;
                    findur = dd33*60+d3*10+d4;
                    Log.i("lob", String.valueOf(findur));

                    double kms1 = abs(temp-kms);
                    int kmms1 = abs(findur1-findur);
                    Log.i("kms = ", String.valueOf(kms1));
                    Log.i("temp = ", String.valueOf(temp));
                    Log.i("dur = ", String.valueOf(findur1));

                    double ff = (kms1*3600)/kmms1;
                    double kk = ff;
                    kk = (double)Math.round(kk * 100d) / 100d;
                    speed1.setText(kk+" kmph");



                    ArrayList<LatLng> points = new ArrayList<LatLng>();
                    PolylineOptions polyLineOptions = new PolylineOptions();
                    points.add(new LatLng(lat1,lng1));
                    points.add(new LatLng(lat,lng));
                    polyLineOptions.width(10);
                    polyLineOptions.geodesic(true);
                    polyLineOptions.color(MapsActivity.this.getResources().getColor(R.color.colorPrimaryDark));
                    polyLineOptions.addAll(points);
                    Polyline polyline = mGoogleMap.addPolyline(polyLineOptions);
                    polyline.setGeodesic(true);

                }
                else {
                    speed1.setText(jj + " kmph");
                }
                kms = (double)Math.round(kms * 100000d) / 100000d;
                km.setText(String.valueOf(kms));
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));


                Geocoder geocoder = new Geocoder(getApplicationContext());
                String loc=null;
                List<Address> addressList = null;
                try {
                    addressList = geocoder.getFromLocation(lat, lng, 1);
                     loc = addressList.get(0).getAddressLine(0) ;
                    Log.i("Location is", loc);
                } catch (IOException e) {
                    e.printStackTrace();
                }

          //      Toast.makeText(MapsActivity.this, "Your location: " +'\n' + loc, Toast.LENGTH_LONG).show();

                prefs = getApplicationContext().getSharedPreferences("MyPrefs", MODE_PRIVATE);
                SharedPreferences.Editor edit = prefs.edit();
                edit.putString("lat", String.valueOf(location.getLatitude()));
                edit.putString("lng", String.valueOf(location.getLongitude()));
                edit.putFloat("kmm", (float) kms);
                edit.putInt("kmm1", (int) findur);
                edit.commit();

                temp=  prefs.getFloat("kmm", 0);
                findur1 = prefs.getInt("kmm1", 0);
            }
        }
    };

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MapsActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION );
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                        mGoogleMap.setMyLocationEnabled(true);
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public double GetDistanceFromLatLonInKm(double lat1, double lon1, double lat2, double lon2)
    {
        final int R = 6371;
        // Radius of the earth in km
        double dLat = deg2rad(lat2 - lat1);
        // deg2rad below
        double dLon = deg2rad(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double d = R * c;
        // Distance in km
        return d;
    }
    private double deg2rad(double deg)
    {
        return deg * (Math.PI / 180);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //if(String.valueOf(button.getText()).equals("STOP"))
        {
            locs = new ArrayList<Double>();

            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

            mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFrag.getMapAsync(this);
            button = findViewById(R.id.button);
            chronometer = findViewById(R.id.duration);
            km = findViewById(R.id.km);
            speed1= findViewById(R.id.speed);

            prefs = getApplicationContext().getSharedPreferences("MyPrefs", MODE_PRIVATE);
            SharedPreferences.Editor edit = prefs.edit();
            edit.putString("lat", "0");
            edit.putString("lng", "0");
            edit.commit();
        }
    }
}






























//
//import android.Manifest;
//import android.content.pm.PackageManager;
//import android.location.Address;
//import android.location.Criteria;
//import android.location.Geocoder;
//import android.location.Location;
//import android.location.LocationListener;
//import android.location.LocationManager;
//import android.support.v4.app.ActivityCompat;
//import android.support.v4.app.FragmentActivity;
//import android.os.Bundle;
//
//import com.google.android.gms.maps.CameraUpdateFactory;
//import com.google.android.gms.maps.GoogleMap;
//import com.google.android.gms.maps.OnMapReadyCallback;
//import com.google.android.gms.maps.SupportMapFragment;
//import com.google.android.gms.maps.model.LatLng;
//import com.google.android.gms.maps.model.Marker;
//import com.google.android.gms.maps.model.MarkerOptions;
//
//import java.io.IOException;
//import java.util.List;
//
//public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
//
//    private GoogleMap mMap;
//    LocationManager locationManager;
//    Marker marker;
//    MarkerOptions markerOptions;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_maps);
//        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
//        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
//                .findFragmentById(R.id.map);
//        mapFragment.getMapAsync(this);
//        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }
//        if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
//            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, new LocationListener() {
//                @Override
//                public void onLocationChanged(Location location) {
//
//                    double lat = location.getLatitude();
//                    double lon = location.getLongitude();
//                    LatLng latLng = new LatLng(lat, lon);
//                    mMap.addMarker(new MarkerOptions().position(latLng).title("My Current Location"));
//                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10f));
//                }
//
//                @Override
//                public void onStatusChanged(String provider, int status, Bundle extras) {
//
//                }
//
//                @Override
//                public void onProviderEnabled(String provider) {
//
//                }
//
//                @Override
//                public void onProviderDisabled(String provider) {
//
//                }
//            });
//        }
//
//        else if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
//            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
//                @Override
//                public void onLocationChanged(Location location) {
//
//                    double lat = location.getLatitude();
//                    double lon = location.getLongitude();
//                    LatLng latLng = new LatLng(lat, lon);
//                    mMap.addMarker(new MarkerOptions().position(latLng).title("My Current location"));
//                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10f));
//                }
//
//                @Override
//                public void onStatusChanged(String provider, int status, Bundle extras) {
//
//                }
//
//                @Override
//                public void onProviderEnabled(String provider) {
//
//                }
//
//                @Override
//                public void onProviderDisabled(String provider) {
//
//                }
//            });
//        }
//    }
//
//    @Override
//    public void onMapReady(GoogleMap googleMap) {
//        mMap = googleMap;
//
//    }
//}
