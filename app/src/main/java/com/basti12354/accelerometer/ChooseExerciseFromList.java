package com.basti12354.accelerometer;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

/**
 * Created by Basti on 25.05.2016.
 */

public class ChooseExerciseFromList extends Activity implements AdapterView.OnItemClickListener{

    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.liste_auswahl);

        //ListView
        String [] andereWorkouts = getResources().getStringArray(R.array.uebungen);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.list_center_item, andereWorkouts);


        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);

    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            AndroidSensorsActivity.choosenExercise = position;

            finish();
            Intent intent = new Intent(view.getContext(), GetSensordatenActivity.class);
            startActivity(intent);









    }
}
