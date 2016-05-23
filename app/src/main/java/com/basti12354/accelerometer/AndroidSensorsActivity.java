package com.basti12354.accelerometer;


import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;



import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Basti on 13.04.2016.
 */
public class AndroidSensorsActivity extends ExternalSensorsActivity implements SensorEventListener {


    // Variablen für AudioPlayer und Label
    private int choosenExercise = 0;
    public static String label;
    private int nextExerciseAudio;
    private MediaPlayer mp;


    public SensorManager sensorManager;
    public Sensor accelerometer, gyroscop, näherungssensor, linearAcc, magnetometer, rotationVector;



    // ACC Variablen
    private float deltaX = 0;
    private float deltaY = 0;
    private float deltaZ = 0;
    public float vibrateThreshold = 0;
 //   private TextView currentX, currentY, currentZ;
    public Vibrator v;

    // Linear ACC
    private float linearAccX, linearAccY, linearAccZ;

    // Gyro
    private float gyroX;
    private float gyroY;
    private float gyroZ;
  //  private TextView currentGyroX, currentGyroY, currentGyroZ;


    // Orientation -> Azimut, Pitch, Roll
    private float[] mMagneticValues;
    private float[] mAccelerometerValues;

    private  float mAzimuth;
    private  float mPitch;
    private  float mRoll;
    private static final int MATRIX_SIZE = 9;

    // Näherungssensor Variablen
    private float distance;


    // Rotationsvector
    private float rotationVectorX;
    private float rotationVectorY;
    private float rotationVectorZ;

    // Speicherplatz auf Gerät
    public String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Sensordaten";

    // Arrayliste für Sensordaten
    ArrayList<String> sensorData = new ArrayList<>();

    // Intervalldinge um in regelmäßigen Abständen zu speichern
    private android.os.Handler mHandler;
    private boolean timerIsRunning = false;
    // Intervall in welcher Zeit xy aufgerufen wird.
    private int mInterval = 10; // jede 10 ms! = 100 Hz  -> 20 ms wären 50 Hz

    private Date d;

    // Video
    public ProgressDialog pDialog;
    public VideoView videoView;

    // TextView Satzzähler
    public static int aktuellerSatz = 1;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.exercise_layout);

    }

//    public void initializeViews() {
//        currentX = (TextView) findViewById(R.id.currentX);
//        currentY = (TextView) findViewById(R.id.currentY);
//        currentZ = (TextView) findViewById(R.id.currentZ);
//
//        currentGyroX = (TextView) findViewById(R.id.gyroX);
//        currentGyroY = (TextView) findViewById(R.id.gyroY);
//        currentGyroZ = (TextView) findViewById(R.id.gyroZ);
//
//    }

    public void intitializeSmartphoneSensors(){
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        initializiseAccelerometer();

        initializiseLinearAccelerometer();

        initializiseNäherungssensor();

        initializiseGyroscop();

        initializiseMagnetometer();

        initializiseRotationVectorSensor();


        // ######## EXTERN ###########
        // Externe Sensoren
        initializiseExternSensors();


    }

    private void initializiseAccelerometer(){
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            // success! we have an accelerometer

            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            vibrateThreshold = accelerometer.getMaximumRange() / 2;
        } else {
            // fail we dont have an accelerometer!
        }
    }

    private void initializiseLinearAccelerometer(){
        if (sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) != null) {
            // success! we have an accelerometer

            linearAcc = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
            sensorManager.registerListener(this, linearAcc, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            // fail we dont have an accelerometer!
        }
    }

    private void initializiseNäherungssensor(){
        if (sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY) != null) {
            // success! we have an Näherungssensor

            näherungssensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
            sensorManager.registerListener(this, näherungssensor, SensorManager.SENSOR_DELAY_NORMAL);



        } else {
            // fail we dont have an accelerometer!
        }
    }

    private void initializiseGyroscop(){
        if (sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY) != null) {
            // success! we have an Näherungssensor

            gyroscop = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            sensorManager.registerListener(this, gyroscop, SensorManager.SENSOR_DELAY_NORMAL);

        } else {
            // fail we dont have an accelerometer!
        }
    }

    private void initializiseMagnetometer(){

            magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void initializiseRotationVectorSensor(){

        rotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        sensorManager.registerListener(this, rotationVector, SensorManager.SENSOR_DELAY_NORMAL);


    }

    //onResume() register the accelerometer for listening the events
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, näherungssensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, gyroscop, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, linearAcc, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    //onPause() unregister the accelerometer for stop listening the events
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {


        Sensor sensor = event.sensor;
        switch (event.sensor.getType()) {
            //######### Accelerometer #############
            case Sensor.TYPE_ACCELEROMETER:
                // clean current values
               // displayCleanValues();
                // display the current x,y,z accelerometer values
               // displayCurrentValues();

                deltaX = event.values[0];
                deltaY = event.values[1];
                deltaZ = event.values[2];

                // für Orientationberechnung
                mAccelerometerValues = event.values.clone();
                break;


                //######### Näherungssensor #############
            case Sensor.TYPE_PROXIMITY:
                //TODO: get values
                // Näherungssensor
                distance = event.values[0];
                break;
            //######### Gyroscop #############
            case  Sensor.TYPE_GYROSCOPE:

                gyroX = event.values[0];
                gyroY = event.values[1];
                gyroZ = event.values[2];

//                // Zeige Änderungen auf Display
//                currentGyroX.setText(Float.toString(gyroX));
//                currentGyroY.setText(Float.toString(gyroY));
//                currentGyroZ.setText(Float.toString(gyroZ));

            break;

            //######### Linear ACC #############
            case Sensor.TYPE_LINEAR_ACCELERATION:

                linearAccX = event.values[0];
                linearAccY = event.values[1];
                linearAccZ = event.values[2];
                break;

            case Sensor.TYPE_MAGNETIC_FIELD:
                mMagneticValues = event.values.clone();
                break;

            case Sensor.TYPE_ROTATION_VECTOR:
                rotationVectorX = event.values[0];
                rotationVectorY = event.values[1];
                rotationVectorZ = event.values[2];
                break;
        }

        if (mMagneticValues != null && mAccelerometerValues != null) {
            float[] R = new float[MATRIX_SIZE];
            SensorManager.getRotationMatrix(R, null, mAccelerometerValues, mMagneticValues);
            float[] orientation = new float[3];
            SensorManager.getOrientation(R, orientation);
            mAzimuth = orientation[0];
            mPitch = orientation[1];
            mRoll = orientation[2];
//            Log.i("OrientationTestActivity", String.format("Orientation: %f, %f, %f",
//                    orientation[0], orientation[1], orientation[2]));
        }



    }

//    public void displayCleanValues() {
//        currentX.setText("0.0");
//        currentY.setText("0.0");
//        currentZ.setText("0.0");
//    }
//
//    // display the current x,y,z accelerometer values
//    public void displayCurrentValues() {
//        currentX.setText(Float.toString(deltaX));
//        currentY.setText(Float.toString(deltaY));
//        currentZ.setText(Float.toString(deltaZ));
//    }

    private void saveSensorDataAndActualTimeToArray(){

        String label = "Liegestützen";

        // Aktuellen Timestamp
        Long tsLong = System.currentTimeMillis();
        String timestamp = tsLong.toString();

        Log.i( LOG + " DATEN", timestamp + ": ACC1: " +  Float.toString(ExternalSensorsActivity.extAccX)+ "," + Float.toString(ExternalSensorsActivity.extAccY)+ "," + Float.toString(ExternalSensorsActivity.extAccZ)
                + ",  ACC2: " + Float.toString(ExternalSensorsActivity.extAccX2)+ "," + Float.toString(ExternalSensorsActivity.extAccY2)+ "," + Float.toString(ExternalSensorsActivity.extAccZ2)
                + "," + Float.toString(ExternalSensorsActivity.extGyroX)+ "," + Float.toString(ExternalSensorsActivity.extGyroY)+ "," + Float.toString(ExternalSensorsActivity.extGyroZ)
                + "," + Float.toString(ExternalSensorsActivity.extGyroX2)+ "," + Float.toString(ExternalSensorsActivity.extGyroY2)+ "," + Float.toString(ExternalSensorsActivity.extGyroZ2));

        sensorData.add(label + "," + timestamp + "," + Float.toString(deltaX) + "," + Float.toString(deltaY) + "," + Float.toString(deltaZ)
                + "," + Float.toString(gyroX) + "," + Float.toString(gyroY) + "," + Float.toString(gyroZ)
                + "," + Float.toString(linearAccX) + "," + Float.toString(linearAccY) + "," + Float.toString(linearAccZ)
                + "," + Float.toString(mAzimuth)+ "," + Float.toString(mPitch)+ "," + Float.toString(mRoll)
                + "," + Float.toString(rotationVectorX)+ "," + Float.toString(rotationVectorY)+ "," + Float.toString(rotationVectorZ)
                + "," + Float.toString(distance) + "," + aktuellerSatz

                // Externe AccSensoren
                + "," + Float.toString(ExternalSensorsActivity.extAccX)+ "," + Float.toString(ExternalSensorsActivity.extAccY)+ "," + Float.toString(ExternalSensorsActivity.extAccZ)
                + "," + Float.toString(ExternalSensorsActivity.extAccX2)+ "," + Float.toString(ExternalSensorsActivity.extAccY2)+ "," + Float.toString(ExternalSensorsActivity.extAccZ2)
                + "," + Float.toString(ExternalSensorsActivity.extAccX3)+ "," + Float.toString(ExternalSensorsActivity.extAccY3)+ "," + Float.toString(ExternalSensorsActivity.extAccZ3)
                + "," + Float.toString(ExternalSensorsActivity.extAccX4)+ "," + Float.toString(ExternalSensorsActivity.extAccY4)+ "," + Float.toString(ExternalSensorsActivity.extAccZ4)

                // Extern Gyro
                + "," + Float.toString(ExternalSensorsActivity.extGyroX)+ "," + Float.toString(ExternalSensorsActivity.extGyroY)+ "," + Float.toString(ExternalSensorsActivity.extGyroZ)
                + "," + Float.toString(ExternalSensorsActivity.extGyroX2)+ "," + Float.toString(ExternalSensorsActivity.extGyroY2)+ "," + Float.toString(ExternalSensorsActivity.extGyroZ2)
                + "," + Float.toString(ExternalSensorsActivity.extGyroX3)+ "," + Float.toString(ExternalSensorsActivity.extGyroY3)+ "," + Float.toString(ExternalSensorsActivity.extGyroZ3)
                + "," + Float.toString(ExternalSensorsActivity.extGyroX4)+ "," + Float.toString(ExternalSensorsActivity.extGyroY4)+ "," + Float.toString(ExternalSensorsActivity.extGyroZ4)
                );

       // Log.d("Proximity", distance + "");
    //    Log.d("Linear", linearAccX + "," + linearAccY +  "," + linearAccZ);
    //    Log.d("ACC", Float.toString(deltaX) + "," + Float.toString(deltaY) + "," + Float.toString(deltaZ));
        // wenn Handy-Display in Himmel schaut
//        Log.d("Gyro","Orientation X (Roll) :" + Float.toString(gyroY) +""+
//                "Orientation Y (Pitch) :"+ Float.toString(gyroX) +""+
//                "Orientation Z (Yaw) :"+ Float.toString(gyroZ));
    }

    public void save(String fileName){
        File folder = new File(path);
        Log.d("Pfad", path);
        if (!folder.exists()){
            try{
                if(folder.mkdir()) {
                    System.out.println("Directory created");

                } else {
                    System.out.println("Directory is not created");


                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }
//        else {
//            System.out.println("Existiert bereits");
//            File fileTTS = new File(path + "/" + fileName +".txt");
//        }

        PrintWriter out = null;
        try {
            out = new PrintWriter(new FileWriter(path + "/" + fileName + ".txt"));
            for (String text : sensorData) {
                out.println(text);
            }
        } catch (IOException e) {
            System.err.println("Caught IOException: " +  e.getMessage());

        } finally {
            if (out != null) {
                out.close();
            }
        }

    }

    public void printArrayList(){
        for (int i = 0; i < sensorData.size(); i ++){
            Log.d(LOG + " List", sensorData.get(i));
        }
    }

    // ######### Starte die Sensoren und speichere Werte alle ... ms in Arraylist!
    public void startSensors(){

        mHandler = new android.os.Handler();

        startRepeatingTask();
        timerIsRunning = true;
    }

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                changeColorOfTextViews();

                // Hole mir alle Daten der Externen -> falls diese nicht bereit sind => 0 wird geliefert!
                getAccDataFromExtern();

                // Speichere die aktuellen Daten zur Arraylist
                saveSensorDataAndActualTimeToArray();
                //updateStatus(); //this function can change value of mInterval.
            } finally {
                // 100% guarantee that this always happens, even if
                // your update method throws an exception
                mHandler.postDelayed(mStatusChecker, mInterval);
            }
        }
    };

    void startRepeatingTask() {
        mStatusChecker.run();
    }


    public void stopSensors() {
        if (timerIsRunning) {
            mHandler.removeCallbacks(mStatusChecker);
            timerIsRunning = false;

            SimpleDateFormat dfDate = new SimpleDateFormat("dd-MM-HH-mm-ss");
            String data = "";
            Calendar c = Calendar.getInstance();
            data = dfDate.format(c.getTime());
            String datum = data.toString();
            String speicherName = MainActivity.probandenName + "_" + MainActivity.label + "_" + aktuellerSatz + "_" + datum;

            save(speicherName);
        }

    }
    private void playVideo(){

        videoView = (VideoView) findViewById(R.id.videoView);
        videoView.setVisibility(View.VISIBLE);


        try {
            MediaController mediaController = new MediaController(this);
            mediaController.setAnchorView(videoView);

            //  Uri vidUri = Uri.parse(vidAddress);
            Uri vidUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.liege);
            videoView.setMediaController(mediaController);
            videoView.setVideoURI(vidUri);

        }
        catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        videoView.requestFocus();
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                //pDialog.dismiss();
                videoView.start();
                //View view = findViewById(R.id.background);
                //view.setBackgroundColor(0xff000000);

                videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        videoView.setVisibility(View.GONE);

                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

                    }
                });

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);



        return super.onCreateOptionsMenu(menu);
    }







    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            playVideo();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){

        }
    }

    public void setExerciseLabelAndPlaySoundNextExercise(){
        switch (choosenExercise){
            case 0:
                choosenExercise = 1;
                label = "Crunch";
                nextExerciseAudio = R.raw.uebung_crunch;
                break;
            case 1:
                choosenExercise = 2;
                label = "Lunge";
                nextExerciseAudio = R.raw.uebung_ausfall;
                break;
            case 2:
                choosenExercise = 3;
                label = "Jumping Jack";
                nextExerciseAudio = R.raw.uebung_jumpingjack;
                break;
            case 3:
                choosenExercise = 4;
                label = "Bicycle Crunch";
                nextExerciseAudio = R.raw.uebung_bicycle;
                break;
            case 4:
                choosenExercise = 5;
                label = "Squat";
                nextExerciseAudio = R.raw.uebung_kniebeugen;
                break;
            case 5:
                choosenExercise = 6;
                label = "Mountain Climber";
                nextExerciseAudio = R.raw.uebung_bergsteiger;
                break;
            case 6:
                choosenExercise = 7;
                label = "Russian Twist";
                nextExerciseAudio = R.raw.uebung_russiantwist;
                break;
            case 7:
                choosenExercise = 8;
                label = "Push Up";
                nextExerciseAudio = R.raw.uebung_liegestuetzen;
                break;
        }
        playNextExerciseSound();
    }

    private void playNextExerciseSound() {


        mp = MediaPlayer.create(getBaseContext(), nextExerciseAudio);
        mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {

                mp.start();

            }
        });
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {

                mp.reset();
                mp.release();



            }
        });


    }


}
