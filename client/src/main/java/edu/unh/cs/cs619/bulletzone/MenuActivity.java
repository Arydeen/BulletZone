package edu.unh.cs.cs619.bulletzone;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.NonConfigurationInstance;

import edu.unh.cs.cs619.bulletzone.rest.GridPollerTask;

@EActivity(R.layout.activity_menu)
public class MenuActivity extends Activity {

    private static final String TAG = "MenuActivity";

    @NonConfigurationInstance
    @Bean
    GridPollerTask gridPollTask;

    private long userId = -1;
    private long tankId = -1;

    @Bean
    MenuController menuController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate");
    }

    @AfterViews
    protected void afterViewInjection() {
        Log.d(TAG, "afterViewInjection");
        userId = getIntent().getLongExtra("USER_ID", -1);
    }

    @Click(R.id.joinButton)
    @Background
    void join() {
        try {
            tankId = menuController.joinAsync();
            gridPollTask.doPoll();
            // Start the Client activity
            Intent intent = new Intent(this, ClientActivity_.class);
            intent.putExtra("USER_ID", userId);
            intent.putExtra("TANK_ID", tankId);
            Log.d("MenuActivity", "Starting ClientActivity_");
            startActivity(intent);
            Log.d("MenuActivity", "ClientActivity_ started");
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Error joining game", e);
        }
    }
}