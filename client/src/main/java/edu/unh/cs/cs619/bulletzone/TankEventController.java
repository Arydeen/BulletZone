package edu.unh.cs.cs619.bulletzone;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EBean;
import org.androidannotations.rest.spring.annotations.RestService;

import edu.unh.cs.cs619.bulletzone.rest.BulletZoneRestClient;

/**
 * Made by Alec Rydeen
 *
 * This class takes the responsibility of communicating with the Rest Client away from the ClientActivity,
 * and moves it here, which is then used inside of ClientActivity
 */

// Controller Class to move rest client calls for tank controls outside of ClientActivity
@EBean
public class TankEventController {

    @RestService
    BulletZoneRestClient restClient;

    public TankEventController() {}

    @Background
    public void moveAsync(long tankId, byte direction) {
        restClient.move(tankId, direction);
    }

    @Background
    public void turnAsync(long tankId, byte direction) {
        restClient.turn(tankId, direction);
    }

    @Background
    public void fire(long tankId) {
        restClient.fire(tankId);
    }

    @Background
    void leaveGameAsync(long tankId) {
        restClient.leave(tankId);
    }

}