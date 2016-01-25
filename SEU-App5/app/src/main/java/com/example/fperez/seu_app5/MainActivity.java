package com.example.fperez.seu_app5;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements LocationListener,SensorEventListener {

    private static final String ACTUALPOSITION = "actualposition";
    private static final String LAST_LAT = "lastLat";
    private static final String LAST_DISTANCE = "distance";
    private static final String LAST_LONG = "lastLong";
    private LocationManager locationManager;
    private MyReceiver receiver;
    private SensorManager sensorMgr;
    private Sensor sensor;
    static final private String TEMP="temp";
    static final private String AVERAGE="average";
    static final private String N="n";
    static final private String ACHTUNG = "agtum";

    private float temp=0;
    private float average = 0;
    private float n = 0;
    private boolean achtung = false;

    private CheckBox cbGPS = null;
    private CheckBox cbTemp = null;
    private double lastLat = 0;
    private double lastLong = 0;
    private float distance = 0;

    protected void onSaveInstanceState(Bundle estado){
        super.onSaveInstanceState(estado);
        estado.putDouble(LAST_LAT,lastLat);
        estado.putDouble(LAST_LONG,lastLong);
        estado.putFloat(LAST_DISTANCE, distance);
        estado.putFloat(TEMP, temp);
        estado.putFloat(N, n);
        estado.putFloat(AVERAGE, average);
        estado.putBoolean(ACHTUNG, achtung);

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cbGPS = (CheckBox) findViewById(R.id.rb_Gps);
        cbTemp = (CheckBox) findViewById(R.id.rbTMP);

        if(savedInstanceState != null){
            temp = savedInstanceState.getFloat(TEMP);
            n = savedInstanceState.getFloat(N);
            average = savedInstanceState.getFloat(AVERAGE);
            achtung = savedInstanceState.getBoolean(ACHTUNG);

            lastLat = savedInstanceState.getDouble(LAST_LAT);
            lastLong = savedInstanceState.getDouble(LAST_LONG);
            distance = savedInstanceState.getFloat(LAST_DISTANCE);
        }
        receiver = new MyReceiver();
        registerReceiver(receiver, new IntentFilter(Intent.ACTION_BATTERY_LOW));
        registerReceiver(receiver, new IntentFilter(Intent.ACTION_BATTERY_OKAY));

        initializeValues();


    }

    private void initializeValues() {

        TextView textView = (TextView) findViewById(R.id.tmpValue);
        textView.setText(Float.toString(temp));

        textView = (TextView) findViewById(R.id.txtAverage);
        textView.setText(Float.toString(average));

        textView = (TextView) findViewById(R.id.gpsPosition);

        String text = String.valueOf(getResources().getString(R.string.longi) + "= " + Double.toString(lastLong) + "; "+
                getResources().getString(R.string.lati) + "= " + Double.toString(lastLat) );
        textView.setText(text);

        textView = (TextView) findViewById(R.id.txtDistance);
        textView.setText(Double.toString(distance));

    }

    public void btnGPS(View view)
    {

        CheckBox gpsActivated = (CheckBox) view;
        if (gpsActivated.isChecked() && !isBatteryLow())
        {
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

            String mensaje = "Activating GPS";
            Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
            if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) &&
                    (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            } else {
                Toast.makeText(this, getResources().getString(R.string.error), Toast.LENGTH_LONG).show();
                finish();
            }
        }else if(!isBatteryLow())
        {
            String mensaje = "Desactivaing GPS";
            Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
            if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) &&
                    (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED))
                locationManager.removeUpdates(this);
        }

    }

    public void btnTemp(View view)
    {
        CheckBox tempActivated = (CheckBox) view;
        if (tempActivated.isChecked() && !isBatteryLow())
        {
            String mensaje = "Activating Temperature";
            //Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
            sensorMgr =  (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            if ((sensor = sensorMgr.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)) == null){
                //Toast.makeText(this, R.string.errorTemp, Toast.LENGTH_LONG).show();
                if ((sensor = sensorMgr.getDefaultSensor(Sensor.TYPE_TEMPERATURE)) == null) {
                    Toast.makeText(this, R.string.otroerror, Toast.LENGTH_LONG).show();
                    finish();
                }
            }
            sensorMgr.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST);
        }else
        {
            if(!isBatteryLow())
            {
                String mensaje = "Desactivaing Temperature";
                Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
                sensorMgr.unregisterListener(this);
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    public void onLocationChanged(Location location) {
        //String mensaje = getResources().getString(R.string.longi) + "= " + Double.toString(location.getLongitude()) +
        //        "; " + getResources().getString(R.string.lati) + "= " + Double.toString(location.getLatitude());
        //Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
        if(lastLong != 0 && lastLat != 0 )
        {
            float[] results = new float[3];
            Location.distanceBetween(lastLat, lastLong, location.getLatitude(), location.getLongitude(), results);
            distance = results[0];
        }else
        {
            lastLat = location.getLatitude();
            lastLong = location.getLongitude();
        }

        TextView textView = (TextView) findViewById(R.id.txtDistance);
        textView.setText(Double.toString(distance));

        textView = (TextView) findViewById(R.id.gpsPosition);
        String text = String.valueOf(getResources().getString(R.string.longi) + "= " + Double.toString(location.getLongitude()) + "; "+
                getResources().getString(R.string.lati) + "= " + Double.toString(location.getLatitude()) );
        textView.setText(text);
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    public void onProviderEnabled(String provider) {
    }

    public void onProviderDisabled(String provider) {
    }

    @Override
    protected void onResume() {
        super.onResume();
        turnOnComponents();

       // if(isTempActivated() && !isBatteryLow() )
       // {
       //     Log.d("SensorTemp", "reActivated");
       //     sensorMgr.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
       // }
       // if(isGpsActivated() && !isBatteryLow())
       // {
       //     Log.d("SensorGPS", "reActivated");
       //     if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) &&
       //             (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
       //         locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
       //     } else {
       //         Toast.makeText(this, getResources().getString(R.string.error), Toast.LENGTH_LONG).show();
       //         finish();
       //     }
       // }
    }
    @Override
    protected void onPause() {
        super.onPause();
        turnOffcomponents();
       // if(isTempActivated()) {
       //     sensorMgr.unregisterListener(this);
       //     Log.d("SensorTemp", "desactivated");
       // }
       // if(isGpsActivated()) {
       //     if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) &&
       //             (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED))
       //               locationManager.removeUpdates(this);
       //     Log.d("SensorGPS", "desactivated");
       // }
    }
    @Override
    public final void onSensorChanged(SensorEvent event) {
        if (temp!=event.values[0]) {
            Toast.makeText(this, getResources().getString(R.string.temp) + " / " + event.values[0], Toast.LENGTH_SHORT).show();
            temp=event.values[0];
            TextView textView = (TextView) findViewById(R.id.tmpValue);
            String text = String.valueOf(temp);
            textView.setText(text);
            getAverage(Float.parseFloat(text));
        }
    }
    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void getAverage(float newValue) {
            average += newValue;
            n++;
            TextView textView = (TextView) findViewById(R.id.txtAverage);
            textView.setText(Float.toString(average / n));
    }

    public boolean isTempActivated() {
        return cbTemp.isChecked();
    }

    public boolean isGpsActivated() {
        return cbGPS.isChecked();
    }

    public boolean isBatteryLow() {
        return achtung;
    }

    public void turnOnComponents() {
        if(isGpsActivated() && !isBatteryLow())
        {
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

            String mensaje = "Activating GPS";
            Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
            if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) &&
                    (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            } else {
                Toast.makeText(this, getResources().getString(R.string.error), Toast.LENGTH_LONG).show();
                finish();
            }
        }
        if(isTempActivated() && !isBatteryLow())
        {
            String mensaje = "Activating Temperature";
            Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
            sensorMgr =  (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            if ((sensor = sensorMgr.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)) == null){
                //Toast.makeText(this, R.string.errorTemp, Toast.LENGTH_LONG).show();
                if ((sensor = sensorMgr.getDefaultSensor(Sensor.TYPE_TEMPERATURE)) == null) {
                    Toast.makeText(this, R.string.otroerror, Toast.LENGTH_LONG).show();
                    finish();
                }
            }
            mensaje = "Activating Temperature";
            Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
            sensorMgr.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST);

        }
    }

    public void turnOffcomponents() {
        String mensaje = "Desactivaing GPS";
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) &&
                (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED))
            locationManager.removeUpdates(this);

        mensaje = "Desactivaing Temperature";
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
        sensorMgr.unregisterListener(this);
    }

    public class MyReceiver extends BroadcastReceiver {
        public MyReceiver() {}

        @Override
        public void onReceive(Context context, Intent intent) {
            achtung = (intent.getAction().equals("android.intent.action.BATTERY_LOW")) ;
            if(achtung)
                turnOffcomponents();
            else
                turnOnComponents();

        }
    }
}