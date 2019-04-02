package com.example.captainmeteo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class WeatherController extends AppCompatActivity {

    //Constants:
    final String WEATHER_URL = "http://api.openweathermap.org/data/2.5/weather";

    //Request code
    final int REQUEST_CODE = 123;

    //App ID to use OpenWeather data
    final String APP_ID = "bbeb90481fc268402b2a0c751914516d";

    //Time between location updates (5000 milliseconds or 5 seconds)
    final long MIN_TIME = 5000;

    //Distance between location updates (1000m 0r 1km)
    final float MIN_DISTANCE = 1000;

    //Location Provider here
    String LOCATION_PROVIDER = LocationManager.NETWORK_PROVIDER;

    //Member variables
    TextView mCityLabel;
    ImageView mWeatherImage;
    TextView mTemperatureLabel;

    //LocationManager and LocationListener
    LocationManager mLocationManager;
    LocationListener mLocationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_controller_layout);

        //Linking the elements in the layout to Java code
        mCityLabel = (TextView) findViewById(R.id.locationTV);
        mWeatherImage = (ImageView) findViewById(R.id.weatherSymbolIV);
        mTemperatureLabel = (TextView) findViewById(R.id.tempTV);
        ImageButton changeCityButton = (ImageButton) findViewById(R.id.changeCityButton);

        //onClickListener on the changeCityButton
        changeCityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WeatherController.this, ChangeCityController.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent myIntent = getIntent();
        String city = myIntent.getStringExtra("City");

        if(city != null){
            getWeatherForNewCity(city);
        }else {

            Toast.makeText(this, "Getting weather for current location", Toast.LENGTH_SHORT).show();
            getWeatherForCurrentLocation();
        }
    }

    //getWeatherForNewCity(String city) method here
    private void getWeatherForNewCity(String city){
        RequestParams params = new RequestParams();
        params.put("q",city);
        params.put("appid",APP_ID);
        letsDoSomeNetworking(params);
    }

    //getWeatherForCurrentLocation() method here
    private void getWeatherForCurrentLocation() {
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Toast.makeText(WeatherController.this, "On location changed", Toast.LENGTH_SHORT).show();

                String longitude = String.valueOf(location.getLongitude());
                String latitude = String.valueOf(location.getLatitude());

                RequestParams params = new RequestParams();
                params.put("lat", latitude);
                params.put("lon", longitude);
                params.put("appid", APP_ID);
                letsDoSomeNetworking(params);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                Toast.makeText(WeatherController.this, "On status changed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProviderEnabled(String provider) {
                Toast.makeText(WeatherController.this, "On provider enabled", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProviderDisabled(String provider) {
                Toast.makeText(WeatherController.this, "On provider disabled", Toast.LENGTH_SHORT).show();
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_CODE);
            
            return;
        }
        mLocationManager.requestLocationUpdates(LOCATION_PROVIDER, MIN_TIME, MIN_DISTANCE, mLocationListener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if(requestCode == REQUEST_CODE){
            
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //letsDoSomeNetworking(RequestParams params) method here
    public void letsDoSomeNetworking(RequestParams params){

        AsyncHttpClient client = new AsyncHttpClient();

        client.get(WEATHER_URL, params, new JsonHttpResponseHandler(){

            @Override
            public void onSuccess(int statusCode, Header [] headers, JSONObject response) {

                WeatherDataModel weatherData = WeatherDataModel.fromJSon(response);
                updateUI(weatherData);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Toast.makeText(WeatherController.this, "Failed: "+errorResponse, Toast.LENGTH_SHORT).show();
            }
        });
    }

    //updateUI() method here
    private void updateUI(WeatherDataModel weather){
        mCityLabel.setText(weather.getmCity());
        mTemperatureLabel.setText(weather.getmTemperature());

        int resourceID = getResources().getIdentifier(weather.getmIconName(),"drawable", getPackageName());
        mWeatherImage.setImageResource(resourceID);
    }

    //onPause() method here


    @Override
    protected void onPause() {
        super.onPause();

        if(mLocationManager != null){
            mLocationManager.removeUpdates(mLocationListener);
        }
    }
}
