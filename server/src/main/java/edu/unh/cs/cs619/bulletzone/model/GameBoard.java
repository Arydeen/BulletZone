//package edu.unh.cs.cs619.bulletzone.model;
//
//import edu.unh.cs.cs619.bulletzone.datalayer.terrain.TerrainType;
//
//public class GameBoard {
//    private int width;
//    private int height;
//    private boolean wrapHorizontal;
//    private boolean wrapVertical;
//    private FieldHolder[][] grid;
//
//    public GameBoard(int width, int height, boolean wrapHorizontal, boolean wrapVertical) {
//        this.width = width;
//        this.height = height;
//        this.wrapHorizontal = wrapHorizontal;
//        this.wrapVertical = wrapVertical;
//        this.grid = new FieldHolder[width][height];
//    }
//
//    public void add(FieldHolder cell) {
//        int x = cell.getPosition() % width;
//        int y = cell.getPosition() / width;
//        grid[x][y] = cell;
//    }
//
//    // Gets the FieldHolder at (x, y)
//    public FieldHolder get(int x, int y) {
//        if (wrapHorizontal) x = (x + width) % width;
//        if (wrapVertical) y = (y + height) % height;
//        return grid[x][y];
//    }
//
//    // Sets a FieldEntity at (x, y)
//    public void setEntity(int x, int y, FieldEntity entity) {
//        grid[x][y].setFieldEntity(entity);
//    }
//
//    public void setTerrain(int x, int y, TerrainType terrain) {
//        grid[x][y] = new FieldHolder(x + y * width);
//        // Further configuration based on the terrain if necessary
//    }
//}
//
