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

    Boolean paused = false;
    Boolean firstStart = true;

    // 30000 entspricht 30 Sekunden
    long pauseDuration = 30000l;

    Timer pauseTimer;
    Thread t;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.exercise_layout);

      //  initializeViews();

        intitializeSmartphoneSensors();

        // Wird beim ersten Start der Activity hier ausgeführt -> danach sobald der Pause Btn gedrückt wird
        setExerciseLabelAndPlaySoundNextExercise();

        // Keep Screen ON
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

//        getActionBar().setDisplayHomeAsUpEnabled(true);

        startBtn = (Button) findViewById(R.id.startBtn);
 //       stopBtn = (Button) findViewById(R.id.stopBtn);

        startBtn.setOnClickListener(this);
  //      stopBtn.setOnClickListener(this);


        setTextViewAktuellerSatz();

        // Soll anzeigen, welche Sensoren verbunden sind!
        changeColorOfTextViews();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.startBtn:

                // Erster Start -> bei Klick wird die Datenerhebung gestartet!
                if ( firstStart) {
                    startBtn.setText("Stop");
                    startSensors();
                    // Ändere die LEDs der verbundenen Sensoren -> damit Verbindungsfehler gezeigt werden können!
                    changeLEDColorToStateRUNNING();

                    // STARTE STREAMING DER EXTERNEN
                    getAllDataFromExternalSensors();

                    firstStart = false;
                }

                // Sensoren laufen gerade und werden nun pausiert!
                else {
                   // Log.i(LOG + "SAVE", "Button geklicked OBEN");
                    if (paused == false && choosenExercise != 8) {
                        startBtn.setText("PAUSE...");

                      //  Log.i(LOG + "SAVE", "Button geklicked");

                        // Speichert die Arrayliste mit Daten ab. Muss vor der Erhöhung des Zählers geschehen!
                        stopSensors();

                        // Ändert das Label für die nächste Übung
                        setExerciseLabelAndPlaySoundNextExercise();

                        changeLEDColorToStatePAUSE();


                        // Zurück zur MAIN-Activity wenn Übung 3 Mal gemacht wurde!
                        if (AndroidSensorsActivity.aktuellerSatz == 3) {
                            AndroidSensorsActivity.aktuellerSatz = 1;

                            backToMainActivity();

                        }
                        // Übung noch keine 3 Mal gemacht -> Pause-Timer gestartet und Übung muss nach diesem Timer noch einmal gemacht werden!
                        else {
                            // Erhöhe den Zähler um eins!
                            AndroidSensorsActivity.aktuellerSatz = AndroidSensorsActivity.aktuellerSatz + 1;

                            // Pause zwischen Sätzen EINER Übung!
                            pauseTimer = new ExampleTimer(1000l, pauseDuration);
                            startPauseTimer();
                        }

                        setTextViewAktuellerSatz();


                        //printArrayList();
                    }
                    // Die gemacht Übung war die LETZTE ÜBUNG auf der Liste -> Sensoren werden gestoppt und geschlossen -> über normale Navigaiotn
                    else if (choosenExercise == 8){

                            startBtn.setText("AUFNAHME BEENDET!");
                            // Speichert die Arrayliste mit Daten ab. Muss vor der Erhöhung des Zählers geschehen!
                            stopSensors();
                            closeExternalSensors();
                        }




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
        // Setze Boolean um ein Klicken auf den PAUSE BTN zu verhindern
        paused = true;

        // starte AUDIO -> "In 5 Sekunden gehts weiter"
        playPauseSound();

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
                                            // Pause ist nun vorbei setze boolean wieder in alten Zustand, damit der Btn wieder gedrückt werden kann
                                            paused = false;
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


        // es müssen noch weitere Übungen aufgenommen werden!

            // Nach VIBRATION werden die Sensoren wieder gestartet!
            startSensors();

            startBtn.setText("STOP!");

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


    // Stoppt Sensoren
    private void backToMainActivity(){
        closeExternalSensors();
        if ((pauseTimer != null) &&  pauseTimer.isRunning()) {
            pauseTimer.cancel();
        }
        stopSensors();

        AndroidSensorsActivity.aktuellerSatz = 1;

        finish();
        Intent intent  = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
