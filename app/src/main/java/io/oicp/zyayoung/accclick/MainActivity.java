package io.oicp.zyayoung.accclick;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;

import java.net.URL;
import java.net.HttpURLConnection;

public class MainActivity extends Activity implements SensorEventListener {
    SensorManager sensormanager;
    EditText editText;
    EditText editText2;
    EditText editText3;
    WebView score;
    int tot_count = 0;
    int count = 0;
    int countdown = 10;
    int trigger_countdown = 100;
    int find_max_countdown = 0;
    float[] last_acc = new float[3];
    float[] last_last_acc = new float[3];
    float cur_max_jerk=0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        score=(WebView)findViewById(R.id.score);
        editText=(EditText)findViewById(R.id.editText1);
        editText2=(EditText)findViewById(R.id.editText2);
        editText3=(EditText)findViewById(R.id.editText3);
        sensormanager=(SensorManager)getSystemService(Context.SENSOR_SERVICE);
        final Button calibrate = (Button) findViewById(R.id.calibrate);
        final Button focus = (Button) findViewById(R.id.focus);

        calibrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(req_calibrate).start();
            }
        });
        focus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(req_focus).start();
            }
        });
    }


    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        sensormanager.registerListener(this, sensormanager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), 10000);
    }


    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        sensormanager.unregisterListener(this);
    }


    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub


    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            // TODO: http request.
            try {
                URL url = new URL("http://" + editText2.getText().toString() + ":3575/?action=click");
                HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
                urlConn.setConnectTimeout(1 * 1000);
                urlConn.setReadTimeout(1 * 1000);
                urlConn.setUseCaches(true);
                urlConn.setRequestMethod("GET");
                urlConn.setRequestProperty("Content-Type", "application/json");
                urlConn.addRequestProperty("Connection", "Keep-Alive");
                urlConn.connect();
                if (urlConn.getResponseCode() == 200) {
                    urlConn.getInputStream();
                }
                urlConn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    Runnable req_focus = new Runnable() {
        @Override
        public void run() {
            // TODO: http request.
            try {
                URL url = new URL("http://localhost:8080/focus");
                HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
                urlConn.setConnectTimeout(1 * 1000);
                urlConn.setReadTimeout(1 * 1000);
                urlConn.setUseCaches(true);
                urlConn.setRequestMethod("GET");
                urlConn.setRequestProperty("Content-Type", "application/json");
                urlConn.addRequestProperty("Connection", "Keep-Alive");
                urlConn.connect();
                if (urlConn.getResponseCode() == 200) {
                    urlConn.getInputStream();
                }
                urlConn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    Runnable req_calibrate = new Runnable() {
        @Override
        public void run() {
            // TODO: http request.
            try {
                URL url = new URL("http://" + editText2.getText().toString() + ":3575/?action=calibrate");
                HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
                urlConn.setConnectTimeout(1 * 1000);
                urlConn.setReadTimeout(1 * 1000);
                urlConn.setUseCaches(true);
                urlConn.setRequestMethod("GET");
                urlConn.setRequestProperty("Content-Type", "application/json");
                urlConn.addRequestProperty("Connection", "Keep-Alive");
                urlConn.connect();
                if (urlConn.getResponseCode() == 200) {
                    urlConn.getInputStream();
                }
                urlConn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };



    @Override
    public void onSensorChanged(SensorEvent event) {
        float[] values=event.values;
        float jerk_now =
                (last_acc[0]-values[0])*(last_acc[0]-values[0])+
                        (last_acc[1]-values[1])*(last_acc[1]-values[1])+
                        (last_acc[2]-values[2])*(last_acc[2]-values[2]);
        float jerk =
                (last_acc[0]-last_last_acc[0])*(last_acc[0]-last_last_acc[0])+
                (last_acc[1]-last_last_acc[1])*(last_acc[1]-last_last_acc[1])+
                (last_acc[2]-last_last_acc[2])*(last_acc[2]-last_last_acc[2]);

        tot_count+=1;
        float last_last_change =
                        (last_last_acc[0]-values[0])*(last_last_acc[0]-values[0])+
                        (last_last_acc[1]-values[1])*(last_last_acc[1]-values[1])+
                        (last_last_acc[2]-values[2])*(last_last_acc[2]-values[2]);

        if(!(jerk_now>100 && last_last_change < 4)) {
            if (Math.abs(jerk) > 100) {
                countdown -= 1;
            } else {
                countdown = 3;
            }
            if (countdown <= 0 && trigger_countdown == 0) {
                count += 1;
                find_max_countdown = 20;
                cur_max_jerk = jerk;
                new Thread(runnable).start();
                countdown = 3;
                trigger_countdown = 250;
            }
            trigger_countdown = Math.max(0, trigger_countdown - 1);

            if(find_max_countdown >=0){
                cur_max_jerk = Math.max(cur_max_jerk, jerk);
                if(find_max_countdown==0){
                    editText3.setText(String.valueOf(Math.sqrt(cur_max_jerk)));
                }
                find_max_countdown-=1;
            }

            last_last_acc[0] = last_acc[0];
            last_last_acc[1] = last_acc[1];
            last_last_acc[2] = last_acc[2];
        }
        if(tot_count > 100){
            score.loadUrl("http://" + editText2.getText().toString() + ":3575/");
            tot_count = 0;
        }



        editText.setText(String.valueOf(count));
        last_acc[0] = values[0];
        last_acc[1] = values[1];
        last_acc[2] = values[2];
    }

}
