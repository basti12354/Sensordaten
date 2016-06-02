package com.basti12354.accelerometer;

/**
 * Created by Basti on 23.03.2016.
 */
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;


public class MainActivity extends Activity implements View.OnClickListener {
    private EditText editText;
    public static String probandenName;



    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Starte das Bluetooth automatisch!
        startBluetooth();


        Button starteDatenerhebung = (Button) findViewById(R.id.starteDatenerhebung);
        starteDatenerhebung.setText(GetSensordatenActivity.label);
        starteDatenerhebung.setOnClickListener(this);

        Button chooseExerciseFromList = (Button) findViewById(R.id.listButton);
        chooseExerciseFromList.setOnClickListener(this);

        editText = (EditText) findViewById(R.id.editText);

        Switch aSwitch = (Switch) findViewById(R.id.switch1);

        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked){
                    startBluetooth();
                }else{
                    stopBluetooth();
                }

            }
        });

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    private void startBluetooth(){

        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
        }

        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();

        }
    }
    private void stopBluetooth(){

        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
        }

        if (mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.disable();

        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.starteDatenerhebung:
                probandenName = editText.getText().toString();

                if (probandenName.length() > 0){
                    if (mBluetoothAdapter.isEnabled()) {
                        Intent intent = new Intent(v.getContext(), GetSensordatenActivity.class);
                        startActivity(intent);
                    }
                    else {
                        Toast.makeText(MainActivity.this, "WARTE AUF BLUETOOTH! BITTE GLEICH NOCHMAL KLICKEN!", Toast.LENGTH_SHORT).show();
                    }

                }
                else {
                    Toast.makeText(MainActivity.this, "Bitte Namen eingeben!", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.listButton:
                probandenName = editText.getText().toString();

                if (probandenName.length() > 0){
                    if (mBluetoothAdapter.isEnabled()) {
                        Intent intent = new Intent(v.getContext(), ChooseExerciseFromList.class);
                        startActivity(intent);
                    }
                    else {
                        Toast.makeText(MainActivity.this, "WARTE AUF BLUETOOTH! BITTE GLEICH NOCHMAL KLICKEN!", Toast.LENGTH_SHORT).show();
                    }

                }
                else {
                    Toast.makeText(MainActivity.this, "Bitte Namen eingeben!", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }



}

