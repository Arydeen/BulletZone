package edu.unh.cs.cs619.bulletzone.rest;

import android.os.SystemClock;
import android.text.style.UpdateAppearance;
import android.util.Log;

import edu.unh.cs.cs619.bulletzone.events.GameEventProcessor;
import edu.unh.cs.cs619.bulletzone.events.UpdateBoardEvent;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.rest.spring.annotations.RestService;

import org.greenrobot.eventbus.EventBus;
import org.springframework.web.client.RestClientException;

import java.util.Collection;

import edu.unh.cs.cs619.bulletzone.events.GameEvent;
import edu.unh.cs.cs619.bulletzone.util.GameEventCollectionWrapper;
import edu.unh.cs.cs619.bulletzone.util.GridWrapper;
import edu.unh.cs.cs619.bulletzone.util.ResultWrapper;

/**
 * Created by simon on 10/3/14.
 */
@EBean
public class GridPollerTask {
    @RestService
    BulletZoneRestClient restClient;

    private long previousTimeStamp = -1;
    private boolean updateUsingEvents = false;

    public boolean toggleEventUsage() {
        updateUsingEvents = !updateUsingEvents;
        return updateUsingEvents;
    }

    @Background(id = "grid_poller_task")
    public void doPoll(GameEventProcessor eventProcessor) {
        try {
            GridWrapper grid = restClient.grid();
            onGridUpdate(grid);
            previousTimeStamp = grid.getTimeStamp();
            eventProcessor.setBoard(grid.getGrid());
            eventProcessor.start();

            while (true) {
                Log.d("Poller", "Updating board using events");
                Log.d("PollerTS", "Previous Timestamp: " + previousTimeStamp);

                try {
                    GameEventCollectionWrapper events = restClient.events(previousTimeStamp);
                    boolean haveEvents = false;

                    for (GameEvent event : events.getEvents()) {
                        Log.d("Event-check", event.toString());
                        EventBus.getDefault().post(event);
                        previousTimeStamp = event.getTimeStamp();
                        Log.d("PollerTS", "Current Timestamp: " + previousTimeStamp);
                        haveEvents = true;
                    }

                    if (haveEvents)
                        EventBus.getDefault().post(new UpdateBoardEvent());

                } catch (RestClientException e) {
                    Log.e("GridPollerTask", "Error fetching events", e);
                }

                SystemClock.sleep(100);
            }
        } catch (Exception e) {
            Log.e("GridPollerTask", "Unexpected error in doPoll", e);
        }
    }

    @UiThread
    public void onGridUpdate(GridWrapper gw) {
        EventBus.getDefault().post(new GridUpdateEvent(gw));
    }
}