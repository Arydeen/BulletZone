package edu.unh.cs.cs619.bulletzone.model;

import edu.unh.cs.cs619.bulletzone.R;

public class BoardCell {
    protected int resourceID; /// The resource ID for the image to display
    protected int rawValue; /// The value as represented on the server
    protected int row, col; /// The location of this cell on the grid

    public BoardCell(int val, int r, int c) {
        rawValue = val;
        row = r;
        col = c;
        resourceID = R.drawable.blank;
    }
    public Integer getResourceID() { return resourceID; }
    public int getRotation() { return 0; }
    public int getRawValue() { return rawValue; }

    public String getCellType() {
        return "Empty";
    }


    public String getCellInfo() { return "Location: (" + this.col + ", " + this.row + ")"; }
}
