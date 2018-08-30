package proyectos.jaime.tfg;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

public class ConfigActivity extends Activity {

    //public static String STREAM_URL = "rtsp://awsatlas.duckdns.org:1935/casus/android";
    public static String STREAM_URL_AIR = "rtsp://192.168.1.34:1935/casus/android_air";
    public static String STREAM_URL_LAND = "rtsp://192.168.1.34:1935/casus/android_land";


    public static final String PUBLISHER_USERNAME = "casus";
    public static final String PUBLISHER_PASSWORD = "Casus_";

    //public static String ip = "awsatlas.duckdns.org";
    public static String ip = "192.168.1.34";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.d("TFG_debug", "ConfigActivity");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.config_layout);

        EventoTeclado evTeclado = new EventoTeclado();
        EditText ipT = (EditText) findViewById(R.id.editText);
        ipT.setOnEditorActionListener(evTeclado);
    }

    class EventoTeclado implements TextView.OnEditorActionListener {

        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

            if (actionId == EditorInfo.IME_ACTION_DONE){

                EditText ipT = (EditText) findViewById(R.id.editText);
                String auxIP = (String) ipT.getText().toString();
                if(auxIP!=""){
                    ip=auxIP;
                    STREAM_URL_AIR = "rtsp://" + ip + ":1935/casus/android_air";
                    STREAM_URL_LAND = "rtsp://" + ip + ":1935/casus/android_land";
                }
                Log.d("TFG_debug", "IP: " + ip);

                InputMethodManager miteclado = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                miteclado.hideSoftInputFromWindow(ipT.getWindowToken(), 0);

                Log.d("TFG_debug", "Nos vamos!");
                finish();
                return true;
            }
            return false;
        }
    }
}

/* BACKUP -> LAND ACTIVITY
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
                aux = true;
            }
        }
        degree = azimut;
        txtAngle.setText("N: " + (int) degree + "º");
        Log.d("TFG_debug", "currentDegree= "+currentDegree);
        Log.d("TFG_debug", "degree= " + degree);
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

        if (aux){
            Log.d("TFG_debug", "currentDegreeB= "+currentDegreeB);
            Log.d("TFG_debug", "degreeB= " + degreeB);
            txtAngleB.setText("B: " + (int) degreeB + "º");
            RotateAnimation ra2 = new RotateAnimation(
                    degreeB,
                    degreeB,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF,
                    0.5f);
            ra.setDuration(1000);
            ra.setFillAfter(true);
            imgCompassB.startAnimation(ra2);
            currentDegreeB = -degreeB;
            aux=false;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    // --------------------------------------------------------------------------------------------

    public void getStreaming (View vista){

        VideoView videoRec= (VideoView) findViewById(R.id.videoReceiver);
        videoRec.setVisibility(View.VISIBLE);

        String viewSource = ConfigActivity.STREAM_URL;
        Log.d("TFG_debug", "Intentado conectar a: " + ConfigActivity.STREAM_URL);
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

        vect_x = longitud_est - lon;
        vect_y = latitud_est - lat;

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
*/