package edu.unh.cs.cs619.bulletzone.web;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import org.greenrobot.eventbus.EventBus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;


import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import edu.unh.cs.cs619.bulletzone.BulletZoneServer;
import edu.unh.cs.cs619.bulletzone.model.events.EventHistory;
import edu.unh.cs.cs619.bulletzone.model.events.GameEvent;
import edu.unh.cs.cs619.bulletzone.model.events.SpawnEvent;
import edu.unh.cs.cs619.bulletzone.repository.InMemoryGameRepository;

import static org.mockito.Mockito.when;
import java.util.Collection;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {BulletZoneServer.class})
public class EventHistoryTest {

    MockMvc mockMvc;
    @Mock
    private EventHistory eventHistory;
    @InjectMocks
    private GameStateController gameStateController;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(gameStateController).build();
        eventHistory = EventHistory.getInstance();
        eventHistory.clearHistory();
        if (!EventBus.getDefault().isRegistered(eventHistory)) {
            EventBus.getDefault().register(eventHistory);
        }
    }

    @Test
    public void EventHistory_SendEvent_AddsEventToEventHistory() throws Exception {
        GameEvent gameEvent = mock(GameEvent.class);
        when(gameEvent.getTimeStamp()).thenReturn(System.currentTimeMillis());
        eventHistory.onEventNotification(gameEvent);
        verify(eventHistory).onEventNotification(gameEvent);
    }

    @Test
    public void EventHistory_SendEvents_AddsMultipleEventsToEventHistory() throws Exception {
        GameEvent gameEvent = mock(GameEvent.class);
        when(gameEvent.getTimeStamp()).thenReturn(System.currentTimeMillis());
        for(int i = 0; i < 50; i++){
            EventBus.getDefault().post(gameEvent);
        }
        Thread.sleep(200);
        Collection<GameEvent> hist = eventHistory.getHistory();
        assertTrue(hist.size() == 50);
    }

    @Test
    public void EventHistory_getHistory_TrimsOldEvents() throws Exception {
        long t = System.currentTimeMillis();
        GameEvent e1 = new SpawnEvent();
        e1.setTimeStamp(t-25000);
        GameEvent e2 = new SpawnEvent();
        e2.setTimeStamp(t-130000);
        GameEvent e3 = new SpawnEvent();
        e3.setTimeStamp(t-150000);
        EventBus.getDefault().post(e1);
        EventBus.getDefault().post(e2);
        EventBus.getDefault().post(e3);

        Thread.sleep(200);
        Collection<GameEvent> hist = eventHistory.getHistory();
        assertTrue(hist.contains(e1));
        assertFalse(hist.contains(e2));
        assertFalse(hist.contains(e3));
    }

    @Test
    public void EventHistory_getHistoryQuery_TrimsEventsSinceTimeRequested() throws Exception {
        long t = System.currentTimeMillis();
        GameEvent e1 = new SpawnEvent();
        e1.setTimeStamp(t-25000);
        GameEvent e2 = new SpawnEvent();
        e2.setTimeStamp(t-30000);
        GameEvent e3 = new SpawnEvent();
        e3.setTimeStamp(t-50000);
        EventBus.getDefault().post(e1);
        EventBus.getDefault().post(e2);
        EventBus.getDefault().post(e3);

        Thread.sleep(200);
        Collection<GameEvent> hist = eventHistory.getHistory(t-35000);
        assertTrue(hist.contains(e1));
        assertTrue(hist.contains(e2));
        assertFalse(hist.contains(e3));
    }

}
