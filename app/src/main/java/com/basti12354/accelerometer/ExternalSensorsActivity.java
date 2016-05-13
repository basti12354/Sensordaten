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
import com.mbientlab.metawear.module.*;
import com.mbientlab.metawear.module.Accelerometer;

/**
 * Created by Basti on 06.05.2016.
 */
public class ExternalSensorsActivity extends AppCompatActivity implements ServiceConnection {

    public static float extAccX, extAccY, extAccZ, extAccX2, extAccY2, extAccZ2, extAccX3, extAccY3, extAccZ3, extAccX4, extAccY4, extAccZ4;

    // Externe Sensoren!
    private MetaWearBleService.LocalBinder serviceBinder;
    private Accelerometer accelModule, accelModule2, accelModule3, accelModule4;

    private MetaWearBoard mwBoard, mwBoard2, mwBoard3, mwBoard4;


    private final String LOG = "Externe Sensoren";




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

        BluetoothManager btManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);

        BluetoothDevice btDevice = btManager.getAdapter().getRemoteDevice(mwMacAddress);
        BluetoothDevice btDevice2 = btManager.getAdapter().getRemoteDevice(mwMacAddress2);



        mwBoard = serviceBinder.getMetaWearBoard(btDevice);
        mwBoard2 = serviceBinder.getMetaWearBoard(btDevice2);
//        mwBoard3 = serviceBinder.getMetaWearBoard(btDevice);
//        mwBoard4 = serviceBinder.getMetaWearBoard(btDevice);

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

    public void getAccDataFromExtern(){
        final String acc1 = "acc_stream_key";

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


    }






}
