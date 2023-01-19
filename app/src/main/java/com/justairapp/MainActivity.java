package com.justairapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private static final String CHANNEL_ID = "NOTIF" ;
    private TextView txtCo2;
    private EditText editText;
    private Button btnSend;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        txtCo2 = (TextView) findViewById(R.id.txtCO2);
        txtCo2.setVisibility(View.INVISIBLE);
        editText = (EditText) findViewById(R.id.editTxt);
        btnSend = (Button) findViewById(R.id.btnSend);
        itemImage = (ImageView) findViewById(R.id.imageViewOutside);
        spinnerChoiceSensor = (Spinner) findViewById(R.id.chooseSensor);
        sensorList.add("Choose your sensor");

        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, sensorList);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinnerChoiceSensor.setAdapter(adapter);
        spinnerChoiceSensor.setOnItemSelectedListener(new SpinnerActivity());

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
                txtCo2.setText(info);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //dbRef2.child(salon.getRoomName()).setValue(salon);
                //dbRef2.child(cuisine.getRoomName()).setValue(cuisine);
            }
        });
        btnSend.setVisibility(View.INVISIBLE);
        editText.setVisibility(View.INVISIBLE);

        RotateAnimation rotate = new RotateAnimation(
                0, 360,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        rotate.setDuration(900);
        rotate.setRepeatCount(Animation.INFINITE);
        itemImage.startAnimation(rotate);

        /*lineChart = (LineChart) findViewById(R.id.graph);

        dataVals.add(new Entry(0,20));
        dataVals.add(new Entry(1.5F,24));
        dataVals.add(new Entry(2,29));
        dataVals.add(new Entry(3,30));

        dataVals2.add(new Entry(0,32));
        dataVals2.add(new Entry(1.5F,23));
        dataVals2.add(new Entry(2,29));
        dataVals2.add(new Entry(4,5));

        LineDataSet lineDataSet = new LineDataSet(dataVals, "CO2");
        LineDataSet lineDataSet2 = new LineDataSet(dataVals2, "CO");
        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(lineDataSet);
        lineDataSet.setColor(Color.GREEN);
        lineDataSet.setLineWidth(2);
        lineDataSet.setDrawCircles(false);
        dataSets.add(lineDataSet2);
        lineDataSet2.setColor(Color.BLACK);
        lineDataSet2.setLineWidth(2);
        lineDataSet2.setDrawCircles(false);

        LineData lineData = new LineData(dataSets);

        lineChart.setData(lineData);
        lineChart.invalidate();
        lineChart.setBackgroundColor(Color.WHITE);
        lineChart.setNoDataText("No Data");
        lineChart.setDrawGridBackground(true);
        lineChart.setDrawBorders(true);

        Description description = new Description();
        description.setText("Cuisine");
        description.setTextColor(Color.BLUE);
        description.setTextSize(25);
        lineChart.setDescription(description);

        Legend legend = lineChart.getLegend();
        legend.setEnabled(true);
        legend.setTextColor(Color.BLACK);*/
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
                                dataSets.add(lineDataSet);
                                lineDataSet.setLineWidth(2);
                                lineDataSet.setDrawCircles(false);
                            }
                            LineData lineData = new LineData(dataSets);
                            lineChart.setData(lineData);
                            lineChart.invalidate();
                            txtCo2.setText(info);

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


    public void changeIcon(float co2value) {
        if ((0.0 <= co2value) && (co2value <= 599.0)) {
            itemImage.setImageResource(R.drawable.justairfleche);
        } else if ((600.0 <= co2value) && (co2value <= 1999.0)) {
            itemImage.setImageResource(R.drawable.justairflecheorange);
        } else {
            itemImage.setImageResource(R.drawable.justairflechered);
        }
    }
}