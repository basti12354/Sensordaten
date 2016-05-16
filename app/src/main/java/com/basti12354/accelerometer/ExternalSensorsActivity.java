package com.basti12354.accelerometer;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.mbientlab.metawear.AsyncOperation;
import com.mbientlab.metawear.Message;
import com.mbientlab.metawear.MetaWearBleService;
import com.mbientlab.metawear.MetaWearBoard;
import com.mbientlab.metawear.RouteManager;
import com.mbientlab.metawear.UnsupportedModuleException;
import com.mbientlab.metawear.data.CartesianFloat;
import com.mbientlab.metawear.module.*;
import com.mbientlab.metawear.module.Accelerometer;

import java.util.ArrayList;

/**
 * Created by Basti on 06.05.2016.
 */
public class ExternalSensorsActivity extends AppCompatActivity implements ServiceConnection {

    public static float extAccX = 0, extAccY = 0, extAccZ = 0, extAccX2 = 0, extAccY2 = 0, extAccZ2 = 0,
            extAccX3 = 0, extAccY3 = 0, extAccZ3 = 0, extAccX4 = 0, extAccY4 = 0, extAccZ4 = 0;

    public static float extGyroX = 0, extGyroY = 0, extGyroZ = 0, extGyroX2 = 0, extGyroY2 = 0, extGyroZ2 = 0,
            extGyroX3 = 0, extGyroY3 = 0, extGyroZ3 = 0, extGyroX4 = 0, extGyroY4 = 0, extGyroZ4 = 0;

    // Externe Sensoren!
    private MetaWearBleService.LocalBinder serviceBinder;

    TextView textViewSensor1, textViewSensor2, textViewSensor3, textViewSensor4;

    private boolean[] isSensorConnected = {false, false, false, false};

    private ArrayList<MetaWearBoard> metaWearBoards = new ArrayList<MetaWearBoard>();
    private ArrayList<Bmi160Accelerometer> accelerometerArrayList = new ArrayList<Bmi160Accelerometer>();
    private ArrayList<Bmi160Gyro> gyroArrayList = new ArrayList<Bmi160Gyro>();

    public final String LOG = "Externe Sensoren";

    private boolean sensor1, sensor2, sensor3, sensor4;

    private int anzahl_der_externen_sensoren = 2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.exercise_layout);

    }

    public void initializiseExternSensors(){
        // Bind the service when the activity is created
        getApplicationContext().bindService(new Intent(this, MetaWearBleService.class),
                this, Context.BIND_AUTO_CREATE);
          Log.i(LOG, "Service Bind");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Unbind the service when the activity is destroyed
        getApplicationContext().unbindService(this);


    }

    // Externe Sensoren
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        // Typecast the binder to the service's LocalBinder class
        serviceBinder = (MetaWearBleService.LocalBinder) service;

         Log.i(LOG, "Service Bind2");

        // Replace with boards MAC Adress
        final String mwMacAddress = "D1:C4:AA:BF:0B:22";
        final String mwMacAddress2 = "C6:9E:B6:91:A5:F7";

        final ArrayList<String> macAdressen = new ArrayList<String>();
        macAdressen.add(mwMacAddress);
        macAdressen.add(mwMacAddress2);

        BluetoothManager btManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);


        for (int i = 0; i < anzahl_der_externen_sensoren; i++) {

            BluetoothDevice btDevice = btManager.getAdapter().getRemoteDevice(macAdressen.get(i));
            final MetaWearBoard metaWearBoard = serviceBinder.getMetaWearBoard(btDevice);


            final int zahl = i;

            metaWearBoard.setConnectionStateHandler(new MetaWearBoard.ConnectionStateHandler() {

                @Override
                public void connected() {
                    ArrayList<String> kopieDerMacAdresse = macAdressen;
                    Log.i(LOG, kopieDerMacAdresse.get(zahl) + " Connected");

                    metaWearBoards.add(metaWearBoard);

                    connectedSensorLED(metaWearBoard);

                    initializeAccExtern(metaWearBoard);
                    initializeGyroExtern(metaWearBoard);

                    isSensorConnected[zahl] = true;

                }

                @Override
                public void disconnected() {
                    Log.i(LOG, "Connected Lost");
                    isSensorConnected[zahl] = false;
                }

                @Override
                public void failure(int status, Throwable error) {
                    Log.e(LOG, "Error connecting", error);
                    Toast.makeText(getBaseContext(), "EXTERNE Sensoren NICHT richtig verbunden!", Toast.LENGTH_SHORT).show();
                }
            });

            metaWearBoard.connect();


        }


    }



    @Override
    public void onServiceDisconnected(ComponentName componentName) { }


    private void initializeAccExtern(MetaWearBoard metaWearBoard){

        for (int i = 0; i < anzahl_der_externen_sensoren; i++) {
            try {

                Bmi160Accelerometer  accelerometer = metaWearBoard.getModule(Bmi160Accelerometer.class);

                // Set the sampling frequency to 50Hz, or closest valid ODR
                accelerometer.configureAxisSampling()
                        .setFullScaleRange(Bmi160Accelerometer.AccRange.AR_4G)
                        .setOutputDataRate(Bmi160Accelerometer.OutputDataRate.ODR_200_HZ)
                        .commit();
                // enable axis sampling
                accelerometer.enableAxisSampling();
                // Switch the accelerometer to active mode
                accelerometer.start();
                Log.i(LOG, "Externe-ACC Sensoren GESTARTET");


                accelerometerArrayList.add(accelerometer);
            }


            catch (UnsupportedModuleException e) {
                e.printStackTrace();
            }


        }



    }

    private void initializeGyroExtern(MetaWearBoard metaWearBoard) {
        Log.i(LOG, "Starte ExterneGYRO Sensoren");
        for (int i = 0; i < anzahl_der_externen_sensoren; i++) {
            try {
                Bmi160Gyro bmi160Gyro = metaWearBoard.getModule(Bmi160Gyro.class);


                bmi160Gyro.configure()
                        .setFullScaleRange(Bmi160Gyro.FullScaleRange.FSR_2000)
                        .setOutputDataRate(Bmi160Gyro.OutputDataRate.ODR_200_HZ)
                        .commit();
                bmi160Gyro.start();

                Log.i(LOG, "Externe-GYRO Sensoren GESTARTET");

                gyroArrayList.add(bmi160Gyro);
            }
            catch (UnsupportedModuleException e) {
                e.printStackTrace();
            }
        }


    }


    public void getAccDataFromExtern(){
        final String acc1 = "acc_stream_key";
        final String acc2 = "acc_stream_key2";


                accelerometerArrayList.get(0).routeData().fromAxes().stream(acc1).commit().onComplete(new AsyncOperation.CompletionHandler<RouteManager>() {
                    @Override
                    public void success(RouteManager result) {
                        result.subscribe(acc1, new RouteManager.MessageHandler() {
                            @Override
                            public void process(Message message) {
                                CartesianFloat axes = message.getData(CartesianFloat.class);
                             //   Log.i(acc1, message.getData(CartesianFloat.class).toString());
                                extAccX = axes.x();
                                extAccY = axes.y();
                                extAccZ = axes.z();
                            }
                        });

                    }
                });

        accelerometerArrayList.get(1).routeData().fromAxes().stream(acc2).commit().onComplete(new AsyncOperation.CompletionHandler<RouteManager>() {
                    @Override
                    public void success(RouteManager result) {
                        result.subscribe(acc2, new RouteManager.MessageHandler() {
                            @Override
                            public void process(Message message) {
                                CartesianFloat axes = message.getData(CartesianFloat.class);
                              //  Log.i(acc2, message.getData(CartesianFloat.class).toString());
                                extAccX2 = axes.x();
                                extAccY2 = axes.y();
                                extAccZ2 = axes.z();
                            }
                        });

                    }
                });

    }

    public void getGyroDataFromExtern(){
        final String gyro_stream_key1 = "gyro_stream_key1";
        final String gyro_stream_key2 = "gyro_stream_key2";


        gyroArrayList.get(0).routeData().fromAxes().stream(gyro_stream_key1).commit().onComplete(new AsyncOperation.CompletionHandler<RouteManager>() {
            @Override
            public void success(RouteManager result) {
                result.subscribe(gyro_stream_key1, new RouteManager.MessageHandler() {
                    @Override
                    public void process(Message message) {
                        CartesianFloat axes = message.getData(CartesianFloat.class);
                        Log.i(gyro_stream_key1, message.getData(CartesianFloat.class).toString());
                        extGyroX = axes.x();
                        extGyroY = axes.y();
                        extGyroZ = axes.z();
                    }
                });

            }
        });

        gyroArrayList.get(1).routeData().fromAxes().stream(gyro_stream_key2).commit().onComplete(new AsyncOperation.CompletionHandler<RouteManager>() {
            @Override
            public void success(RouteManager result) {
                result.subscribe(gyro_stream_key2, new RouteManager.MessageHandler() {
                    @Override
                    public void process(Message message) {
                        CartesianFloat axes = message.getData(CartesianFloat.class);
                        Log.i(gyro_stream_key2, message.getData(CartesianFloat.class).toString());
                        extGyroX2 = axes.x();
                        extGyroY2 = axes.y();
                        extGyroX2 = axes.z();
                    }
                });

            }
        });

    }

//    public boolean checkIfSensorsAreReady(){
//        if (sensor1 && sensor2) {
//            changeColorOfTextViews();
//            return true;
//        }
//        else return false;
//    }

    public void changeColorOfTextViews(){
        textViewSensor1 = (TextView) findViewById(R.id.sensor1);
        textViewSensor2 = (TextView) findViewById(R.id.sensor2);
        textViewSensor3 = (TextView) findViewById(R.id.sensor3);
        textViewSensor4 = (TextView) findViewById(R.id.sensor4);

        if (isSensorConnected[0]){
            textViewSensor1.setBackgroundColor(Color.GREEN);
        }
        if (isSensorConnected[1]){
            textViewSensor2.setBackgroundColor(Color.GREEN);
        }
        if (isSensorConnected[2]){
            textViewSensor3.setBackgroundColor(Color.GREEN);
        }
        if (isSensorConnected[3]){
            textViewSensor4.setBackgroundColor(Color.GREEN);
        }
    }

    private void connectedSensorLED(MetaWearBoard metaWearBoard){

        try {
            Led ledModule = metaWearBoard.getModule(Led.class);
            ledModule.configureColorChannel(Led.ColorChannel.GREEN)
                    .setRiseTime((short) 0).setPulseDuration((short) 1000)
                    .setRepeatCount((byte) -1).setHighTime((short) 500)
                    .setHighIntensity((byte) 16).setLowIntensity((byte) 16)
                    .commit();
            ledModule.play(true);
        } catch (UnsupportedModuleException e) {
            e.printStackTrace();
        }
    }


    public void closeExternalSensors(){
        Log.i(LOG, "Externe Sensoren werden deaktiviert!");
        for (int i = 0; i < metaWearBoards.size(); i++) {
            Log.i(LOG, "1.Externe Sensoren werden deaktiviert!" + metaWearBoards.get(i));
            try {
                Log.i(LOG, "Externe Sensoren werden deaktiviert!" + metaWearBoards.get(i));
                Led ledModule = metaWearBoards.get(i).getModule(Led.class);
                ledModule.play(false);
                ledModule.stop(true);
            } catch (UnsupportedModuleException e) {
                e.printStackTrace();
            }


        }
    }








}
