package proyectos.jaime.tfg;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import java.util.ArrayList;
import java.util.List;

public class landActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.land_layout);
        Log.d("TFG_debug", "ACTIVIDAD LAND");
    }

    public void getStreaming (View vista){

        VideoView videoRec= (VideoView) findViewById(R.id.videoReceiver);
        videoRec.setVisibility(View.VISIBLE);

        String viewSource = "rtsp://192.168.1.184:1935/live/android_test";

        videoRec.setVideoURI(Uri.parse(viewSource));
        //videoRec.setMediaController(new MediaController(this));
        //videoRec.requestFocus();
        //try{
        videoRec.start();
        //}catch()
    }

    public void getGPS(View vista){

        Log.d("TFG_debug", "Función getGPS");
        LocationManager locationManager = null;
        List<String> providers = new ArrayList<String>();
        try {
            locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
            providers = locationManager.getProviders(false);
        }
        catch (java.lang.NullPointerException e){
            Log.d("TFG_debug", "No se pudieron obtener proveedores!");
        }
        Location bestLocation = null;

        Log.d("TFG_debug", "Procedemos a obtener localización");
        for (String provider : providers) {
            Location aux =null;
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                aux = locationManager.getLastKnownLocation(provider);
            Log.d("TFG_debug", "Provider: " + provider + " => " + aux);
            if (aux != null) {
                if (bestLocation == null || aux.getAccuracy() < bestLocation.getAccuracy()) {
                    bestLocation = aux;
                }
            }
        }

        if (bestLocation != null) {
            Log.d("TFG_debug", "La mejor localización es: " + bestLocation);
            updateWithNewLocation(bestLocation);
        } else
            Log.d("TFG_debug", "No se pudo obtener localización...");
    }

    private void updateWithNewLocation(Location location){

        Log.d("TFG_debug", "updateWithNewLocation");

        TextView latText, lonText, altText, provText, bearingText, speedText;
        latText = (TextView) findViewById(R.id.latitudeV);
        lonText = (TextView) findViewById(R.id.longitudeV);
        altText = (TextView) findViewById(R.id.altitudeV);
        provText = (TextView) findViewById(R.id.provV);
        bearingText = (TextView) findViewById(R.id.bearingV);
        speedText = (TextView) findViewById(R.id.speedV);

        double lat = location.getLatitude();
        double lon = location.getLongitude();
        double alt = location.getAltitude();
        double bearing = location.getBearing();
        double speed = location.getSpeed();
        String prov = location.getProvider();

        Log.d("TFG_debug", "Lat: " + lat);
        Log.d("TFG_debug", "Long: " + lon);
        Log.d("TFG_debug", "Alt: " + alt);
        Log.d("TFG_debug", "Prov: " + prov);
        Log.d("TFG_debug", "Speed: " + speed);
        Log.d("TFG_debug", "Bear: " + bearing);
        latText.setText(""+lat);
        lonText.setText("" + lon);
        altText.setText("" + alt);
        bearingText.setText(""+ bearing);
        speedText.setText("" + speed);

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
}