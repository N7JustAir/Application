package com.justairapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class NotificationForegroundService extends Service {
    public static final String CHANNEL_ID = "ForegroundServiceChannel";
    /**
     * Firebase
     */
    private FirebaseDatabase rootNode;
    private DatabaseReference dbRef; //having a link to the firebase db

    private SharedPreferences pref;

    @Override
    public void onCreate() {
        super.onCreate();
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
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                for (DataSnapshot snap : snapshot.child("gaz").getChildren()) {
                    int timestamp = 0;
                    float value = 0;
                    for (DataSnapshot ds : snap.child("measures").getChildren()) {
                        if (timestamp <= ds.child("timestamp").getValue(Integer.class)) {
                            value = ds.child("value").getValue(Float.class);
                            timestamp = ds.child("timestamp").getValue(Integer.class);
                        }
                    }
                    changeIconCO2(value);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String input = intent.getStringExtra("inputExtra");
        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Foreground Service")
                .setContentText(input)
                .setSmallIcon(R.drawable.justairlogo)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);
        //do heavy work on a background thread
        //stopSelf();
        return START_NOT_STICKY;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
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
            editor.putString("whichImage","green");
        } else if ((600.0 <= co2value) && (co2value <= 1999.0)) {
            editor.putString("whichImage","orange");
        } else {
            editor.putString("whichImage","red");
            displayNotification("You should open the windows", "Warning");
        }
        editor.commit();
    }

    private void changeicon() {
        // enable old icon
        PackageManager manager = getPackageManager();
        manager.setComponentEnabledSetting(new ComponentName(getApplicationContext(),"com.prepare.makedirectory.MainActivity"), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

        // disable new icon
        manager.setComponentEnabledSetting(new ComponentName(getApplicationContext(),"com.prepare.makedirectory.MainActivityAlias"),PackageManager.COMPONENT_ENABLED_STATE_DISABLED,PackageManager.DONT_KILL_APP);
        Toast.makeText(getApplicationContext(), "Enable Old Icon",Toast.LENGTH_LONG).show();
    }
}
