package race.main.com.deadrace;

import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.List;

public class MapsActivity extends FragmentActivity implements LocationListener, GoogleMap.OnMarkerClickListener {

    // Might be null if Google Play services APK is not available.
    private GoogleMap mMap;
    private Circle mCircle;

    // flag for GPS status
    boolean isGPSEnabled = false;

    // flag for network status
    boolean isNetworkEnabled = false;

    boolean canGetLocation = false;

    Location location; // location
    double latitude; // latitude
    double longitude; // longitude

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 20; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 3 * 1; // 1 minute

    // Declaring a Location Manager
    protected LocationManager locationManager;

    User user = new User(MapsActivity.this);
    ParseObject locationObject = new ParseObject("Location");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        setUpMapIfNeeded();
        getLocation();
        getEnemy();
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                //add start and finish point to race
            }
        });
        mMap.setOnMarkerClickListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    @Override
    protected void onStop() {
        super.onStop();
        clearRecord();
        stopUsingGPS();
    }

    private void getEnemy(){
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Location");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if (e == null) for (int i = 0; i < list.size(); i++) {
                    double latitude = (double) list.get(i).getNumber("Latitude");
                    double longitude = (double) list.get(i).getNumber("Longitude");
                    String username = (String) list.get(i).getString("username");

                    LatLng latLng = new LatLng(latitude, longitude);

                    double distance = calculateDistance(latitude, longitude);

                    if (distance < 160 && !username.equals(user.getUsername())) {
                        drawMarker(latLng, username);
                    }

                }
            }
        });
    }

    public void clearRecord(){
        ParseQuery<ParseObject> query=ParseQuery.getQuery("Location");
        query.whereEqualTo("username",user.getUsername());
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if(e==null) {

                    for (ParseObject delete : parseObjects) {
                        delete.deleteInBackground();
                        Toast.makeText(getApplicationContext(), "deleted", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(getApplicationContext(), "error in deleting", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void stopUsingGPS(){
        if(locationManager != null){
            try {
                locationManager.removeUpdates(MapsActivity.this);
                locationManager = null;
            }
            catch (Exception e){
                e.getMessage();
            }
        }
    }

    private double calculateDistance (double latitude,double longitude){

        Location playerLocation = new Location("LocationDifference");
        playerLocation.setLatitude(location.getLatitude());
        playerLocation.setLongitude(location.getLongitude());
        Location enemyLocation = new Location("LocationDifference");
        enemyLocation.setLatitude(latitude);
        enemyLocation.setLongitude(longitude);
        double distance=playerLocation.distanceTo(enemyLocation);
        return distance;
    }

    private void drawMarker (LatLng latLng, String who){
        if (who == user.getUsername()) {
            double radiusInMeters = 20;
            CircleOptions circleOptions = new CircleOptions().center(latLng).radius(radiusInMeters).fillColor(Color.TRANSPARENT).strokeColor(Color.BLUE).strokeWidth(2);
            mCircle = mMap.addCircle(circleOptions);
            mMap.addMarker(new MarkerOptions().position(latLng).title(user.getUsername()));
        }
        else {
            mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title(who)
                    .snippet("BMW")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.arrow)));
        }
    }

    public Location getLocation() {
        try {
            locationManager = (LocationManager) this
                    .getSystemService(LOCATION_SERVICE);

            // getting GPS status
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
                Toast.makeText(MapsActivity.this,"Error In your network provider try again later",Toast.LENGTH_LONG).show();
            } else {
                this.canGetLocation = true;
                // First get location from Network Provider
                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                    Log.d("Network", "Network");
                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Log.d("GPS Enabled", "GPS Enabled");
                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return location;
    }

    public void changeUi(LatLng latLng){
        mMap.clear();
        getEnemy();
       // createEnemy();

        drawMarker(latLng, user.getUsername());
        // Zoom in, animating the camera.
        mMap.animateCamera(CameraUpdateFactory.zoomIn());

        // Zoom out to zoom level 10, animating with a duration of 2 seconds.
        mMap.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);

        // Construct a CameraPosition focusing on Mountain View and animate the camera to that position.
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)      // Sets the center of the map to Mountain View
                .zoom(20)                   // Sets the zoom
                .bearing(90)                // Sets the orientation of the camera to east
                .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                getLocation();
            }
        }
    }



    @Override
    public void onLocationChanged( Location location) {
        Toast.makeText(MapsActivity.this,"location changed",Toast.LENGTH_LONG).show();

        final double latitude = location.getLatitude();
        final double longitude = location.getLongitude();
        final String currentUsername = user.getUsername();
        LatLng latLng = new LatLng(latitude,longitude);

        if (user != null) {

            locationObject.put("Latitude", latitude);
            locationObject.put("Longitude", longitude);
            locationObject.put("username", currentUsername);

            locationObject.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        Toast.makeText(MapsActivity.this, "Success! update your location", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(MapsActivity.this, "cant update your location please check your network", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
        changeUi(latLng);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        String title = marker.getTitle();
        if (user.getUsername().equals(title)){
            return false;
        }
        else {
            user.showDialogWithPic(title);
            return true;
        }
    }
}
