package proyectos.jaime.tfg;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSION_ACCESS_CAMERA = 20;
    static final int MY_PERMISSION_ACCESS_FINE_LOCATION = 10;

    private AlphaAnimation buttonClick = new AlphaAnimation(1F, 0.6F);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("TFG_debug", "\nComienza APP\n");
    }

    public void pulsado_boton_land (View vista){

        vista.startAnimation(buttonClick);

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_ACCESS_FINE_LOCATION);
        }
        else {

            Intent i = new Intent(this, landActivity.class);
            startActivity(i);

        }


    }

    public void pulsado_boton_air (View vista){

        vista.startAnimation(buttonClick);

        if(ContextCompat.checkSelfPermission( this, Manifest.permission.CAMERA ) != PackageManager.PERMISSION_GRANTED ) {

            ActivityCompat.requestPermissions( this, new String[] {android.Manifest.permission.CAMERA}, MY_PERMISSION_ACCESS_CAMERA);
        }
        else {
            Intent i = new Intent(this, airActivity.class);
            startActivity(i);
        }
    }

    public void pulsado_boton_sett (View vista){

        vista.startAnimation(buttonClick);

        Intent i = new Intent(this, configActivity.class);
        startActivity(i);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_ACCESS_CAMERA: {

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Log.d("TFG_debug", "PERMISO ACEPTADO!");


                } else {

                    Log.d("TFG_debug", "PERMISO DENEGADO!");

                }
            } break;

            case MY_PERMISSION_ACCESS_FINE_LOCATION: {

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {


                    Log.d("TFG_debug", "PERMISO ACEPTADO!");

                } else {

                    Log.d("TFG_debug", "PERMISO DENEGADO!");

                }
            }break;
        }
    }
}