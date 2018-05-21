package proyectos.jaime.tfg;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import net.majorkernelpanic.streaming.gl.SurfaceView;

public class airActivity extends Activity {

    boolean permiso_camara = true;
    private static SurfaceView mSurfaceView;

    protected void onCreate(Bundle savedInstanceState) {

        Log.d("TFG_debug", "AIR ACTIVITY");
        Activity act = (Activity) this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.air_layout);

        mSurfaceView = findViewById(R.id.surface);
        StreamingClass st = new StreamingClass(act, mSurfaceView);
        st.toggleStreaming();

        //sensorClass msensor = new sensorClass();
        //msensor.main(this);
    }

}
