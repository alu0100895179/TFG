package proyectos.jaime.tfg;

import android.app.Activity;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import net.majorkernelpanic.streaming.gl.SurfaceView;

import java.util.ArrayList;
import java.util.List;

import static proyectos.jaime.tfg.StreamingClass.streamOk;

public class LandActivity extends Activity implements LocationListener {

    FirebaseDatabase database = FirebaseDatabase.getInstance();

    boolean aux_first_noti = true;
    int idNoti = 0;
    String CHANNEL_ID = "CHANNEL_1";
    double lat_drone, lon_drone = 0;
    double lat, lon = 0;

    private LocationManager locationManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d("TFG_debug", "Activity:LAND.");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.land_layout);

        SurfaceView mSurfaceView;
        SurfaceHolder mSurfaceHolder;
        mSurfaceView = findViewById(R.id.surface_land);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.setSizeFromLayout();
        StreamingClass st = new StreamingClass(this, mSurfaceView, 1);
        st.toggleStreaming();
        Log.d("TFG_debug", "Streaming LAND");

        TextView record_text_land = (TextView) findViewById(R.id.record_text_land);
        comprobar_cambio(record_text_land);


        Log.d("TFG_debug", "Obtener proveedores de GPS.");
        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        String provider = get_best_provider();
        if(provider!=null) {
            locationManager.requestLocationUpdates(get_best_provider(), 500, 0, this);
        }

        final DatabaseReference notiRef = database.getReference("notification");
        final DatabaseReference droneRef = database.getReference("GPS/drone");

        notiRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                idNoti = Integer.parseInt(dataSnapshot.getValue().toString());
                droneRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        lat_drone = Double.parseDouble(dataSnapshot.child("lat").getValue().toString());
                        lon_drone = Double.parseDouble(dataSnapshot.child("lon").getValue().toString());
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        // Failed to read value
                        Log.d("TFG_debug", "Failed to read value.", error.toException());
                    }
                });
                String notiTitle = getString(R.string.notiTitleST);
                createNotificationChannel();
                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                        .setSmallIcon(R.drawable.droneicon)
                        .setContentTitle(notiTitle)
                        .setContentText(getString(R.string.latitudeST) + lat_drone + " | " + getString(R.string.longitudeST) + lon_drone)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(getString(R.string.latitudeST) + lat_drone + " | " + getString(R.string.longitudeST) + lon_drone))
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                if (aux_first_noti) {
                    aux_first_noti = false;
                }
                else {
                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
                    notificationManager.notify(idNoti, mBuilder.build());
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.d("TFG_debug", "Failed to read value.", error.toException());
            }
        });
    }

    private void createNotificationChannel(){
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "CHANNEL NAME";
            String description ="CHANNEL DESCRIPTION";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void comprobar_cambio(final TextView record_text_land){
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                if (streamOk==1)
                    record_text_land.setTextColor(getApplicationContext().getResources().getColor(R.color.green));
                else {
                    record_text_land.setTextColor(getApplicationContext().getResources().getColor(R.color.red));
                    comprobar_cambio(record_text_land);
                }
            }
        }, 3000);
    }

    private String get_best_provider(){
        List<String> providers = new ArrayList<>();
        try {
            Log.d("TFG_debug", "Try");
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            providers = locationManager.getProviders(false);
        }
        catch (java.lang.NullPointerException e){
            Log.d("TFG_debug", "No se pudieron obtener proveedores!");
        }
        Location bestLocation = null;
        Log.d("TFG_debug", "Prueba");
        for (String provider : providers) {
            Location aux_loc;
            //if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            Log.d("TFG_debug", "Prueba2");
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
        TextView gps_text = (TextView) findViewById(R.id.gps_text_land);
        gps_text.setTextColor(this.getResources().getColor(R.color.green));

        DatabaseReference latRef = database.getReference("GPS/land/lat");
        DatabaseReference lonRef = database.getReference("GPS/land/lon");

        lat = location.getLatitude();
        lon = location.getLongitude();

        latRef.setValue(lat);
        lonRef.setValue(lon);

    }

    @Override
    public void onProviderDisabled(String provider) {

        Log.d("TFG_debug", "Gps apagado");
        TextView gps_text = (TextView) findViewById(R.id.gps_text_land);
        gps_text.setTextColor(this.getResources().getColor(R.color.red));
    }

    @Override
    public void onProviderEnabled(String provider) {

        Log.d("TFG_debug", "Gps encendido");

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

        Log.d("TFG_debug", "Cambio de estado.");
    }
}