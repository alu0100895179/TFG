package proyectos.jaime.tfg;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import net.majorkernelpanic.streaming.gl.SurfaceView;
import static proyectos.jaime.tfg.StreamingClass.streamOk;

public class LandActivity extends Activity {

    int idNoti = 0;
    double lat, lon = 0;
    String CHANNEL_ID = "CHANNEL_1";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d("TFG_debug", "Activity:LAND.");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.land_layout);

        SurfaceView mSurfaceView;
        mSurfaceView = findViewById(R.id.surface);
        StreamingClass st = new StreamingClass(this, mSurfaceView, 1);
        st.toggleStreaming();
        Log.d("TFG_debug", "Streaming LAND");

        TextView record_text_land = (TextView) findViewById(R.id.record_text_land);
        comprobar_cambio(record_text_land);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference notiRef = database.getReference("notification");
        final DatabaseReference droneRef = database.getReference("GPS/drone");

        notiRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                idNoti = Integer.parseInt(dataSnapshot.getValue().toString());
                droneRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        lat = Double.parseDouble(dataSnapshot.child("lat").getValue().toString());
                        lon = Double.parseDouble(dataSnapshot.child("lon").getValue().toString());
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        // Failed to read value
                        Log.d("TFG_debug", "Failed to read value.", error.toException());
                    }
                });
                createNotificationChannel();
                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                        .setSmallIcon(R.drawable.droneicon)
                        .setContentTitle("Coordenadas del DRONE")
                        .setContentText("Latitud: " + lat + " | Longitud: "+ lon)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText("Latitud: " + lat + " | Longitud: "+ lon))
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
                notificationManager.notify(idNoti, mBuilder.build());
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
}