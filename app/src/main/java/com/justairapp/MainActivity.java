package com.justairapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private static final String CHANNEL_ID = "NOTIF" ;
    private TextView txtCo2;
    private EditText editText;
    private Button btnSend;

    /**
     * Firebase
     */
    private DatabaseReference dbRef; //having a link to the firebase db

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtCo2 = (TextView) findViewById(R.id.txtCO2);
        editText = (EditText) findViewById(R.id.editTxt);
        btnSend = (Button) findViewById(R.id.btnSend);

        //Firebase
        //to get a link to our database for the gazMeasures
        dbRef = FirebaseDatabase.getInstance().getReference("gazMeasures");
        //retrieve data from the database and display them in a textView
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String co2value = snapshot.child("co2Value").getValue().toString();
                Toast.makeText(MainActivity.this, "Cédric a pété dans le salon", Toast.LENGTH_SHORT).show();
                txtCo2.setText("CO₂ : " + co2value);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!(editText.getText().equals(""))) {
                    //Toast.makeText(MainActivity.this, "Here : " + editText.getText(), Toast.LENGTH_SHORT).show();
                    dbRef.child("co2Value").setValue(editText.getText().toString());
                    Toast.makeText(MainActivity.this, "Send : " + editText.getText(), Toast.LENGTH_SHORT).show();
                }
            }
        });
        btnSend.setVisibility(View.INVISIBLE);
        editText.setVisibility(View.INVISIBLE);
    }
}