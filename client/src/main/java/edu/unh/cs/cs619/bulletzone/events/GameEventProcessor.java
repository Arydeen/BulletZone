package edu.unh.cs.cs619.bulletzone.events;

import android.util.Log;

import org.androidannotations.annotations.EBean;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import edu.unh.cs.cs619.bulletzone.rest.GridUpdateEvent;

@EBean
public class GameEventProcessor {
    private int[][] board;

    public EventBus eb = null;

    public void setBoard(int[][] newBoard) { board = newBoard; }

    public void start() {
//        EventBus.getDefault().register(this);
        if (eb == null) {
            EventBus.getDefault().register(this);
        } else {
            eb.register(this);
        }

    }

    public void stop() {
//        EventBus.getDefault().unregister(this);
        if (eb == null) {
            EventBus.getDefault().unregister(this);
        } else {
            eb.unregister(this);
        }
    }

    @Subscribe
    public void onNewEvent(GameEvent event) {
        Log.d("GameEventProcessor", "Applying " + event);
        event.applyTo(board);
    }
}
