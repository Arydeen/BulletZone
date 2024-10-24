package edu.unh.cs.cs619.bulletzone.ui;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.SystemService;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import edu.unh.cs.cs619.bulletzone.R;
import edu.unh.cs.cs619.bulletzone.events.UpdateBoardEvent;
import edu.unh.cs.cs619.bulletzone.model.BoardCell;
import edu.unh.cs.cs619.bulletzone.model.SimulationBoard;
import edu.unh.cs.cs619.bulletzone.model.TankItem;
import edu.unh.cs.cs619.bulletzone.model.TurnableGoblin;

@EBean
public class GridAdapter extends BaseAdapter {

    private final Object monitor = new Object();
    @SystemService
    protected LayoutInflater inflater;
    private int[][] mEntities = new int[16][16];
    private final SimulationBoard simBoard = new SimulationBoard(16,16);
    public boolean isUpdated = false;
    private long tankId = -1;

    /**
     * Updates the entities array of new input after events have changed it from the server
     * @param entities Game board array
     */
    public void updateList(int[][] entities) {
        synchronized (monitor) {
            this.mEntities = entities;
            this.notifyDataSetChanged();
            simBoard.setUsingBoard(mEntities); // Not sure if this is needed here
            this.isUpdated = true;
        }
    }

    @AfterInject
    protected void afterInject() {
        EventBus.getDefault().register(this);
    }

    /**
     * Subscribes to changes from events form the UpdateBoardEvent and updates the view
     * @param event New event made to the board
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleUpdate(UpdateBoardEvent event) {
        this.notifyDataSetChanged();
        simBoard.setUsingBoard(mEntities); // Updates simulation board when events are posted
        this.isUpdated = true;
    }

    public int[][] getBoard() { return mEntities; }

    @Override
    public int getCount() {
        return 16 * 16;
    }

    @Override
    public Object getItem(int position) {
        return mEntities[(int) position / 16][position % 16];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setTankId(long tankId) {this.tankId = tankId;}


    /**
     * Updates the desired cell from events in the gridView, using SimulationBoard's Board Cells
     * @param position The position in the SimulationBoard to be updated
     * @param convertView The view to be updated, it is gridView
     * @param parent The parent activity of the view, Client Activity
     * @return Returns the updated view
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ImageView imageView;

        if (convertView == null) {
            imageView = new ImageView(parent.getContext());
            imageView.setLayoutParams(new GridView.LayoutParams(50, 50));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(1, 1, 1, 1);
        } else {
            imageView = (ImageView) convertView;
        }

        if (this.isUpdated) {
            BoardCell currCell = simBoard.getCell(position);
            // Check if the current cell is a Tank
            if (currCell.getCellType().equals("Tank")) {
                // If it is a tank, set get the TankID from the raw value;
                int tankIdTest = (currCell.getRawValue() / 10000) - 1000;
//                Log.d("tankID", "TankId: " + tankIdTest);
//                Log.d("userTankID", "UserTankID: " + this.tankId);
                // If the tankID is equal to the user's tank ID, set the resource different
                if (tankIdTest == this.tankId) {
                    imageView.setImageResource(R.drawable.small_goblin_red);
                } else { // Else set it to what it should be
                    imageView.setImageResource(currCell.getResourceID());
                }
            } else {
                imageView.setImageResource(currCell.getResourceID());
            }
            Log.d("fromAdapter", "Rotate Goblin");
            imageView.setRotation(currCell.getRotation());
        } else {
            imageView.setImageResource(R.drawable.blank);
        }

        return imageView;
    }
}


