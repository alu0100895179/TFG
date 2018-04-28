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

    String ipS = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ip_settings);

        Log.d("TFG_debug", "configActivity");
    }

    class EventoTeclado implements TextView.OnEditorActionListener {

        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

            Log.d("TFG_debug", "HOLI!");
            setContentView(R.layout.ip_settings);

            EventoTeclado teclado = new EventoTeclado();
            EditText ip = (EditText) findViewById(R.id.editText);
            ip.setOnEditorActionListener(teclado);

            Log.d("TFG_debug", "AAAAA");
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                EditText ip = (EditText) findViewById(R.id.editText);
                ipS = ip.getText().toString();
                Log.d("TFG_debug", "IP: " + ipS);
                InputMethodManager miteclado = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                miteclado.hideSoftInputFromWindow(ip.getWindowToken(), 0);
                setContentView(R.layout.activity_main);
                return true;
            }
            return false;
        }
    }
}
