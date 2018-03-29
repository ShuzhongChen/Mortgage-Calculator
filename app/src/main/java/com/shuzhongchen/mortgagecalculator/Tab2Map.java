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
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
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
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.reflect.TypeToken;
import com.shuzhongchen.mortgagecalculator.helper.FragmentCommunication;
import com.shuzhongchen.mortgagecalculator.model.BasicInfo;
import com.shuzhongchen.mortgagecalculator.util.ModelUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by syrhuang on 3/24/18.
 */

public class Tab2Map extends Fragment {

    MapView mMapView;
    private GoogleMap googleMap;
    private static final String MODEL_BASICINFO = "model_basicinfo";
    private static final String PROPERTY_ID = "property_id";

    private List<BasicInfo> savedBasicInfo;
    private List<Marker> markers;

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

                UiSettings mapUISetting = googleMap.getUiSettings();
                mapUISetting.setZoomControlsEnabled(true);

                initialization();


                googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(final Marker marker) {

                        Log.d("Shuzhong getSnippet", marker.getSnippet() + "");
                        Log.d("Shuzhong marker size", markers.size() + "");
                        Log.d("Shuzhong basic size", savedBasicInfo.size() + "");
                        // Auto-generated method stub
                        int markerIndex = -1;

                        for (int i = 0; i < markers.size(); i++) {
                            if (markers.get(i).getSnippet().equals(marker.getSnippet())) {
                                markerIndex = i;
                                break;
                            }

                        }

                        if (markerIndex == -1) {
                            return false;
                        }

                        final int i = markerIndex;

                        final BasicInfo geoInfo = savedBasicInfo.get(markerIndex);

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
                        loan.setText(Double.toString(geoInfo.propertyPrice - geoInfo.downPayment));
                        TextView apr = dialogDetails.findViewById(R.id.apr);
                        apr.setText(Double.toString(geoInfo.apr));
                        TextView monthly = dialogDetails.findViewById(R.id.monthly_payment);
                        monthly.setText(new DecimalFormat("##0.00").format(geoInfo.monthyPayment));

                        Button edit = dialogDetails.findViewById(R.id.btn_edit);
                        final ViewPager viewPager = (ViewPager) getActivity().findViewById(R.id.container);
                        edit.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                dialogDetails.onBackPressed();
                                ((FragmentCommunication) getActivity()).passIndex(i);
                                viewPager.setCurrentItem(0);
                            }
                        });

                        Button delete = dialogDetails.findViewById(R.id.btn_delete);
                        delete.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                savedBasicInfo.remove(geoInfo);
                                ModelUtils.save(getContext(), MODEL_BASICINFO, savedBasicInfo);
                                markers.get(i).remove();
                                dialogDetails.onBackPressed();
                                initialization();
                            }
                        });

                        dialogDetails.show();

                        return true;

                    }
                });
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
        googleMap.clear();
        savedBasicInfo = ModelUtils.read(getContext(), MODEL_BASICINFO,
                new TypeToken<List<BasicInfo>>(){});
        markers = new ArrayList<Marker>();
        if (savedBasicInfo == null) {
            Toast.makeText(getActivity().getApplication().getApplicationContext(),
                    "No saved property!", Toast.LENGTH_LONG).show();
        } else {
            for (int i = 0; i < savedBasicInfo.size(); i++) {
                final BasicInfo geoInfo = savedBasicInfo.get(i);

                // For dropping a marker at a point on the Map
                LatLng pos = new LatLng(geoInfo.lat, geoInfo.lng);
                Marker m = googleMap.addMarker(new MarkerOptions()
                        .position(pos).title(geoInfo.propertyType).snippet("id" + i));
                markers.add(m);

                // For zooming automatically to the location of the marker
                CameraPosition cameraPosition = new CameraPosition.Builder().target(pos).zoom(12).build();
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
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