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

    FirebaseDatabase database = FirebaseDatabase.getInstance();

    private LocationManager locationManager;
    double lat, lon, alt, bearing, speed = 0;
    double lat_old, lon_old = 0;
    private double vect_x, vect_y = 0;
    private double latBase, lonBase = 0;
    private boolean aux = false;
    private boolean first_location = true;

    // Valores de los grados
    private float currentDegree, currentDegreeB, currentDegreeR = 0f;
    float degree, degreeB, degreeOld = 0f;

    // El sensor manager del dispositivo
    private SensorManager mSensorManager;
    // Sensores para la brújula
    private Sensor accelerometer;
    private Sensor magnetometer;
    float azimut;
    float[] mGravity;
    float[] mGeomagnetic;

    ///////////////////////////////////////////////////////////////////// CONSTRUCTOR /////////////////////////////////////////////////////////////////////
    protected void onCreate(Bundle savedInstanceState) {

        Log.d("TFG_debug", "AIR ACTIVITY");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.air_layout);

        Log.d("TFG_debug", "Comienza Streamming");
        SurfaceView mSurfaceView;
        mSurfaceView = findViewById(R.id.surface_air);
        StreamingClass st = new StreamingClass(this, mSurfaceView, 0);
        st.toggleStreaming();

        TextView record_text_air = (TextView) findViewById(R.id.record_text_air);
        comprobar_cambio(record_text_air);

        Log.d("TFG_debug", "Sensores brujula");
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mGravity = null;
        mGeomagnetic = null;

        Log.d("TFG_debug", "Obtener proveedores de GPS.");
        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        String provider = get_best_provider();
        if(provider!=null) {
            locationManager.requestLocationUpdates(get_best_provider(), 500, 0, this);
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

    private void comprobar_cambio(final TextView record_text_air){
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {

                if (streamOk==1)
                    record_text_air.setTextColor(getApplicationContext().getResources().getColor(R.color.green));
                else {
                    record_text_air.setTextColor(getApplicationContext().getResources().getColor(R.color.red));
                    comprobar_cambio(record_text_air);
                }
            }
        }, 3000);
    }

    ///////////////////////////////////////////////////////////////////// GPS /////////////////////////////////////////////////////////////////////
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

        Log.d("TFG_debug", "onLocationChanged");
        TextView gps_text_air = (TextView) findViewById(R.id.gps_text_air);
        gps_text_air.setTextColor(this.getResources().getColor(R.color.green));

        DatabaseReference latRef = database.getReference("GPS/drone/lat");
        DatabaseReference lonRef = database.getReference("GPS/drone/lon");
        DatabaseReference altRef = database.getReference("GPS/drone/alt");
        DatabaseReference bearingRef = database.getReference("GPS/drone/bearing");
        DatabaseReference speedRef = database.getReference("GPS/drone/speed");
        DatabaseReference provRef = database.getReference("GPS/drone/prov");

        lat_old = lat;
        lon_old = lon;

        lat = location.getLatitude();
        lon = location.getLongitude();
        alt = location.getAltitude();
        bearing = location.getBearing();
        speed = location.getSpeed();
        String prov = location.getProvider();

        latRef.setValue(lat);
        lonRef.setValue(lon);
        altRef.setValue(alt);
        bearingRef.setValue(bearing);
        speedRef.setValue(speed);
        provRef.setValue(prov);

        if(first_location) {

            latBase = lat;
            lonBase = lon;
            first_location = false;

            DatabaseReference latBaseRef = database.getReference("GPS/base/lat");
            DatabaseReference lonBaseRef = database.getReference("GPS/base/lon");

            latBaseRef.setValue(latBase);
            lonBaseRef.setValue(lonBase);
        }
        vect_x = lonBase - lon;
        vect_y = latBase - lat;
    }

    @Override
    public void onProviderDisabled(String provider) {

        Log.d("TFG_debug", "Gps apagado");
        TextView gps_text_air = (TextView) findViewById(R.id.gps_text_air);
        gps_text_air.setTextColor(this.getResources().getColor(R.color.red));
    }

    @Override
    public void onProviderEnabled(String provider) {

        Log.d("TFG_debug", "Gps encendido");

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

        Log.d("TFG_debug", "Cambio de estado.");
    }

    ////////////////////////////////////////// BRUJULA /////////////////////////////////////////////////////
    public void onSensorChanged(SensorEvent event) {

        boolean envia=true;
        TextView compass_text_air = (TextView) findViewById(R.id.compass_text_air);

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

            compass_text_air.setTextColor(this.getResources().getColor(R.color.green));

            float RotationMatrix[] = new float[16];
            boolean success = SensorManager.getRotationMatrix(RotationMatrix, null, mGravity, mGeomagnetic);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(RotationMatrix, orientation);
                azimut = orientation[0] * (180 / (float) Math.PI);
                calcular_base();
                //calcular_rumbo();
                degreeB = (degreeB - azimut)%360;
                if (azimut < 0)
                    azimut = 360 + azimut;
                if (degreeB < 0)
                    degreeB = 360 + degreeB;
                aux=true;
            }
        }
        else{
            compass_text_air.setTextColor(this.getResources().getColor(R.color.red));
        }
        degree = azimut;
        if(Math.abs(degreeOld-degree)<1)
            envia=false;
        if(envia) {
            DatabaseReference degreeRef = database.getReference("compass/degree");
            DatabaseReference currentDegreeRef = database.getReference("compass/currentDegree");
            DatabaseReference degreeBRef = database.getReference("compass/degreeB");
            DatabaseReference currentDegreeBRef = database.getReference("compass/currentDegreeB");

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

        // Se registra un listener para los sensores del accelerometer y el magnetometer
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

    private void calcular_rumbo (){

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