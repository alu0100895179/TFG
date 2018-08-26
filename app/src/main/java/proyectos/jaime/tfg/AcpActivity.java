package proyectos.jaime.tfg;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


import java.text.DecimalFormat;


public class AcpActivity  extends Activity implements  OnMapReadyCallback{

    static double alt;
    static double lat;
    static double lon;
    static double bearing;
    static double speed;

    static double lonBase;
    static double latBase;

    DecimalFormat dec1 = new DecimalFormat("#.0");
    DecimalFormat dec2 = new DecimalFormat("#.00");

    private AlphaAnimation buttonClick = new AlphaAnimation(1F, 0.6F);

    GoogleMap mMap;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("TFG_debug", "ACTIVIDAD PMA");
        setContentView(R.layout.acp_layout);
        Log.d("TFG_debug", "PINTADA");

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        final TextView latText, lonText, altText, provText, bearingText, speedText;
        latText = (TextView) findViewById(R.id.latitudeV);
        lonText = (TextView) findViewById(R.id.longitudeV);
        altText = (TextView) findViewById(R.id.altitudeV);
        provText = (TextView) findViewById(R.id.provV);
        bearingText = (TextView) findViewById(R.id.bearingV);
        speedText = (TextView) findViewById(R.id.speedV);

        final ImageView imgCompass = findViewById(R.id.imgViewCompass);
        final ImageView imgCompassB = findViewById(R.id.imgViewArrowBlue);
        final ImageView imgCompassR = findViewById(R.id.imgViewArrowRed);
        final TextView txtAngle = findViewById(R.id.txtAngle);
        final TextView txtAngleB = findViewById(R.id.txtAngleB);

        FirebaseDatabase database = FirebaseDatabase.getInstance();

        DatabaseReference droneRef = database.getReference("GPS/drone");
        droneRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                alt = Double.parseDouble(dataSnapshot.child("alt").getValue().toString());
                lat = Double.parseDouble(dataSnapshot.child("lat").getValue().toString());
                lon = Double.parseDouble(dataSnapshot.child("lon").getValue().toString());
                bearing = Double.parseDouble(dataSnapshot.child("bearing").getValue().toString());
                speed = Double.parseDouble(dataSnapshot.child("speed").getValue().toString());
                String prov = dataSnapshot.child("prov").getValue().toString();

                String latitudeS = "";
                String longitudeS = "";

                int latINT = (int) lat;
                latitudeS += latINT + "º";
                double latDBL = (lat-latINT)*60;
                latINT = (int) latDBL;
                latitudeS += latINT + "'";
                latDBL = (latDBL-latINT)*60;
                latINT = (int) latDBL;
                latitudeS += latINT + "''";

                int lonINT = (int) lon;
                longitudeS += lonINT + "º";
                double lonDBL = (lon-lonINT)*60;
                lonINT = (int) lonDBL;
                longitudeS += lonINT + "'";
                lonDBL = (lonDBL-lonINT)*60;
                lonINT = (int) lonDBL;
                longitudeS += lonINT + "''";

                latText.setText(latitudeS);
                lonText.setText(longitudeS);
                altText.setText(dec2.format(alt) + "m");
                bearingText.setText(""+ dec2.format(bearing) + " º");
                speedText.setText("" + dec2.format(speed) + " m/s");

                String aux;
                switch (prov) {
                    case "network":
                        aux = "NET";
                        break;
                    case "gps":
                        aux = "GPS";
                        break;
                    case "pasive":
                        aux = "PAS";
                        break;
                    default:
                        aux = "";
                }
                provText.setText(aux);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.d("TFG_debug", "Failed to read value.", error.toException());
            }
        });

        DatabaseReference brujulaRef = database.getReference("brujula");
        brujulaRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("TFG_debug", "UNO");
                float degree = Float.parseFloat(dataSnapshot.child("degree").getValue().toString());
                float currentDegree = Float.parseFloat(dataSnapshot.child("currentDegree").getValue().toString());
                float degreeB = Float.parseFloat(dataSnapshot.child("degreeB").getValue().toString());
                float currentDegreeB = Float.parseFloat(dataSnapshot.child("currentDegreeB").getValue().toString());
                Log.d("TFG_debug", "DOS");
                //txtAngle.setText("N: " + degree + "º");
                Log.d("TFG_debug", "TRES");
                RotateAnimation ra = new RotateAnimation(
                        currentDegree,
                        degree,
                        Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF,
                        0.5f);
                // el tiempo durante el cual la animación se llevará a cabo
                ra.setDuration(1000);
                // establecer la animación después del final de la estado de reserva
                ra.setFillAfter(true);
                // Inicio de la animacion
                imgCompass.startAnimation(ra);
                Log.d("TFG_debug", "CUATRO");
                //txtAngleB.setText("B: " + (int) degreeB + "º");
                RotateAnimation ra2 = new RotateAnimation(
                        degreeB,
                        degreeB,
                        Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF,
                        0.5f);
                ra.setDuration(1000);
                ra.setFillAfter(true);
                imgCompassB.startAnimation(ra2);
                Log.d("TFG_debug", "CINCO");
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("TFG_debug", "Failed to read value.", error.toException());
            }
        });

        DatabaseReference baseRef = database.getReference("GPS/base");
        baseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                latBase = Double.parseDouble(dataSnapshot.child("lat").getValue().toString());
                lonBase = Double.parseDouble(dataSnapshot.child("lon").getValue().toString());

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.d("TFG_debug", "Failed to read value.", error.toException());
            }
        });

        Log.d("TFG_debug", "END CREATE");
    }

    public void pulsado_boton_maps (View vista){

        vista.startAnimation(buttonClick);
        onMapReady(mMap);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

        UiSettings uiSettings = mMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);

        float zoom=20;
        LatLng drone = new LatLng(lat, lon);
        LatLng base = new LatLng(latBase, lonBase);
        mMap.addMarker(new MarkerOptions().position(drone).title("DRONE Position"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(drone,zoom));
        mMap.addMarker(new MarkerOptions().position(base).title("BASE Position").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
    }
}