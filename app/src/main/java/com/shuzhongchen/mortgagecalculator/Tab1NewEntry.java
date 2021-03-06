package com.shuzhongchen.mortgagecalculator;

/**
 * Created by shuzhongchen on 3/24/18.
 */

import android.location.Address;
import android.location.Geocoder;
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
import com.shuzhongchen.mortgagecalculator.helper.ReceiverInterface;
import com.shuzhongchen.mortgagecalculator.model.BasicInfo;
import com.shuzhongchen.mortgagecalculator.util.ModelUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

import xdroid.toaster.Toaster;

public class Tab1NewEntry extends Fragment implements ReceiverInterface {

    private static final String MODEL_BASICINFO = "model_basicinfo";

    private List<BasicInfo> basicInfos;

    Button save, clear;
    Spinner state;
    EditText street_address, city, zipcode, property_price, down_payment,
            annual_percentage_rate;

    RadioGroup terms_radio_group, property_type;

    TextView monthy_payment;



    double propertyPrice, downPayment, apr, monthyPayment;
    int terms, index;



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

        clear = rootView.findViewById(R.id.clear);
        clear.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                initialize();
            }
        });

        save = rootView.findViewById(R.id.save);
        save.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                final BasicInfo basicInfo;

                if (index == -1) {
                    basicInfo = new BasicInfo();
                } else {
                    basicInfo = basicInfos.get(index);
                }

                String radiovalue = ((RadioButton)getView().findViewById(property_type.getCheckedRadioButtonId())).getText().toString();
                basicInfo.propertyType = radiovalue;

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

                if (basicInfo.streetAddress.length() == 0 || city.length() == 0) {
                    Toaster.toast("Fail to save! Please enter street address and city!");
                    return;
                }

                if (annual_percentage_rate.length() == 0) {
                    Toaster.toast("Fail to save! Please enter annual percentage rate!");
                    return;
                }

                double lat;
                double lng;
                Geocoder geoCoder = new Geocoder(getContext(), Locale.getDefault());
                try {
                    List<Address> addresses =
                            geoCoder.getFromLocationName(basicInfo.streetAddress + "," +
                                    basicInfo.city + "," + sState, 2);

                    if (addresses.size() < 1) {
                        Toaster.toast("Please provide valid address!");
                    } else {
                        lat = addresses.get(0).getLatitude();
                        lng = addresses.get(0).getLongitude();
                        Log.d("coor", "" + lat);
                        Log.d("coor", "" + lng);
                        basicInfo.lat = lat;
                        basicInfo.lng = lng;
                        if (index == -1) {
                            basicInfos.add(basicInfo);
                        }
                        ModelUtils.save(getContext(), MODEL_BASICINFO, basicInfos);

                        Toaster.toast("Your data has been saved!");
                        initialize();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Toaster.toast("Please provide valid address!");
                }
            }
        });

        initialize();
        return rootView;
    }

    public void getMonthyPayment() {

        double loan = propertyPrice - downPayment;

        double mir = apr / 1200 ;

        int n = terms * 12;

        monthyPayment = loan * mir * Math.pow(1 + mir, n) / (Math.pow(1 + mir, n) - 1);

        monthy_payment.setText(formatDouble(monthyPayment));

    }

    public boolean isDigit(String str) {
        return str.matches("\\d+(?:\\.\\d+)?");
    }

    public void initialize() {
        property_type.check(R.id.house);
        street_address.setText("");
        city.setText("");
        zipcode.setText("");
        state.setSelection(5);

        property_price.setText("0");
        down_payment.setText("0");
        //annual_percentage_rate.setText("0");
        monthy_payment.setText("0");
        terms_radio_group.check(R.id.radio_15);
        terms = 15;
        propertyPrice = 0;
        downPayment = 0;
        //apr = 0;
        monthyPayment = 0;
        index = -1;
    }

    public void editDisplay(int i){
        index = i;

        basicInfos = ModelUtils.read(getContext(),
                MODEL_BASICINFO,
                new TypeToken<List<BasicInfo>>(){});

        BasicInfo geoInfo = basicInfos.get(i);

        if (geoInfo.propertyType.equals("House")) {
            property_type.check(R.id.house);
        } else if (geoInfo.propertyType.equals("Condo")) {
            property_type.check(R.id.condo);
        } else {
            property_type.check(R.id.townhouse);
        }

        street_address.setText(geoInfo.streetAddress.toString());

        city.setText(geoInfo.city);
        zipcode.setText(geoInfo.zipcode);
        state.setSelection(geoInfo.state);
        property_price.setText(Double.toString(geoInfo.propertyPrice));
        down_payment.setText(Double.toString(geoInfo.downPayment));
        annual_percentage_rate.setText(Double.toString(geoInfo.apr));
        monthy_payment.setText(formatDouble(geoInfo.monthyPayment));
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

    public String formatDouble(double s) {
        DecimalFormat fmt = new DecimalFormat("##0.00");
        return fmt.format(s);
    }

    @Override
    public void receiveMessage(int index) {
        editDisplay(index);
    }
}
