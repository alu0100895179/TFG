package proyectos.jaime.tfg;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import net.majorkernelpanic.streaming.gl.SurfaceView;

import java.util.ArrayList;
import java.util.List;
import static proyectos.jaime.tfg.StreamingClass.streamOk;


public class AirActivity extends Activity implements SensorEventListener, LocationListener {

    //private static SurfaceView mSurfaceView;

    private boolean aux = false;
    private double vect_x ;
    private double vect_y;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    private LocationManager locationManager;

    // guarda el angulo (grado) actual del compass
    private float currentDegree = 0f;
    private float currentDegreeB = 0f;
    private float currentDegreeR = 0f;

    // El sensor manager del dispositivo
    private SensorManager mSensorManager;

    // Los dos sensores que son necesarios porque TYPE_ORINETATION esta deprecated
    private Sensor accelerometer;
    private Sensor magnetometer;

    // Los angulos del movimiento de la flecha que señala al norte
    float degree;
    float degreeB;
    float degreeOld;
    // Guarda el valor del azimut
    float azimut;
    // Guarda los valores que cambián con las variaciones del sensor TYPE_ACCELEROMETER
    float[] mGravity;
    // Guarda los valores que cambián con las variaciones del sensor TYPE_MAGNETIC_FIELD
    float[] mGeomagnetic;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    protected void onCreate(Bundle savedInstanceState) {

        Log.d("TFG_debug", "AIR ACTIVITY");
        //Activity act = (Activity) this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.air_layout);

        Log.d("TFG_debug", "Comienza Streamming");
        SurfaceView mSurfaceView;
        mSurfaceView = findViewById(R.id.surface);
        StreamingClass st = new StreamingClass(this, mSurfaceView, 0);
        st.toggleStreaming();

        TextView record_text = (TextView) findViewById(R.id.record_text);
        comprobar_cambio(record_text);

        // Se inicializan los sensores del dispositivo android
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        Log.d("TFG_debug", "Declaramos sensores");
        mGravity = null;
        mGeomagnetic = null;

        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        String provider = get_best_provider();
        Log.d("TFG_debug", "BEST_PROVIDER");
        if(provider!=null) {
            locationManager.requestLocationUpdates(get_best_provider(), 500, 0, this);
            Log.d("TFG_debug", "AFTER LISTENER");
        }
        /*if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //Requiere permisos para Android 6.0
            Log.e("TFG_debug", "No se tienen permisos necesarios!, se requieren.");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 225);
        }else{
            Log.i("TFG_debug", "Permisos necesarios OK!.");
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 500, 0, this);
        }*/

    }

    private void comprobar_cambio(final TextView record_text){
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {

                if (streamOk==1)
                    record_text.setTextColor(getApplicationContext().getResources().getColor(R.color.green));
                else {
                    record_text.setTextColor(getApplicationContext().getResources().getColor(R.color.red));
                    comprobar_cambio(record_text);
                }
            }
        }, 3000);
    }

    /////////////////////////////////GPS/////////////////////////////////////////////////////////////////////
    private String get_best_provider(){
        List<String> providers = new ArrayList<>();
        try {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            providers = locationManager.getProviders(false);
        }
        catch (java.lang.NullPointerException e){
            Log.d("TFG_debug", "No se pudieron obtener proveedores!");
        }
        Location bestLocation = null;

        for (String provider : providers) {
            Location aux_loc;
            //if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            aux_loc = locationManager.getLastKnownLocation(provider);
            if (aux_loc != null) {
                if (bestLocation == null || aux_loc.getAccuracy() < bestLocation.getAccuracy()) {
                    bestLocation = aux_loc;
                }
            }
        }

        return bestLocation.getProvider();
    }

    @Override
    public void onLocationChanged(Location location) {

        //private latitud_est = 28.459983;
        //private double longitud_est = -16.274791;

        // Mi casa 28.460202, -16.274843

        TextView gps_text = (TextView) findViewById(R.id.gps_text);
        gps_text.setTextColor(this.getResources().getColor(R.color.green));

        double latBase = 28.463625;
        double lonBase = -16.276480;

        Log.d("TFG_debug", "updateWithNewLocation");

        double lat = location.getLatitude();
        double lon = location.getLongitude();
        double alt = location.getAltitude();
        double bearing = location.getBearing();
        double speed = location.getSpeed();
        String prov = location.getProvider();

        DatabaseReference latRef = database.getReference("GPS/drone/lat");
        DatabaseReference lonRef = database.getReference("GPS/drone/lon");
        DatabaseReference altRef = database.getReference("GPS/drone/alt");
        DatabaseReference bearingRef = database.getReference("GPS/drone/bearing");
        DatabaseReference speedRef = database.getReference("GPS/drone/speed");
        DatabaseReference provRef = database.getReference("GPS/drone/prov");
        DatabaseReference latBaseRef = database.getReference("GPS/base/lat");
        DatabaseReference lonBaseRef = database.getReference("GPS/base/lon");

        latRef.setValue(lat);
        lonRef.setValue(lon);
        altRef.setValue(alt);
        bearingRef.setValue(bearing);
        speedRef.setValue(speed);
        provRef.setValue(prov);
        latBaseRef.setValue(latBase);
        lonBaseRef.setValue(lonBase);

        vect_x = lonBase - lon;
        vect_y = latBase - lat;
    }

    @Override
    public void onProviderDisabled(String provider) {

        Log.d("TFG_debug", "Gps turned off");

        TextView gps_text = (TextView) findViewById(R.id.gps_text);
        gps_text.setTextColor(this.getResources().getColor(R.color.red));
    }

    @Override
    public void onProviderEnabled(String provider) {

        Log.d("TFG_debug", "Gps turned on");

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d("TFG_debug", "estao cambio");
    }

    ////////////////////////////////////////// BRUJULA /////////////////////////////////////////////////////
    public void onSensorChanged(SensorEvent event) {

        boolean envia=true;

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

            TextView compass_text = (TextView) findViewById(R.id.compass_text);
            compass_text.setTextColor(this.getResources().getColor(R.color.green));

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
        else{
            TextView compass_text = (TextView) findViewById(R.id.compass_text);
            compass_text.setTextColor(this.getResources().getColor(R.color.red));
        }
        degree = azimut;
        if(Math.abs(degreeOld-degree)<1)
            envia=false;
        if(envia) {
            DatabaseReference degreeRef = database.getReference("brujula/degree");
            DatabaseReference currentDegreeRef = database.getReference("brujula/currentDegree");
            DatabaseReference degreeBRef = database.getReference("brujula/degreeB");
            DatabaseReference currentDegreeBRef = database.getReference("brujula/currentDegreeB");

            /*Log.d("TFG_debug", "currentDegree= "+currentDegree);
            Log.d("TFG_debug", "degree= " + degree);*/
            currentDegree = -degree;
            if (Math.abs(degreeOld - degree) > 0.5)
                degreeRef.setValue(degree);
            degreeOld = degree;
            currentDegreeRef.setValue(currentDegree);

            if (aux) {
                /*Log.d("TFG_debug", "currentDegreeB= "+currentDegreeB);
                Log.d("TFG_debug", "degreeB= " + degreeB);*/

                degreeBRef.setValue(degreeB);
                currentDegreeBRef.setValue(currentDegreeB);
                //GIRAR
                currentDegreeB = -degreeB;
                currentDegreeBRef.setValue(currentDegreeB);
                aux = false;
            }
        }
        degreeOld=degree;

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
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

    /////////////////////////////////////// GRADOS BASE ///////////////////////////////////////////////
    private void calcular_base (){

        if (vect_x == 0) {
            if (vect_y == 0) {
                //Log.d("TFG_debug", "PARADOS");
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