package com.asadmshah.drawnearby.screens.main;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.asadmshah.drawnearby.R;

class RoomViewHolder extends RecyclerView.ViewHolder {

    final TextView viewRoomName;

    public RoomViewHolder(View itemView) {
        super(itemView);
        viewRoomName = (TextView) itemView.findViewById(R.id.room_name);
    }
}
