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

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private static final String KEY_NOTIFY = "key-notify";
    private static final String KEY_INTERVAL = "key-interval";
    private static final String KEY_HOUR = "key-hour";
    private static final String KEY_MINUTE = "key-minute";

    private TimePicker timePicker;
    private Button btnNotigy;
    private EditText editMinutes;
    private boolean actived;
    private SharedPreferences storage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        storage = getSharedPreferences("storage", Context.MODE_PRIVATE);

        timePicker = findViewById(R.id.time_picker);
        btnNotigy = findViewById(R.id.notify);
        editMinutes = findViewById(R.id.edit_number_interval);

        actived = storage.getBoolean(KEY_NOTIFY, false);

        setupUI(actived, storage);

        timePicker.setIs24HourView(true);

        btnNotigy.setOnClickListener(notifyListener);

    }


    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i("Teste", "onRestart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("Teste", "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i("Teste", "onPause");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i("Teste", "onStart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i("Teste", "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("Teste", "onDestroy");
    }

    private void alert(int resId) {

        Toast.makeText(MainActivity.this, resId, Toast.LENGTH_LONG).show();
    }

    private boolean intervalIsValid() {
        String sInterval = editMinutes.getText().toString();

        if (sInterval.isEmpty()) {
            alert(R.string.Validation);
            return false;
        }

        if (sInterval.equals("0")) {
            alert(R.string.zero_value);
            return false;
        }

        return true;
    }

    private void setupUI(boolean actived, SharedPreferences storage) {
        if (actived) {

            btnNotigy.setText(R.string.pause);
            btnNotigy.setBackgroundResource(R.drawable.bg_button_backgroud);
            editMinutes.setText(String.valueOf(storage.getInt(KEY_INTERVAL, 0)));
            timePicker.setCurrentHour(storage.getInt(KEY_HOUR, timePicker.getCurrentHour()));
            timePicker.setCurrentMinute(storage.getInt(KEY_MINUTE, timePicker.getCurrentMinute()));
        } else {

            btnNotigy.setText(R.string.notify);
            btnNotigy.setBackgroundResource(R.drawable.bg_button_accent);
        }
    }

    private void updateStorage(boolean added, int interval, int hour, int minute) {
        SharedPreferences.Editor edit = storage.edit();
        edit.putBoolean(KEY_NOTIFY, added);
        if (added) {
            edit.putInt(KEY_INTERVAL, interval);
            edit.putInt(KEY_HOUR, hour);
            edit.putInt(KEY_MINUTE, minute);
        } else {
            edit.remove(KEY_INTERVAL);
            edit.remove(KEY_HOUR);
            edit.remove(KEY_MINUTE);
        }


        edit.apply();
    }

    private void setupNotification(boolean added, int interval, int hour, int minute) {
        Intent notificationIntent = new Intent(MainActivity.this, NotificationPublisher.class);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        if (added) {

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);

            notificationIntent.putExtra(NotificationPublisher.KEY_NOTIFICATION, "Hora de beber água!");
            notificationIntent.putExtra(NotificationPublisher.KEY_NOTIFICATION_ID, 1);

            PendingIntent broadcast = PendingIntent.getBroadcast(MainActivity.this, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            assert alarmManager != null;
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), interval * 60 * 1000, broadcast);
        } else {
            PendingIntent broadcast = PendingIntent.getBroadcast(MainActivity.this, 0, notificationIntent, 0);

            alarmManager.cancel(broadcast);
        }
    }

    private View.OnClickListener notifyListener =
            new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    if (!actived) {

                        if (!intervalIsValid()) return;

                        int interval = Integer.parseInt(editMinutes.getText().toString());
                        int hour = timePicker.getCurrentHour();
                        int minute = timePicker.getCurrentMinute();

                        updateStorage(true, interval, hour, minute);

                        setupUI(true, storage);

                        setupNotification(true, interval, hour, minute);

                        alert(R.string.notified);

                        Log.d("Teste", String.format("%d, %d, %d", interval, hour, minute));

                        actived = true;
                    } else {
                        updateStorage(false, 0, 0, 0);
                        setupUI(false, storage);
                        setupNotification(false, 0, 0, 0);
                        alert(R.string.notified_pause);

                        actived = false;


                    }
                }
            };
}
