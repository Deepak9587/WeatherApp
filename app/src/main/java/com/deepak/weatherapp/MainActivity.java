package com.deepak.weatherapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private RelativeLayout homeRL;
    private ProgressBar loadingPB;
    private TextView cityNameTV, temperatureTV,conditionTV;
    private RecyclerView weatherRV;
    private TextInputEditText cityEdt;
    private ImageView backIV,searchIV,iconIV;
    private ArrayList<WeatherRVModal> weatherRVModalArrayList;
    private  WeatherRVAdapter weatherRVAdapter;

    private LocationManager locationManager;
    private  int PERMISSION_CODE=1;
    private String cityName;
    private double locationLongitude,locationLatitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_main);

        homeRL=findViewById(R.id.idRLHome);
        loadingPB=findViewById(R.id.idPBLoading);
        cityNameTV=findViewById(R.id.idTVCityName);
        temperatureTV=findViewById(R.id.idTVTemperature);
        conditionTV=findViewById(R.id.idTVCondition);
        weatherRV=findViewById(R.id.idRVWeather);
        cityEdt=findViewById(R.id.idEdtCity);
        backIV=findViewById(R.id.idIVBack);
        searchIV=findViewById(R.id.idIVSearch);
        iconIV=findViewById(R.id.idIVIcon);

        weatherRVModalArrayList= new ArrayList<>();
        weatherRVAdapter= new WeatherRVAdapter(this,weatherRVModalArrayList);
        weatherRV.setAdapter(weatherRVAdapter);

        locationManager= (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},PERMISSION_CODE);
        }
        Location location =locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
         locationLongitude = location.getLongitude();
         locationLatitude = location.getLatitude();
         cityName=getCityName(locationLongitude,locationLatitude);
         getWeatherInfo(cityName);


        searchIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String city= cityEdt.getText().toString();
                if(city.isEmpty()){
                    Toast.makeText(MainActivity.this,"Please Enter city Name",Toast.LENGTH_SHORT).show();
                }
                else{
                    cityNameTV.setText(cityName);
                    getWeatherInfo(city);
                }

            }
        });

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==PERMISSION_CODE){
            if(grantResults.length>0  && grantResults[0]==PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permissions Granted....", Toast.LENGTH_SHORT).show();

            }
            else{
                Toast.makeText(this,"Please provide permissions ",Toast.LENGTH_SHORT).show();
                finish();


            }
        }
    }

    private  String getCityName(double longitude, double latitude){
        String cityName= "NOt found";
        Geocoder gcd= new Geocoder(getBaseContext(), Locale.getDefault());
        try{
            List<Address> addresses =gcd.getFromLocation(longitude,latitude,10);
            for(Address adr: addresses){
                if(adr!=null){
                    String city= adr.getLocality();
                     if(city!=null && city.equals("")){
                         cityName=city;
                     }
                     else{
                         Log.d("Tag","CITY NOT FOUND");
                         Toast.makeText(this,"User CITY NOT FOUND",Toast.LENGTH_SHORT).show();
                     }
                }
            }

        }catch (IOException e){
            e.printStackTrace();
        }
        return  cityName;

    }
    private  void getWeatherInfo(String cityName){
        String url="http://api.weatherapi.com/v1/forecast.json?key=8f2ebe33e8c94da0b5355044211110&q=" + cityName + "&days=1&aqi=no&alerts=no\n";
        cityNameTV.setText(cityName);
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);

        JsonObjectRequest jsonObjectRequest= new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                loadingPB.setVisibility(View.GONE);
                homeRL.setVisibility(View.VISIBLE);

                weatherRVModalArrayList.clear();

                try {
                    String temperature= response.getJSONObject("current").getString("temp_c");
                    temperatureTV.setText(temperature+"°C");
                    int isDay= response.getJSONObject("current").getInt("is_day");
                    String condition = response.getJSONObject("current").getJSONObject("condition").getString("text");
                    String conditionIcon = response.getJSONObject("current").getJSONObject("condition").getString("icon");
                    Picasso.get().load("http:".concat(conditionIcon)).into(iconIV);
                    conditionTV.setText(condition);

                    if(isDay==1){
                        Picasso.get().load("https://images.unsplash.com/photo-1513002749550-c59d786b8e6c?ixid=MnwxMjA3fDB8MHxzZWFyY2h8MXx8c2t5fGVufDB8fDB8fA%3D%3D&ixlib=rb-1.2.1&auto=format&fit=crop&w=500&q=60").into(backIV);
                    }
                    else{
                        Picasso.get().load("https://images.unsplash.com/photo-1507400492013-162706c8c05e?ixid=MnwxMjA3fDB8MHxzZWFyY2h8NXx8bmlnaHR8ZW58MHx8MHx8&ixlib=rb-1.2.1&auto=format&fit=crop&w=500&q=60").into(backIV);

                    }

                    JSONObject forcastObj= response.getJSONObject("forecast");
                    JSONObject forcast0= forcastObj.getJSONArray("forcastday").getJSONObject(0);
                    JSONArray horuArray=forcast0.getJSONArray("hour");

                    for(int i=0;i<horuArray.length();i++){
                        JSONObject hourObj = horuArray.getJSONObject(i);

                        String time =hourObj.getString("time");
                        String temper =hourObj.getString("temp_c");
                        String img =hourObj.getJSONObject("condition").getString("icon");
                        String wind=hourObj.getString("wind_kph");
                        weatherRVModalArrayList.add(new WeatherRVModal(time,temper,img,wind));

                    }
                    weatherRVAdapter.notifyDataSetChanged();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "Please enter valid city Name", Toast.LENGTH_SHORT).show();
            }
        });

        requestQueue.add(jsonObjectRequest);

    }
}