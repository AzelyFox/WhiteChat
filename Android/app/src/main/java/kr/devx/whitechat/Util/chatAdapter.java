package kr.devx.whitechat.Util;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

import kr.devx.whitechat.Data.Message;
import kr.devx.whitechat.Data.Room;
import kr.devx.whitechat.Data.User;
import kr.devx.whitechat.R;
import kr.devx.whitechat.WhiteApplication;

public class chatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private WhiteApplication whiteApplication;
    private Context context;
    private Room room;
    private ArrayList<Message> messages;
    private OnChatClickListener listener;

    public interface OnChatClickListener {
        void onChatClick(int message_index);
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        LinearLayout viewParent;
        LinearLayout viewAParent, viewBParent;
        ImageView thumbnailAView, thumbnailBView;
        TextView nicknameAView, nicknameBView, messageAView, messageBView, timeAView, timeBView;

        ChatViewHolder(View view){
            super(view);
            viewParent = view.findViewById(R.id.item_chat_parent);
            viewAParent = view.findViewById(R.id.item_chat_typeA_parent);
            viewBParent = view.findViewById(R.id.item_chat_typeB_parent);
            thumbnailAView = view.findViewById(R.id.item_chat_typeA_thumbnail);
            thumbnailBView = view.findViewById(R.id.item_chat_typeB_thumbnail);
            nicknameAView = view.findViewById(R.id.item_chat_typeA_nickname);
            nicknameBView = view.findViewById(R.id.item_chat_typeB_nickname);
            timeAView = view.findViewById(R.id.item_chat_typeA_datetime);
            timeBView = view.findViewById(R.id.item_chat_typeB_datetime);
            messageAView = view.findViewById(R.id.item_chat_typeA_message);
            messageBView = view.findViewById(R.id.item_chat_typeB_message);
        }
    }

    public chatAdapter(Context context, Room targetRoom, ArrayList<Message> messages){
        this.whiteApplication = (WhiteApplication) context.getApplicationContext();
        this.context = context;
        this.room = targetRoom;
        this.messages = messages;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, parent, false);

        return new ChatViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        ChatViewHolder myViewHolder = (ChatViewHolder) holder;

        if (position >= getItemCount()) return;

        Message message = messages.get(position);
        User owner = null;
        for (User user : room.participants) {
            if (user.user_index == message.message_owner) {
                owner = user;
                break;
            }
        }

        Log.d("WCHAT", "CHAT ADAPTER > ITEM : #" + position + " > INFO : " + "index#" + message.message_index + " owner#" + message.message_owner + " content#" + message.message_content);

        if (whiteApplication.User.user_index == message.message_owner) {
            myViewHolder.viewBParent.setVisibility(View.VISIBLE);
            myViewHolder.viewAParent.setVisibility(View.GONE);
            Glide.with(context).load(whiteApplication.User.user_thumbnail).apply(new RequestOptions().circleCrop()).into(myViewHolder.thumbnailBView);
            myViewHolder.nicknameBView.setText(whiteApplication.User.user_nickname);
            myViewHolder.timeBView.setText(message.message_created.replace("T", " ").replace("Z", ""));
            myViewHolder.messageBView.setText(message.message_content);
        } else {
            myViewHolder.viewAParent.setVisibility(View.VISIBLE);
            myViewHolder.viewBParent.setVisibility(View.GONE);
            if (owner != null) {
                Glide.with(context).load(owner.user_thumbnail).apply(new RequestOptions().circleCrop()).into(myViewHolder.thumbnailAView);
                myViewHolder.nicknameAView.setText(owner.user_nickname);
            } else {
                myViewHolder.nicknameAView.setText(String.valueOf(message.message_owner));
            }
            myViewHolder.timeAView.setText(message.message_created.replace("T", " ").replace("Z", ""));
            myViewHolder.messageAView.setText(message.message_content);
        }

    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void setOnChatClickListener(OnChatClickListener listener) {
        this.listener = listener;
    }

}