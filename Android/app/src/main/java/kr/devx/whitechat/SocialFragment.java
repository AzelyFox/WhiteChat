package kr.devx.whitechat;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;

import org.json.JSONArray;
import org.json.JSONObject;

import io.socket.emitter.Emitter;
import kr.devx.whitechat.Util.socialAdapter;

public class SocialFragment extends Fragment {

    private WhiteApplication whiteApplication;
    private int currentPage;
    private SharedPreferences appPreferences;

    private RecyclerView mainSocialListView;
    private socialAdapter mainSocialAdapter;

    private LinearLayout addFriendView;

    public static SocialFragment newInstance(int currentPage) {
        SocialFragment fragment = new SocialFragment();
        Bundle args = new Bundle();
        args.putInt("currentPage", currentPage);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentPage = getArguments().getInt("currentPage", 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        whiteApplication = (WhiteApplication) getContext().getApplicationContext();
        View rootView = inflater.inflate(R.layout.fragment_social, container, false);
        initializeId(rootView);

        ((MainActivity)getActivity()).onSocialFragmentReady(this);
        ((MainActivity)getActivity()).mSocket.on("relation", onRelation);

        return rootView;
    }

    private void initializeId(View rootView) {
        mainSocialListView = rootView.findViewById(R.id.social_listView);
        addFriendView = rootView.findViewById(R.id.social_add);
        addFriendView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddFriendDialog();
            }
        });
    }

    public void onLifeStart() {
        mainSocialAdapter = new socialAdapter(getActivity(), whiteApplication.User.friends);
        mainSocialListView.setAdapter(mainSocialAdapter);
        mainSocialAdapter.setOnSocialClickListener(new socialAdapter.OnSocialClickListener() {
            @Override
            public void onSocialClick(int position) {

            }
        });
    }

    private void showAddFriendDialog() {
        final EditText idEditor = new EditText(getContext());
        SweetAlertDialog idInputDialog = new SweetAlertDialog(getContext(), SweetAlertDialog.NORMAL_TYPE)
                .setTitleText(getString(R.string.social_add_friend))
                .setContentText(getString(R.string.social_add_friend_content))
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        if (idEditor.getText().toString().trim().length() < 1) {
                            Toast.makeText(getContext(), getString(R.string.social_add_friend_content), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        try {
                            JSONObject data = new JSONObject();
                            data.put("targetID", idEditor.getText().toString().trim());
                            data.put("relation", 1);
                            data.put("key",whiteApplication.User.user_key);
                            ((MainActivity)getActivity()).mSocket.emit("relation", data);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        sweetAlertDialog.dismiss();
                    }
                });
        idInputDialog.show();
        LinearLayout linearLayout = idInputDialog.findViewById(R.id.loading);
        int index = linearLayout.indexOfChild(linearLayout.findViewById(R.id.content_text));
        linearLayout.addView(idEditor, index + 1);
    }

    private Emitter.Listener onRelation = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d("WCHAT", "SOCKET > onRelation");
            try {
                JSONObject receivedData = (JSONObject) args[0];
                int result = receivedData.getInt("result");
                String message = receivedData.getString("message");
                String error = receivedData.getString("error");
                switch (result) {
                    case 1:
                        ((MainActivity)getActivity()).makeAlertDialog(SweetAlertDialog.WARNING_TYPE, message, true, null);
                        return;
                    case 2:
                        ((MainActivity)getActivity()).makeAlertDialog(SweetAlertDialog.WARNING_TYPE, error, true, null);
                        return;
                }
                int relation = receivedData.getInt("relation");
                String targetID = receivedData.getString("targetID");
                switch (relation) {
                    case 0:
                        ((MainActivity)getActivity()).makeAlertDialog(SweetAlertDialog.SUCCESS_TYPE, targetID + " " + getString(R.string.social_relation_result_0), true, null);
                        break;
                    case 1:
                        ((MainActivity)getActivity()).makeAlertDialog(SweetAlertDialog.SUCCESS_TYPE, targetID + " " + getString(R.string.social_relation_result_1), true, null);
                        break;
                }
                ((MainActivity)getActivity()).refreshDatas();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    public void notifyAdapterDataChanged(final int socialIndex) {
        if (socialIndex == -1) {
            mainSocialListView.post(new Runnable() {
                @Override
                public void run() {
                    mainSocialAdapter.notifyDataSetChanged();
                }
            });
            return;
        }
        mainSocialListView.post(new Runnable() {
            @Override
            public void run() {
                mainSocialAdapter.notifyItemChanged(socialIndex);
            }
        });
    }

}
