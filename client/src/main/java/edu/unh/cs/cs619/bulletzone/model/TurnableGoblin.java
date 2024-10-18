package edu.unh.cs.cs619.bulletzone.model;

import edu.unh.cs.cs619.bulletzone.R;

public class TurnableGoblin extends TankItem{
    public final int tankType = 1000000;
    private int orientation;

    public static int UP = 0;
    public static int RIGHT = 2;
    public static int DOWN = 4;
    public static int LEFT = 6;

    public TurnableGoblin(int val, int r, int c) {
        super(val, r, c);
        int typeVal = (val / tankType) * tankType;
        int scaleFactor = 1000;

        resourceID = R.drawable.small_goblin;
        orientation = val % 10;
        cellType = "Tank";
        tankID = (val - typeVal) / scaleFactor;
    }

    public int getOrientation() { return orientation; }

    @Override
    public int getRotation() { return 45 * (orientation - 2); }

}
