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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class LandActivity extends Activity implements SensorEventListener {

    boolean aux = false;
    double vect_x ;
    double vect_y;

    // ----------------------------------------------------------------------------------------

    // Views donde se cargaran los elementos del XML
    private TextView txtAngle;
    private TextView txtAngleB;
    private ImageView imgCompass;
    private ImageView imgCompassB;
    private ImageView imgCompassR;

    // guarda el angulo (grado) actual del compass
    private float currentDegree = 0f;
    private float currentDegreeB = 0f;
    private float currentDegreeR = 0f;

    //double latitud_est = 28.459983;
    //double longitud_est = -16.274791;

    double latitud_est = 28.462292;
    double longitud_est = -16.277440;

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

    // ----------------------------------------------------------------------------------------

    DecimalFormat dec1 = new DecimalFormat("#.0");
    DecimalFormat dec2 = new DecimalFormat("#.00");

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.land_layout);
        Log.d("TFG_debug", "ACTIVIDAD LAND");

        Button auxButton = (Button) findViewById(R.id.button);
        getGPS(auxButton);


        // ----------------------------------------------------------------------------------------
        // Se guardan en variables los elementos del layout
        imgCompass = (ImageView) findViewById(R.id.imgViewCompass);
        imgCompassB = (ImageView) findViewById(R.id.imgViewArrowBlue);
        imgCompassR = (ImageView) findViewById(R.id.imgViewArrowRed);
        txtAngle = (TextView) findViewById(R.id.txtAngle);
        txtAngleB = (TextView) findViewById(R.id.txtAngleB);

        // Se inicializa los sensores del dispositivo android
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        mGravity = null;
        mGeomagnetic = null;
        // ----------------------------------------------------------------------------------------
    }