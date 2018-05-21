package proyectos.jaime.tfg;

import android.content.Context;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.LOCATION_SERVICE;
import static android.content.Context.SENSOR_SERVICE;

public class sensorClass implements SensorEventListener {

    private boolean aux = false;
    private double vect_x ;
    private double vect_y;

    //private latitud_est = 28.459983;
    //private double longitud_est = -16.274791;

    private double latitud_est = 28.462292;
    private double longitud_est = -16.277440;

    // guarda el angulo (grado) actual del compass
    private float currentDegree = 0f;
    private float currentDegreeB = 0f;
    private float currentDegreeR = 0f;

    // El sensor manager del dispositivo
    private SensorManager mSensorManager;
    // Los dos sensores que son necesarios porque TYPE_ORINETATION esta deprecated
    private Sensor accelerometer;
    private Sensor magnetometer;

    float degreeB;

    // Los angulos del movimiento de la flecha que señala al norte
    float degree;
    // Guarda el valor del azimut
    float azimut;
    // Guarda los valores que cambián con las variaciones del sensor TYPE_ACCELEROMETER
    float[] mGravity;
    // Guarda los valores que cambián con las variaciones del sensor TYPE_MAGNETIC_FIELD
    float[] mGeomagnetic;

    FirebaseDatabase database = FirebaseDatabase.getInstance();

    DecimalFormat dec1 = new DecimalFormat("#.0");
    DecimalFormat dec2 = new DecimalFormat("#.00");

    public void main(Context mContext) {

        Log.d("TFG_debug", "CLASE SENSORES!");

        getGPS(mContext);

        // Se inicializa los sensores del dispositivo android
        mSensorManager = (SensorManager) mContext.getSystemService(SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        mGravity = null;
        mGeomagnetic = null;
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
            boolean success = SensorManager.getRotationMatrix(RotationMatrix, null, mGravity, mGeomagnetic);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(RotationMatrix, orientation);
                azimut = orientation[0] * (180 / (float) Math.PI);
                calcular_base();
                degreeB = (degreeB - azimut)%360;
                if (azimut < 0)
                    azimut = 360 + azimut;
                if (degreeB < 0)
                    degreeB = 360 + degreeB;
                aux=true;
            }
        }
        degree = azimut;

        DatabaseReference degreeRef = database.getReference("brujula/degree");
        DatabaseReference currentDegreeRef = database.getReference("brujula/currentDegree");
        DatabaseReference degreeBRef = database.getReference("brujula/degreeB");
        DatabaseReference currentDegreeBRef = database.getReference("brujula/currentDegreeB");
        degreeRef.setValue(degree);

        Log.d("TFG_debug", "currentDegree= "+currentDegree);
        Log.d("TFG_debug", "degree= " + degree);
        currentDegree = -degree;

        currentDegreeRef.setValue(currentDegree);

        if (aux){
            Log.d("TFG_debug", "currentDegreeB= "+currentDegreeB);
            Log.d("TFG_debug", "degreeB= " + degreeB);

            degreeBRef.setValue(degreeB);
            currentDegreeBRef.setValue(currentDegreeB);
            //GIRAR
            currentDegreeB = -degreeB;
            currentDegreeBRef.setValue(currentDegreeB);
            aux=false;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    private void getGPS(Context mContext){

        Log.d("TFG_debug", "Función getGPS");
        LocationManager locationManager = null;
        List<String> providers = new ArrayList<String>();
        try {
            locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
            providers = locationManager.getProviders(false);
        }
        catch (java.lang.NullPointerException e){
            Log.d("TFG_debug", "No se pudieron obtener proveedores!");
        }
        Location bestLocation = null;

        Log.d("TFG_debug", "Procedemos a obtener localización");
        for (String provider : providers) {
            Location aux_loc =null;
            if (ContextCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                aux_loc = locationManager.getLastKnownLocation(provider);
            Log.d("TFG_debug", "Provider: " + provider + " => " + aux_loc);
            if (aux_loc != null) {
                if (bestLocation == null || aux_loc.getAccuracy() < bestLocation.getAccuracy()) {
                    bestLocation = aux_loc;
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

        String latitudeS = "";
        String longitudeS = "";

        double lat = location.getLatitude();
        double lon = location.getLongitude();
        double alt = location.getAltitude();
        double bearing = location.getBearing();
        String prov = location.getProvider();

        DatabaseReference latRef = database.getReference("GPS/lat");
        DatabaseReference lonRef = database.getReference("GPS/lon");
        DatabaseReference altRef = database.getReference("GPS/alt");
        DatabaseReference bearingRef = database.getReference("GPS/bearing");
        DatabaseReference provRef = database.getReference("GPS/prov");

        latRef.setValue(lat);
        lonRef.setValue(lon);
        altRef.setValue(alt);
        bearingRef.setValue(bearingRef);
        provRef.setValue(prov);

        vect_x = longitud_est - lon;
        vect_y = latitud_est - lat;

        Log.d("TFG_debug", "Lat: " + lat);
        Log.d("TFG_debug", "Long: " + lon);
        Log.d("TFG_debug", "Alt: " + alt);
        Log.d("TFG_debug", "Prov: " + prov);
        Log.d("TFG_debug", "Bear: " + bearing);

    }

    private void calcular_base (){

        if (vect_x == 0) {
            if (vect_y == 0) {
                Log.d("TFG_debug", "PARADOS");
                // 0 grados - estamos parados
            } else {
                if (vect_y > 0) {
                    // 0 grados NORTE
                    Log.d("TFG_debug", "NORTE");
                } else {
                    degreeB = 180;
                    // 180 grados SUR
                    Log.d("TFG_debug", "SUR");
                }
            }
        } else if (vect_y == 0) {
            if (vect_x > 0) {
                degreeB = 90;
                // 90 grados ESTE
                Log.d("TFG_debug", "ESTE");
            } else {
                degreeB = 180;
                // 180 grados OESTE
                Log.d("TFG_debug", "OESTE");
            }
        } else {
            if (vect_x > 0) {
                if (vect_y > 0) {
                    //NE
                    degreeB = (float) Math.toDegrees(Math.atan(Math.abs(vect_y / vect_x)));
                } else {
                    //SE
                    degreeB = (float) Math.toDegrees(Math.atan(Math.abs(vect_y/vect_x)));
                    degreeB = 180 - degreeB;
                }
            } else {
                if (vect_y > 0) {
                    //NO
                    degreeB = (float) Math.toDegrees(Math.atan(Math.abs(vect_y / vect_x)));
                    degreeB = 360 - degreeB;
                } else {
                    //SO
                    degreeB = (float) Math.toDegrees(Math.atan(Math.abs(vect_y / vect_x)));
                    degreeB = 180 + degreeB;
                }
            }
        }
    }
}

    /*
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
    */
