package com.basti12354.accelerometer;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by Basti on 13.04.2016.
 */
public class GetSensordatenActivity extends AndroidSensorsActivity implements View.OnClickListener {
    Button startBtn, stopBtn;
    TextView aktuellerSatz;
    Boolean isRunning = false;

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
        stopBtn = (Button) findViewById(R.id.stopBtn);

        startBtn.setOnClickListener(this);
        stopBtn.setOnClickListener(this);

        TextView uebung = (TextView) findViewById(R.id.exerciseName);
        uebung.setText(MainActivity.label);

        setTextViewAktuellerSatz();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.startBtn:
               startSensors();
                isRunning = true;


                break;
            case R.id.stopBtn:
            //   printArrayList();

                if (isRunning) {
                    stopSensors();

                    // Erhöhe den Zähler um eins!
                    AndroidSensorsActivity.aktuellerSatz = AndroidSensorsActivity.aktuellerSatz + 1;
                    setTextViewAktuellerSatz();

                    isRunning = false;
                    printArrayList();
                }



                break;
        }
    }

    private void setTextViewAktuellerSatz(){
        aktuellerSatz = (TextView) findViewById(R.id.satz);
        aktuellerSatz.setText(AndroidSensorsActivity.aktuellerSatz + "");
    }
}
