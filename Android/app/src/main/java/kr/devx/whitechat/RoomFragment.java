package kr.devx.whitechat;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import io.socket.emitter.Emitter;
import kr.devx.whitechat.Data.Message;
import kr.devx.whitechat.Data.Room;
import kr.devx.whitechat.Util.roomAdapter;

public class RoomFragment extends Fragment {

    private WhiteApplication whiteApplication;
    private int currentPage;
    private SharedPreferences appPreferences;

    private RecyclerView mainRoomListView;
    private roomAdapter mainRoomAdapter;

    private LinearLayout roomCreateView, roomJoinView;

    public static RoomFragment newInstance(int currentPage) {
        RoomFragment fragment = new RoomFragment();
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
        View rootView = inflater.inflate(R.layout.fragment_room, container, false);
        initializeId(rootView);

        ((MainActivity)getActivity()).onRoomFragmentReady(this);
        ((MainActivity)getActivity()).mSocket.on("create", onCreate);
        ((MainActivity)getActivity()).mSocket.on("join", onJoin);

        return rootView;
    }

    private void initializeId(View rootView) {
        mainRoomListView = rootView.findViewById(R.id.room_listView);
        roomCreateView = rootView.findViewById(R.id.room_create);
        roomJoinView = rootView.findViewById(R.id.room_join);
    }

    public void onLifeStart() {
        mainRoomAdapter = new roomAdapter(getActivity(), whiteApplication.User.rooms);
        mainRoomListView.setAdapter(mainRoomAdapter);
        mainRoomAdapter.setOnRoomClickListener(new roomAdapter.OnRoomClickListener() {
            @Override
            public void onRoomClick(int room_index) {
                ((MainActivity)getActivity()).onRoomClicked(room_index);
            }
        });
        if (whiteApplication.User.rooms != null && whiteApplication.User.rooms.size() > 0) {
            ((MainActivity)getActivity()).mSocket.on("retrieve_basics", onRetrieveBasics);
            for (Room room: whiteApplication.User.rooms) {
                if (((MainActivity)getActivity()).mSocket == null || !((MainActivity)getActivity()).mSocket.connected()) {
                    return;
                }
                try {
                    JSONObject data = new JSONObject();
                    data.put("room", room.room_index);
                    data.put("index", -1);
                    ((MainActivity) getActivity()).mSocket.emit("retrieve_basics", data);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        roomCreateView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCreateRoomDialog();
            }
        });
        roomJoinView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showJoinRoomDialog();
            }
        });
    }

    private void showCreateRoomDialog() {
        final EditText titleEditor = new EditText(getContext());
        titleEditor.setHint(getString(R.string.room_dialog_hint_title));
        final EditText passwordEditor = new EditText(getContext());
        passwordEditor.setHint(getString(R.string.room_dialog_hint_password));
        passwordEditor.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        SweetAlertDialog roomCreateDialog = new SweetAlertDialog(getContext(), SweetAlertDialog.NORMAL_TYPE)
                .setTitleText(getString(R.string.room_create))
                .setContentText(getString(R.string.room_create_content))
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        if (titleEditor.getText().toString().trim().length() < 1) {
                            Toast.makeText(getContext(), getString(R.string.room_create_content), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        try {
                            JSONObject data = new JSONObject();
                            data.put("title", titleEditor.getText().toString().trim());
                            data.put("password", passwordEditor.getText().toString().trim());
                            data.put("key",whiteApplication.User.user_key);
                            ((MainActivity)getActivity()).mSocket.emit("create", data);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        sweetAlertDialog.dismiss();
                    }
                });
        roomCreateDialog.show();
        LinearLayout linearLayout = roomCreateDialog.findViewById(R.id.loading);
        int index = linearLayout.indexOfChild(linearLayout.findViewById(R.id.content_text));
        linearLayout.addView(titleEditor, index + 1);
        linearLayout.addView(passwordEditor, index + 2);
    }

    private void showJoinRoomDialog() {
        final EditText indexEditor = new EditText(getContext());
        indexEditor.setHint(getString(R.string.room_dialog_hint_index));
        indexEditor.setInputType(InputType.TYPE_CLASS_NUMBER);
        final EditText passwordEditor = new EditText(getContext());
        passwordEditor.setHint(getString(R.string.room_dialog_hint_password));
        passwordEditor.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        SweetAlertDialog roomJoinDialog = new SweetAlertDialog(getContext(), SweetAlertDialog.NORMAL_TYPE)
                .setTitleText(getString(R.string.room_join))
                .setContentText(getString(R.string.room_join_content))
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        if (indexEditor.getText().toString().trim().length() < 1) {
                            Toast.makeText(getContext(), getString(R.string.room_join_content), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        try {
                            JSONObject data = new JSONObject();
                            data.put("room", indexEditor.getText().toString().trim());
                            data.put("password", passwordEditor.getText().toString().trim());
                            data.put("key",whiteApplication.User.user_key);
                            ((MainActivity)getActivity()).mSocket.emit("join", data);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        sweetAlertDialog.dismiss();
                    }
                });
        roomJoinDialog.show();
        LinearLayout linearLayout = roomJoinDialog.findViewById(R.id.loading);
        int index = linearLayout.indexOfChild(linearLayout.findViewById(R.id.content_text));
        linearLayout.addView(indexEditor, index + 1);
        linearLayout.addView(passwordEditor, index + 2);
    }

    private Emitter.Listener onRetrieveBasics = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d("WCHAT", "SOCKET > onRetrieveBasics");
            try {
                JSONObject receivedData = (JSONObject) args[0];
                if (receivedData.length() == 0) return;
                if (receivedData.getInt("result") != 0) return;
                int targetRoomIndex = receivedData.getInt("room");
                String participantCountStr = receivedData.getString("participant_count");
                int participantCount = -1;
                try {
                    participantCount = Integer.valueOf(participantCountStr.substring(participantCountStr.lastIndexOf(")") + 3, participantCountStr.lastIndexOf("}")));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.d("WCHAT", "SOCKET > onRetrieveBasics : #" + targetRoomIndex + " has " + participantCount + " participants");
                Room targetRoom = null;
                for (Room room: whiteApplication.User.rooms) {
                    if (room.room_index == targetRoomIndex) {
                        targetRoom = room;
                        break;
                    }
                }
                if (targetRoom == null) return;
                try {
                    JSONArray messageArray = receivedData.getJSONArray("last_message");
                    JSONObject messageObject = messageArray.getJSONObject(0);
                    int message_index = messageObject.getInt("message_index");
                    int message_owner = messageObject.getInt("message_owner");
                    String message_created = messageObject.getString("message_created");
                    message_created = message_created.substring(0, message_created.lastIndexOf(".")).replace("T"," ").replace("Z","");
                    String message_content = messageObject.getString("message_content");
                    Message newMessage = new Message(message_index, message_owner, message_content, message_created);
                    targetRoom.messages.add(newMessage);
                    Log.d("WCHAT", "SOCKET > onRetrieveBasics : #" + targetRoomIndex + " last message is " + message_content);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                targetRoom.room_count = participantCount;
                final int roomIndex = whiteApplication.User.rooms.indexOf(targetRoom);
                mainRoomListView.post(new Runnable() {
                    @Override
                    public void run() {
                        mainRoomAdapter.notifyItemChanged(roomIndex);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener onCreate = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d("WCHAT", "SOCKET > onCreate");
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
                ((MainActivity)getActivity()).makeAlertDialog(SweetAlertDialog.SUCCESS_TYPE, getString(R.string.room_create_success), true, null);
                ((MainActivity)getActivity()).refreshDatas();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener onJoin = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d("WCHAT", "SOCKET > onJoin");
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
                ((MainActivity)getActivity()).makeAlertDialog(SweetAlertDialog.SUCCESS_TYPE, getString(R.string.room_join_success), true, null);
                ((MainActivity)getActivity()).refreshDatas();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    public void notifyAdapterDataChanged(final int roomIndex) {
        if (roomIndex == -1) {
            mainRoomListView.post(new Runnable() {
                @Override
                public void run() {
                    mainRoomAdapter.notifyDataSetChanged();
                }
            });
            return;
        }
        mainRoomListView.post(new Runnable() {
            @Override
            public void run() {
                mainRoomAdapter.notifyItemChanged(roomIndex);
            }
        });
    }

}
