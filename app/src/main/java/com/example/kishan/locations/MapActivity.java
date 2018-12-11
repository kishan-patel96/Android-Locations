package com.example.kishan.locations;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Location;
import android.location.LocationManager;
import android.os.Looper;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.example.kishan.locations.MainActivity.googleApiClient;
import static com.example.kishan.locations.MainActivity.latitude;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "MapActivity";

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;

    private GoogleMap mMap;
    private Boolean mLocationPermissionsGranted = false;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private static ExecutorService mapService;
    private MarkerOptions markerOptions;
    private boolean killMapService = false;
    private Marker marker;
    private ImageView locator;
    private boolean killBackgroundThread = false;
    private static ExecutorService service;
    private Map<Marker, Integer> markerToId;
    private Map<Integer, Marker> idToMarker;
    private ExecutorService checkWithinDist;



    static boolean active = false;

    @Override
    public void onStart() {
        super.onStart();
        active = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        active = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        locator = findViewById(R.id.map_locatoricon);
        idToMarker = new HashMap<>();

        //Toast.makeText(this, "Starting Location Updator!", Toast.LENGTH_SHORT).show();
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(MainActivity.UPDATE_INTERVAL);
        //locationRequest.setFastestInterval(FASTEST_INTERVAL);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            Toast.makeText(this, "Location permission unavailable!", Toast.LENGTH_SHORT).show();
        }

        getLocationPermission();
        centerCurrentLocation();

        googleApiClient.connect();
        if(googleApiClient.isConnected())
        {
            LocationServices.FusedLocationApi.requestLocationUpdates(MainActivity.googleApiClient, locationRequest, new LocationCallback()
            {
                @Override
                public void onLocationResult(LocationResult result)
                {
                    //DebugUtils.log("onLocationResult");
                    Location location = result.getLastLocation();
                    if(location != null)
                    {
                        killBackgroundThread = true;
                        MainActivity.longitude = Math.round(location.getLongitude() * 1000000.0) / 1000000.0;
                        latitude = Math.round(location.getLatitude() * 1000000.0) / 1000000.0;
                    }
                }

                @Override
                public void onLocationAvailability(LocationAvailability locationAvailability)
                {
                    //DebugUtils.log("onLocationAvailability: isLocationAvailable =  " + locationAvailability.isLocationAvailable());
                    //Toast.makeText(MainActivity.this, "onLocationAvailability: isLocationAvailable =  " + locationAvailability.isLocationAvailable(), Toast.LENGTH_SHORT).show();

                    if(service == null)
                    {
                        killBackgroundThread = false;
                        service = Executors.newFixedThreadPool(5);
                        service.execute(() -> getLastKnownLocation());
                    }
                }
            }, null);
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(MapActivity.this));
        moveCamera(new LatLng(latitude, MainActivity.longitude), DEFAULT_ZOOM);
        showAllCheckIns();
        addMarkerOnClick();

        if(service == null)
        {
            killBackgroundThread = false;
            service = Executors.newFixedThreadPool(5);
            service.execute(() -> getLastKnownLocation());
        }

        if(checkWithinDist == null)
        {
            checkWithinDist = Executors.newFixedThreadPool(5);
            checkWithinDist.execute(() -> checkWithinDistance());
        }

        /*// Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/
        /*try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/

        if (mLocationPermissionsGranted) {
            mapService = Executors.newFixedThreadPool(5);
            mapService.execute(() -> getDeviceLocation());

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                Toast.makeText(this, "Need location permissions!", Toast.LENGTH_SHORT).show();
                return;
            }
            //mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setCompassEnabled(true);
            mMap.getUiSettings().setZoomControlsEnabled(true);
            //mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.getUiSettings().setCompassEnabled(true);
        }
    }

    private void checkWithinDistance()
    {
        while(active)
        {
            Cursor res = MainActivity.mainLocDb.getAll();

            while(res.moveToNext())
            {
                Marker m = idToMarker.get(Integer.parseInt(res.getString(0)));

                if(m != null)
                {
                    float[] retval = new float[1];
                    runOnUiThread(() -> {
                        Location.distanceBetween(m.getPosition().latitude, m.getPosition().longitude, MainActivity.latitude, MainActivity.longitude, retval);

                        if(retval[0] <= 30.0 && !m.isInfoWindowShown() && !m.getTitle().equals(""))
                        {
                            m.showInfoWindow();
                        }
                        else if(retval[0] > 30.0 && m.isInfoWindowShown())
                        {
                            m.hideInfoWindow();
                        }
                    });
                }
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void addMarkerOnClick()
    {
        if(markerToId == null)
        {
            markerToId = new HashMap<>();
        }
        mMap.setOnMapClickListener(latLng -> {

            double longitude = Math.round(latLng.longitude * 1000000.0) / 1000000.0;
            double latitude = Math.round(latLng.latitude * 1000000.0) / 1000000.0;

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Marker Name");
            final EditText builderInput = new EditText(this);
            builderInput.setText(null);
            builderInput.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(builderInput);

            builder.setPositiveButton("Submit", (dialog, which) ->
            {
                Marker m = mMap.addMarker(new MarkerOptions().position(latLng).title(builderInput.getText().toString()).draggable(true));
                MainActivity.mainLocDb.addData(builderInput.getText().toString(), Calendar.getInstance().getTime().toString(),
                        longitude + ", " + latitude, getAddress(latLng));

                Cursor res = MainActivity.mainLocDb.getId(builderInput.getText().toString(), longitude + ", " + latitude);
                if(res != null && res.getCount() != 0)
                {
                    res.moveToNext();
                    markerToId.put(m, Integer.parseInt(res.getString(0)));
                    idToMarker.put(Integer.parseInt(res.getString(0)), m);
                    //Toast.makeText(MapActivity.this, res.getString(0) + "", Toast.LENGTH_SHORT).show();
                }

            });
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
            builder.show();
        });

        mMap.setOnMarkerClickListener(marker -> {
            if(marker.isInfoWindowShown())
            {
                marker.hideInfoWindow();
            }
            else
            {
                marker.showInfoWindow();
            }
            return true;
        });

        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {

            @Override
            public void onMarkerDragStart(Marker marker) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                // TODO Auto-generated method stub

                double longitude = Math.round(marker.getPosition().longitude * 1000000.0) / 1000000.0;
                double latitude = Math.round(marker.getPosition().latitude * 1000000.0) / 1000000.0;

                AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
                builder.setTitle("Marker Name");
                final EditText builderInput = new EditText(MapActivity.this);
                builderInput.setText(null);
                builderInput.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(builderInput);

                builder.setPositiveButton("Submit", (dialog, which) ->
                {
                    marker.setTitle(builderInput.getText().toString());
                    //Toast.makeText(MapActivity.this, markerToId.get(marker) + "", Toast.LENGTH_SHORT).show();
                    MainActivity.mainLocDb.updateMarkerData(markerToId.get(marker), builderInput.getText().toString(), Calendar.getInstance().getTime().toString(),
                            longitude + ", " + latitude, getAddress(new LatLng(latitude, longitude)));

                });
                builder.setNegativeButton("Cancel", (dialog, which) -> {
                    MainActivity.mainLocDb.updateMarkerData(markerToId.get(marker), marker.getTitle(), Calendar.getInstance().getTime().toString(),
                            longitude + ", " + latitude, getAddress(new LatLng(latitude, longitude)));
                    dialog.cancel();
                });
                builder.show();
            }

            @Override
            public void onMarkerDrag(Marker marker) {
                // TODO Auto-generated method stub

            }
        });
    }

    public String getAddress(LatLng l)
    {
        try
        {
            List<Address> addresses = MainActivity.geocoder.getFromLocation(l.latitude, l.longitude, 1);
            if(addresses != null && addresses.size() != 0)
            {
                return addresses.get(0).getAddressLine(0);
                //Toast.makeText(this, add, Toast.LENGTH_SHORT).show();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    private void centerCurrentLocation()
    {
        locator.setOnClickListener(v -> {
            moveCamera(new LatLng(latitude, MainActivity.longitude), DEFAULT_ZOOM);
        });
    }

    private void showAllCheckIns()
    {
        Cursor res = MainActivity.mainLocDb.getAll();
        if(res != null && res.getCount() != 0)
        {
            while (res.moveToNext())
            {
                String name = res.getString(1);
                String[] coords = res.getString(3).split(", ");
                Marker m = mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(coords[1]),
                        Double.parseDouble(coords[0]))).title(name));
                idToMarker.put(Integer.parseInt(res.getString(0)), m);
            }
        }

    }

    private void getDeviceLocation() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Toast.makeText(this, "Need location permissions!", Toast.LENGTH_SHORT).show();
            return;
        }

        killMapService = false;
        while (!killMapService) {
            Task location = fusedLocationProviderClient.getLastLocation();

            if (location != null)
            {
                //Toast.makeText(this, "Location is NOT null!", Toast.LENGTH_SHORT).show();
                location.addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        //Toast.makeText(this, location.getResult().toString(), Toast.LENGTH_SHORT).show();
                        Location currentLocation = (Location) task.getResult();

                        if (currentLocation != null) {
                            killBackgroundThread = true;
                            //moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM);
                        }
                        else
                        {
                            //moveCamera(new LatLng(MainActivity.latitude, MainActivity.longitude), DEFAULT_ZOOM);
                        }
                    }
                    else
                    {
                        //Toast.makeText(this, "Location is null!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            else
            {
                //Toast.makeText(this, "Location is null!", Toast.LENGTH_SHORT).show();
            }

            runOnUiThread(() -> {
                currLocMarker();
            });
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void currLocMarker()
    {
        if(markerOptions == null)
        {
            markerOptions = new MarkerOptions();
            markerOptions.title("Current Location");
            markerOptions.snippet("");
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
            markerOptions.position(new LatLng(MainActivity.latitude, MainActivity.longitude));
            //markerOptions.alpha(0.5f);
            marker = mMap.addMarker(markerOptions);
        }

        marker.setPosition(new LatLng(MainActivity.latitude, MainActivity.longitude));
    }

    /*private BitmapDescriptor bitmapDescriptorFromVector(Context context, @DrawableRes int vectorDrawableResourceId) {
        Drawable background = ContextCompat.getDrawable(context, R.drawable.ic_map_pin_filled_blue_48dp);
        background.setBounds(0, 0, background.getIntrinsicWidth(), background.getIntrinsicHeight());
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorDrawableResourceId);
        vectorDrawable.setBounds(40, 20, vectorDrawable.getIntrinsicWidth() + 40, vectorDrawable.getIntrinsicHeight() + 20);
        Bitmap bitmap = Bitmap.createBitmap(background.getIntrinsicWidth(), background.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        background.draw(canvas);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }*/

/*    private void getDeviceLocation2() {
        locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        List<String> providers = locationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            Location l = locationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }

        if(bestLocation != null)
        {
            moveCamera(new LatLng(bestLocation.getLatitude(), bestLocation.getLongitude()), DEFAULT_ZOOM);
        }
    }*/

    private void moveCamera(LatLng latLng, float zoom)
    {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    private void getLocationPermission(){
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                mLocationPermissionsGranted = true;
                initMap();
            }else{
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        }else{
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionsGranted = false;

        switch(requestCode){
            case LOCATION_PERMISSION_REQUEST_CODE:{
                if(grantResults.length > 0){
                    for(int i = 0; i < grantResults.length; i++){
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            mLocationPermissionsGranted = false;
                            return;
                        }
                    }
                    mLocationPermissionsGranted = true;
                    //initialize our map
                    initMap();
                }
            }
        }
    }

    private void initMap(){
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(MapActivity.this);
    }

    public void getLastKnownLocation()
    {
        while(!killBackgroundThread)
        {
            LocationManager mLocationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
            List<String> providers = mLocationManager.getProviders(true);
            Location bestLocation = null;
            for (String provider : providers) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                Location l = mLocationManager.getLastKnownLocation(provider);
                if (l == null) {
                    continue;
                }
                if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                    // Found best last known location: %s", l);
                    bestLocation = l;
                }
            }

            if(bestLocation == null)
            {
                continue;
            }

            runOnUiThread(() -> {
                //Toast.makeText(this, MainActivity.longitude + ", " + MainActivity.latitude, Toast.LENGTH_SHORT).show();
            });
            MainActivity.longitude = Math.round(bestLocation.getLongitude() * 1000000.0) / 1000000.0;
            latitude = Math.round(bestLocation.getLatitude() * 1000000.0) / 1000000.0;
            runOnUiThread(() -> currLocMarker());

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        runOnUiThread(() -> {
            //Toast.makeText(this, "Terminated background thread!", Toast.LENGTH_SHORT).show();
            service = null;
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MainActivity.killBackgroundThread = true;
        if(MainActivity.service != null)
        {
            MainActivity.service.shutdownNow();
            MainActivity.service = null;
        }
        killMapService = true;
        killBackgroundThread = true;
        service = null;
    }
}
