package edu.unh.cs.cs619.bulletzone.repository;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.Mockito;
import java.util.Timer;
import java.util.TimerTask;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


import edu.unh.cs.cs619.bulletzone.model.Bullet;
import edu.unh.cs.cs619.bulletzone.model.Direction;
import edu.unh.cs.cs619.bulletzone.model.IllegalTransitionException;
import edu.unh.cs.cs619.bulletzone.model.LimitExceededException;
import edu.unh.cs.cs619.bulletzone.model.Tank;
import edu.unh.cs.cs619.bulletzone.model.Game;
import edu.unh.cs.cs619.bulletzone.model.TankDoesNotExistException;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class InMemoryGameRepositoryTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    @InjectMocks
    InMemoryGameRepository repo;
    private InMemoryGameRepository gameRepository;
    @Mock
    private Constraints constraints;

    @Mock
    private Game mockGame;
    @Mock
    private Tank mockTank;

    private final long mockMillis = 500;
    private final int[] bulletDelay = {500, 1000, 1500};
    private final long tankId = 1L;
    private final String tankIp = "192.168.1.1";

    @Before
    public void setUp() throws Exception {
        constraints = mock(Constraints.class);
        gameRepository = new InMemoryGameRepository(constraints, new GameBoardBuilder());
        mockGame = mock(Game.class);
        mockTank = mock(Tank.class);
        Map<Long, Tank> tanks = new HashMap<>();
        tanks.put(tankId, mockTank);
//        when(mockGame.getTanks()).thenReturn(tanks);
        gameRepository.create();
    }

    @Test
    public void testJoin() throws Exception {
        Tank tank = repo.join("");
        Assert.assertNotNull(tank);
        Assert.assertTrue(tank.getId() >= 0);
        Assert.assertNotNull(tank.getDirection());
        Assert.assertTrue(tank.getDirection() == Direction.Up);
        Assert.assertNotNull(tank.getParent());
    }
    /*
    @Test
    public void testTurn() throws Exception {
        Tank tank = repo.join("");
        Assert.assertNotNull(tank);
        Assert.assertTrue(tank.getId() >= 0);
        Assert.assertNotNull(tank.getDirection());
        Assert.assertTrue(tank.getDirection() == Direction.Up);
        Assert.assertNotNull(tank.getParent());

        Assert.assertTrue(repo.turn(tank.getId(), Direction.Right));
        Assert.assertTrue(tank.getDirection() == Direction.Right);

        thrown.expect(TankDoesNotExistException.class);
        thrown.expectMessage("Tank '1000' does not exist");
        repo.turn(1000, Direction.Right);
    } */


    @Test
    public void turn_VehicleFacingUpTurnRight_TurnSucceeds() throws IllegalTransitionException, TankDoesNotExistException {
        Tank tank = repo.join("turningVehicle");

        Assert.assertEquals(Direction.Up, tank.getDirection());

        repo.turn(tank.getId(), Direction.fromByte((byte) 2));
        Assert.assertEquals(Direction.Right, tank.getDirection());

        thrown.expect(TankDoesNotExistException.class);
        thrown.expectMessage("Tank '1000' does not exist");
        repo.turn(1000, Direction.Right);
    }

    @Test
    public void turn_VehicleFacingUpTurnLeft_TurnSucceeds() throws IllegalTransitionException, TankDoesNotExistException {
        Tank tank = repo.join("turningVehicle");

        Assert.assertEquals(Direction.Up, tank.getDirection());

        repo.turn(tank.getId(), Direction.fromByte((byte) 6));

        Assert.assertEquals(Direction.Left, tank.getDirection());

        thrown.expect(TankDoesNotExistException.class);
        thrown.expectMessage("Tank '1000' does not exist");
        repo.turn(1000, Direction.Left);
    }

    @Test
    public void turn_VehicleFacingDownTurnLeft_TurnSucceeds() throws IllegalTransitionException, LimitExceededException, TankDoesNotExistException {
        Tank tank = repo.join("turningVehicle");

        Assert.assertEquals(Direction.Up, tank.getDirection());
        tank.setDirection(Direction.Down);
        Assert.assertEquals(Direction.Down, tank.getDirection());

        repo.turn(tank.getId(), Direction.fromByte((byte) 6));
        Assert.assertEquals(Direction.Left, tank.getDirection());

        thrown.expect(TankDoesNotExistException.class);
        thrown.expectMessage("Tank '1000' does not exist");
        repo.turn(1000, Direction.Up);
    }

    @Test
    public void turn_VehicleFacingDownTurnRight_TurnSucceeds() throws IllegalTransitionException, LimitExceededException, TankDoesNotExistException {
        Tank tank = repo.join("turningVehicle");
        Assert.assertEquals(Direction.Up, tank.getDirection());
        tank.setDirection(Direction.Down);
        Assert.assertEquals(Direction.Down, tank.getDirection());

        repo.turn(tank.getId(), Direction.fromByte((byte) 2));
        Assert.assertEquals(Direction.Right, tank.getDirection());

        thrown.expect(TankDoesNotExistException.class);
        thrown.expectMessage("Tank '1000' does not exist");
        repo.turn(1000, Direction.Up);
    }

    @Test
    public void turn_VehicleCanTurnAfterTimePasses_TurnFails() throws TankDoesNotExistException {
        // Mock the current time to simulate time-dependent behavior
        Tank tank = repo.join("turningVehicle");
        tank.setLastMoveTime(System.currentTimeMillis());
        // First turn attempt without enough time passing (simulate last move time)
        Assert.assertEquals(Direction.Up, tank.getDirection());
        tank.setDirection(Direction.Down);
        Assert.assertEquals(Direction.Down, tank.getDirection());
        constraints.canTurn(tank.getId(), mockGame, Direction.fromByte((byte) 2), mockMillis + tank.getLastMoveTime());

        boolean turnAttempt1 = repo.turn(tank.getId(), Direction.fromByte((byte) 2));
        Assert.assertTrue("First turn", turnAttempt1);
        // Simulate time passage - advance the mock time by 500 milliseconds
        Assert.assertEquals(Direction.Right, tank.getDirection());
        // Second turn attempt after enough time has passed
        constraints.canTurn(tank.getId(), mockGame, Direction.fromByte((byte) 0), mockMillis + tank.getLastMoveTime());

        boolean turnAttempt2 = repo.turn(tank.getId(), Direction.fromByte((byte) 0));
        Assert.assertTrue("Tank should be able to turn after enough time has passed", turnAttempt2);

        // Verify that the direction has changed to Left
        Assert.assertEquals(Direction.Up, tank.getDirection());
    }


    @Test
    public void move_VehicleFacingUpMoveForward_MoveSucceeds() throws IllegalTransitionException, TankDoesNotExistException {
        Tank tank = repo.join("movingVehicle");

        int tankPos = tank.getParent().getPosition();

        tank.setDirection(Direction.Up);
        Assert.assertEquals(Direction.Up, tank.getDirection());

        boolean move = repo.move(tank.getId(), Direction.fromByte((byte)0));
        Assert.assertTrue("Backward movement should succeed", move);

        Assert.assertNotEquals(tankPos, tank.getParent().getPosition());
        Assert.assertEquals(Direction.Up, tank.getDirection());
    }

    @Test
    public void move_VehicleFacingDownMoveForward_MoveSucceeds() throws TankDoesNotExistException {
        Tank tank = repo.join("movingVehicle");

        int tankPos = tank.getParent().getPosition();

        tank.setDirection(Direction.Down);
        Assert.assertEquals(Direction.Down, tank.getDirection());

        boolean move = repo.move(tank.getId(), Direction.fromByte((byte)4));
        Assert.assertTrue("Forward movement should succeed", move);

        Assert.assertNotEquals(tankPos, tank.getParent().getPosition());
        Assert.assertEquals(Direction.Down, tank.getDirection());
    }

    @Test
    public void move_VehicleFacingRightMoveForward_MoveSucceeds() throws TankDoesNotExistException {
        Tank tank = repo.join("movingVehicle");

        int tankPos = tank.getParent().getPosition();

        tank.setDirection(Direction.Right);
        Assert.assertEquals(Direction.Right, tank.getDirection());

        boolean move = repo.move(tank.getId(), Direction.fromByte((byte)2));
        Assert.assertTrue("Forward movement should succeed", move);

        Assert.assertNotEquals(tankPos, tank.getParent().getPosition());
        Assert.assertEquals(Direction.Right, tank.getDirection());
    }

    @Test
    public void move_VehicleFacingDownMoveBackwards_MoveSucceeds() throws TankDoesNotExistException {
        Tank tank = repo.join("movingVehicle");

        int tankPos = tank.getParent().getPosition();

        tank.setDirection(Direction.Down);
        Assert.assertEquals(Direction.Down, tank.getDirection());

        boolean move = repo.move(tank.getId(), Direction.fromByte((byte)0));
        Assert.assertTrue("Backward movement should succeed", move);

        Assert.assertNotEquals(tankPos, tank.getParent().getPosition());
        Assert.assertEquals(Direction.Up, tank.getDirection());
    }

    @Test
    public void move_VehicleFacingRightMoveBackward_MoveSucceeds() throws TankDoesNotExistException {
        Tank tank = repo.join("movingVehicle");

        assert tank != null;
        int tankPos = tank.getParent().getPosition();

        tank.setDirection(Direction.Right);
        Assert.assertEquals(Direction.Right, tank.getDirection());

        boolean move = repo.move(tank.getId(), Direction.fromByte((byte) 6));
        Assert.assertTrue("Backward movement should succeed", move);

        Assert.assertNotEquals(tankPos, tank.getParent().getPosition());
        Assert.assertEquals(Direction.Left, tank.getDirection());
    }

    @Test
    public void move_VehicleFacingLeftMoveForward_MoveSucceeds() throws TankDoesNotExistException {
        Tank tank = repo.join("movingVehicle");

        int tankPos = tank.getParent().getPosition();

        tank.setDirection(Direction.Left);
        Assert.assertEquals(Direction.Left, tank.getDirection());

        boolean move = repo.move(tank.getId(), Direction.fromByte((byte) 6));
        Assert.assertTrue("Forward movement should succeed", move);

        Assert.assertNotEquals(tankPos, tank.getParent().getPosition());
        Assert.assertEquals(Direction.Left, tank.getDirection());
    }

    @Test
    public void move_VehicleFacingLeftMoveBackward_MoveSucceeds() throws TankDoesNotExistException {
        Tank tank = repo.join("movingVehicle");

        int tankPos = tank.getParent().getPosition();

        tank.setDirection(Direction.Left);
        Assert.assertEquals(Direction.Left, tank.getDirection());

        boolean move = repo.move(tank.getId(), Direction.fromByte((byte) 2));
        Assert.assertTrue("Backward movement should succeed", move);

        Assert.assertNotEquals(tankPos, tank.getParent().getPosition());
        Assert.assertEquals(Direction.Right, tank.getDirection());
    }

    @Test
    public void move_VehicleCanMoveAfterTimePasses_MoveSuccess() throws TankDoesNotExistException {
        // Mock the current time to simulate time-dependent behavior
        Tank tank = repo.join("turningVehicle");
        tank.setLastMoveTime(System.currentTimeMillis());
        // First turn attempt without enough time passing (simulate last move time)
        Assert.assertEquals(Direction.Up, tank.getDirection());
        tank.setDirection(Direction.Up);
        Assert.assertEquals(Direction.Up, tank.getDirection());
        constraints.canMove(tank.getId(), mockGame, Direction.fromByte((byte) 0), mockMillis + tank.getLastMoveTime());

        boolean moveAttempt1 = repo.move(tank.getId(), Direction.fromByte((byte) 0));
        Assert.assertTrue("First move", moveAttempt1);
        // Simulate time passage - advance the mock time by 500 milliseconds
        Assert.assertEquals(Direction.Up, tank.getDirection());
        // Second turn attempt after enough time has passed
        constraints.canMove(tank.getId(), mockGame, Direction.fromByte((byte) 4), mockMillis + tank.getLastMoveTime());

        boolean moveAttempt2 = repo.move(tank.getId(), Direction.fromByte((byte) 4));
        Assert.assertTrue("Second move after 0.5 interval, should be able to move after enough time has passed", moveAttempt2);

        // Verify that the direction has changed to Left
        Assert.assertEquals(Direction.Down, tank.getDirection());
    }

    @Test
    public void testFire() throws Exception {

    }

    @Test
    public void fire_VehicleCallsFire_SendsBullet() throws LimitExceededException, TankDoesNotExistException {
        Tank tank = repo.join("movingVehicle");

        if (repo.fire(tank.getId(), 1)) {
            Assert.assertTrue(true);
        }

        Assert.assertEquals(Direction.Up, tank.getDirection());
    }

    @Test
    public void fire_VehicleCanFireAfterTimePasses_FireSuccess() throws TankDoesNotExistException {
        // Mock the current time to simulate time-dependent behavior
        Tank tank = repo.join("turningVehicle");
        tank.setLastFireTime(System.currentTimeMillis());
        // First turn attempt without enough time passing (simulate last move time)
        Assert.assertEquals(Direction.Up, tank.getDirection());
        tank.setDirection(Direction.Up);
        Assert.assertEquals(Direction.Up, tank.getDirection());
        constraints.canFire(tank, mockMillis + tank.getLastFireTime(), 1, bulletDelay);

        boolean fireAttempt1 = repo.fire(tank.getId(), 1);
        Assert.assertTrue("Tank should be able to turn", fireAttempt1);
        // Simulate time passage - advance the mock time by 500 milliseconds
        Assert.assertEquals(Direction.Up, tank.getDirection());
        // Second turn attempt after enough time has passed
        constraints.canFire(tank, mockMillis + tank.getLastFireTime(), 1, bulletDelay);

        boolean fireAttempt2 = repo.fire(tank.getId(), 1);
        Assert.assertFalse("Tank should be able to turn after enough time has passed", fireAttempt2);

        // Verify that the direction has changed to Left
//        Assert.assertEquals(Direction.Down, tank.getDirection());
    }

    @Test
    public void testLeave() throws Exception {

    }
}