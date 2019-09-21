package kr.devx.whitechat.Util;

import android.content.Context;
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

import kr.devx.whitechat.Data.User;
import kr.devx.whitechat.R;
import kr.devx.whitechat.WhiteApplication;

public class socialAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private WhiteApplication whiteApplication;
    private Context context;
    private ArrayList<User> social;
    private OnSocialClickListener listener;

    public interface OnSocialClickListener {
        void onSocialClick(int position);
    }

    public static class SocialViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout viewParent;
        ImageView thumbnailView;
        TextView nickView, subView, rankView;

        SocialViewHolder(View view){
            super(view);
            viewParent = view.findViewById(R.id.item_social_parent);
            thumbnailView = view.findViewById(R.id.item_social_thumbnail);
            nickView = view.findViewById(R.id.item_social_nickname);
            subView = view.findViewById(R.id.item_social_sub);
            rankView = view.findViewById(R.id.item_social_rank);
        }
    }

    public socialAdapter(Context context, ArrayList<User> social){
        this.whiteApplication = (WhiteApplication) context.getApplicationContext();
        this.context = context;
        this.social = social;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_social, parent, false);

        return new SocialViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        SocialViewHolder myViewHolder = (SocialViewHolder) holder;

        User user = social.get(position);
        myViewHolder.nickView.setText(user.user_nickname);
        myViewHolder.subView.setText(user.user_id);
        myViewHolder.rankView.setText(String.valueOf(user.user_rank));
        Glide.with(context).load(user.user_thumbnail).apply(new RequestOptions().circleCrop()).into(myViewHolder.thumbnailView);
        Log.d("WCHAT", "SOCIAL ADAPTER IMAGE > " + "https://avatars1.githubusercontent.com/u/" + 25262302 + social.get(position).user_index);
        if (listener != null) {
            myViewHolder.viewParent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onSocialClick(position);
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return social.size();
    }

    public void setOnSocialClickListener(OnSocialClickListener listener) {
        this.listener = listener;
    }

}