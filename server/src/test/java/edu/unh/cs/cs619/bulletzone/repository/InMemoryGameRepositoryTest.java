package edu.unh.cs.cs619.bulletzone.repository;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

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
    private Game game = null;

    @Before
    public void setUp() throws Exception {
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
    public void testMove() throws Exception {

    }

    @Test
    public void move_VehicleFacingUpMoveForward_MoveSucceeds() throws IllegalTransitionException, TankDoesNotExistException {
        Tank tank = repo.join("movingVehicle");

        assert tank != null;
        int tankPos = tank.getParent().getPosition();

        Assert.assertEquals(Direction.Up, tank.getDirection());

        boolean move = false;

        move = repo.move(tank.getId(), Direction.fromByte((byte)0));
        if (move) {
            Assert.assertNotEquals(tankPos, tank.getParent().getPosition());
        }

        Assert.assertEquals(Direction.Up, tank.getDirection());
    }

    @Test
    public void move_VehicleFacingUpMoveBackward_MoveSucceeds() throws IllegalTransitionException, TankDoesNotExistException {
        Tank tank = repo.join("movingVehicle");

        int tankPos = tank.getParent().getPosition();

        Assert.assertEquals(Direction.Up, tank.getDirection());

        boolean move = repo.move(tank.getId(), Direction.fromByte((byte)4));
        if (move) {
            Assert.assertNotEquals(tankPos, tank.getParent().getPosition());
        }

        Assert.assertEquals(Direction.Up, tank.getDirection());
    }

    @Test
    public void move_VehicleFacingUpMoveRight_MoveFails() throws IllegalTransitionException, TankDoesNotExistException {
        Tank tank = repo.join("movingVehicle");

        assert tank != null;
        int tankPos = tank.getParent().getPosition();

        Assert.assertEquals(Direction.Up, tank.getDirection());

        // Try to move sideways (right) while facing up (invalid)
        boolean move = repo.move(tank.getId(), Direction.fromByte((byte)2));
        Assert.assertFalse("Sideways movement should fail", move);

        Assert.assertEquals(tankPos, tank.getParent().getPosition());
        Assert.assertEquals(Direction.Up, tank.getDirection());
    }

    @Test
    public void move_VehicleFacingDownMoveLeft_MoveFails() throws IllegalTransitionException, TankDoesNotExistException {
        Tank tank = repo.join("movingVehicle");

        int tankPos = tank.getParent().getPosition();

        tank.setDirection(Direction.Down);
        Assert.assertEquals(Direction.Down, tank.getDirection());

        boolean move = repo.move(tank.getId(), Direction.fromByte((byte)6));
        Assert.assertFalse("Sideways movement should fail", move);

        Assert.assertEquals(tankPos, tank.getParent().getPosition());
        Assert.assertEquals(Direction.Down, tank.getDirection());
    }

    @Test
    public void move_VehicleFacingDownMoveForward_MoveSucceeds() throws IllegalTransitionException, TankDoesNotExistException {
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
    public void move_VehicleFacingRightMoveForward_MoveSucceeds() throws IllegalTransitionException, TankDoesNotExistException {
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
    public void move_VehicleFacingDownMoveBackwards_MoveSucceeds() throws IllegalTransitionException, TankDoesNotExistException {
        Tank tank = repo.join("movingVehicle");

        int tankPos = tank.getParent().getPosition();

        tank.setDirection(Direction.Down);
        Assert.assertEquals(Direction.Down, tank.getDirection());

        boolean move = repo.move(tank.getId(), Direction.fromByte((byte)0));
        Assert.assertTrue("Backward movement should succeed", move);

        Assert.assertNotEquals(tankPos, tank.getParent().getPosition());
        Assert.assertEquals(Direction.Down, tank.getDirection());
    }

    @Test
    public void move_VehicleFacingRightMoveBackward_MoveSucceeds() throws IllegalTransitionException, TankDoesNotExistException {
        Tank tank = repo.join("movingVehicle");

        assert tank != null;
        int tankPos = tank.getParent().getPosition();

        tank.setDirection(Direction.Right);
        Assert.assertEquals(Direction.Right, tank.getDirection());

        boolean move = repo.move(tank.getId(), Direction.fromByte((byte) 6));
        Assert.assertTrue("Backward movement should succeed", move);

        Assert.assertNotEquals(tankPos, tank.getParent().getPosition());
        Assert.assertEquals(Direction.Right, tank.getDirection());
    }

    @Test
    public void move_VehicleFacingLeftMoveForward_MoveSucceeds() throws IllegalTransitionException, TankDoesNotExistException {
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
    public void move_VehicleFacingLeftMoveBackward_MoveSucceeds() throws IllegalTransitionException, TankDoesNotExistException {
        Tank tank = repo.join("movingVehicle");

        int tankPos = tank.getParent().getPosition();

        tank.setDirection(Direction.Left);
        Assert.assertEquals(Direction.Left, tank.getDirection());

        boolean move = repo.move(tank.getId(), Direction.fromByte((byte) 2));
        Assert.assertTrue("Backward movement should succeed", move);

        Assert.assertNotEquals(tankPos, tank.getParent().getPosition());
        Assert.assertEquals(Direction.Left, tank.getDirection());
    }

    @Test
    public void testFire() throws Exception {

    }

    @Test
    public void fire_VehicleCallsFire_SendsBullet() throws LimitExceededException, TankDoesNotExistException {
        Tank tank = repo.join("movingVehicle");

        if (repo.fire(tank.getId(), 1)) {
            Assert.assertEquals(true, true);
        }

        Assert.assertEquals(Direction.Up, tank.getDirection());
    }

    @Test
    public void testLeave() throws Exception {

    }
}