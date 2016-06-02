package com.basti12354.accelerometer;


import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * KLASSE kümmert sich um alle Sensoren des SMARTPHONES
 */
public class AndroidSensorsActivity extends ExternalSensorsActivity implements SensorEventListener {


    // Variablen für AudioPlayer und Label
    public static int choosenExercise = 0;
    public static String label = "Crunch"; // Erste Übung Crunch -> für Benennung des Btn bereits hier instantiiert!
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

    // ################### Intervall in welcher Zeit xy aufgerufen wird. ##########################
    private int mInterval = 10; // jede 10 ms! = 100 Hz  -> 20 ms wären 50 Hz


    // TextView Satzzähler
    public static int aktuellerSatz = 1;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.exercise_layout);

    }


    public void intitializeSmartphoneSensors(){
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        // Fülle Arrayliste mit Sensordaten mit Attributen für das Machine Learning
        // Dies ist nur beim ersten Start der Sensoren nötig, da alle weiteren Sensordaten hinzugefügt werden!
      //  fillArraylistWithMachineLearningAttributes();

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


    // Fügt dem Array einen neuen EIntrag hinzu -> wird im Thread alle x-Millisekungen aufgerufen!
    private void saveSensorDataAndActualTimeToArray(){


        // Aktuellen Timestamp
        Long tsLong = System.currentTimeMillis();
        String timestamp = tsLong.toString();

        Log.i( LOG + " DATEN", timestamp + ": ACC1: " +  Float.toString(ExternalSensorsActivity.extAccX)+ "," + Float.toString(ExternalSensorsActivity.extAccY)+ "," + Float.toString(ExternalSensorsActivity.extAccZ)
                + ",  ACC2: " + Float.toString(ExternalSensorsActivity.extAccX2)+ "," + Float.toString(ExternalSensorsActivity.extAccY2)+ "," + Float.toString(ExternalSensorsActivity.extAccZ2)
                + "," + Float.toString(ExternalSensorsActivity.extGyroX)+ "," + Float.toString(ExternalSensorsActivity.extGyroY)+ "," + Float.toString(ExternalSensorsActivity.extGyroZ)
                + "," + Float.toString(ExternalSensorsActivity.extGyroX2)+ "," + Float.toString(ExternalSensorsActivity.extGyroY2)+ "," + Float.toString(ExternalSensorsActivity.extGyroZ2));

      //  Log.i( LOG + " Array:", label);
                sensorData.add(label  + "," + aktuellerSatz + "," + timestamp + "," + Float.toString(deltaX) + "," + Float.toString(deltaY) + "," + Float.toString(deltaZ)
                + "," + Float.toString(gyroX) + "," + Float.toString(gyroY) + "," + Float.toString(gyroZ)
                + "," + Float.toString(linearAccX) + "," + Float.toString(linearAccY) + "," + Float.toString(linearAccZ)
                + "," + Float.toString(mAzimuth)+ "," + Float.toString(mPitch)+ "," + Float.toString(mRoll)
                + "," + Float.toString(rotationVectorX)+ "," + Float.toString(rotationVectorY)+ "," + Float.toString(rotationVectorZ)
                + "," + Float.toString(distance)

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

    // Speichert die Arrayliste als File und erstellt eine Ordnerstruktur auf dem Smartphone
    //            /Sensordaten/NAME-PROBAND/
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
        File subFolder = new File(path + "/" + MainActivity.probandenName);
        Log.d("Pfad", path);
        if (!subFolder.exists()){
            try{
                if(subFolder.mkdir()) {
                    Log.i(LOG,"subFolder created");

                } else {
                    Log.i(LOG,"subFolder is not created");


                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }


        PrintWriter out = null;
        try {
            out = new PrintWriter(new FileWriter(path + "/" + MainActivity.probandenName + "/" + fileName + ".txt"));
            for (String text : sensorData) {
                out.println(text);
            }
        } catch (IOException e) {
            System.err.println("Caught IOException: " +  e.getMessage());

        } finally {
            if (out != null) {
                out.close();

                // Löscht die Arrayliste, damit die Daten nicht doppelt gespeichert werden
                sensorData.clear();
            }
        }

    }

    public void printArrayList(){
        for (int i = 0; i < sensorData.size(); i ++){
            Log.d(LOG + " List", sensorData.get(i));
        }
    }

    // ######### Starte die Sensoren (auch die externen) und speichere Werte alle ... ms in Arraylist!
    public void startSensors(){

        getAllDataFromExternalSensors();

        mHandler = new android.os.Handler();

        startRepeatingTask();
        timerIsRunning = true;


    }

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                // Ändert die Farben der TextViews je nach verbundenen EXTERNEN Sensoren
                changeColorOfTextViews();

                // Speichere die aktuellen Daten zur Arraylist
                saveSensorDataAndActualTimeToArray();

            } finally {
                // Ruft die Methode nach Ausführung erneut auf
                mHandler.postDelayed(mStatusChecker, mInterval);
            }
        }
    };

    void startRepeatingTask() {
        mStatusChecker.run();
    }


    // Thread wird gestoppt
    // Gesammelten Daten werden als File abgespeichert
    public void stopSensors() {
        if (timerIsRunning) {
            mHandler.removeCallbacks(mStatusChecker);
            timerIsRunning = false;


            SimpleDateFormat dfDate = new SimpleDateFormat("dd-MM_HH-mm-ss");
            String data = "";
            Calendar c = Calendar.getInstance();
            data = dfDate.format(c.getTime());
            String datum = data.toString();
            String speicherName = MainActivity.probandenName + "_" + label + "_" + aktuellerSatz + "_" + datum;

            save(speicherName);
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);


        return super.onCreateOptionsMenu(menu);
    }


    // Ändert die Anzeige der aktuellen Übung
    // Spielt außerdem ein Soundfile ab -> "Nächste ÜBUNG ...."
    public void setExerciseLabelAndPlaySoundNextExercise(){
        switch (choosenExercise){
            case 0:
                if (aktuellerSatz == 2){
                    choosenExercise = 1;
                }

                label = "Crunch";
                nextExerciseAudio = R.raw.uebung_crunch;
                break;
            case 1:
                if (aktuellerSatz == 2){
                choosenExercise = 2;
                }
                label = "Ausfallschritt";
                nextExerciseAudio = R.raw.uebung_ausfall;
                break;
            case 2:
                if (aktuellerSatz == 2){
                choosenExercise = 3;
                }
                label = "Hampelmann";
                nextExerciseAudio = R.raw.uebung_jumpingjack;
                break;
            case 3:
                if (aktuellerSatz == 2){
                choosenExercise = 4;
                }
                label = "Bicycle Crunch";
                nextExerciseAudio = R.raw.uebung_bicycle;
                break;
            case 4:
                if (aktuellerSatz == 2){
                choosenExercise = 5;
                }
                label = "Kniebeugen";
                nextExerciseAudio = R.raw.uebung_kniebeugen;
                break;
            case 5:
                if (aktuellerSatz == 2){
                choosenExercise = 6;
                }
                label = "Bergsteiger";
                nextExerciseAudio = R.raw.uebung_bergsteiger;
                break;
            case 6:
                if (aktuellerSatz == 2){
                choosenExercise = 7;
                }
                label = "Russian Twist";
                nextExerciseAudio = R.raw.uebung_russiantwist;
                break;
            case 7:
                if (aktuellerSatz == 2) {
                    choosenExercise = 8;
                }
                label = "Liegestuetz";
                nextExerciseAudio = R.raw.uebung_liegestuetzen;
                break;
        }
        playNextExerciseSound();
        TextView uebung = (TextView) findViewById(R.id.exerciseName);
        uebung.setText(label);

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
