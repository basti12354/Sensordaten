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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;


public class MainActivity extends Activity implements AdapterView.OnItemClickListener{
    private EditText editText;
    public static String probandenName;
    public static String label;

    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Starte das Bluetooth automatisch!
        startBluetooth();

        //ListView
        String [] andereWorkouts = getResources().getStringArray(R.array.uebungen);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.list_center_item, andereWorkouts);


        ListView listView = (ListView) findViewById(android.R.id.list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);

        editText = (EditText) findViewById(R.id.editText);

    }



    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        probandenName = editText.getText().toString();

        if (probandenName.length() > 0){
            if (mBluetoothAdapter.isEnabled()) {
                Intent intent = new Intent(view.getContext(), GetSensordatenActivity.class);
                startActivity(intent);
            }
            else {
                Toast.makeText(MainActivity.this, "WARTE AUF BLUETOOTH! BITTE GLEICH NOCHMAL KLICKEN!", Toast.LENGTH_SHORT).show();
            }


//            switch (position){
//                case 0:
//                    label = "Push Up";
//                    break;
//                case 1:
//                    label = "Lunge";
//                    break;
//                case 2:
//                    label = "Crunch";
//                    break;
//                case 3:
//                    label = "Jumping Jack";
//                    break;
//                case 4:
//                    label = "Donkey Kick";
//                    break;
//                case 5:
//                    label = "Squat";
//                    break;
//                case 6:
//                    label = "Mountain Climber";
//                    break;
//                case 7:
//                    label = "Bicycle Crunch";
//                    break;
//                case 8:
//                    label = "Burpees";
//                    break;
//                case 9:
//                    label = "Russian Twist";
//                    break;
//
//
//            }
//            Log.i("Label: ", label);
        }
        else {
            Toast.makeText(MainActivity.this, "Bitte Namen eingeben!", Toast.LENGTH_SHORT).show();
        }

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
            Log.i("BT: ", "Bluetooth was succesfully started!");
        }
    }
}

