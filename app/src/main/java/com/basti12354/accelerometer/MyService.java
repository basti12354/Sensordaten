package com.basti12354.accelerometer;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Basti on 14.04.2016.
 *
 * SOLL NICHT BENUTZT WERDEN DA GROßE NACHTEILE siehe
 * http://androidtrainningcenter.blogspot.de/2013/12/handler-vs-timer-fixed-period-execution.html
 * http://stackoverflow.com/questions/18605403/timertask-vs-thread-sleep-vs-handler-postdelayed-most-accurate-to-call-functio
 *
 */
public class MyService extends Service {

    private Task retryTask;
    Timer myTimer;

    private boolean timerRunning = false;

    private long RETRY_TIME = 10;
    private long START_TIME = 50;

    ArrayList<String> sensorData = new ArrayList<>();

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        myTimer = new Timer();
        myTimer.scheduleAtFixedRate(new Task(), START_TIME, RETRY_TIME);
        timerRunning = true;
        Log.d("Test", "Hier");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!timerRunning) {
            myTimer = new Timer();
            myTimer.scheduleAtFixedRate(new Task(), START_TIME, RETRY_TIME);
            timerRunning = true;
        }

        return super.onStartCommand(intent, flags, startId);

    }

    public class Task extends TimerTask {

        @Override
        public void run() {
            Date d = new Date();
            // Aktuelles Datum und Zeit in ms
            String date = new SimpleDateFormat("yyyy-MM-dd").format(d);
            String actualTime = new SimpleDateFormat("HH:mm:ss.SSS").format(d);

            // Aktuellen Timestamp
            Long tsLong = System.currentTimeMillis();
            String timestamp = tsLong.toString();
            sensorData.add(date + "," + actualTime + "," + timestamp);
            // DO WHAT YOU NEED TO DO HERE

            Log.d("Test:",date + "," + actualTime + "," + timestamp);
        }

    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();

        if (myTimer != null) {
            myTimer.cancel();

        }
        printArrayList();
        timerRunning = false;
    }

    public void printArrayList(){
        for (int i = 0; i < sensorData.size(); i ++){
            Log.d("Arraylist-Inhalt", sensorData.get(i));
        }
    }
}