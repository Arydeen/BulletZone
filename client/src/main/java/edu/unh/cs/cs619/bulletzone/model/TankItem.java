package edu.unh.cs.cs619.bulletzone.model;

import edu.unh.cs.cs619.bulletzone.R;

/**
 * Class to interpret the raw value from the server for bullets and be created from the factory.
 */
public class TankItem extends edu.unh.cs.cs619.bulletzone.model.BoardCell {
    protected  String cellType;
    int tankID;

    public final int BulletType = 2000000;
    // Add Bullet damage and find it from val
    public TankItem(int val, int r, int c) {
        super (val, r, c);

        int typeVal = BulletType;
        int scaleFactor = 1000;

        resourceID = R.drawable.shovel_icon;
        cellType = "Bullet";

        tankID = (val - BulletType) / scaleFactor;
    }

    public String getCellType() {return cellType;}

    public String getCellInfo() {
        return super.getCellInfo() + "\nTank ID: " + tankID;
    }

    public int getTankID() {return tankID;}
}
