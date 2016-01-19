package com.asadmshah.drawnearby.screens.main;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.asadmshah.drawnearby.R;
import com.asadmshah.drawnearby.models.LobbyStatus;

class RoomsAdapter extends RecyclerView.Adapter<RoomViewHolder> {

    private Listener listener;
    private LobbyStatus lobbyStatus;

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void setLobbyStatus(LobbyStatus lobbyStatus) {
        this.lobbyStatus = lobbyStatus;
    }

    @Override
    public RoomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final RoomViewHolder vh = new RoomViewHolder(inflater.inflate(R.layout.viewholder_room_name, parent, false));
        vh.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onRoomNameClicked(lobbyStatus.getRoomName(vh.getAdapterPosition()));
                }
            }
        });
        return vh;
    }

    @Override
    public void onBindViewHolder(RoomViewHolder holder, int position) {
        holder.viewRoomName.setText(lobbyStatus.getRoomName(position));
    }

    @Override
    public int getItemCount() {
        return lobbyStatus != null ? lobbyStatus.size() : 0;
    }

    interface Listener {
        void onRoomNameClicked(String roomName);
    }
}
