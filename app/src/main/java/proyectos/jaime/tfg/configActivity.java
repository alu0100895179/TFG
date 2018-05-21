package proyectos.jaime.tfg;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

public class configActivity extends Activity {

    public static String STREAM_URL = "rtsp://awsatlas.duckdns.org:1935/casus/android";
    //public static String STREAM_URL = "rtsp://192.168.1.183:1935/casus/android_test";

    public static final String PUBLISHER_USERNAME = "casus";
    public static final String PUBLISHER_PASSWORD = "Casus_";

    public static String ip = "awsatlas.duckdns.org";
    //public static String ip = "192.168.1.183";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.d("TFG_debug", "configActivity");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.ip_settings);

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
                    STREAM_URL = "rtsp://" + ip + ":1935/casus/android";
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