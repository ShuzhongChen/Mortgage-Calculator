package com.shuzhongchen.mortgagecalculator;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.reflect.TypeToken;
import com.shuzhongchen.mortgagecalculator.model.BasicInfo;
import com.shuzhongchen.mortgagecalculator.util.ModelUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by syrhuang on 3/24/18.
 */

public class Tab2Map extends Fragment {

    /*@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab2map, container, false);
        return rootView;
    }*/

    MapView mMapView;
    private GoogleMap googleMap;
    private static final String MODEL_BASICINFO = "model_basicinfo";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab2map, container, false);

        mMapView = (MapView) rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;

                Context context = getActivity().getApplication().getApplicationContext();

                // For showing a move to my location button
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
                } else {
                    Toast.makeText(context, "You have to accept to enjoy all app's services!", Toast.LENGTH_LONG).show();
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        mMap.setMyLocationEnabled(true);
                    }
                }

                initialization();

            }
        });

        return rootView;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            // Refresh your fragment here
            initialization();
        }
    }

    private void initialization() {
        final List<BasicInfo> savedBasicInfo = ModelUtils.read(getContext(), MODEL_BASICINFO,
                new TypeToken<List<BasicInfo>>(){});
        if (savedBasicInfo == null) {
            Toast.makeText(getActivity().getApplication().getApplicationContext(),
                    "No saved property!", Toast.LENGTH_LONG).show();
        } else {
            for (int i = 0; i < savedBasicInfo.size(); i++) {
                final BasicInfo geoInfo = savedBasicInfo.get(i);

                // For dropping a marker at a point on the Map
                LatLng pos = new LatLng(geoInfo.lat, geoInfo.lng);
                final Marker thisMarker = googleMap.addMarker(new MarkerOptions()
                        .position(pos).title(geoInfo.propertyType).snippet(Float.toString(geoInfo.propertyPrice)));

                // For zooming automatically to the location of the marker
                CameraPosition cameraPosition = new CameraPosition.Builder().target(pos).zoom(12).build();
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {

                        // Auto-generated method stub
                        if (marker.equals(thisMarker)) {
                            Log.d("Click", "test");

                            /*final AlertDialog.Builder dialogDetails = new AlertDialog.Builder(getActivity());
                            dialogDetails.setTitle("Property Details");
                            dialogDetails.setMessage(geoInfo.propertyType);
                            dialogDetails.setPositiveButton("Delete",new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    savedBasicInfo.remove(geoInfo);
                                    ModelUtils.save(getContext(), MODEL_BASICINFO, savedBasicInfo);
                                    initialization();
                                }
                            });*/

                            final Dialog dialogDetails = new Dialog(getActivity());
                            dialogDetails.setTitle("Property Details");
                            dialogDetails.setContentView(R.layout.dialog_details);

                            TextView propertyType = dialogDetails.findViewById(R.id.property_type);
                            propertyType.setText(geoInfo.propertyType);
                            TextView address = dialogDetails.findViewById(R.id.street_address);
                            address.setText(geoInfo.streetAddress);
                            TextView city = dialogDetails.findViewById(R.id.city);
                            city.setText(geoInfo.city);
                            TextView loan = dialogDetails.findViewById(R.id.loan_amount);
                            loan.setText(Float.toString(geoInfo.propertyPrice - geoInfo.downPayment));
                            TextView apr = dialogDetails.findViewById(R.id.apr);
                            apr.setText(Float.toString(geoInfo.apr));
                            TextView monthly = dialogDetails.findViewById(R.id.monthly_payment);
                            monthly.setText(Float.toString(geoInfo.monthyPayment));

                            Button edit = dialogDetails.findViewById(R.id.btn_edit);
                            edit.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    Fragment Tab1NewEntry = new Tab1NewEntry();
                                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                                    transaction.replace(R.id.calView, Tab1NewEntry ); // give your fragment container id in first parameter
                                    transaction.addToBackStack(null);  // if written, this transaction will be added to backstack
                                    transaction.commit();
                                }
                            });
                            Button delete = dialogDetails.findViewById(R.id.btn_delete);
                            delete.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    savedBasicInfo.remove(geoInfo);
                                    ModelUtils.save(getContext(), MODEL_BASICINFO, savedBasicInfo);
                                    thisMarker.remove();
                                    //dialogDetails.dismiss();
                                    initialization();
                                }
                            });

                            dialogDetails.show();

                            return true;
                        }
                        return false;

                    }
                });
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

}