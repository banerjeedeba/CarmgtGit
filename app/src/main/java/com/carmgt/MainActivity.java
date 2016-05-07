package com.carmgt;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Toast;

import com.carmgt.com.carmgt.util.HttpUtils;
import com.carmgt.com.carmgt.util.TextDrawable;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity implements GoogleMap.OnMyLocationButtonClickListener, OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback, LocationListener {

    //private static final LatLng DAVAO = new LatLng(7.0722, 125.6131);
    /**
     * Request code for location permission request.
     *
     * @see #onRequestPermissionsResult(int, String[], int[])
     */
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    /**
     * Flag indicating whether a requested permission has been denied after returning in
     * {@link #onRequestPermissionsResult(int, String[], int[])}.
     */
    private boolean mPermissionDenied = false;

    Context context = MainActivity.this;
    FloatingActionButton fab;
    Button contactDriverBtn;


    public static final CameraPosition DAVAO =
            new CameraPosition.Builder().target(new LatLng(7.0722, 125.6131))
                    .zoom(15.5f)
                    .bearing(0)
                    .tilt(25)
                    .build();
    private GoogleMap map;
    private Animation fab_open,fab_close;
    private String name;
    private String mobile;
    BitmapDescriptor icon;

    private ArrayList<Marker> driverMarkers = new ArrayList<Marker>();
    private ScheduledExecutorService scheduleTaskExecutor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();

        name = intent.getStringExtra("name");
        mobile = intent.getStringExtra("mobile");

        SupportMapFragment mapFragment = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map));
        mapFragment.getMapAsync(this);



        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.INVISIBLE);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Contacting driver", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fab_close);

        contactDriverBtn =(Button) findViewById(R.id.button);
        contactDriverBtn.setVisibility(View.INVISIBLE);
        contactDriverBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Contacting driver", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        scheduleTaskExecutor= Executors.newScheduledThreadPool(5);

        // This schedule a task to run every 10 minutes:
        scheduleTaskExecutor.scheduleAtFixedRate(new Runnable() {
            public void run() {

                // If you need update UI, simply do this:
                runOnUiThread(new Runnable() {
                    public void run() {
                        // update your UI component here.
                        DrawMarkerTask markerTask = new DrawMarkerTask();
                        markerTask.execute((Void) null);

                    }
                });
            }
        }, 0, 5, TimeUnit.SECONDS);

        icon = BitmapDescriptorFactory.fromResource(R.drawable.marker);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    protected void onPause() {
        super.onPause();
        removeUser(name);
        if(scheduleTaskExecutor != null)
            scheduleTaskExecutor.shutdown();
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.map = googleMap;
        //if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        // TODO: Consider calling
        //    ActivityCompat#requestPermissions
        // here to request the missing permissions, and then overriding
        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
        //                                          int[] grantResults)
        // to handle the case where the user grants the permission. See the documentation
        // for ActivityCompat#requestPermissions for more details.
        //return;
        //}
        map.setOnMyLocationButtonClickListener(this);
        enableMyLocation();

        //map.addMarker(new MarkerOptions().position(new LatLng(7.0722, 125.6131)).title("Davao City"));

        map.setPadding(0,0,0,100);
        // We will provide our own zoom controls.
        UiSettings uiSettings = map.getUiSettings();
        uiSettings.setZoomControlsEnabled(false);
        uiSettings.setCompassEnabled(true);
        uiSettings.setMyLocationButtonEnabled(true);

        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {

            @Override
            public boolean onMarkerClick(Marker marker) {
                String title=marker.getTitle();
                String name = title.substring(0, title.indexOf("-") - 1);
                String number = title.substring(title.indexOf("-")+2);
                String btnText = name+"<br/><i>"+number+"</i>";
                //fab.setImageDrawable(new TextDrawable(fab.getContext(), title, ColorStateList.valueOf(Color.BLACK), 32.f, TextDrawable.VerticalAlignment.BASELINE));
                //if(fab.getVisibility() == View.INVISIBLE){
                contactDriverBtn.setText(Html.fromHtml(btnText));
                contactDriverBtn.startAnimation(fab_close);
                //}
                contactDriverBtn.startAnimation(fab_open);
                return true;
            }

        });

        // zoom in the camera to Davao city
        //map.moveCamera(CameraUpdateFactory.newCameraPosition(DAVAO));

        // animate the zoom process
        //map.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_COARSE_LOCATION, true);
        } else if (map != null) {
            // Access to the location has been granted to the app.
            map.setMyLocationEnabled(true);
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            String bestProvider = locationManager.getBestProvider(criteria, true);

            Location location = locationManager.getLastKnownLocation(bestProvider);
            if (location != null) {
                onLocationChanged(location);
                map.animateCamera(CameraUpdateFactory.zoomTo(12));
            }

            locationManager.requestLocationUpdates(bestProvider, 20000, 0, this);
        }
    }

    @Override
    public void onLocationChanged(Location location) {

        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        LatLng latLng = new LatLng(latitude, longitude);
        //map.addMarker(new MarkerOptions().position(latLng));
        map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        //map.animateCamera(CameraUpdateFactory.zoomTo(15));

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
    public boolean onMyLocationButtonClick() {
        //Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }

    public class DrawMarkerTask extends AsyncTask<Void, Void, Boolean> {


        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.





            // TODO: register the new account here.
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            RequestParams rp = new RequestParams();
            HttpUtils.get("drivers_list", rp, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    // If the response is JSONObject instead of expected JSONArray
                    Log.d("logcarmgt suc object", "---------------- this is response : " + response);
                    try {

                        JSONObject serverResp = new JSONObject(response.toString());


                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONArray timeline) {
                    // Pull out the first event on the public timeline
                    try {
                        //JSONArray slideContent = (JSONArray) jsonObject.get("presentationSlides");
                        ArrayList<Marker> driverMarkersTemp = new ArrayList<Marker>();
                        for(int i=0; i<timeline.length();i++) {
                            Log.d("logcarmgt success array", "for "+i);
                            JSONObject driverLocation = timeline.getJSONObject(i);
                            String name = driverLocation.getString("name");
                            String mobileNo = driverLocation.getString("mobileNo");
                            String longitudeString = driverLocation.getString("longitude");
                            String latitudeString = driverLocation.getString("latitude");
                            double longitude = 0.0;
                            double latitude = 0.0;
                            if(longitudeString!=null){
                                longitude = Double.parseDouble(longitudeString);
                            }
                            if(latitudeString!=null){
                                latitude = Double.parseDouble(latitudeString);
                            }
                            Log.d("logcarmgt success array", "name "+name);
                            boolean addMarker = true;
                            if(name !=null && mobileNo !=null && longitude!=0.0 && latitude!=0.0)
                            {
                                Marker marker =null;
                                for(Marker driver : driverMarkers)
                                {
                                    Log.d("logcarmgt success array", "drivers list ");

                                    if(driver.getTitle().equalsIgnoreCase(name+" - "+mobileNo)){
                                        if(driver.getPosition().latitude != latitude
                                                && driver.getPosition().longitude != longitude) {
                                            Log.d("logcarmgt success array", "already exist ");
                                            driver.remove();

                                        } else {
                                            addMarker = false;
                                            marker = driver;
                                        }

                                        driverMarkers.remove(driver);
                                        break;
                                    }

                                }

                                if(addMarker) {
                                    Log.d("logcarmgt success array", "marker ");
                                    marker = map.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title(name + " - " + mobileNo).icon(icon));
                                }
                                driverMarkersTemp.add(marker);
                            }

                        }
                        for(Marker deletedDrivers : driverMarkers)
                        {

                            if(contactDriverBtn!=null && (contactDriverBtn.getText().toString()).contains(deletedDrivers.getTitle().substring(0,(deletedDrivers.getTitle().indexOf("-")-1))))
                            {
                                contactDriverBtn.startAnimation(fab_close);
                            }
                            deletedDrivers.remove();
                            driverMarkers.remove(deletedDrivers);
                        }
                        driverMarkers=driverMarkersTemp;

                        Log.d("logcarmgt success array", "---------------- this is response : " + timeline.length() + " " +timeline );
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    super.onFailure(statusCode, headers, responseString, throwable);
                    Log.d("logcarmgt failure", "---------------- this is response : " + responseString + throwable.getMessage());
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                    Log.d("logcarmgt fa json obt", "---------------- this is response : " + throwable.getMessage());
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                    Log.d("logcarmgt fa json array", "---------------- this is response : "  + throwable.getMessage());
                }
            });

        }


    }

    public class RemoveUser extends AsyncTask<Void, Void, Boolean> {

        String userName;

        RemoveUser(String userName)
        {
            this.userName = userName;
        }

        @Override
        protected Boolean doInBackground(Void... params) {


            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {

        }


    }

    private void removeUser(String userName)
    {
        HttpUtils.get("remove_customer?name="+userName, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                Log.d("logcarmgt ru suc object", "---------------- this is response : " + response);
                try {

                    JSONObject serverResp = new JSONObject(response.toString());


                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray timeline) {
                // Pull out the first event on the public timeline

                Log.d("logcarmgt ru suc array", "---------------- this is response : " +timeline );


            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                Log.d("logcarmgt ru failure", "---------------- this is response : " + responseString + throwable.getMessage());
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                Log.d("logcarmgt ru fa obt", "---------------- this is response : " + errorResponse.toString() + throwable.getMessage());
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                Log.d("logcarmgt ru fa array", "---------------- this is response : " + errorResponse.toString() + throwable.getMessage());
            }
        });
    }
}
