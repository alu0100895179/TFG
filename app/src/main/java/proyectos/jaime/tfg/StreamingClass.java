package proyectos.jaime.tfg;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.SurfaceHolder;

import net.majorkernelpanic.streaming.Session;
import net.majorkernelpanic.streaming.SessionBuilder;
import net.majorkernelpanic.streaming.audio.AudioQuality;
import net.majorkernelpanic.streaming.gl.SurfaceView;
import net.majorkernelpanic.streaming.rtsp.RtspClient;
import net.majorkernelpanic.streaming.video.VideoQuality;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StreamingClass implements RtspClient.Callback, Session.Callback, SurfaceHolder.Callback {

    private Context mContext;
    private SharedPreferences prefs;

    private SurfaceView mSurfaceView;

    private Session mSession;
    private RtspClient mClient;

    private String ip, port, path;

    private String firebaseKey;

    int tipo = 0;
    public static int streamOk = -1;

    public StreamingClass(Context context, SurfaceView surfaceView,int tipo_aux) {
        tipo=tipo_aux;
        mContext = context;
        prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

        mSurfaceView = surfaceView;
        mSurfaceView.getHolder().addCallback(this);

        initRtspClient();
    }

    public void setFirebaseKey(String key) {
        firebaseKey = key;
    }

    private void initRtspClient() {
        // Configure the SessionBuilder
        mSession = SessionBuilder.getInstance()
                .setContext(mContext)
                .setAudioEncoder(SessionBuilder.AUDIO_NONE)
                .setVideoEncoder(SessionBuilder.VIDEO_H264)
                //.setVideoQuality(new VideoQuality(352, 288, 30, 400000))
                .setVideoQuality(new VideoQuality(176,144,10,200000))
                .setSurfaceView(mSurfaceView)
                .setPreviewOrientation(0)
                .setCallback(this)
                .build();
                //.setAudioQuality(new AudioQuality(8000, 16000))
                //.setVideoQuality(new VideoQuality(176,144,10,200000))

        mSession.setPreviewOrientation(90);
        mSession.configure();

        // Configure the RTSP client
        mClient = new RtspClient();
        mClient.setSession(mSession);
        mClient.setCallback(this);
        mSurfaceView.setAspectRatioMode(SurfaceView.ASPECT_RATIO_PREVIEW);

        setServerData();
    }

    public void toggleStreaming() {

        if(!mClient.isStreaming()) {
            // Update the server url, username and password before starting the streaming
            setServerData();
            mSession.startPreview();
            mClient.startStream();
        } else {
            mSession.stopPreview();
            mClient.stopStream();
        }
    }


    private void setServerData() {
        // Parse the URI
        Pattern uri = Pattern.compile("rtsp://(.+):(\\d+)/(.+)");
        Matcher m;

        if(tipo==0)
            m = uri.matcher(ConfigActivity.STREAM_URL_AIR);
        else
           m = uri.matcher(ConfigActivity.STREAM_URL_LAND);

        m.find();
        ip = m.group(1);
        port = m.group(2);
        path = m.group(3);

        mClient.setCredentials(ConfigActivity.PUBLISHER_USERNAME, ConfigActivity.PUBLISHER_PASSWORD);
        mClient.setServerAddress(ip, Integer.parseInt(port));
        mClient.setStreamPath("/" + path);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        Log.d("TFG_debug", "surfaceCreated");
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        Log.d("TFG_debug", "surfaceChanged");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        Log.d("TFG_debug", "surfaceDestroyed");
    }

    @Override
    public void onRtspUpdate(int message, Exception exception) {
        Log.d("TFG_debug", "onRtspUpdate");
    }

    @Override
    public void onBitrateUpdate(long bitrate) {
        //Log.d("TFG_debug", "onBitrateUpdate");
    }

    @Override
    public void onSessionError(int reason, int streamType, Exception e) {
        Log.d("TFG_debug", "onSessionError");
    }

    @Override
    public void onPreviewStarted() {
        Log.d("TFG_debug", "onPreviewStarted");
    }

    @Override
    public void onSessionConfigured() {
        Log.d("TFG_debug", "onSessionConfigured");
    }

    @Override
    public void onSessionStarted() {
        Log.d("TFG_debug", "onSessionStarted");
        streamOk=1;
    }

    @Override
    public void onSessionStopped() {
        Log.d("TFG_debug", "onSessionStopped");
        streamOk=-1;
    }
}