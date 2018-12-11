package com.example.kishan.locations;


import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatTextView;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener
{
    String locationName;
    Button checkIn;
    public static double longitude;
    public static double latitude;
    TextView coordinates;
    TextView address;
    List<Address> addresses;
    public static Geocoder geocoder;
    public static LocationDatabase mainLocDb;
    //LocRelationDatabase mainRelDb;
    ImageView infoIcon;
    ImageView mapIcon;


    public static GoogleApiClient googleApiClient;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private LocationRequest locationRequest;
    public static long UPDATE_INTERVAL = 1000;
    //private long FASTEST_INTERVAL = 5000;

    private ArrayList<String> permissionsToRequest;
    private ArrayList<String> permissionsRejected = new ArrayList<>();
    private ArrayList<String> permissions = new ArrayList<>();

    private static final int ALL_PERMISSIONS_RESULT = 1011;

    public static boolean killBackgroundThread = false;
    public static ExecutorService service;

    private final int ERROR_DIALOG_REQUEST = 9001;
    private static ExecutorService autoCheckIn;
    private boolean autoCheckInFlag = false;
    private Switch autoCheckInSwitch;


    @Override
    public void onStop() {
        super.onStop();
        //killBackgroundThread = true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle("Locator");
        centerTitle();

        mainLocDb = new LocationDatabase(this);
        //mainRelDb = new LocRelationDatabase(this);
        coordinates = findViewById(R.id.main_coordinates);
        address = findViewById(R.id.main_address);
        checkIn = findViewById(R.id.main_checkin);
        infoIcon = findViewById(R.id.main_infoicon);
        mapIcon = findViewById(R.id.main_mapicon);
        autoCheckInSwitch = findViewById(R.id.main_checkinswitch);


        geocoder = new Geocoder(this, Locale.getDefault());

        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);

        permissionsToRequest = permissionsToRequest(permissions);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (permissionsToRequest.size() > 0) {
                requestPermissions(permissionsToRequest.toArray(new String[permissions.size()]), ALL_PERMISSIONS_RESULT);
            }
        }

        googleApiClient = new GoogleApiClient.Builder(this).
                addApi(LocationServices.API).
                addConnectionCallbacks(this).
                addOnConnectionFailedListener(this).build();

        getCheckIn();
        viewLocationsList();
        goToGoogleMaps();
        toggleAutoCheckIn();
    }

    private void addRutgersLocations()
    {
        if(!checkInDatabase("Busch Student Center", "40.523411, -74.458717", "604 Bartholomew Rd, Piscataway Township, NJ 08854, USA"))
        {
            mainLocDb.addData("Busch Student Center", Calendar.getInstance().getTime().toString(),
                    "40.523411, -74.458717", "604 Bartholomew Rd, Piscataway Township, NJ 08854, USA");
        }
        if(!checkInDatabase("Sonny Werblin Recreation Center", "40.520390, -74.457542", "656 Bartholomew Rd, Piscataway Township, NJ 08854, USA"))
        {
            mainLocDb.addData("Sonny Werblin Recreation Center", Calendar.getInstance().getTime().toString(),
                    "40.520390, -74.457542", "656 Bartholomew Rd, Piscataway Township, NJ 08854, USA");
        }
        if(!checkInDatabase("Buell Apartments", "40.522360, -74.456010", "55 Bevier Rd, Piscataway Township, NJ 08854, USA"))
        {
            mainLocDb.addData("Buell Apartments", Calendar.getInstance().getTime().toString(),
                    "40.522360, -74.456010", "55 Bevier Rd, Piscataway Township, NJ 08854, USA");
        }
        if(!checkInDatabase("Library of Science and Medicine", "40.525810, -74.465050", "165 Bevier Rd, Piscataway Township, NJ 08854, USA"))
        {
            mainLocDb.addData("Library of Science and Medicine", Calendar.getInstance().getTime().toString(),
                    "40.525810, -74.465050", "165 Bevier Rd, Piscataway Township, NJ 08854, USA");
        }
        if(!checkInDatabase("Busch Engineering Science and Technology Hall", "40.522180, -74.455920", "50 Bevier Rd, Piscataway Township, NJ 08854, USA"))
        {
            mainLocDb.addData("Busch Engineering Science and Technology Hall", Calendar.getInstance().getTime().toString(),
                    "40.522180, -74.455920", "50 Bevier Rd, Piscataway Township, NJ 08854, USA");
        }
    }

    private boolean checkInDatabase(String name, String coords, String address)
    {
        Cursor res = mainLocDb.getAll();
        if(res != null && res.getCount() != 0)
        {
            while(res.moveToNext())
            {
                if(res.getString(1).equals(name)
                        && res.getString(3).equals(coords)
                        && res.getString(4).equals(address))
                {
                    return true;
                }
            }
        }
        return false;
    }

    private void toggleAutoCheckIn()
    {
        autoCheckInSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            autoCheckInFlag = !autoCheckInFlag;
            if(autoCheckInFlag)
            {
                autoCheckIn = Executors.newFixedThreadPool(5);
                autoCheckIn.execute(() -> autoCheckIn());
            }
            else
            {
                autoCheckIn.shutdownNow();
            }
        });
    }

    public boolean isServiceOK()
    {
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);

        if(available == ConnectionResult.SUCCESS)
        {
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available))
        {
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }
        else
        {
            Toast.makeText(this, "You can't make map requests!", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private ArrayList<String> permissionsToRequest(ArrayList<String> wantedPermissions) {
        ArrayList<String> result = new ArrayList<>();

        for (String perm : wantedPermissions) {
            if (!hasPermission(perm)) {
                result.add(perm);
            }
        }

        return result;
    }

    private boolean hasPermission(String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
        }

        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();

        addRutgersLocations();
        killBackgroundThread = false;
        if (googleApiClient != null) {
            googleApiClient.connect();
            //Toast.makeText(this, "Connected to google api client!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!checkPlayServices()) {
            coordinates.setText("Install Google Play Services!");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (googleApiClient != null && googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            googleApiClient.disconnect();
            //Toast.makeText(this, "Disconnected from google api client!", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);

        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                Toast.makeText(this, "Google play services unavailable!", Toast.LENGTH_SHORT).show();
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST);
            } else {
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        //Toast.makeText(this, "Getting last location!", Toast.LENGTH_SHORT).show();
        Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        updateAddress();

        if(service == null)
        {
            killBackgroundThread = false;
            service = Executors.newFixedThreadPool(5);
            service.execute(() -> getLastKnownLocation());
        }

        if (location != null)
        {
            coordinates.setText(location.getLongitude() + ", " + location.getLatitude());
            updateAddress();
        }
        /*
        else {
            Toast.makeText(this, "Location is null!", Toast.LENGTH_SHORT).show();

            if(service == null)
            {
                killBackgroundThread = false;
                service = Executors.newFixedThreadPool(1);
                service.execute(() -> getLastKnownLocation());
            }
        }*/

        startLocationUpdates();
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

            final Location location = bestLocation;
            runOnUiThread(() -> {
                //Toast.makeText(this, "Location Updated!", Toast.LENGTH_SHORT).show();
                longitude = Math.round(location.getLongitude() * 1000000.0) / 1000000.0;
                latitude = Math.round(location.getLatitude() * 1000000.0) / 1000000.0;
                coordinates.setText(longitude + ", " + latitude);
                updateAddress();
            });


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

    private void startLocationUpdates()
    {
        //Toast.makeText(this, "Starting Location Updator!", Toast.LENGTH_SHORT).show();
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        //locationRequest.setFastestInterval(FASTEST_INTERVAL);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            Toast.makeText(this, "Location permission unavailable!", Toast.LENGTH_SHORT).show();
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, new LocationCallback()
        {
            @Override
            public void onLocationResult(LocationResult result)
            {
                //DebugUtils.log("onLocationResult");
                Location location = result.getLastLocation();
                if(location != null)
                {
                    longitude = Math.round(location.getLongitude() * 1000000.0) / 1000000.0;
                    latitude = Math.round(location.getLatitude() * 1000000.0) / 1000000.0;
                    coordinates.setText(longitude + ", " + latitude);
                    updateAddress();
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

    @Override
    public void onConnectionSuspended(int i)
    {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
    {

    }

    @Override
    public void onLocationChanged(Location location)
    {
        if(location != null)
        {
            killBackgroundThread = true;
            longitude = Math.round(location.getLongitude() * 1000000.0) / 1000000.0;
            latitude = Math.round(location.getLatitude() * 1000000.0) / 1000000.0;
            coordinates.setText(longitude + ", " + latitude);
            updateAddress();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        switch (requestCode)
        {
            case ALL_PERMISSIONS_RESULT:
                for(String perm : permissionsToRequest)
                {
                    if(!hasPermission(perm))
                    {
                        permissionsRejected.add(perm);
                    }
                }

                if(permissionsRejected.size() > 0)
                {
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    {
                        if(shouldShowRequestPermissionRationale(permissionsRejected.get(0)))
                        {
                            new AlertDialog.Builder(MainActivity.this).
                                    setMessage("Allow these permissions for location access!").
                                    setPositiveButton("OK", (dialog, which) -> {
                                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                                        {
                                            requestPermissions(permissionsRejected.toArray(new String[permissionsRejected.size()]),
                                                    ALL_PERMISSIONS_RESULT);
                                            restart();
                                        }
                                    }).
                                    setNegativeButton("Cancel", ((dialog, which) ->
                                            finishAndRemoveTask())).create().show();
                            //Toast.makeText(MainActivity.this, "failed!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                }
                else
                {
                    if(googleApiClient != null)
                    {
                        googleApiClient.connect();
                        restart();
                    }
                }
                break;
        }
    }

    public void viewLocationsList()
    {
        infoIcon.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, InfoList.class);
            startActivity(i);
        });
    }

    public void goToGoogleMaps()
    {
        mapIcon.setOnClickListener(v ->
        {
            if(isServiceOK())
            {
                Intent i = new Intent(MainActivity.this, MapActivity.class);
                startActivity(i);
            }
        });
    }

    public void updateAddress()
    {
        try
        {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if(addresses != null && addresses.size() != 0)
            {
                String add = addresses.get(0).getAddressLine(0);
                address.setText(add);
                //Toast.makeText(this, add, Toast.LENGTH_SHORT).show();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void getCheckIn()
    {
        checkIn.setOnClickListener(v -> getName());
    }

    public void getName()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Location Name");
        final EditText builderInput = new EditText(this);
        builderInput.setText(null);
        builderInput.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(builderInput);

        builder.setPositiveButton("Submit", (dialog, which) ->
        {
            Cursor res = mainLocDb.getAll();
            if(res != null && res.getCount() != 0)
            {
                while(res.moveToNext())
                {
                    String[] coords = res.getString(3).split(", ");
                    double lonA = Double.parseDouble(coords[0]);
                    double latA = Double.parseDouble(coords[1]);

                    float[] retval = new float[1];
                    Location.distanceBetween(latA, lonA, latitude, longitude, retval );
                    //Toast.makeText(this, retval[0] + "", Toast.LENGTH_SHORT).show();

                    if(retval[0] <= 30.0)
                    {
                        Toast.makeText(this, "Check-in location associated with existing check-in location", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }

            locationName = builderInput.getText().toString();
            mainLocDb.addData(locationName, Calendar.getInstance().getTime().toString(), longitude + ", " + latitude, address.getText().toString());
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }


    public void centerTitle()
    {
        ArrayList<View> textViews = new ArrayList<>();
        getWindow().getDecorView().findViewsWithText(textViews, getTitle(), View.FIND_VIEWS_WITH_TEXT);

        if(textViews.size() > 0)
        {
            AppCompatTextView appCompatTextView = null;
            if(textViews.size() == 1)
            {
                appCompatTextView = (AppCompatTextView) textViews.get(0);
            }
            else
            {
                for(View v : textViews)
                {
                    if(v.getParent() instanceof Toolbar)
                    {
                        appCompatTextView = (AppCompatTextView) v;
                        break;
                    }
                }
            }

            if(appCompatTextView != null)
            {
                ViewGroup.LayoutParams params = appCompatTextView.getLayoutParams();
                params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                appCompatTextView.setLayoutParams(params);
                appCompatTextView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            }
        }
    }

    private void restart()
    {
        Intent i = getBaseContext().getPackageManager()
                .getLaunchIntentForPackage( getBaseContext().getPackageName() );
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
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

    private void autoCheckIn()
    {
        while(autoCheckInFlag)
        {
            final long startTime = System.currentTimeMillis();
            double currlong = longitude;
            double currlat = latitude;

            while(autoCheckInFlag && System.currentTimeMillis() - startTime < 1000*60*5)
            {
                float[] retval = new float[1];
                Location.distanceBetween(currlat, currlong, latitude, longitude, retval);
                if(retval[0] > 100.0)
                {
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            float[] retval = new float[1];
            Location.distanceBetween(currlat, currlong, latitude, longitude, retval);
            //Toast.makeText(this, retval[0] + "", Toast.LENGTH_SHORT).show();

            if(retval[0] <= 30.0)
            {
                Toast.makeText(this, "Check-in location associated with existing check-in location", Toast.LENGTH_SHORT).show();
                continue;
            }
            mainLocDb.addData("", Calendar.getInstance().getTime().toString(),
                    longitude + ", " + latitude, getAddress(new LatLng(latitude, longitude)));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        killBackgroundThread = true;
        if(service != null)
        {
            service.shutdownNow();
        }
        autoCheckInFlag = false;
        if(autoCheckIn != null)
        {
            autoCheckIn.shutdownNow();
        }
    }
}
