package proyectos.jaime.tfg;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class landActivity extends Activity implements SensorEventListener {

    // Views donde se cargaran los elementos del XML
    private TextView txtAngle;
    private ImageView imgCompass;

    // guarda el angulo (grado) actual del compass
    private float currentDegree = 0f;

    // El sensor manager del dispositivo
    private SensorManager mSensorManager;
    // Los dos sensores que son necesarios porque TYPE_ORINETATION esta deprecated
    private Sensor accelerometer;
    private Sensor magnetometer;

    // Los angulos del movimiento de la flecha que señala al norte
    float degree;
    // Guarda el valor del azimut
    float azimut;
    // Guarda los valores que cambián con las variaciones del sensor TYPE_ACCELEROMETER
    float[] mGravity;
    // Guarda los valores que cambián con las variaciones del sensor TYPE_MAGNETIC_FIELD
    float[] mGeomagnetic;

    DecimalFormat dec1 = new DecimalFormat("#.0");
    DecimalFormat dec2 = new DecimalFormat("#.00");

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.land_layout);
        Log.d("TFG_debug", "ACTIVIDAD LAND");


        // ----------------------------------------------------------------------------------------
        // Se guardan en variables los elementos del layout
        imgCompass = (ImageView) findViewById(R.id.imgViewCompass);
        txtAngle = (TextView) findViewById(R.id.txtAngle);

        // Se inicializa los sensores del dispositivo android
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        mGravity = null;
        mGeomagnetic = null;
        // ----------------------------------------------------------------------------------------
    }

    // --------------------------------------------------------------------------------------------
    @Override
    protected void onResume() {
        super.onResume();

        // Se registra un listener para los sensores del accelerometer y el             magnetometer
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Se detiene el listener para no malgastar la bateria
        mSensorManager.unregisterListener(this);
    }

    public void onSensorChanged(SensorEvent event) {

        // Se comprueba que tipo de sensor está activo en cada momento
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                mGravity = event.values;
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                mGeomagnetic = event.values;
                break;
        }

        if ((mGravity != null) && (mGeomagnetic != null)) {
            float RotationMatrix[] = new float[16];
            boolean success = SensorManager.getRotationMatrix(RotationMatrix,                                                             null, mGravity, mGeomagnetic);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(RotationMatrix, orientation);
                azimut = orientation[0] * (180 / (float) Math.PI);
                if(azimut<0)
                    azimut=360+azimut;
            }
        }
        degree = azimut;
        txtAngle.setText("N: " + (int) degree + "º");
        //txtAngle.setText("N: " + Float.toString(aux) + "º");
        // se crea la animacion de la rottacion (se revierte el giro en grados, negativo)
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
        currentDegree = -degree;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    // --------------------------------------------------------------------------------------------

    public void getStreaming (View vista){

        VideoView videoRec= (VideoView) findViewById(R.id.videoReceiver);
        videoRec.setVisibility(View.VISIBLE);

        String viewSource = configActivity.STREAM_URL;
        Log.d("TFG_debug", "Intentado conectar a: " + configActivity.STREAM_URL);
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

        String latitudeS = "";
        String longitudeS = "";

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

        int latINT = (int) lat;
        latitudeS += latINT + "º";
        lat = (lat-latINT)*60;
        latINT = (int) lat;
        latitudeS += latINT + "'";
        lat = (lat-latINT)*60;
        latINT = (int) lat;
        latitudeS += latINT + "''";

        int lonINT = (int) lon;
        longitudeS += lonINT + "º";
        lon = (lon-lonINT)*60;
        lonINT = (int) lon;
        longitudeS += lonINT + "'";
        lon = (lon-lonINT)*60;
        lonINT = (int) lon;
        longitudeS += lonINT + "''";

        latText.setText(latitudeS);
        lonText.setText(longitudeS);
        altText.setText(dec2.format(alt) + "m");
        bearingText.setText(""+ dec2.format(bearing));
        speedText.setText("" + dec2.format(speed));

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