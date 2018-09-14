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

    public static String STREAM_URL_AIR = "rtsp://192.168.1.34:1935/casus/android_air";
    public static String STREAM_URL_LAND = "rtsp://192.168.1.34:1935/casus/android_land";

    public static final String PUBLISHER_USERNAME = "casus";
    public static final String PUBLISHER_PASSWORD = "Casus_";

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