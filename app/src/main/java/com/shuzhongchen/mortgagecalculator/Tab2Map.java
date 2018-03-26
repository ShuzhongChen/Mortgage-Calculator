package com.shuzhongchen.mortgagecalculator;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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

                List<BasicInfo> savedBasicInfo = ModelUtils.read(getContext(), MODEL_BASICINFO,
                        new TypeToken<List<BasicInfo>>(){});
                if (savedBasicInfo == null) {
                    Toast.makeText(context, "No saved property!", Toast.LENGTH_LONG).show();
                } else {
                    for (int i = 0; i < savedBasicInfo.size(); i++) {
                        final BasicInfo geoInfo = savedBasicInfo.get(i);

                        // For dropping a marker at a point on the Map
                        LatLng pos = new LatLng(geoInfo.lat, geoInfo.lng);
                        final Marker thisMarker = googleMap.addMarker(new MarkerOptions().position(pos).title(geoInfo.propertyType).snippet(geoInfo.propertyPrice));

                        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                            @Override
                            public boolean onMarkerClick(Marker marker) {

                                // Auto-generated method stub
                                if (marker.equals(thisMarker)) {
                                    Log.d("Click", "test");

                                    Dialog dialogDetails = new Dialog(MainActivity.this);
                                    dialogDetails.setTitle("Show Me");
                                    dialogDetails.setContentView(R.layout.dialog_details);

                                    TextView propertyType = dialogDetails.findViewById(R.id.property_type);
                                    propertyType.setText(geoInfo.propertyType);
                                    TextView address = dialogDetails.findViewById(R.id.street_address);
                                    address.setText(geoInfo.streetAddress);
                                    TextView city = dialogDetails.findViewById(R.id.city);
                                    city.setText(geoInfo.city);
                                    TextView loan = dialogDetails.findViewById(R.id.loan_amount);
                                    loan.setText(Integer.parseInt(geoInfo.propertyPrice) - Integer.parseInt(geoInfo.downPayment));
                                    TextView apr = dialogDetails.findViewById(R.id.apr);
                                    apr.setText(geoInfo.apr);
                                    TextView monthly = dialogDetails.findViewById(R.id.monthly_payment);
                                    monthly.setText(geoInfo.monthlyPayment);

                                    Button edit = dialogDetails.findViewById(R.id.btn_edit);
                                    edit.setOnClickListener(new View.OnClickListener() {
                                        public void onClick(View v) {

                                        }
                                    });
                                    Button delete = dialogDetails.findViewById(R.id.btn_delete);
                                    delete.setOnClickListener(new View.OnClickListener() {
                                        public void onClick(View v) {

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

                // For zooming automatically to the location of the marker
                /*CameraPosition cameraPosition = new CameraPosition.Builder().target(sydney).zoom(12).build();
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));*/

            }
        });

        return rootView;
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

/*public class Tab2Map extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tab2map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    /*@Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }
}*/
