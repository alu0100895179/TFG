package proyectos.jaime.tfg;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

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

import java.text.DecimalFormat;

public class AcpActivity  extends Activity implements  OnMapReadyCallback{

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    int idNoti = 0;

    double alt, lat, lon, bearing, speed;
    double latBase, lonBase;
    double latLand, lonLand;

    DecimalFormat dec1 = new DecimalFormat("#.0");
    DecimalFormat dec2 = new DecimalFormat("#.00");

    private AlphaAnimation buttonClick = new AlphaAnimation(1F, 0.6F);

    MapFragment mapFragment;
    GoogleMap mMap;
    boolean viendo_mapa = false;
    float zoom=20;

    VideoView mVideoView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acp_layout);
        Log.d("TFG_debug", "ACTIVIDAD PMA");

        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mapFragment.getView().setVisibility(View.INVISIBLE);

        final TextView latText, lonText, altText, provText, bearingText, speedText;
        latText = (TextView) findViewById(R.id.latitudeV);
        lonText = (TextView) findViewById(R.id.longitudeV);
        altText = (TextView) findViewById(R.id.altitudeV);
        provText = (TextView) findViewById(R.id.provV);
        bearingText = (TextView) findViewById(R.id.bearingV);
        speedText = (TextView) findViewById(R.id.speedV);

        final ImageView imgCompass = findViewById(R.id.imgViewArrowBlack);
        final ImageView imgCompassB = findViewById(R.id.imgViewArrowBlue);
        final ImageView imgCompassR = findViewById(R.id.imgViewArrowRed);
        final TextView txtAngleV = findViewById(R.id.txtAngleV);
        final TextView txtAngleBV = findViewById(R.id.txtAngleBV);
        final TextView txtAngleRV = findViewById(R.id.txtAngleRV);

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

                if (viendo_mapa){
                    actualiza_mapa(-1);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.d("TFG_debug", "Failed to read value.", error.toException());
            }
        });

        DatabaseReference brujulaRef = database.getReference("compass");
        brujulaRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                float degree = Float.parseFloat(dataSnapshot.child("degree").getValue().toString());
                float currentDegree = Float.parseFloat(dataSnapshot.child("currentDegree").getValue().toString());
                float degreeB = Float.parseFloat(dataSnapshot.child("degreeB").getValue().toString());
                float currentDegreeB = Float.parseFloat(dataSnapshot.child("currentDegreeB").getValue().toString());
                float degreeR = Float.parseFloat(dataSnapshot.child("degreeR").getValue().toString());
                float currentDegreeR = Float.parseFloat(dataSnapshot.child("currentDegreeR").getValue().toString());

                txtAngleV.setText(""+(int) degree + " º");
                RotateAnimation ra = new RotateAnimation(
                        degree,
                        currentDegree,
                        Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF,
                        0.5f);

                ra.setDuration(1500);
                ra.setFillAfter(true);
                imgCompass.startAnimation(ra);

                txtAngleBV.setText(""+(int) degreeB + " º");
                RotateAnimation ra2 = new RotateAnimation(
                        degreeB,
                        currentDegreeB,
                        Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF,
                        0.5f);
                ra.setDuration(1500);
                ra.setFillAfter(true);
                imgCompassB.startAnimation(ra2);

                txtAngleRV.setText("" +(int) degreeR + " º");
                RotateAnimation ra3 = new RotateAnimation(
                        degreeR,
                        currentDegreeR,
                        Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF,
                        0.5f);
                ra.setDuration(1500);
                ra.setFillAfter(true);
                imgCompassR.startAnimation(ra3);
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

                if (viendo_mapa){
                    actualiza_mapa(-1);
                }

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.d("TFG_debug", "Failed to read value.", error.toException());
            }
        });


        DatabaseReference landRef = database.getReference("GPS/land");
        landRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                latLand = Double.parseDouble(dataSnapshot.child("lat").getValue().toString());
                lonLand = Double.parseDouble(dataSnapshot.child("lon").getValue().toString());

                if (viendo_mapa){
                    actualiza_mapa(-1);
                }

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.d("TFG_debug", "Failed to read value.", error.toException());
            }
        });

        Log.d("TFG_debug", "END CREATE");
    }

    public void pulsado_boton_notification (View vista){

        vista.startAnimation(buttonClick);
        idNoti++;
        database.getReference("notification").setValue(idNoti);

    }

    public void pulsado_boton_maps (View vista){

        vista.startAnimation(buttonClick);

        viendo_mapa = true;
        VideoView view = findViewById(R.id.videoReceiver);
        view.setVisibility(View.GONE);
        mapFragment.getView().setVisibility(View.VISIBLE);
        onMapReady(mMap);

    }

    public void pulsado_boton_drone (View vista){

        vista.startAnimation(buttonClick);

        viendo_mapa = false;
        mapFragment.getView().setVisibility(View.INVISIBLE);
        VideoView view = findViewById(R.id.videoReceiver);
        view.setVisibility(View.VISIBLE);

        String direccion = ConfigActivity.STREAM_URL_AIR;
        Uri video = Uri.parse(direccion);
        mVideoView = (VideoView)this.findViewById(R.id.videoReceiver);
        mVideoView.setVideoURI(video);
        mVideoView.setMediaController(new MediaController(this));
        mVideoView.requestFocus();
        mVideoView.postInvalidateDelayed(100);
        mVideoView.start();

    }

    public void pulsado_boton_man (View vista){

        vista.startAnimation(buttonClick);

        viendo_mapa = false;
        mapFragment.getView().setVisibility(View.INVISIBLE);
        VideoView view = findViewById(R.id.videoReceiver);
        view.setVisibility(View.VISIBLE);

        String direccion = ConfigActivity.STREAM_URL_LAND;
        Uri video = Uri.parse(direccion);
        mVideoView =  (VideoView)this.findViewById(R.id.videoReceiver);
        mVideoView.setVideoURI(video);
        mVideoView.setMediaController(new MediaController(this));
        mVideoView.requestFocus();
        mVideoView.postInvalidateDelayed(100);
        mVideoView.start();

    }

    public void actualiza_mapa(float zoom){

        mMap.clear();

        LatLng drone = new LatLng(lat, lon);
        LatLng base = new LatLng(latBase, lonBase);
        LatLng land = new LatLng(latLand, lonLand);
        if(zoom>0)
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(drone,zoom));
        mMap.addMarker(new MarkerOptions().position(drone).title(getString(R.string.mapsDroneST)).icon(BitmapDescriptorFactory.fromResource(R.drawable.dronemarker)));
        mMap.addMarker(new MarkerOptions().position(land).title(getString(R.string.mapsLandST)).icon(BitmapDescriptorFactory.fromResource(R.drawable.landmarker)));
        mMap.addMarker(new MarkerOptions().position(base).title(getString(R.string.mapsBaseST)).icon(BitmapDescriptorFactory.fromResource(R.drawable.basemarker)));

        // mMap.addMarker(new MarkerOptions().position(land).title("LAND Position").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        UiSettings uiSettings = mMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);

        actualiza_mapa(20);
    }
}
