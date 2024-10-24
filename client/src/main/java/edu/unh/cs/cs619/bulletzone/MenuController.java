package edu.unh.cs.cs619.bulletzone;

import android.os.SystemClock;
import android.util.Log;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.NonConfigurationInstance;
import org.androidannotations.rest.spring.annotations.RestService;

import edu.unh.cs.cs619.bulletzone.events.GameEventProcessor;
import edu.unh.cs.cs619.bulletzone.rest.BulletZoneRestClient;
import edu.unh.cs.cs619.bulletzone.rest.GridPollerTask;
import edu.unh.cs.cs619.bulletzone.ui.GridAdapter;

/**
 * Made by Alec Rydeen
 *
 * Simple Class to take some of the Rest Client Calls out of MenuActivity
 */

@EBean
public class MenuController {

    private static final String TAG = "MenuController";

    @RestService
    BulletZoneRestClient restClient;

    public MenuController() {}

    public long joinAsync() {
        return restClient.join().getResult();
    }
}
