package com.shuzhongchen.mortgagecalculator;

/**
 * Created by shuzhongchen on 3/24/18.
 */

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
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
    private static final String PROPERTY_ID = "property_id";

    private List<BasicInfo> basicInfos;

    Button save;
    Spinner state;
    EditText property_type, street_address, city, zipcode, property_price, down_payment,
            annual_percentage_rate;

    RadioGroup terms_radio_group;

    TextView monthy_payment;



    double propertyPrice, downPayment, apr, monthyPayment;
    int terms;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab1newentry, container, false);

        property_type = rootView.findViewById(R.id.property_type);
        street_address = rootView.findViewById(R.id.street_address);
        state = rootView.findViewById(R.id.state);
        city = rootView.findViewById(R.id.city);
        zipcode = rootView.findViewById(R.id.zipcode);
        monthy_payment = rootView.findViewById(R.id.monthy_payment);

        property_price = rootView.findViewById(R.id.property_price);
        property_price.addTextChangedListener(new TextWatcher () {
            public void afterTextChanged(Editable s) {}
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!isDigit(s.toString())) {
                    monthy_payment.setText("Invalid property price");
                } else {
                    propertyPrice = Double.parseDouble(s.toString());
                    getMonthyPayment();
                }
            }
        });

        down_payment = rootView.findViewById(R.id.down_payment);
        down_payment.addTextChangedListener(new TextWatcher () {
            public void afterTextChanged(Editable s) {}
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!isDigit(s.toString())) {
                    monthy_payment.setText("Invalid down payment");
                } else {
                    downPayment = Double.parseDouble(s.toString());
                    getMonthyPayment();
                }
            }
        });

        annual_percentage_rate = rootView.findViewById(R.id.annual_percentage_rate);
        annual_percentage_rate.addTextChangedListener(new TextWatcher () {
            public void afterTextChanged(Editable s) {}
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!isDigit(s.toString())) {
                    monthy_payment.setText("Invalid annual percentage rate");
                } else {
                    apr = Double.parseDouble(s.toString());
                    getMonthyPayment();
                }
            }
        });

        terms_radio_group = rootView.findViewById(R.id.terms);
        terms_radio_group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch(checkedId) {
                    case R.id.radio_15:
                        terms = 15;
                        getMonthyPayment();
                        break;
                    case R.id.radio_30:
                        terms = 30;
                        getMonthyPayment();
                        break;
                }
            }
        });

        List<BasicInfo> savedBasicInfo = ModelUtils.read(getContext(),
                MODEL_BASICINFO,
                new TypeToken<List<BasicInfo>>(){});
        basicInfos = savedBasicInfo == null ? new ArrayList<BasicInfo>() : savedBasicInfo;

        save = rootView.findViewById(R.id.save);
        save.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                final BasicInfo basicInfo = new BasicInfo();

                basicInfo.propertyType = property_type.getText().toString();

                basicInfo.streetAddress = street_address.getText().toString();
                basicInfo.city = city.getText().toString();
                basicInfo.state = state.getSelectedItemPosition();
                String sState = state.getSelectedItem().toString();
                basicInfo.zipcode = zipcode.getText().toString();

                basicInfo.propertyPrice = propertyPrice;
                basicInfo.downPayment = downPayment;
                basicInfo.apr = apr;
                basicInfo.terms = terms;
                basicInfo.monthyPayment = monthyPayment;

                if (basicInfo.streetAddress.length() == 0 && city.length() == 0) {
                    Toaster.toast("Fail to save! Please enter street address and city!");
                    return;
                }

                final String tmpUrl = "http://maps.google.com/maps/api/geocode/json?address=" +
                        basicInfo.streetAddress + " " + basicInfo.city + " " + sState + " " + basicInfo.zipcode + "&sensor=false";
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
                            String status = (String)jsonObject.get("status");
                            Log.d("shuzhong debug status: ",status);
                            if (status.equals("OK")) {

                                basicInfo.lat = lat;
                                basicInfo.lng = lng;

                                basicInfos.add(basicInfo);
                                ModelUtils.save(getContext(), MODEL_BASICINFO, basicInfos);

                                Toaster.toast("Your data has been saved!");
                                getActivity().runOnUiThread(new Runnable(){
                                    @Override
                                    public void run() {

                                        //stuff that updates ui
                                        initialize();

                                    }
                                });

                            }

                        } catch (Exception e) {
                            Log.d("Exception", "" + e);
                            Toaster.toast("Fail to save! Cannot get geo info of this address!");
                        }
                    }
                }).start();
            }
        });

        Bundle bundle = getArguments();
        if (bundle != null) {
            Integer property_id = bundle.getInt(PROPERTY_ID);
            Toast.makeText(getActivity().getApplication().getApplicationContext(), "Fragment changed!" + property_id, Toast.LENGTH_LONG).show();
            editDisplay(property_id);
        } else {
            initialize();
        }

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

    public void getMonthyPayment() {

        double loan = propertyPrice - downPayment;

        double mir = apr / 12;

        monthyPayment = loan * mir * Math.pow(1 + mir, terms) / (Math.pow(1 + mir, terms) + 1);

        monthy_payment.setText(monthyPayment + "");

    }

    public boolean isDigit(String str) {
        return str.matches("\\d+(?:\\.\\d+)?");
    }

    public void initialize() {
        property_type.setText("");
        street_address.setText("");
        city.setText("");
        zipcode.setText("");
        state.setSelection(-1);

        property_price.setText("0");
        down_payment.setText("0");
        annual_percentage_rate.setText("0");
        monthy_payment.setText("0");
        terms_radio_group.check(R.id.radio_15);
        terms = 15;
        propertyPrice = 0;
        downPayment = 0;
        apr = 0;
        monthyPayment = 0;
    }

    public void editDisplay(int i){
        BasicInfo geoInfo = basicInfos.get(i);
        property_type.setText(geoInfo.propertyType);
        street_address.setText(geoInfo.streetAddress);
        city.setText(geoInfo.city);
        zipcode.setText(geoInfo.zipcode);
        state.setSelection(geoInfo.state);
        property_price.setText(Double.toString(geoInfo.propertyPrice));
        down_payment.setText(Double.toString(geoInfo.downPayment));
        annual_percentage_rate.setText(Double.toString(geoInfo.apr));
        monthy_payment.setText(Double.toString(geoInfo.monthyPayment));
        if (geoInfo.terms == 15) {
            terms_radio_group.check(R.id.radio_15);
        } else {
            terms_radio_group.check(R.id.radio_30);
        }
        terms = geoInfo.terms;
        propertyPrice = geoInfo.propertyPrice;
        downPayment = geoInfo.downPayment;
        apr = geoInfo.apr;
        monthyPayment = geoInfo.monthyPayment;
    }
}
