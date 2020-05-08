package com.example.countdown;

import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private EditText EditTextInput;
    private TextView TextViewCountDown;
    private Button ButtonSet;
    private Button ButtonStartPause;
    private Button ButtonReset;
    private CountDownTimer CountDownTimer;
    private boolean TimerRunning;
    private long StartTimeInMillis;
    private long TimeLeftInMillis;
    private long EndTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditTextInput = findViewById(R.id.edit_text_input);
        TextViewCountDown = findViewById(R.id.text_view_countdown);

        ButtonSet = findViewById(R.id.button_set);
        ButtonStartPause = findViewById(R.id.button_start_pause);
        ButtonReset = findViewById(R.id.button_reset);

        ButtonSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = EditTextInput.getText().toString();
                if (input.length() == 0) {
                    Toast.makeText(MainActivity.this, "Field can't be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                long millisInput = Long.parseLong(input) * 60000;
                if (millisInput == 0) {
                    Toast.makeText(MainActivity.this, "Please enter a positive number", Toast.LENGTH_SHORT).show();
                    return;
                }

                setTime(millisInput);
                EditTextInput.setText("");
            }
        });

        ButtonStartPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TimerRunning) {
                    pauseTimer();
                } else {
                    startTimer();
                }
            }
        });

        ButtonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetTimer();
            }
        });
    }

    private void setTime(long milliseconds) {
        StartTimeInMillis = milliseconds;
        resetTimer();
        closeKeyboard();
    }

    private void startTimer() {
        EndTime = System.currentTimeMillis() + TimeLeftInMillis;

        CountDownTimer = new CountDownTimer(TimeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                TimeLeftInMillis = millisUntilFinished;
                updateCountDownText();
            }

            @Override
            public void onFinish() {
                TimerRunning = false;
                updateWatchInterface();
            }
        }.start();

        TimerRunning = true;
        updateWatchInterface();
    }

    private void pauseTimer() {
        CountDownTimer.cancel();
        TimerRunning = false;
        updateWatchInterface();
    }

    private void resetTimer() {
        TimeLeftInMillis = StartTimeInMillis;
        updateCountDownText();
        updateWatchInterface();
    }

    private void updateCountDownText() {
        int hours = (int) (TimeLeftInMillis / 1000) / 3600;
        int minutes = (int) ((TimeLeftInMillis / 1000) % 3600) / 60;
        int seconds = (int) (TimeLeftInMillis / 1000) % 60;

        String timeLeftFormatted;
        if (hours > 0) {
            timeLeftFormatted = String.format(Locale.getDefault(),
                    "%d:%02d:%02d", hours, minutes, seconds);
        } else {
            timeLeftFormatted = String.format(Locale.getDefault(),
                    "%02d:%02d", minutes, seconds);
        }

        TextViewCountDown.setText(timeLeftFormatted);
    }

    private void updateWatchInterface() {
        if (TimerRunning) {
            EditTextInput.setVisibility(View.INVISIBLE);
            ButtonSet.setVisibility(View.INVISIBLE);
            ButtonReset.setVisibility(View.INVISIBLE);
            ButtonStartPause.setText("Pause");
        } else {
            EditTextInput.setVisibility(View.VISIBLE);
            ButtonSet.setVisibility(View.VISIBLE);
            ButtonStartPause.setText("Start");

            if (TimeLeftInMillis < 1000) {
                ButtonStartPause.setVisibility(View.INVISIBLE);
            } else {
                ButtonStartPause.setVisibility(View.VISIBLE);
            }

            if (TimeLeftInMillis < StartTimeInMillis) {
                ButtonReset.setVisibility(View.VISIBLE);
            } else {
                ButtonReset.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void closeKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putLong("startTimeInMillis", StartTimeInMillis);
        editor.putLong("millisLeft", TimeLeftInMillis);
        editor.putBoolean("timerRunning", TimerRunning);
        editor.putLong("endTime", EndTime);

        editor.apply();

        if (CountDownTimer != null) {
            CountDownTimer.cancel();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);

        StartTimeInMillis = prefs.getLong("startTimeInMillis", 600000);
        TimeLeftInMillis = prefs.getLong("millisLeft", StartTimeInMillis);
        TimerRunning = prefs.getBoolean("timerRunning", false);

        updateCountDownText();
        updateWatchInterface();

        if (TimerRunning) {
            EndTime = prefs.getLong("endTime", 0);
            TimeLeftInMillis = EndTime - System.currentTimeMillis();

            if (TimeLeftInMillis < 0) {
                TimeLeftInMillis = 0;
                TimerRunning = false;
                updateCountDownText();
                updateWatchInterface();
            } else {
                startTimer();
            }
        }
    }
}