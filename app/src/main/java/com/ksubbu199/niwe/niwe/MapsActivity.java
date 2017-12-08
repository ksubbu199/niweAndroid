package com.ksubbu199.niwe.niwe;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_SATELLITE;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,LocationListener {

    private static final int PERMISSION_ACCESS_FINE_LOCATION = 0,PERMISSION_ACCESS_COARSE_LOCATION = 1;

    /*
        Adjust these values as per requirement
     */


    private final double AEP_LIMIT = 1398320, CUF_LIMIT = 15.9705;

    private boolean isAreaGood(double aep, double cuf)
    {
        return aep >  AEP_LIMIT || cuf > CUF_LIMIT;
    }

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
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        TextView verdictV = findViewById(R.id.text_view_verdict);
        TextView addrV = findViewById(R.id.text_view_address);
        addrV.setText("Select a region");
        verdictV.setText("Status");
        verdictV.setTextColor(Color.BLUE);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            if (!doWeHaveLocationPerm()) {
                Log.d(TAG, "Asking fine permissions");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ACCESS_FINE_LOCATION);
            }


        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.getView().setBackgroundColor(Color.WHITE);

        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS).setCountry("IN")
                .build();

        autocompleteFragment.setFilter(typeFilter);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.

                setMap(place.getLatLng());
                Log.i(TAG, "Place: " + place.getName()+place.getLatLng());//get place details here
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status.toString());
            }
        });


        ImageView imageView = findViewById(R.id.gps_icon);
        View.OnClickListener clickListener = new View.OnClickListener() {
            public void onClick(View v) {
                    if(doWeHaveLocationPerm())
                    {
                        Location loc=getLocation();
                        setMap(new LatLng(loc.getLatitude(),loc.getLongitude()));
                    }
                    else
                    {
                        getLocPerm();
                    }
                }

        };
        imageView.setOnClickListener(clickListener);

        final ImageView imageViewT = findViewById(R.id.view_switch);
        View.OnClickListener clickListenerT = new View.OnClickListener() {
            public void onClick(View v) {
                if(mMap.getMapType()==GoogleMap.MAP_TYPE_NORMAL)
                {
                    mMap.setMapType(MAP_TYPE_SATELLITE);
                    //imageViewT.setBackgroundResource(R.drawable.normal);
                    imageViewT.setImageResource(R.drawable.normal);
                }
                else
                {
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    imageViewT.setImageResource(R.drawable.sat);
                }
                // Write your awesome code here
            }

        };
        imageViewT.setOnClickListener(clickListenerT);

    }

    private void getLocPerm()
    {
        Toast.makeText(this, "Please provide location access!", Toast.LENGTH_SHORT).show();
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ACCESS_FINE_LOCATION);
    }

    private void setMap(LatLng latLng){
        getData(latLng);
        String addr=getCompleteAddressString(latLng.latitude,latLng.longitude);
        Log.d(TAG, "addr:"+addr);
        marker.setPosition(latLng);
        Log.d(TAG, "latitude : "+ marker.getPosition().latitude);
        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        moveToCurrentLocation(latLng);
        TextView addrV = findViewById(R.id.text_view_address);
        if(addr.isEmpty())
            addrV.setText("OOPS!if this persists clear data of app");
        else
            addrV.setText(addr);
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
        mMap.animateCamera(CameraUpdateFactory.zoomTo(13), 2000, null);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_ACCESS_FINE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    location=getLocation();
                    if(location!=null)
                    {
                        LatLng latLng=new LatLng(location.getLatitude(),location.getLongitude());
                        setMap(latLng);
                    }

                } else {
                    Toast.makeText(this, "Need your location to continue!", Toast.LENGTH_SHORT).show();
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ACCESS_FINE_LOCATION);
                }

                break;

        }
    }


    private String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                    Log.w(TAG, "Got for this i:"+i);
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append(" ");
                }
                strAdd = strReturnedAddress.toString();
                Log.w(TAG, strReturnedAddress.toString());
            } else {
                Log.w(TAG, "No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.w(TAG, "Canont get Address!");
        }
        return strAdd;
    }

    private boolean doWeHaveLocationPerm(){
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

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
        if (!doWeHaveLocationPerm()) {
                return null;
        }
        if (isLocationEnabled(this)) {
            Criteria criteria = new Criteria();
            String bestProvider = String.valueOf(locationManager.getBestProvider(criteria, true));
            Location location = locationManager.getLastKnownLocation(bestProvider);
            if (location != null) {
                Log.e(TAG, "GPS is on");
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                moveToCurrentLocation(new LatLng(latitude,longitude));
                return location;
            }
            else{
                Log.e(TAG, "Else brah");
                locationManager.requestLocationUpdates(bestProvider, 1000, 0, this);
                return null;
            }
        }
        else
        {
            Log.e(TAG, "Else fucked up");
            Toast.makeText(this, "Please Turn-On GPS!", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private void getData(LatLng ll)
    {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="http://14.139.172.6:8080/getLatInfo?lat="+ll.latitude+"&long="+ll.longitude+"&area="+900;
        Log.d(TAG, url);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, response);
                        try{
                            JSONObject obj = new JSONObject(response);
                            if(obj.getString("error")!=null)
                            {
                                Log.d(TAG, obj.getString("CUF"));
                                String snip= obj.toString();
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
                Log.d(TAG, "API Server Error");
                marker.setSnippet("");
                return;

            }
        });
        queue.add(stringRequest);

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker arg0) {

                Log.d(TAG, "ohh"+arg0.getSnippet());
                View v = getLayoutInflater().inflate(R.layout.windowlayout, null);
                TextView titleV = v.findViewById(R.id.tv_title);
                titleV.setText("Powered by NIWE");

                TextView aep = v.findViewById(R.id.tv_aep);
                TextView cuf = v.findViewById(R.id.tv_cuf);
                TextView ghi = v.findViewById(R.id.tv_ghi);
                TextView dni = v.findViewById(R.id.tv_dni);
                TextView dhi = v.findViewById(R.id.tv_dhi);

                TextView verdictV = findViewById(R.id.text_view_verdict);

                if(arg0.getSnippet()!=null)
                {
                    try{
                        JSONObject obj = new JSONObject(arg0.getSnippet());
                        LatLng latLng = arg0.getPosition();
                        aep.setText("AEP:"+obj.getJSONObject("AEP").getString("value")+" "+obj.getJSONObject("AEP").getString("units"));
                        cuf.setText("CUF:"+obj.getJSONObject("CUF").getString("value")+" "+obj.getJSONObject("CUF").getString("units"));
                        ghi.setText("GHI:"+obj.getJSONObject("GHI").getString("value")+" "+obj.getJSONObject("GHI").getString("units"));
                        dhi.setText("DHI:"+obj.getJSONObject("DHI").getString("value")+" "+obj.getJSONObject("DHI").getString("units"));
                        dni.setText("DNI:"+obj.getJSONObject("DNI").getString("value")+" "+obj.getJSONObject("DNI").getString("units"));

                        if(isAreaGood(Double.parseDouble(obj.getJSONObject("AEP").getString("value")),Double.parseDouble(obj.getJSONObject("CUF").getString("value"))))
                        {
                            verdictV.setText("Good");
                            verdictV.setTextColor(Color.parseColor("#4CAF50"));
                        }
                        else
                        {
                            verdictV.setText("Bad");
                            verdictV.setTextColor(Color.RED);
                        }


                    }
                    catch (JSONException e)
                    {
                        verdictV.setText("Status");
                        verdictV.setTextColor(Color.BLUE);
                        e.printStackTrace();
                    }
                }
                else
                {
                    cuf.setText("We got no data here!");
                    ghi.setText("");
                    dni.setText("");
                    dhi.setText("");
                    aep.setText("");
                    verdictV.setText("Status");
                    verdictV.setTextColor(Color.BLUE);
                }
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

        LatLng ll = new LatLng(lat, lng);
        marker = mMap.addMarker(new MarkerOptions().position(ll).title("Marked Location").draggable(true));
        setMap(ll);
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                setMap(latLng);
            }
        });

        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {

            @Override
            public void onMarkerDragStart(Marker marker) {
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                Log.d(TAG, "latitude : "+ marker.getPosition().latitude);
                mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));

            }

            @Override
            public void onMarkerDrag(Marker marker) {
            }

        });

    }
}
