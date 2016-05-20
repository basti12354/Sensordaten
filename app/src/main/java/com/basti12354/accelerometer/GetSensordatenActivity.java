package com.basti12354.accelerometer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.basti12354.accelerometer.timer.ExampleTimer;
import com.basti12354.accelerometer.timer.Timer;

/**
 * Created by Basti on 13.04.2016.
 */
public class GetSensordatenActivity extends AndroidSensorsActivity implements View.OnClickListener {
    Button startBtn, stopBtn;
    TextView aktuellerSatz;
    Boolean isRunning = false;

    long pauseDuration = 30000l;
    Timer pauseTimer = new ExampleTimer(1000l, pauseDuration);
    Thread t;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.exercise_layout);

      //  initializeViews();

        intitializeSmartphoneSensors();


        // Keep Screen ON
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

//        getActionBar().setDisplayHomeAsUpEnabled(true);

        startBtn = (Button) findViewById(R.id.startBtn);
 //       stopBtn = (Button) findViewById(R.id.stopBtn);

        startBtn.setOnClickListener(this);
  //      stopBtn.setOnClickListener(this);

        TextView uebung = (TextView) findViewById(R.id.exerciseName);
        uebung.setText(MainActivity.label);

        setTextViewAktuellerSatz();

        // Soll anzeigen, welche Sensoren verbunden sind!
        changeColorOfTextViews();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.startBtn:

                // Fall: Sensoren laufen gerade NICHT!
                if (isRunning == false) {
                    startBtn.setText("Stop");
                    startSensors();
                    // Ändere die LEDs der verbundenen Sensoren -> damit Verbindungsfehler gezeigt werden können!

                    isRunning = true;
                }

                // Sensoren laufen gerade und werden nun pausiert!
                else {
                    startBtn.setText("Start");

                    startPauseTimer();

                    stopSensors();

                    changeLEDColorToStatePAUSE();
                    // Erhöhe den Zähler um eins!
                    AndroidSensorsActivity.aktuellerSatz = AndroidSensorsActivity.aktuellerSatz + 1;
                    setTextViewAktuellerSatz();

                    isRunning = false;
                    //printArrayList();


                }
                break;


        }
    }

    private void setTextViewAktuellerSatz(){
        aktuellerSatz = (TextView) findViewById(R.id.satz);
        aktuellerSatz.setText(AndroidSensorsActivity.aktuellerSatz + "");
    }

    // Starte den PauseTimer -> jede Pause gleich lange Zeit
    private void startPauseTimer(){
        //Start the timer.
        pauseTimer.start();
        if (pauseTimer.isRunning()) {

            t = new Thread() {

                @Override
                public void run() {
                    try {
                        while (!isInterrupted()) {
                            Thread.sleep(100);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    // update TextView here!

                                    Integer remainingTime =	 (int) pauseTimer.getRemainingTime()/1000;
                                    String time = remainingTime.toString();
                                    Log.i("VERBLEIBENDE ZEIT: ", time);


                                        // WENN VERBLEIBENDE ZEIT UNTER 0 ist!!!!
                                        if (remainingTime <= 0) {
                                           stopPauseTimer();
                                            t.interrupt();
                                        }


                                }
                            });
                        }
                    } catch (InterruptedException e) {
                    }
                }
            };

            t.start(); }
    }

    // beendet den Pause Timer
    // starte Vibration am Ende
    // Beginne wieder mit der Datensammlung
    private void stopPauseTimer(){

        changeLEDColorToStateRUNNING();

                    pauseTimer.cancel();


        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 1000 milliseconds
        v.vibrate(1000);

        // Nach VIBRATION werden die Sensoren wieder gestartet!
        startSensors();
        isRunning = true;
    }


    @Override
    public void onBackPressed() {
        Log.i(LOG, "Return Taste");

        backToMainActivity();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                Log.i(LOG, "Return Menü");

                backToMainActivity();


                break;




        }
        return true;
    }

    // TODO: Externe Sensoren leuchten weiter, wenn einmal der StartButton geklickt wurde -> Ursache unbekannt!
    // Stoppt Sensoren
    private void backToMainActivity(){
        closeExternalSensors();
        if (pauseTimer.isRunning()) {
            pauseTimer.cancel();
        }
        stopSensors();
        isRunning = false;

        finish();
        Intent intent  = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
