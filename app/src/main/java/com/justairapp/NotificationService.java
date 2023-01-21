package com.justairapp;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class NotificationService extends Service {
    private static final String TAG = "JustAirService";
    private static final String CHANNEL_ID = "JustAirNotifId";
    private static final String CHANNEL_NAME = "JustAirNotifName";
    private static final String CHANNEL_DESC = "JustAir Desc";

    /**
     * Firebase
     */
    private FirebaseDatabase rootNode;
    private DatabaseReference dbRef; //having a link to the firebase db

    private SharedPreferences pref;

    Timer timer;
    TimerTask timerTask;
    int Your_X_SECS = 5;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);

        startTimer();

        return START_STICKY;
    }

    @Override
    public void onCreate() {
        Log.e(TAG, "onCreate");
        pref = getSharedPreferences("my_shared_preferences",MODE_PRIVATE);
        /**
         * Firebase RootNode
         */
        rootNode = FirebaseDatabase.getInstance();

        /**
         * Methode pour retirer toutes les valeurs des sensors
         */
        dbRef = rootNode.getReference("Sensors");
        dbRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Toast.makeText(getApplicationContext(), "onChildAdded", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Toast.makeText(getApplicationContext(), "onChildChanged", Toast.LENGTH_LONG).show();
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
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy");
        stoptimertask();
        super.onDestroy();
    }

    //we are going to use a handler to be able to run in our TimerTask
    final Handler handler = new Handler();


    public void startTimer() {
        //set a new Timer
        timer = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask();

        //schedule the timer, after the first 5000ms the TimerTask will run every 10000ms
        timer.schedule(timerTask, 5000, Your_X_SECS * 1000); //
        //timer.schedule(timerTask, 5000,1000); //
    }

    public void stoptimertask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public void initializeTimerTask() {

        timerTask = new TimerTask() {
            public void run() {

                //use a handler to run a toast that shows the current timestamp
                handler.post(new Runnable() {
                    public void run() {
                        //TODO CALL NOTIFICATION FUNC

                    }
                });
            }
        };
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
            editor.putString("whichImage","green");
        } else if ((600.0 <= co2value) && (co2value <= 1999.0)) {
            editor.putString("whichImage","orange");
        } else {
            editor.putString("whichImage","red");
            displayNotification("You should open the windows", "Warning");
        }
        editor.commit();
    }
}
