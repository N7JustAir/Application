package com.justairapp;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Home extends AppCompatActivity {

    private static final String CHANNEL_ID = "JustAirNotifId";
    private static final String CHANNEL_NAME = "JustAirNotifName";
    private static final String CHANNEL_DESC = "JustAir Desc";
    private static final String TAG = "JustAirTAG";

    private ImageView itemImage;
    private Spinner spinnerChoiceSensor;

    private List<Sensor> allMySensors;

    private LineChart lineChart;

    private ArrayList<ArrayList<Entry>> allData = new ArrayList<ArrayList<Entry>>();

    private ArrayList<ILineDataSet> dataSets = new ArrayList<>();

    private ArrayList<String> sensorList = new ArrayList<String>();

    /**
     * Firebase
     */
    private FirebaseDatabase rootNode;
    private DatabaseReference dbRef; //having a link to the firebase db

    private SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        itemImage = (ImageView) findViewById(R.id.imageViewOutside);
        spinnerChoiceSensor = (Spinner) findViewById(R.id.chooseSensor);
        sensorList.add("Choose your sensor");

        pref = getSharedPreferences("my_shared_preferences", MODE_PRIVATE);
        String whichImage = pref.getString("whichImage", "green");
        if (whichImage.equals("orange")) {
            itemImage.setImageResource(R.drawable.justairflecheorange);
        } else if (whichImage.equals("red")) {
            itemImage.setImageResource(R.drawable.justairflechered);
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(CHANNEL_DESC);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, sensorList);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinnerChoiceSensor.setAdapter(adapter);
        spinnerChoiceSensor.setOnItemSelectedListener(new Home.SpinnerActivity());

        /**
         * Firebase RootNode
         */
        rootNode = FirebaseDatabase.getInstance();

        /**
         * Graph to display data
         */
        lineChart = (LineChart) findViewById(R.id.graph);
        lineChart.setVisibility(View.INVISIBLE);

        /**
         * Methode pour retirer toutes les valeurs des sensors
         */
        allMySensors = new ArrayList<Sensor>();
        dbRef = rootNode.getReference("Sensors");
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String info = "";
                //On regarde tous les Senors
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String roomName = dataSnapshot.child("roomName").getValue().toString();
                    String idSensor = dataSnapshot.child("idSensor").getValue().toString();
                    Sensor sensor = new Sensor(idSensor, roomName);
                    /*List<Gaz> gazList = new ArrayList<Gaz>();
                    List<Measure> measureList = new ArrayList<Measure>();
                    //On parcourt tous les gazs du capteur
                    for (DataSnapshot ds : dataSnapshot.child("gaz").getChildren()) {
                        String str = ds.child("measures").getValue().toString();
                        Gaz gazFromFirebase = new Gaz(ds.child("gazName").getValue().toString());
                        //ArrayList<Entry> dataValues = new ArrayList<Entry>();
                        //On parcourt toutes les mesures du capteur
                        for (DataSnapshot snap : ds.child("measures").getChildren()) {
                            Measure measure = new Measure(snap.child("timestamp").getValue(Integer.class), snap.child("value").getValue(Float.class));
                            //dataValues.add(new Entry(snap.child("timestamp").getValue(Integer.class), snap.child("value").getValue(Float.class)));
                            measureList.add(measure);
                        }
                        gazFromFirebase.setMeasures(measureList);
                        gazList.add(gazFromFirebase);
                        //LineDataSet lineDataSet = new LineDataSet(dataValues, gazFromFirebase.getGazName());
                    }
                    sensor.setGaz(gazList);*/
                    allMySensors.add(sensor);
                    if (!(sensorList.contains(sensor.getRoomName()))) {
                        sensorList.add(sensor.getRoomName());
                    }
                }
                for (Sensor sensor : allMySensors) {
                    info += sensor.toString() + "\n";
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        dbRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Toast.makeText(getApplicationContext(), "onChildAdded", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Toast.makeText(getApplicationContext(), "onChildChanged", Toast.LENGTH_LONG).show();
                //on regarde tous les gaz du sensors
                for (DataSnapshot snap : snapshot.child("gaz").getChildren()) {
                    int timestamp = 0;
                    float value = 0;
                    for (DataSnapshot ds : snap.child("measures").getChildren()) {
                        if (timestamp <= ds.child("timestamp").getValue(Integer.class)) {
                            value = ds.child("value").getValue(Float.class);
                            timestamp = ds.child("timestamp").getValue(Integer.class);
                        }
                    }
                    Log.d(TAG, "last value registered " + snap.child("gazName").getValue().toString() + " : " + value + " at " + timestamp);
                    changeIconCO2(value);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                Toast.makeText(getApplicationContext(), "onChildRemoved", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Toast.makeText(getApplicationContext(), "onChildMoved", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        RotateAnimation rotate = new RotateAnimation(
                0, 360,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        rotate.setDuration(900);
        rotate.setRepeatCount(Animation.INFINITE);
        itemImage.startAnimation(rotate);
    }

    @Override
    protected void onStop() {
        startService(new Intent(this, NotificationService.class));
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.e(TAG, "onDestroy");
            startForegroundService(new Intent(this, NotificationForegroundService.class));
        }
        super.onDestroy();
    }

    public class SpinnerActivity extends Activity implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            if (pos == 0) {
                lineChart.setVisibility(View.INVISIBLE);
            } else {
                lineChart.setVisibility(View.VISIBLE);
                // An item was selected. You can retrieve the selected item using
                // parent.getItemAtPosition(pos)
                DatabaseReference db = rootNode.getReference("Sensors").child(sensorList.get(pos)).child("gaz");
                db.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot != null) {
                            String info = "";
                            //info += snapshot.child("0").toString();
                            //On enregistre toutes les mesures pour le sensor demandé
                            //On parcourt les gaz mesuré par le sensor
                            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                ArrayList<Entry> dataVals = new ArrayList<Entry>();
                                //On parcourt toute les mesures faites
                                for (DataSnapshot snap : dataSnapshot.child("measures").getChildren()) {
                                    Measure measure = new Measure(snap.child("timestamp").getValue(Integer.class), snap.child("value").getValue(Float.class));
                                    //info += measure.toString() + "\n";
                                    dataVals.add(new Entry(measure.getTimestamp(), measure.getValue()));
                                }
                                info += dataSnapshot.child("gazName").getValue().toString();
                                LineDataSet lineDataSet = new LineDataSet(dataVals, dataSnapshot.child("gazName").getValue().toString());
                                lineDataSet.setDrawValues(false);
                                dataSets.add(lineDataSet);
                                lineDataSet.setLineWidth(2);
                                lineDataSet.setDrawCircles(false);
                            }
                            LineData lineData = new LineData(dataSets);
                            lineChart.setData(lineData);
                            lineChart.invalidate();

                            Description description = new Description();
                            description.setText(sensorList.get(pos));
                            description.setTextColor(Color.BLUE);
                            description.setTextSize(25);
                            lineChart.setDescription(description);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        }

        public void onNothingSelected(AdapterView<?> parent) {
            // Another interface callback
        }
    }

    private void displayNotification(String content, String title) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.justairlogo)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(1, builder.build());
    }


    public void changeIconCO2(float co2value) {
        SharedPreferences.Editor editor = pref.edit();
        if ((0.0 <= co2value) && (co2value <= 599.0)) {
            itemImage.setImageResource(R.drawable.justairfleche);
            editor.putString("whichImage", "green");
        } else if ((600.0 <= co2value) && (co2value <= 1999.0)) {
            itemImage.setImageResource(R.drawable.justairflecheorange);
            editor.putString("whichImage", "orange");
        } else {
            itemImage.setImageResource(R.drawable.justairflechered);
            editor.putString("whichImage", "red");
            displayNotification("You should open the windows", "Warning");
        }
        editor.commit();
    }
}