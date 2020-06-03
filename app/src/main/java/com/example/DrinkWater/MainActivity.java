package com.example.DrinkWater;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private static final String KEY_NOTIFY = "key-notify";
    private static final String KEY_INTERVAL = "key-interval";
    private static final String KEY_HOUR = "key-hour";
    private static final String KEY_MINUTE = "key-minute";

    private TimePicker timePicker;
    private Button btnNotigy;
    private EditText editMinutes;
    private int interval;
    private Integer hour;
    private Integer minute;
    private boolean actived;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        timePicker = findViewById(R.id.time_picker);
        btnNotigy = findViewById(R.id.notify);
        editMinutes = findViewById(R.id.edit_number_interval);

        final SharedPreferences storage = getSharedPreferences("storage", Context.MODE_PRIVATE);
        actived = storage.getBoolean(KEY_NOTIFY, false);


        if (actived) {

            btnNotigy.setText(R.string.pause);
            btnNotigy.setBackgroundColor(ContextCompat.getColor(MainActivity.this, android.R.color.black));

            editMinutes.setText(String.valueOf(storage.getInt(KEY_INTERVAL, 0)));
            timePicker.setCurrentHour(storage.getInt(KEY_HOUR, timePicker.getCurrentHour()));
            timePicker.setCurrentMinute(storage.getInt(KEY_MINUTE, timePicker.getCurrentMinute()));
        } else {

            btnNotigy.setText(R.string.notify);
            btnNotigy.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.colorAccent));
        }


        timePicker.setIs24HourView(true);

        btnNotigy.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (!actived) {

                    String sInterval = editMinutes.getText().toString();

                    if (sInterval.isEmpty()) {
                        Toast.makeText(MainActivity.this, R.string.Validation, Toast.LENGTH_LONG).show();
                        return;
                    }

                    interval = Integer.parseInt(sInterval);
                    hour = timePicker.getCurrentHour();
                    minute = timePicker.getCurrentMinute();

                    Log.d("Teste", String.format("%d, %d, %d", interval, hour, minute));

                    btnNotigy.setText(R.string.pause);
                    btnNotigy.setBackgroundColor(ContextCompat.getColor(MainActivity.this, android.R.color.black));

                    SharedPreferences.Editor edit = storage.edit();
                    edit.putBoolean(KEY_NOTIFY, true);
                    edit.putInt(KEY_INTERVAL, interval);
                    edit.putInt(KEY_HOUR, hour);
                    edit.putInt(KEY_MINUTE, minute);
                    edit.apply();

                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.HOUR_OF_DAY, hour);
                    calendar.set(Calendar.MINUTE, minute);
                    calendar.set(Calendar.SECOND, 0);

                    Intent notificationIntent = new Intent(MainActivity.this, NotificationPublisher.class);
                    notificationIntent.putExtra(NotificationPublisher.KEY_NOTIFICATION, "Hora de beber água!");
                    notificationIntent.putExtra(NotificationPublisher.KEY_NOTIFICATION_ID, 1);

                    PendingIntent broadcast = PendingIntent.getBroadcast(MainActivity.this, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
                    AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                    assert alarmManager != null;
                    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), interval * 60 * 1000, broadcast);

                    actived = true;
                } else {
                    btnNotigy.setText(R.string.notify);
                    btnNotigy.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.colorAccent));


                    SharedPreferences.Editor edit = storage.edit();
                    edit.putBoolean(KEY_NOTIFY, false);
                    edit.remove(KEY_INTERVAL);
                    edit.remove(KEY_HOUR);
                    edit.remove(KEY_MINUTE);
                    edit.apply();

                    Intent notificationIntent = new Intent(MainActivity.this, NotificationPublisher.class);
                    PendingIntent broadcast = PendingIntent.getBroadcast(MainActivity.this, 0, notificationIntent, 0);
                    AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                    assert alarmManager != null;
                    alarmManager.cancel(broadcast);



                    actived = false;


                }
            }
        });

    }
}
