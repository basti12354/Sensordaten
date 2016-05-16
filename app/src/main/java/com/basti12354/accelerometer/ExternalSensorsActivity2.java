package com.basti12354.accelerometer;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.mbientlab.metawear.AsyncOperation;
import com.mbientlab.metawear.Message;
import com.mbientlab.metawear.MetaWearBleService;
import com.mbientlab.metawear.MetaWearBoard;
import com.mbientlab.metawear.RouteManager;
import com.mbientlab.metawear.UnsupportedModuleException;
import com.mbientlab.metawear.data.CartesianFloat;
import com.mbientlab.metawear.module.Accelerometer;
import com.mbientlab.metawear.module.Bmi160Gyro;

import java.util.ArrayList;

/**
 * Created by Basti on 06.05.2016.
 */
public class ExternalSensorsActivity2 extends AppCompatActivity implements ServiceConnection {

    public static float extAccX = 0, extAccY = 0, extAccZ = 0, extAccX2 = 0, extAccY2 = 0, extAccZ2 = 0,
            extAccX3 = 0, extAccY3 = 0, extAccZ3 = 0, extAccX4 = 0, extAccY4 = 0, extAccZ4 = 0;

    public static float extGyroX = 0, extGyroY = 0, extGyroZ = 0, extGyroX2 = 0, extGyroY2 = 0, extGyroZ2 = 0,
            extGyroX3 = 0, extGyroY3 = 0, extGyroZ3 = 0, extGyroX4 = 0, extGyroY4 = 0, extGyroZ4 = 0;

    // Externe Sensoren!
    private MetaWearBleService.LocalBinder serviceBinder;
    private Accelerometer accelModule, accelModule2, accelModule3, accelModule4;
    private Bmi160Gyro gyroModule, gyroModule2, gyroModule3, gyroModule4;

    private MetaWearBoard mwBoard, mwBoard2, mwBoard3, mwBoard4;

    private ArrayList<MetaWearBoard> metaWearBoards = new ArrayList<MetaWearBoard>();

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

        ArrayList<String> macAdressen = new ArrayList<String>();
        macAdressen.add(mwMacAddress);
        macAdressen.add(mwMacAddress2);

        BluetoothManager btManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);

        BluetoothDevice btDevice = btManager.getAdapter().getRemoteDevice(mwMacAddress);
        BluetoothDevice btDevice2 = btManager.getAdapter().getRemoteDevice(mwMacAddress2);



        mwBoard = serviceBinder.getMetaWearBoard(btDevice);
        mwBoard2 = serviceBinder.getMetaWearBoard(btDevice2);
//        mwBoard3 = serviceBinder.getMetaWearBoard(btDevice);
//        mwBoard4 = serviceBinder.getMetaWearBoard(btDevice);

        for (int i = 0; i < anzahl_der_externen_sensoren; i++) {



        }


        mwBoard.setConnectionStateHandler(new MetaWearBoard.ConnectionStateHandler() {

            @Override
            public void connected() {
                Log.i(LOG, mwMacAddress + " Connected");
                initializeAccExtern(1);
            }

            @Override
            public void disconnected() {
                Log.i(LOG, "Connected Lost");
            }

            @Override
            public void failure(int status, Throwable error) {
                Log.e(LOG, "Error connecting", error);
            }
        });
        mwBoard2.setConnectionStateHandler(new MetaWearBoard.ConnectionStateHandler() {

            @Override
            public void connected() {
                Log.i(LOG, mwMacAddress2 + " Connected");
                initializeAccExtern(2);
            }

            @Override
            public void disconnected() {
                Log.i(LOG, "Connected Lost");
            }

            @Override
            public void failure(int status, Throwable error) {
                Log.e(LOG, "Error connecting", error);
            }
        });
//        mwBoard3.setConnectionStateHandler(stateHandler);
//        mwBoard4.setConnectionStateHandler(stateHandler);

        mwBoard.connect();
        mwBoard2.connect();
//        mwBoard3.connect();
//        mwBoard4.connect();
    }



    @Override
    public void onServiceDisconnected(ComponentName componentName) { }


    private void initializeAccExtern(int number){
        Log.i(LOG, "Starte Externe Sensoren");
        try {
            switch (number){
                case 1:
                    // ############## ERSTER SENSOR #############
                    accelModule= mwBoard.getModule(Accelerometer.class);
                    // Set the sampling frequency to 50Hz, or closest valid ODR
                    accelModule.setOutputDataRate(50.f);
                    // Set the measurement range to +/- 4g, or closet valid range
                    accelModule.setAxisSamplingRange(4.0f);
                    // enable axis sampling
                    accelModule.enableAxisSampling();
                    // Switch the accelerometer to active mode
                    accelModule.start();

                    sensor1 = true;
                    break;

                case 2:
                    // ############## 2. SENSOR #############
                    accelModule2= mwBoard2.getModule(Accelerometer.class);
                    // Set the sampling frequency to 50Hz, or closest valid ODR
                    accelModule2.setOutputDataRate(50.f);
                    // Set the measurement range to +/- 4g, or closet valid range
                    accelModule2.setAxisSamplingRange(4.0f);
                    // enable axis sampling
                    accelModule2.enableAxisSampling();
                    // Switch the accelerometer to active mode
                    accelModule2.start();
                    sensor2 = true;
                    break;
                case 3:
                    break;
                case 4:
                    break;
            }




//            // ############## 3. SENSOR #############
//            accelModule3 = mwBoard3.getModule(Accelerometer.class);
//            // Set the sampling frequency to 50Hz, or closest valid ODR
//            accelModule3.setOutputDataRate(50.f);
//            // Set the measurement range to +/- 4g, or closet valid range
//            accelModule3.setAxisSamplingRange(4.0f);
//            // enable axis sampling
//            accelModule3.enableAxisSampling();
//            // Switch the accelerometer to active mode
//            accelModule3.start();
//
//            // ############## 4. SENSOR #############
//            accelModule4 = mwBoard4.getModule(Accelerometer.class);
//            // Set the sampling frequency to 50Hz, or closest valid ODR
//            accelModule4.setOutputDataRate(50.f);
//            // Set the measurement range to +/- 4g, or closet valid range
//            accelModule4.setAxisSamplingRange(4.0f);
//            // enable axis sampling
//            accelModule4.enableAxisSampling();
//            // Switch the accelerometer to active mode
//            accelModule4.start();
        }
        catch (UnsupportedModuleException e){

        }
    }

    private void initializeGyroExtern(int number) {
        Log.i(LOG, "Starte Externe Sensoren");
        try {
            switch (number) {
                case 1:
                    gyroModule = mwBoard.getModule(Bmi160Gyro.class);
                    gyroModule.configure()
                            .setFullScaleRange(Bmi160Gyro.FullScaleRange.FSR_2000)
                            .setOutputDataRate(Bmi160Gyro.OutputDataRate.ODR_100_HZ)
                            .commit();
                    gyroModule.start();
            }
        }
        catch (UnsupportedModuleException e){

        }
    }


    public void getAccDataFromExtern(){
        final String acc1 = "acc_stream_key";
        final String acc2 = "acc_stream_key2";


                accelModule.routeData().fromAxes().stream(acc1).commit().onComplete(new AsyncOperation.CompletionHandler<RouteManager>() {
                    @Override
                    public void success(RouteManager result) {
                        result.subscribe(acc1, new RouteManager.MessageHandler() {
                            @Override
                            public void process(Message message) {
                                CartesianFloat axes = message.getData(CartesianFloat.class);
                                Log.i(acc1, message.getData(CartesianFloat.class).toString());
                                extAccX = axes.x();
                                extAccY = axes.y();
                                extAccZ = axes.z();
                            }
                        });

                    }
                });

                accelModule2.routeData().fromAxes().stream(acc2).commit().onComplete(new AsyncOperation.CompletionHandler<RouteManager>() {
                    @Override
                    public void success(RouteManager result) {
                        result.subscribe(acc2, new RouteManager.MessageHandler() {
                            @Override
                            public void process(Message message) {
                                CartesianFloat axes = message.getData(CartesianFloat.class);
                                Log.i(acc2, message.getData(CartesianFloat.class).toString());
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


        gyroModule.routeData().fromAxes().stream(gyro_stream_key1).commit().onComplete(new AsyncOperation.CompletionHandler<RouteManager>() {
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

        gyroModule2.routeData().fromAxes().stream(gyro_stream_key2).commit().onComplete(new AsyncOperation.CompletionHandler<RouteManager>() {
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

    public boolean checkIfSensorsAreReady(){
        if (sensor1 && sensor2) {
            return true;
        }
        else return false;
    }






}
