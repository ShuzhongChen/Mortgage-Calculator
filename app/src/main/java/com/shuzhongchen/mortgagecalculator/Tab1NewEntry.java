package com.shuzhongchen.mortgagecalculator;

/**
 * Created by shuzhongchen on 3/24/18.
 */

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.gson.reflect.TypeToken;
import com.shuzhongchen.mortgagecalculator.model.BasicInfo;
import com.shuzhongchen.mortgagecalculator.util.ModelUtils;

import java.util.ArrayList;
import java.util.List;

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

                BasicInfo basicInfo = new BasicInfo();

                basicInfo.propertyType = ((EditText)getView().findViewById(R.id.property_type)).getText().toString();
                basicInfo.streetAddress = ((EditText)getView().findViewById(R.id.street_address)).getText().toString();
                basicInfo.city = ((EditText)getView().findViewById(R.id.city)).getText().toString();
                basicInfo.state = ((EditText)getView().findViewById(R.id.state)).getText().toString();
                basicInfo.zipcode = ((EditText)getView().findViewById(R.id.zipcode)).getText().toString();
                basicInfo.propertyPrice = ((EditText)getView().findViewById(R.id.property_price)).getText().toString();
                basicInfo.downPayment = ((EditText)getView().findViewById(R.id.down_payment)).getText().toString();
                basicInfo.apr = ((EditText)getView().findViewById(R.id.annual_percentage_rate)).getText().toString();
                basicInfo.terms = ((EditText)getView().findViewById(R.id.terms)).getText().toString();

                basicInfos.add(basicInfo);

                ModelUtils.save(getContext(), MODEL_BASICINFO, basicInfos);
            }
        });

        return rootView;
    }


}
