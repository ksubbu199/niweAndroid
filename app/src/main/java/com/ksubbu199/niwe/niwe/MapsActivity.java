package com.ksubbu199.niwe.niwe;

import android.Manifest;
//import android.Manifest.permission.ACCESS_FINE_LOCATION;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,LocationListener {

    private static final int PERMISSION_ACCESS_FINE_LOCATION = 0,PERMISSION_ACCESS_COARSE_LOCATION = 1;
    private GoogleMap mMap;
    private static final String TAG = "MapActivity";
    private Marker marker;
    private boolean locationPermissions=false;
    private LocationManager locationManager;
    private Location location;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Toast.makeText(this, "Oreyyy!!! Dont distribute this app!", Toast.LENGTH_SHORT).show();
        Toast.makeText(this, "This is just a testing app!", Toast.LENGTH_SHORT).show();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Asking fine permissions");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ACCESS_FINE_LOCATION);
            }
            else {
                //Toast.makeText(this, "Thanks for eneabling location!", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Got fine permissions");
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Asking coarse permissions");
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_ACCESS_FINE_LOCATION);
                } else {
                  //  Toast.makeText(this, "Thanks for eneabling coarse location!", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Got coarse permissions");
                }
            }



//        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
//
//            @Override
//            public View getInfoWindow(Marker arg0) {
//                return null;
//            }
//
//            @Override
//            public View getInfoContents(Marker marker) {
//
//                LinearLayout info = new LinearLayout(mContext);
//                info.setOrientation(LinearLayout.VERTICAL);
//
//                TextView title = new TextView(mContext);
//                title.setTextColor(Color.BLACK);
//                title.setGravity(Gravity.CENTER);
//                title.setTypeface(null, Typeface.BOLD);
//                title.setText(marker.getTitle());
//
//                TextView snippet = new TextView(mContext);
//                snippet.setTextColor(Color.GRAY);
//                snippet.setText(marker.getSnippet());
//
//                info.addView(title);
//                info.addView(snippet);
//
//                return info;
//            }
//        });

    }


    public void onProviderDisabled(String string)
    {
     //   location=getLocation();
    }

    public void onProviderEnabled(String string)
    {
        location=getLocation();
    }

    public void onStatusChanged(String string, int i,Bundle b)
    {
        //location=getLocation();
    }

    public void onLocationChanged(Location loc)
    {
        location=loc;
    }


    private void moveToCurrentLocation(LatLng currentLocation)
    {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation,15));
        // Zoom in, animating the camera.
        //mMap.animateCamera(CameraUpdateFactory.zoomIn());
        // Zoom out to zoom level 10, animating with a duration of 2 seconds.
        mMap.animateCamera(CameraUpdateFactory.zoomTo(13), 2000, null);


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_ACCESS_FINE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // All good!
                    locationPermissions=true;
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        Log.d(TAG, "Asking coarse permissions");
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_ACCESS_FINE_LOCATION);
                    }
                    else{
                        Toast.makeText(this, "Thanks for eneabling coarse location!", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Got coarse permissions");
                    }
                    location=getLocation();
                    if(location!=null)
                    {
                        LatLng latLng=new LatLng(location.getLatitude(),location.getLongitude());
                        getData(latLng);
                        marker.setPosition(latLng);
                        Log.d(TAG, "latitude : "+ marker.getPosition().latitude);
                        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                        moveToCurrentLocation(latLng);
                    }

                } else {
                    Toast.makeText(this, "Need your location to continue!", Toast.LENGTH_SHORT).show();
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ACCESS_FINE_LOCATION);
                   // locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                }

                break;
            case PERMISSION_ACCESS_COARSE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // All good!
                    locationPermissions=true;
                    Log.d(TAG, "Got coarse permissions");
                    getLocation();

                } else {
                    Toast.makeText(this, "Need your location to continue!", Toast.LENGTH_SHORT).show();
                   // ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ACCESS_FINE_LOCATION);
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_ACCESS_FINE_LOCATION);

                }

                break;

        }
    }

//    private Location getLastBestLocation() {
//
//        return  locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//        //Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//
//        //Location locationNet = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//
//        //long GPSLocationTime = 0;
//        //if (null != locationGPS) { GPSLocationTime = locationGPS.getTime(); }
//
//        //long NetLocationTime = 0;
//
////        if (null != locationNet) {
////            NetLocationTime = locationNet.getTime();
////        }
//
////        if ( 0 < GPSLocationTime - NetLocationTime ) {
////            return locationGPS;
////        }
////        else {
////            return locationNet;
////        }
//    }


    private boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        }else{
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }


    }

    public Location getLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                return null;
        }
        if (isLocationEnabled(this)) {
            //locationManager = (LocationManager)  this.getSystemService(Context.LOCATION_SERVICE);
            Criteria criteria = new Criteria();
//        criteria.setPowerRequirement(Criteria.POWER_LOW);
//        criteria.setAccuracy(Criteria.ACCURACY_FINE);
//        criteria.setSpeedRequired(true);
//        criteria.setAltitudeRequired(false);
//        criteria.setBearingRequired(false);
//        criteria.setCostAllowed(false);
            String bestProvider = String.valueOf(locationManager.getBestProvider(criteria, true));

            //You can still do this if you like, you might get lucky:
            Location location = locationManager.getLastKnownLocation(bestProvider);
            if (location != null) {
                Log.e(TAG, "GPS is on");
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                //Toast.makeText(this, "latitude:" + latitude + " longitude:" + longitude, Toast.LENGTH_SHORT).show();
                //searchNearestPlace(voice2text);
                moveToCurrentLocation(new LatLng(latitude,longitude));
                return location;
            }
            else{
                Log.e(TAG, "Else brah");

                //This is what you need:
                locationManager.requestLocationUpdates(bestProvider, 1000, 0, this);
                return null;
            }
        }
        else
        {
            Log.e(TAG, "Else fucked up");
            Toast.makeText(this, "Please Turn-On GPS!", Toast.LENGTH_SHORT).show();
            return null;
           //prompt user to enable location....
            //.................
        }
    }
    /**
     *
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */



    private void getData(LatLng ll)
    {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="http://14.139.172.6:8080/getLatInfo?lat="+ll.latitude+"&long="+ll.longitude+"&area="+900;
        Log.d(TAG, url);
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        //mTextView.setText("Response is: "+ response.substring(0,500));
                        Log.d(TAG, response);
                        try{
                            JSONObject obj = new JSONObject(response);
                            if(obj.getString("error")!=null)
                            {
                                Log.d(TAG, obj.getString("CUF"));
                                String snip="CUF:"+obj.getJSONObject("CUF").getString("value")+" "+obj.getJSONObject("CUF").getString("units")+"<br>"+
                                        "AEP:"+obj.getJSONObject("AEP").getString("value")+" "+obj.getJSONObject("AEP").getString("units")+"<br>"+
                                        "GHI:"+obj.getJSONObject("GHI").getString("value")+" "+obj.getJSONObject("GHI").getString("units")+"<br>"+
                                        "DHI:"+obj.getJSONObject("DHI").getString("value")+" "+obj.getJSONObject("DHI").getString("units")+"<br>"+
                                        "DNI:"+obj.getJSONObject("DNI").getString("value")+" "+obj.getJSONObject("DNI").getString("units")+"<br>";


                                marker.setSnippet(snip);
                                marker.showInfoWindow();
                            }
                            else
                            {
                                marker.setSnippet("");
                            }
                            marker.setTitle("Poweredby NIWE");


                        }
                        catch(JSONException e)
                        {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //mTextView.setText("That didn't work!");
                Log.d(TAG, "fuckedup myboy");
                String body;
                //get status code here
                String statusCode = String.valueOf(error.networkResponse.statusCode);
                //get response body and parse with appropriate encoding
                if(error.networkResponse.data!=null) {
                    try {
                        body = new String(error.networkResponse.data,"UTF-8");
                        Log.d(TAG, body);

                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        /**
         * @return the last know best location
         */


        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            // Use default InfoWindow frame
            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            // Defines the contents of the InfoWindow
            @Override
            public View getInfoContents(Marker arg0) {

                Log.d(TAG, "ohh"+arg0.getSnippet());
                View v = getLayoutInflater().inflate(R.layout.windowlayout, null);
                if(arg0.getSnippet()!=null&&arg0.getSnippet().contains("<br>"))
                {
                    String[] data=arg0.getSnippet().split("<br>");

                    // Getting view from the layout file info_window_layout


                    // Getting the position from the marker
                    LatLng latLng = arg0.getPosition();

                    // Getting reference to the TextView to set latitude

                    TextView titleV = (TextView) v.findViewById(R.id.tv_title);
                    titleV.setText("Powered by NIWE");

                    TextView aep = (TextView) v.findViewById(R.id.tv_aep);

                    // Getting reference to the TextView to set longitude
                    TextView cui = (TextView) v.findViewById(R.id.tv_cui);
                    TextView ghi = (TextView) v.findViewById(R.id.tv_ghi);
                    TextView dni = (TextView) v.findViewById(R.id.tv_dni);
                    TextView dhi = (TextView) v.findViewById(R.id.tv_dhi);

                    // Setting the latitude
                    cui.setText(data[0]);

                    // Setting the longitude
                    ghi.setText(data[1]);

                    dni.setText(data[2]);
                    dhi.setText(data[3]);
                    aep.setText(data[4]);
                }

                else
                {
                    TextView aep = (TextView) v.findViewById(R.id.tv_aep);

                    // Getting reference to the TextView to set longitude
                    TextView cui = (TextView) v.findViewById(R.id.tv_cui);
                    TextView ghi = (TextView) v.findViewById(R.id.tv_ghi);
                    TextView dni = (TextView) v.findViewById(R.id.tv_dni);
                    TextView dhi = (TextView) v.findViewById(R.id.tv_dhi);

                    // Setting the latitude
                    cui.setText("We got no data here!");

                    // Setting the longitude
                    ghi.setText("");

                    dni.setText("");
                    dhi.setText("");
                    aep.setText("");
                }



                // Returning the view containing InfoWindow contents
                return v;

            }
        });

        location=getLocation();

        double lat,lng;
        if(location!=null)
        {
            lat=location.getLatitude();
            lng=location.getLongitude();
        }
        else
        {
            lat=28.7041;
            lng=77.216721;
        }

        LatLng sydney = new LatLng(lat, lng);
        getData(sydney);
        //marker = new MarkerOptions().position(sydney).title("Marker in Sydney").draggable(true);
        marker = mMap.addMarker(new MarkerOptions().position(sydney).title("Marked Location").draggable(true));

        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                getData(latLng);
                marker.setPosition(latLng);
                Log.d(TAG, "latitude : "+ marker.getPosition().latitude);
                mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                moveToCurrentLocation(latLng);
                //marker.showInfoWindow();
            }
        });

        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {

            @Override
            public void onMarkerDragStart(Marker marker) {
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                Log.d(TAG, "latitude : "+ marker.getPosition().latitude);
                // marker.setSnippet(marker.getPosition().latitude.toString());
                mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));

            }

            @Override
            public void onMarkerDrag(Marker marker) {
            }

        });

    }
}
