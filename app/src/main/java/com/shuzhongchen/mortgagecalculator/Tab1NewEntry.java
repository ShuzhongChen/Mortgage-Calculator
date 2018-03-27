package com.shuzhongchen.mortgagecalculator;

/**
 * Created by shuzhongchen on 3/24/18.
 */

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.reflect.TypeToken;
import com.shuzhongchen.mortgagecalculator.model.BasicInfo;
import com.shuzhongchen.mortgagecalculator.util.ModelUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import xdroid.toaster.Toaster;

public class Tab1NewEntry extends Fragment{

    private static final String MODEL_BASICINFO = "model_basicinfo";

    private List<BasicInfo> basicInfos;

    Button save;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab1newentry, container, false);

        List<BasicInfo> savedBasicInfo = ModelUtils.read(getContext(),
                MODEL_BASICINFO,
                new TypeToken<List<BasicInfo>>(){});
        basicInfos = savedBasicInfo == null ? new ArrayList<BasicInfo>() : savedBasicInfo;

        save = rootView.findViewById(R.id.save);
        save.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                BasicInfo[] infoArray = new BasicInfo[1];

                final BasicInfo basicInfo = new BasicInfo();

                basicInfo.propertyType = ((EditText)getView().findViewById(R.id.property_type)).getText().toString();
                basicInfo.streetAddress = ((EditText)getView().findViewById(R.id.street_address)).getText().toString();
                basicInfo.city = ((EditText)getView().findViewById(R.id.city)).getText().toString();
                basicInfo.state = ((EditText)getView().findViewById(R.id.state)).getText().toString();
                basicInfo.zipcode = ((EditText)getView().findViewById(R.id.zipcode)).getText().toString();

                String propertyPrice = ((EditText)getView().findViewById(R.id.property_price)).getText().toString();
                if (!propertyPrice.matches("\\d+(?:\\.\\d+)?")) {
                    Toast.makeText(getContext(), "Invalid property price", Toast.LENGTH_LONG).show();
                    return;
                }
                basicInfo.propertyPrice = Float.parseFloat(propertyPrice);

                String downPayment = ((EditText)getView().findViewById(R.id.down_payment)).getText().toString();
                if (!downPayment.matches("\\d+(?:\\.\\d+)?")) {
                    Toast.makeText(getContext(), "Invalid down payment", Toast.LENGTH_LONG).show();
                    return;
                }
                basicInfo.downPayment = Float.parseFloat(downPayment);

                String apr = ((EditText)getView().findViewById(R.id.annual_percentage_rate)).getText().toString();
                if (!apr.matches("\\d+(?:\\.\\d+)?")) {
                    Toast.makeText(getContext(), "Invalid annual percentage rate", Toast.LENGTH_LONG).show();
                    return;
                }
                basicInfo.apr = Float.parseFloat(apr);

                String terms = ((EditText)getView().findViewById(R.id.terms)).getText().toString();
                if (!terms.matches("\\d+(?:\\.\\d+)?")) {
                    Toast.makeText(getContext(), "Invalid terms", Toast.LENGTH_LONG).show();
                    return;
                }
                basicInfo.terms = Integer.parseInt(apr);


                final String tmpUrl = "http://maps.google.com/maps/api/geocode/json?address=" +
                        basicInfo.streetAddress + " " + basicInfo.city + " " + basicInfo.state + " " + basicInfo.zipcode + "&sensor=false";
                final String googleMapUrl = tmpUrl.replaceAll(" ", "+");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String response = getLatLongByURL(
                                    googleMapUrl);
                            Log.d("response",""+response);
                            Log.d("googleMapUrl",""+googleMapUrl);

                            JSONObject jsonObject = new JSONObject(response);
                            double lng = ((JSONArray)jsonObject.get("results")).getJSONObject(0)
                                    .getJSONObject("geometry").getJSONObject("location")
                                    .getDouble("lng");

                            double lat = ((JSONArray)jsonObject.get("results")).getJSONObject(0)
                                    .getJSONObject("geometry").getJSONObject("location")
                                    .getDouble("lat");

                            Log.d("latitude", "" + lat);
                            Log.d("longitude", "" + lng);

                            basicInfo.lat = lat;
                            basicInfo.lng = lng;

                            basicInfos.add(basicInfo);
                            ModelUtils.save(getContext(), MODEL_BASICINFO, basicInfos);

                            Toaster.toast("latitude: " + basicInfo.lat + " longitude: " + basicInfo.lng);


                        } catch (Exception e) {
                            Toaster.toast("Cannot get geo info of this address!");
                        }
                    }
                }).start();
            }
        });

        return rootView;
    }


    public String getLatLongByURL(String requestURL) {
        URL url;
        String response = "";
        try {
            url = new URL(requestURL);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");
            conn.setDoOutput(true);
            int responseCode = conn.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line = br.readLine()) != null) {
                    response += line;
                }
            } else {
                response = "";
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }


}
