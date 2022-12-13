package com.justairapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
    private ImageView itemImage;

    /**
     * Firebase
     */
    private DatabaseReference dbRef; //having a link to the firebase db

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        txtCo2 = (TextView) findViewById(R.id.txtCO2);
        editText = (EditText) findViewById(R.id.editTxt);
        btnSend = (Button) findViewById(R.id.btnSend);
        itemImage = (ImageView) findViewById(R.id.imageViewOutside);

        //Firebase
        //to get a link to our database for the gazMeasures
        dbRef = FirebaseDatabase.getInstance().getReference("gazMeasures");
        //retrieve data from the database and display them in a textView
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String co2Txt = snapshot.child("co2Value").getValue().toString();
                float co2value = 0;
                try {
                    co2value = Integer.valueOf(co2Txt);
                    if ((0.0 <= co2value) && (co2value <= 599.0)) {
                        itemImage.setImageResource(R.drawable.justairfleche);
                    } else if ((600.0 <= co2value) && (co2value <= 1999.0)) {
                        itemImage.setImageResource(R.drawable.justairflecheorange);
                    } else {
                        itemImage.setImageResource(R.drawable.justairflechered);
                    }
                } catch (Exception e) {

                }
                txtCo2.setText("CO₂ : " + co2value + "ppm");
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

        RotateAnimation rotate = new RotateAnimation(
                0, 360,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        rotate.setDuration(900);
        rotate.setRepeatCount(Animation.INFINITE);
        itemImage.startAnimation(rotate);
    }
}