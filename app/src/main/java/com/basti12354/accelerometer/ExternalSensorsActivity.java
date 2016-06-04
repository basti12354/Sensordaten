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
import android.os.Environment;
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

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * ############Klasse für die Verbindung/Messungen der Externen SENSOREN#####################
 */
public class ExternalSensorsActivity extends AppCompatActivity implements ServiceConnection {

    // Variablen um externen Daten zu speichern, sobald sie gestreamt wurden
    public static float extAccX = 0, extAccY = 0, extAccZ = 0, extAccX2 = 0, extAccY2 = 0, extAccZ2 = 0,
            extAccX3 = 0, extAccY3 = 0, extAccZ3 = 0, extAccX4 = 0, extAccY4 = 0, extAccZ4 = 0;
    public static float extGyroX = 0, extGyroY = 0, extGyroZ = 0, extGyroX2 = 0, extGyroY2 = 0, extGyroZ2 = 0,
            extGyroX3 = 0, extGyroY3 = 0, extGyroZ3 = 0, extGyroX4 = 0, extGyroY4 = 0, extGyroZ4 = 0;


    // String Arrays werden für die StreamingVerbindung zum Smartphone gebraucht
    private String[] accDataTransferString = {"acc_stream_key1", "acc_stream_key2", "acc_stream_key3","acc_stream_key4"};
    private String[] gyroDataTransferString = {"gyro_stream_key1", "gyro_stream_key2", "gyro_stream_key3","gyro_stream_key4"};


    // Externe Sensoren!
    private MetaWearBleService.LocalBinder serviceBinder;

    // Farben dieser TextViews sollen Zustand/Verbindung zu den externen Sensoren zeigen
    TextView textViewSensor1, textViewSensor2, textViewSensor3, textViewSensor4;
    private boolean[] isSensorConnected = {false, false, false, false};

    // Arraylisten in denen je nach Anzahl der verbundenen Sensoren, die Adapter der Sensoren gespeichert werden
    private ArrayList<MetaWearBoard> metaWearBoards = new ArrayList<MetaWearBoard>();
    private ArrayList<Bmi160Accelerometer> accelerometerArrayList = new ArrayList<Bmi160Accelerometer>();
    private ArrayList<Bmi160Gyro> gyroArrayList = new ArrayList<Bmi160Gyro>();

    public final String LOG = "Externe Sensoren";

    // Variable muss je nach ANZAHL der Sensoren geändert werden; 2 enstpricht hierbei 2 Sensoren, 4 -> 4...
    private int anzahl_der_externen_sensoren = 4;

    //
    final float data_stream_rate = 40.0f;

    // Überprüfe die Frequenz
    private ArrayList<Long> accTimestamp1 = new ArrayList<>(), accTimestamp2 = new ArrayList<>(), accTimestamp3 = new ArrayList<>(), accTimestamp4 = new ArrayList<>();
    private ArrayList<Long> gyroTimestamp1 = new ArrayList<>(), gyroTimestamp2 = new ArrayList<>(), gyroTimestamp3 = new ArrayList<>(), gyroTimestamp4 = new ArrayList<>();
    private ArrayList<String> frequency = new ArrayList<>();
    // Speicherplatz auf Gerät
    public String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Sensordaten";

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

    // Externe Sensoren werden verbunden
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        // Typecast the binder to the service's LocalBinder class
        serviceBinder = (MetaWearBleService.LocalBinder) service;




         Log.i(LOG, "Service Bind2" );

        // MAC Adressen der externen Sensoren
        final String mwMacAddress  = "D1:C4:AA:BF:0B:22";
        final String mwMacAddress2 = "C6:9E:B6:91:A5:F7";
        final String mwMacAddress3 = "CF:E0:DA:D2:B3:D8";
        final String mwMacAddress4 = "EE:BA:5C:F4:EB:EC";

        final ArrayList<String> macAdressen = new ArrayList<String>();
        macAdressen.add(mwMacAddress);
        macAdressen.add(mwMacAddress2);
        macAdressen.add(mwMacAddress3);
        macAdressen.add(mwMacAddress4);

        BluetoothManager btManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);


        for (int i = 0; i < anzahl_der_externen_sensoren; i++) {

            final int numberOfConnectedDevices = i;

            // Verbindet das Board mit den verschiedenen MAC-Adressen von oben
            BluetoothDevice btDevice = btManager.getAdapter().getRemoteDevice(macAdressen.get(numberOfConnectedDevices));
            final MetaWearBoard metaWearBoard = serviceBinder.getMetaWearBoard(btDevice);


            metaWearBoard.setConnectionStateHandler(new MetaWearBoard.ConnectionStateHandler() {

                @Override
                public void connected() {
                    ArrayList<String> kopieDerMacAdresse = macAdressen;
                    Log.i(LOG, kopieDerMacAdresse.get(numberOfConnectedDevices) + " Connected");

                    metaWearBoards.add(metaWearBoard);

                    connectedSensorLED(metaWearBoard);

                    initializeAccExtern(metaWearBoard);
                    initializeGyroExtern(metaWearBoard);

                    isSensorConnected[numberOfConnectedDevices] = true;

                }

                @Override
                public void disconnected() {
                    Log.i(LOG, "Connected Lost");
                    isSensorConnected[numberOfConnectedDevices] = false;
                }

                @Override
                public void failure(int status, Throwable error) {
                    Log.e(LOG, "Error connecting", error);
                   // Toast.makeText(getBaseContext(), "EXTERNE Sensoren NICHT richtig verbunden!", Toast.LENGTH_SHORT).show();
//                    metaWearBoards.remove(numberOfConnectedDevices);
//
//                    accelerometerArrayList.remove(numberOfConnectedDevices);
//                    gyroArrayList.remove(numberOfConnectedDevices);
//                    isSensorConnected[numberOfConnectedDevices] = false;

                 //   metaWearBoard.connect();
                    isSensorConnected[numberOfConnectedDevices] = false;
                }
            });

            metaWearBoard.connect();


        }


    }



    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        Log.e(LOG + " DISC ", "AUFGERUFEN!");
    }

    // Konfiguriert den ACC Adapter -> Board muss übergeben werden, da diese für jedes Board einzeln erstellt und konfig. werden müssen!
    private void initializeAccExtern(MetaWearBoard metaWearBoard){

            try {

                Bmi160Accelerometer accelerometer = metaWearBoard.getModule(Bmi160Accelerometer.class);

                // Set the sampling frequency to 50Hz, or closest valid ODR
                accelerometer.configureAxisSampling()
                        .setFullScaleRange(Bmi160Accelerometer.AccRange.AR_4G)
                       // .setOutputDataRate(Bmi160Accelerometer.OutputDataRate.ODR_50_HZ)
                        .commit();
                accelerometer.setOutputDataRate(data_stream_rate);
                // enable axis sampling
                accelerometer.enableAxisSampling();
                // Switch the accelerometer to active mode
                accelerometer.start();



                accelerometerArrayList.add(accelerometer);
                Log.i(LOG, "Externe-ACC Sensoren GESTARTET " + "Aktuelle Anzahl: " + accelerometerArrayList.size());
            }


            catch (UnsupportedModuleException e) {
                e.printStackTrace();
            }

    }

    // Konfiguriert den GYRO Adapter -> Board muss übergeben werden, da diese für jedes Board einzeln erstellt und konfig. werden müssen!
    private void initializeGyroExtern(MetaWearBoard metaWearBoard) {
       // Log.i(LOG, "Starte ExterneGYRO Sensoren");

            try {
                Bmi160Gyro bmi160Gyro = metaWearBoard.getModule(Bmi160Gyro.class);


                bmi160Gyro.configure()
                        .setFullScaleRange(Bmi160Gyro.FullScaleRange.FSR_2000)
                       // .setOutputDataRate(Bmi160Gyro.OutputDataRate.ODR_50_HZ)
                        .commit();
                bmi160Gyro.setOutputDataRate(data_stream_rate);
                bmi160Gyro.start();

              //  Log.i(LOG, "Externe-GYRO Sensoren GESTARTET");

                gyroArrayList.add(bmi160Gyro);
            }
            catch (UnsupportedModuleException e) {
                e.printStackTrace();
            }



    }

    // Methode updatet die Variablen, indem der STREAM aller Sensoren gestartet wird
    // CAVE: Muss nur einmal aufgerufen werden, das Streaming wird dann je nach obiger Konfiguration in diesen Abständen durchgeführt.
    public void getAllDataFromExternalSensors(){
        getAccDataFromExtern();
        getGyroDataFromExtern();
    }

    // Streamt die Daten aller verbundenen Accelerometer
    private void getAccDataFromExtern(){
//        final String acc1 = "acc_stream_key";
//        final String acc2 = "acc_stream_key2";
//
//
//                accelerometerArrayList.get(0).routeData().fromAxes().stream(acc1).commit().onComplete(new AsyncOperation.CompletionHandler<RouteManager>() {
//                    @Override
//                    public void success(RouteManager result) {
//                        result.subscribe(acc1, new RouteManager.MessageHandler() {
//                            @Override
//                            public void process(Message message) {
//                                CartesianFloat axes = message.getData(CartesianFloat.class);
//                             //   Log.i(acc1, message.getData(CartesianFloat.class).toString());
//                                extAccX = axes.x();
//                                extAccY = axes.y();
//                                extAccZ = axes.z();
//                            }
//                        });
//
//                    }
//                });
    //    Log.i(LOG, "AccelerometerListe: " + accelerometerArrayList.size());
        for (int i = 0; i < accelerometerArrayList.size(); i++) {
            final int arrayListSize = i;
       //     Log.i("STREAM", "Durchlauf: " + arrayListSize);
            accelerometerArrayList.get(i).routeData().fromAxes().stream(accDataTransferString[i]).commit().onComplete(new AsyncOperation.CompletionHandler<RouteManager>() {
                @Override
                public void success(RouteManager result) {
                    result.subscribe(accDataTransferString[arrayListSize], new RouteManager.MessageHandler() {
                        @Override
                        public void process(Message message) {
                            CartesianFloat axes = message.getData(CartesianFloat.class);
                           //   Log.i("STREAM", message.getData(CartesianFloat.class).toString());

                            Long tsLong = System.currentTimeMillis();

                            switch (arrayListSize) {
                                case 0:
                                    // Aktuellen Timestamp zur Liste hinzufügen für Berechnung der Frequenz
                                    accTimestamp1.add(tsLong);

                                 //   Log.i(LOG, "1. ACC");
                                    extAccX = axes.x();
                                    extAccY = axes.y();
                                    extAccZ = axes.z();
                               //     Log.i("STREAM ", "X-WERT ACC: " + extAccX);
                                    break;
                                case 1:
                                    // Aktuellen Timestamp zur Liste hinzufügen für Berechnung der Frequenz
                                    accTimestamp2.add(tsLong);

                                  //  Log.i(LOG, "2. ACC");
                                    extAccX2 = axes.x();
                                    extAccY2 = axes.y();
                                    extAccZ2 = axes.z();
                                    break;
                                case 2:
                                    // Aktuellen Timestamp zur Liste hinzufügen für Berechnung der Frequenz
                                    accTimestamp3.add(tsLong);

                                    extAccX3 = axes.x();
                                    extAccY3 = axes.y();
                                    extAccZ3 = axes.z();
                                    break;
                                case 3:
                                    // Aktuellen Timestamp zur Liste hinzufügen für Berechnung der Frequenz
                                    accTimestamp4.add(tsLong);

                                    extAccX4 = axes.x();
                                    extAccY4 = axes.y();
                                    extAccZ4 = axes.z();
                                    break;
                            }
                        }
                    });

                }
            });
        }
    }
    // Streamt die Daten aller verbundenen Gyros
    private void getGyroDataFromExtern(){

        for (int i = 0; i < gyroArrayList.size(); i++) {
            final int arrayListSize = i;
            gyroArrayList.get(i).routeData().fromAxes().stream(gyroDataTransferString[i]).commit().onComplete(new AsyncOperation.CompletionHandler<RouteManager>() {
                @Override
                public void success(RouteManager result) {
                    result.subscribe(gyroDataTransferString[arrayListSize], new RouteManager.MessageHandler() {
                        @Override
                        public void process(Message message) {
                            CartesianFloat axes = message.getData(CartesianFloat.class);
                         //   Log.i("STREAM " + gyroDataTransferString[arrayListSize], message.getData(CartesianFloat.class).toString());

                            Long tsLong = System.currentTimeMillis();

                            switch (arrayListSize){
                                case 0:
                                    // Aktuellen Timestamp zur Liste hinzufügen für Berechnung der Frequenz
                                    gyroTimestamp1.add(tsLong);

                                    extGyroX = axes.x();
                                    extGyroY = axes.y();
                                    extGyroZ = axes.z();
                              //      Log.i("STREAM ", "X-WERT GYRO: " + extGyroX);
                                    break;
                                case 1:
                                    gyroTimestamp2.add(tsLong);

                                    extGyroX2 = axes.x();
                                    extGyroY2 = axes.y();
                                    extGyroX2 = axes.z();
                                    break;
                                case 2:
                                    gyroTimestamp3.add(tsLong);

                                    extGyroX3 = axes.x();
                                    extGyroY3 = axes.y();
                                    extGyroX3 = axes.z();
                                    break;
                                case 3:
                                    gyroTimestamp4.add(tsLong);

                                    extGyroX4 = axes.x();
                                    extGyroY4 = axes.y();
                                    extGyroX4 = axes.z();
                                    break;
                            }

                        }
                    });

                }
            });
        }

    }


    // Methode soll je nach positiver Verbindung die Farben der TextViews ändern
    // Aktuell kann KEIN DISCONNECT eines Sensors dargestellt werden!
    public void changeColorOfTextViews(){
        textViewSensor1 = (TextView) findViewById(R.id.sensor1);
        textViewSensor2 = (TextView) findViewById(R.id.sensor2);
        textViewSensor3 = (TextView) findViewById(R.id.sensor3);
        textViewSensor4 = (TextView) findViewById(R.id.sensor4);

        if (isSensorConnected[0]){
            textViewSensor1.setBackgroundColor(Color.GREEN);
        }
        else {
            textViewSensor1.setBackgroundColor(Color.RED);
        }
        if (isSensorConnected[1]){
            textViewSensor2.setBackgroundColor(Color.GREEN);
        }
        else {
            textViewSensor2.setBackgroundColor(Color.RED);
        }
        if (isSensorConnected[2]){
            textViewSensor3.setBackgroundColor(Color.GREEN);
        }
        else {
            textViewSensor3.setBackgroundColor(Color.RED);
        }
        if (isSensorConnected[3]){
            textViewSensor4.setBackgroundColor(Color.GREEN);
        }
        else {
            textViewSensor4.setBackgroundColor(Color.RED);
        }
    }

    // Zeigt den Zustand: ERFOLGREICH VERBUNDEN = GRÜN
    private void connectedSensorLED(MetaWearBoard metaWearBoard){

        try {
            Led ledModule = metaWearBoard.getModule(Led.class);
            ledModule.configureColorChannel(Led.ColorChannel.GREEN)
                    .setRiseTime((short) 0).setPulseDuration((short) 1000)
                    .setRepeatCount((byte) -1).setHighTime((short) 500)
                    .setHighIntensity((byte) 16).setLowIntensity((byte) 16)
                    .commit();
            ledModule.play(true);
        }
        catch (UnsupportedModuleException e) {
            e.printStackTrace();
        }
    }

    // Zeigt den Zustand: DATENMESSUNG = RED
    public void changeLEDColorToStateRUNNING(){
        for (int i = 0; i < metaWearBoards.size(); i++) {
            try {
                Log.i(LOG, "Externe Sensoren LAUFEN!" + metaWearBoards.get(i));
                Led ledModule = metaWearBoards.get(i).getModule(Led.class);

                ledModule.stop(true);

                ledModule.configureColorChannel(Led.ColorChannel.RED)
                        .setRiseTime((short) 0).setPulseDuration((short) 1000)
                        .setRepeatCount((byte) -1).setHighTime((short) 500)
                        .setHighIntensity((byte) 16).setLowIntensity((byte) 16)
                        .commit();
                ledModule.play(true);
            } catch (NullPointerException e) {
                e.printStackTrace();
                metaWearBoards.remove(i);
                accelerometerArrayList.remove(i);
                gyroArrayList.remove(i);

                isSensorConnected[i] = false;

            } catch (UnsupportedModuleException e) {
                e.printStackTrace();
            }
        }
    }

    // Zeigt den Zustand: PAUSE = GRÜN
    public void changeLEDColorToStatePAUSE(){
        for (int i = 0; i < metaWearBoards.size(); i++) {
            try {
                Log.i(LOG, "Externe Sensoren werden PAUSIERT!" + metaWearBoards.get(i));
                Led ledModule = metaWearBoards.get(i).getModule(Led.class);

                ledModule.stop(true);

                ledModule.configureColorChannel(Led.ColorChannel.GREEN)
                        .setRiseTime((short) 0).setPulseDuration((short) 1000)
                        .setRepeatCount((byte) -1).setHighTime((short) 500)
                        .setHighIntensity((byte) 16).setLowIntensity((byte) 16)
                        .commit();
                ledModule.play(true);
            }
            catch (NullPointerException e) {
                e.printStackTrace();
                metaWearBoards.remove(i);
                accelerometerArrayList.remove(i);
                gyroArrayList.remove(i);

                isSensorConnected[i] = false;

            }
            catch (UnsupportedModuleException e) {
                e.printStackTrace();
            }
        }
    }


    // Schließt die Verbindungen zu den externen Sensoren!
    // Muss unbedingt aufgerufen werden, da sonst die Verbindung zu den Sensoren nur durch Reset (Batterie raus/rein)
    public void closeExternalSensors(){
       // Log.i(LOG, "Externe Sensoren werden deaktiviert!");
        for (int i = 0; i < metaWearBoards.size(); i++) {
           // Log.i(LOG, "1.Externe Sensoren werden deaktiviert!" + metaWearBoards.get(i));
            try {

                // Stoppe Accelerometer
                accelerometerArrayList.get(i).stop();
                accelerometerArrayList.get(i).disableAxisSampling();

                // Stoppe Gyroscop
                gyroArrayList.get(i).stop();

             //   Log.i(LOG, "Externe Sensoren werden deaktiviert!" + metaWearBoards.get(i));
                Led ledModule = metaWearBoards.get(i).getModule(Led.class);
                ledModule.play(false);
                ledModule.stop(true);
            } catch (UnsupportedModuleException e) {
                e.printStackTrace();
            }
        }
        gyroArrayList = new ArrayList<>();
        accelerometerArrayList = new ArrayList<>();
        metaWearBoards = new ArrayList<>();


    }

    // Pausiert das Datenstreaming
    public void pauseExternalSensors(){
        for (int i = 0; i < metaWearBoards.size(); i++) {


                // Stoppe Accelerometer
                accelerometerArrayList.get(i).stop();
                accelerometerArrayList.get(i).disableAxisSampling();

                // Stoppe Gyroscop
                gyroArrayList.get(i).stop();

              Log.i(LOG + "PAUSE", "Externe Sensoren PAUSIERT");

        }
    }

    // Startet das Streaming nach der Pause wieder
    public void startExternalSensorsAfterPause() {
        for (int i = 0; i < metaWearBoards.size(); i++) {

            Bmi160Gyro bmi160Gyro = gyroArrayList.get(i);;


            bmi160Gyro.configure()
                    .setFullScaleRange(Bmi160Gyro.FullScaleRange.FSR_2000)
                    .setOutputDataRate(Bmi160Gyro.OutputDataRate.ODR_50_HZ)
                    .commit();
            bmi160Gyro.start();

            Bmi160Accelerometer accelerometer = accelerometerArrayList.get(i);

            // Set the sampling frequency to 50Hz, or closest valid ODR
            accelerometer.configureAxisSampling()
                    .setFullScaleRange(Bmi160Accelerometer.AccRange.AR_4G)
                    .setOutputDataRate(Bmi160Accelerometer.OutputDataRate.ODR_50_HZ)
                    .commit();
            // enable axis sampling
            accelerometer.enableAxisSampling();
            // Switch the accelerometer to active mode
            accelerometer.start();

            Log.i(LOG+"RESTART", "Externe Sensoren RESTARTET");
        }

    }

    public void calculateFrequencyOfExternalSensors(){
        String returnString = GetSensordatenActivity.label;

//        // erstelle zwei Arrays für die Berechnung
        ArrayList<ArrayList<Long>> accTimestamp = new ArrayList<>();
        accTimestamp.add(accTimestamp1);
        accTimestamp.add(accTimestamp2);
        accTimestamp.add(accTimestamp3);
        accTimestamp.add(accTimestamp4);

        ArrayList<ArrayList<Long>> gyroTimestamp = new ArrayList<>();
        gyroTimestamp.add(gyroTimestamp1);
        gyroTimestamp.add(gyroTimestamp2);
        gyroTimestamp.add(gyroTimestamp3);
        gyroTimestamp.add(gyroTimestamp4);

        for (int j = 0; j < accTimestamp.size(); j++){
            long abstand = 0;
            double milliseconds = 0;
            for (int i = 0; i < accTimestamp.get(j).size() - 1; i++) {

                long aktuellerAbstand = accTimestamp.get(j).get(i + 1) - accTimestamp.get(j).get(i);
                //   Log.i(LOG+"Abstand", "Aktuell: " + j);
                abstand = abstand + aktuellerAbstand;
                //   Log.i(LOG+"Abstand", "Abstand: " + abstand + " ZAHL " + i + " OBJEKT " );
            }
            if (accTimestamp.get(j).size() > 0) {
                milliseconds = (double) abstand / accTimestamp.get(j).size();
            }
            //  Log.i(LOG+"Abstand", "Milliseconds: " + milliseconds + " ; Abstand " + abstand + " ; Size: " + accTimestamp1.size());
            double frequency = 1000f / milliseconds;
            //   Log.i(LOG+"Abstand", "Frequenz: " + frequency);
            returnString = returnString + " ACC-Sensor" + j + " " + frequency + "; ";
        }
        for (int j = 0; j < gyroTimestamp.size(); j++){
            long abstand = 0;
            double milliseconds = 0;
            for (int i = 0; i < gyroTimestamp.get(j).size() - 1; i++) {

                long aktuellerAbstand = gyroTimestamp.get(j).get(i + 1) - gyroTimestamp.get(j).get(i);
                //   Log.i(LOG+"Abstand", "Aktuell: " + j);
                abstand = abstand + aktuellerAbstand;
                //   Log.i(LOG+"Abstand", "Abstand: " + abstand + " ZAHL " + i + " OBJEKT " );
            }
            if (gyroTimestamp.get(j).size() > 0) {
                milliseconds = (double) abstand / gyroTimestamp.get(j).size();
            }
            //  Log.i(LOG+"Abstand", "Milliseconds: " + milliseconds + " ; Abstand " + abstand + " ; Size: " + accTimestamp1.size());
            double frequency = 1000f / milliseconds;
            //   Log.i(LOG+"Abstand", "Frequenz: " + frequency);
            returnString = returnString + "Gyro-Sensor" + j + " " + frequency + "; ";
        }

      //  Log.i(LOG+"FREQ", returnString);



        accTimestamp1.clear();
        accTimestamp2.clear();
        accTimestamp3.clear();
        accTimestamp4.clear();
        gyroTimestamp1.clear();
        gyroTimestamp2.clear();
        gyroTimestamp3.clear();
        gyroTimestamp4.clear();


        frequency.add(returnString);
    }


    public void saveFrequencyListToTxt(){
        PrintWriter out = null;
        try {
            out = new PrintWriter(new FileWriter(path + "/" + MainActivity.probandenName + "/FREQUENZ.txt", true));
            for (String text : frequency) {
                out.println(text);
            }
        } catch (IOException e) {
            System.err.println("Caught IOException: " +  e.getMessage());

        } finally {
            if (out != null) {
                out.close();
                frequency.clear();

                // Löscht die Arrayliste, damit die Daten nicht doppelt gespeichert werden
             //   frequency.clear();
            }
        }
    }







}
