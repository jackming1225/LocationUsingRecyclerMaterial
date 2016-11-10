package com.example.ming.locationusingrecyclermaterial;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks
        , GoogleApiClient.OnConnectionFailedListener, LocationListener {

    //tabs work
    Toolbar toolbar;
    TabLayout tabLayout;
    ViewPager viewPager;
    ViewPagerAdapter viewPagerAdapter;
    //recyclerview work
    RecyclerView recyclerView, recyclerViewUserSet;
    RecyclerAdapter recyclerAdapter;
    RecyclerAdapterUserSet recyclerAdapterUserSet;
    RecyclerView.LayoutManager layoutManager;
    ArrayList<Market> dataList = new ArrayList<>();
    ArrayList<Market> dataList1 = new ArrayList<>();


    private static final String name = "name";
    private static final String district = "district";
    private static final String state = "state";
    private static final String distance = "distance";
    List<String> list2 = new ArrayList<>();
    String[] autoCompleteList = {"Bangalore", "Delhi", "Mumbai", "Kolkata", "Bandra", "Kolhapur", "Bihar"};
    AutoCompleteTextView editText;


    float lat, longitude;
    private FusedLocationProviderApi locationProviderApi = LocationServices.FusedLocationApi;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private Double myLatitude, myLongitude, getLatUser, getLongUser;
    private int dist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFragment(new CurrentLocationFragment(), "MARKET NEAR ME");
        viewPagerAdapter.addFragment(new UserSetFragment(), "MARKET BY USER");
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        //location work
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();


        locationRequest = new LocationRequest();
        locationRequest.setInterval(10 * 1000);
        locationRequest.setFastestInterval(5 * 1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);


    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {

        requestLocationUpdates();

    }

    private void requestLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(this
                , android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this
                , android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        myLatitude = location.getLatitude();
        myLongitude = location.getLongitude();
        dist = 100;


        lat = Float.parseFloat(String.valueOf(myLatitude));
        longitude = Float.parseFloat(String.valueOf(myLongitude));

        if (googleApiClient.isConnected())
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);

        JsonTask();


    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (googleApiClient.isConnected()) {
            requestLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
    }

    @Override
    protected void onStop() {
        super.onStop();

        googleApiClient.disconnect();

        //JsonTask();
    }


    String json_string;

    public String JsonTask() {


        new AsyncTask<String, String, String>() {


            HttpURLConnection urlConnection = null;
            BufferedReader bufferedReader = null;

            @Override
            protected String doInBackground(String... params) {
                try {
                    //creating client object
                    URL url = new URL("http://data.cropin.in/mandi/getmarket?format=json&lat=" + myLatitude +
                            "&lon=" + myLongitude + "&dist=" + dist);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.connect();


                    //using httppost to use parameter url
                    InputStream inputStream = urlConnection.getInputStream();

                    bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                    //read contect and display
                    StringBuilder stringBuilder = new StringBuilder();
                    String newLine = "";

                    while ((newLine = bufferedReader.readLine()) != null) {
                        stringBuilder.append(newLine);
                        //stringBuffer.append("\n");
                    }
                    return stringBuilder.toString().trim();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    //Close content
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    try {
                        if (bufferedReader != null) {
                            bufferedReader.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }

            @Override
            protected void onProgressUpdate(String... values) {
                super.onProgressUpdate(values);
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                json_string = result;


                //recycler view work
                recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
                layoutManager = new LinearLayoutManager(MainActivity.this);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setHasFixedSize(false);

                recyclerAdapter = new RecyclerAdapter(dataList);

                createAdapter();


            }
        }.execute();
        editText = (AutoCompleteTextView) findViewById(R.id.eText);

        editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocationAutoCOmplete();
            }
        });

        /*ArrayAdapter<String> arrayAdapter1 = new ArrayAdapter<>(MainActivity.this,
                R.layout.autocomplete_layout_item, R.id.itemAutoComplete, autoCompleteList);
        // editText.setThreshold(4);
        editText.setAdapter(arrayAdapter1);*/


        return json_string;
    }

    int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;

    private void LocationAutoCOmplete() {
        try {
            Intent intent =
                    new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                            .build(this);
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException e) {
            // TODO: Handle the error.
        } catch (GooglePlayServicesNotAvailableException e) {
            // TODO: Handle the error.
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                Log.i("OK", "Place: " + place.getName());
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                Log.i("ERROR", status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }

    }

    public void createAdapter() {


        try {
            JSONArray jsonArray = new JSONArray(json_string);
            for (int i = 0; i < jsonArray.length(); i++) {

                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String Name = jsonObject.getString(name);
                String State = jsonObject.getString(state);
                String District = jsonObject.getString(district);
                String Distance = jsonObject.getString(distance);


                Market market = new Market(Name, State, District, Distance);
                dataList.add(market);


            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
        recyclerView.setAdapter(recyclerAdapter);
    }


    public void getGeoLatLongUsingName(View view) throws IOException {


        hideSoftKeyboard(view);

        String locationName = editText.getText().toString();
        Geocoder geocoder = new Geocoder(this);
        List<android.location.Address> addressList = geocoder.getFromLocationName(locationName, 1);
        android.location.Address address = addressList.get(0);
        String localityGot = address.getLocality();

        getLatUser = address.getLatitude();
        getLongUser = address.getLongitude();

        NewJsonTask();

        Toast.makeText(this, localityGot + " " + getLatUser + " " + getLongUser, Toast.LENGTH_LONG).show();

    }


    public void hideSoftKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    String json_string1;

    public String NewJsonTask() {


        new AsyncTask<String, String, String>() {


            HttpURLConnection urlConnection = null;
            BufferedReader bufferedReader = null;

            @Override
            protected String doInBackground(String... params) {
                try {
                    //creating client object
                    URL url = new URL("http://data.cropin.in/mandi/getmarket?format=json&lat=" + getLatUser +
                            "&lon=" + getLongUser + "&dist=100");
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.connect();


                    //using httppost to use parameter url
                    InputStream inputStream = urlConnection.getInputStream();

                    bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                    //read contect and display
                    StringBuilder stringBuilder = new StringBuilder();
                    String newLine = "";

                    while ((newLine = bufferedReader.readLine()) != null) {
                        stringBuilder.append(newLine);
                        //stringBuffer.append("\n");
                    }
                    return stringBuilder.toString().trim();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    //Close content
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    try {
                        if (bufferedReader != null) {
                            bufferedReader.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }

            @Override
            protected void onProgressUpdate(String... values) {
                super.onProgressUpdate(values);
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                json_string1 = result;


                //recycler view work
                recyclerViewUserSet = (RecyclerView) findViewById(R.id.recyclerView1);
                layoutManager = new LinearLayoutManager(MainActivity.this);
                recyclerViewUserSet.setLayoutManager(layoutManager);
                recyclerViewUserSet.setHasFixedSize(false);

                if (dataList1.size() != 0) {
                    dataList1.clear();
                    recyclerAdapterUserSet.notifyDataSetChanged();
                }
                recyclerAdapterUserSet = new RecyclerAdapterUserSet(dataList1);
                createAdapterUser();

            }
        }.execute();
        return json_string1;
    }


    private void createAdapterUser() {

        try {
            JSONArray jsonArray1 = new JSONArray(json_string1);
            for (int i = 0; i < jsonArray1.length(); i++) {

                JSONObject jsonObject1 = jsonArray1.getJSONObject(i);
                String Name = jsonObject1.getString(name);
                String State = jsonObject1.getString(state);
                String District = jsonObject1.getString(district);
                String Distance = jsonObject1.getString(distance);


                Market market = new Market(Name, State, District, Distance);
                dataList1.add(market);


            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
        recyclerViewUserSet.setAdapter(recyclerAdapterUserSet);
    }


}
