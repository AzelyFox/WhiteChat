package kr.devx.whitechat.Util;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import kr.devx.whitechat.Data.Room;
import kr.devx.whitechat.R;
import kr.devx.whitechat.WhiteApplication;

public class roomAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private WhiteApplication whiteApplication;
    private Context context;
    private ArrayList<Room> rooms;
    private OnRoomClickListener listener;

    public interface OnRoomClickListener {
        void onRoomClick(int room_index);
    }

    public static class RoomViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout viewParent;
        ImageView thumbnailView;
        TextView titleView, messageView, recentView, participantView;

        RoomViewHolder(View view){
            super(view);
            viewParent = view.findViewById(R.id.item_room_parent);
            thumbnailView = view.findViewById(R.id.item_room_thumbnail);
            titleView = view.findViewById(R.id.item_room_name);
            messageView = view.findViewById(R.id.item_room_message);
            recentView = view.findViewById(R.id.item_room_recent);
            participantView = view.findViewById(R.id.item_room_participants);
        }
    }

    public roomAdapter(Context context, ArrayList<Room> rooms){
        this.whiteApplication = (WhiteApplication) context.getApplicationContext();
        this.context = context;
        this.rooms = rooms;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_room, parent, false);

        return new RoomViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        RoomViewHolder myViewHolder = (RoomViewHolder) holder;

        final Room room = rooms.get(position);

        Log.d("WCHAT", "ROOM ADAPTER > ITEM : #" + position + " > MESSAGE COUNT : " + room.messages.size());
        myViewHolder.thumbnailView.setImageDrawable(context.getDrawable(R.drawable.icon_room));
        myViewHolder.titleView.setText(room.room_name);
        if (room.messages != null && room.messages.size() > 0) {
            myViewHolder.messageView.setText(room.messages.get(room.messages.size() - 1).message_content);
            myViewHolder.recentView.setText(room.messages.get(room.messages.size() - 1).message_created);
        }
        myViewHolder.participantView.setText(room.room_count + " " + context.getString(R.string.room_participants));
        myViewHolder.viewParent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) listener.onRoomClick(room.room_index);
            }
        });
    }

    @Override
    public int getItemCount() {
        return rooms.size();
    }

    public void setOnRoomClickListener(OnRoomClickListener listener) {
        this.listener = listener;
    }

}