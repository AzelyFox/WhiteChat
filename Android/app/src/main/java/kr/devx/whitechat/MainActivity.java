package kr.devx.whitechat;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;

import devlight.io.library.ntb.NavigationTabBar;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import kr.devx.whitechat.Data.Message;
import kr.devx.whitechat.Data.Room;
import kr.devx.whitechat.Data.User;
import kr.devx.whitechat.Util.SmartFragmentStatePagerAdapter;
import kr.devx.whitechat.Util.chatAdapter;
import kr.devx.whitechat.Util.roomAdapter;

public class MainActivity extends FragmentActivity {

    private WhiteApplication whiteApplication;

    public Socket mSocket;

    private ViewPager mainViewPager;
    private NavigationTabBar mainViewNavigation;
    private SmartFragmentStatePagerAdapter mainViewAdapter;

    private LinearLayout splashParent, mainParent, chatParent, loaderParent;

    // SPLASH SCREEN
    private LinearLayout splashLoginParent;
    private TextView splashLoginRegister;
    private EditText splashLoginInputID, splashLoginInputPW;
    private Button splashLoginButton;
    private LinearLayout splashRegisterParent;
    private TextView splashRegisterLogin;
    private EditText splashRegisterInputID, splashRegisterInputPW, splashRegisterInputPW2, splashRegisterInputName;
    private Button splashRegisterButton;

    private ImageView mainHeaderThumbnail;
    private TextView mainHeaderName, mainHeaderRank, mainHeaderCash, mainHeaderCreated;

    private SocialFragment socialFragment;
    private RoomFragment roomFragment;
    private SettingFragment settingFragment;

    private int RECENT_ROOM_NUMBER;
    private TextView chattingHeader, chattingIndex, chattingLeave;
    private RecyclerView chattingListView;
    private chatAdapter chattingAdapter;
    private EditText chattingEditor;
    private Button chattingButton;

    @Override
    public void onCreate(Bundle onSavedInstanceState) {
        super.onCreate(onSavedInstanceState);
        setContentView(R.layout.main);

        whiteApplication = (WhiteApplication) getApplication();

        initializeIDs();
        initializeListeners();
        initializePager();

        try {
            mSocket = IO.socket("http://devx.kr:7000");
            mSocket.on(Socket.EVENT_CONNECT, onConnect);
            mSocket.on(Socket.EVENT_DISCONNECT, onDisconnect);
            mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onTimeout);
            mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectionError);
            mSocket.on(Socket.EVENT_ERROR, onError);
            mSocket.on("login", onLogin);
            mSocket.on("register", onRegister);
            mSocket.connect();
        } catch(URISyntaxException e) {
            e.printStackTrace();
        }

        showSplashScreen();
    }

    private void initializeIDs() {
        splashParent = findViewById(R.id.splash_parent);
        mainParent = findViewById(R.id.main_parent);
        chatParent = findViewById(R.id.chat_parent);
        loaderParent = findViewById(R.id.loader_parent);
        splashLoginParent = findViewById(R.id.splash_login);
        splashLoginRegister = findViewById(R.id.splash_login_register);
        splashLoginInputID = findViewById(R.id.splash_login_input_id);
        splashLoginInputPW = findViewById(R.id.splash_login_input_pw);
        splashLoginButton = findViewById(R.id.splash_login_button);
        splashRegisterParent = findViewById(R.id.splash_register);
        splashRegisterLogin = findViewById(R.id.splash_register_login);
        splashRegisterInputID = findViewById(R.id.splash_register_input_id);
        splashRegisterInputPW = findViewById(R.id.splash_register_input_pw);
        splashRegisterInputPW2 = findViewById(R.id.splash_register_input_pw_confirm);
        splashRegisterInputName = findViewById(R.id.splash_register_input_nickname);
        splashRegisterButton = findViewById(R.id.splash_register_button);
        mainViewPager = findViewById(R.id.main_content_holder);
        mainViewNavigation = findViewById(R.id.main_content_footer);
        mainHeaderThumbnail = findViewById(R.id.main_header_thumbnail);
        mainHeaderName = findViewById(R.id.main_header_nickname);
        mainHeaderRank = findViewById(R.id.main_header_rank);
        mainHeaderCash = findViewById(R.id.main_header_cash);
        mainHeaderCreated = findViewById(R.id.main_header_created);
        chattingHeader = findViewById(R.id.chat_header);
        chattingIndex = findViewById(R.id.chat_header_index);
        chattingLeave = findViewById(R.id.chat_header_leave);
        chattingListView = findViewById(R.id.chat_listView);
        chattingEditor = findViewById(R.id.chat_editor);
        chattingButton = findViewById(R.id.chat_send);
    }

    private void initializeListeners() {
        splashLoginRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                splashLoginParent.setVisibility(View.GONE);
                splashRegisterParent.setVisibility(View.VISIBLE);
            }
        });
        splashRegisterLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                splashRegisterParent.setVisibility(View.GONE);
                splashLoginParent.setVisibility(View.VISIBLE);
            }
        });
        splashLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (splashLoginInputID.getText().toString().length() == 0) {
                    makeAlertDialog(SweetAlertDialog.WARNING_TYPE, getString(R.string.splash_message_login_failed_input_id), true,null);
                    return;
                }
                if (splashLoginInputPW.getText().toString().length() == 0) {
                    makeAlertDialog(SweetAlertDialog.WARNING_TYPE, getString(R.string.splash_message_login_failed_input_password), true,null);
                    return;
                }
                try {
                    JSONObject data = new JSONObject();
                    data.put("requestID", splashLoginInputID.getText().toString());
                    data.put("requestPW", splashLoginInputPW.getText().toString());
                    mSocket.emit("login", data);
                } catch (Exception e) {
                    e.printStackTrace();
                    makeAlertDialog(SweetAlertDialog.WARNING_TYPE, getString(R.string.app_error_json), true,null);
                }
            }
        });
        splashRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (splashRegisterInputID.getText().toString().length() == 0) {
                    makeAlertDialog(SweetAlertDialog.WARNING_TYPE, getString(R.string.splash_message_register_failed_input_id), true,null);
                    return;
                }
                if (splashRegisterInputPW.getText().toString().length() == 0) {
                    makeAlertDialog(SweetAlertDialog.WARNING_TYPE, getString(R.string.splash_message_register_failed_input_password), true,null);
                    return;
                }
                if (splashRegisterInputPW2.getText().toString().length() == 0) {
                    makeAlertDialog(SweetAlertDialog.WARNING_TYPE, getString(R.string.splash_message_register_failed_input_password2), true,null);
                    return;
                }
                if (splashRegisterInputName.getText().toString().length() == 0) {
                    makeAlertDialog(SweetAlertDialog.WARNING_TYPE, getString(R.string.splash_message_register_failed_input_nickname), true,null);
                    return;
                }
                String inputID = splashRegisterInputID.getText().toString().trim();
                if (inputID.length() < 4) {
                    makeAlertDialog(SweetAlertDialog.WARNING_TYPE, getString(R.string.splash_message_register_failed_input_id_short), true,null);
                    return;
                }
                if (inputID.length() > 20) {
                    makeAlertDialog(SweetAlertDialog.WARNING_TYPE, getString(R.string.splash_message_register_failed_input_id_long), true,null);
                    return;
                }
                String inputPW = splashRegisterInputPW.getText().toString().trim();
                if (!inputPW.equals(splashRegisterInputPW2.getText().toString().trim())) {
                    makeAlertDialog(SweetAlertDialog.WARNING_TYPE, getString(R.string.splash_message_register_failed_password_match), true,null);
                    return;
                }
                if (inputPW.length() < 4) {
                    makeAlertDialog(SweetAlertDialog.WARNING_TYPE, getString(R.string.splash_message_register_failed_input_password_short), true,null);
                    return;
                }
                if (inputPW.length() > 20) {
                    makeAlertDialog(SweetAlertDialog.WARNING_TYPE, getString(R.string.splash_message_register_failed_input_password_long), true,null);
                    return;
                }
                String inputName = splashRegisterInputName.getText().toString().trim();
                if (inputName.length() < 4) {
                    makeAlertDialog(SweetAlertDialog.WARNING_TYPE, getString(R.string.splash_message_register_failed_input_nickname_short), true,null);
                    return;
                }
                if (inputName.length() > 20) {
                    makeAlertDialog(SweetAlertDialog.WARNING_TYPE, getString(R.string.splash_message_register_failed_input_nickname_long), true,null);
                    return;
                }
                try {
                    JSONObject data = new JSONObject();
                    data.put("requestID", inputID);
                    data.put("requestPW", inputPW);
                    data.put("requestName", inputName);
                    mSocket.emit("register", data);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        chattingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (chattingEditor.getText().toString().trim().length() <= 0) return;
                try {
                    mSocket.on("send", new Emitter.Listener() {
                        @Override
                        public void call(Object... args) {
                            try {
                                JSONObject receivedData = (JSONObject) args[0];
                                int resultCode = receivedData.getInt("result");
                                if (resultCode != 0) makeAlertDialog(SweetAlertDialog.WARNING_TYPE, getString(R.string.app_unknown_error), true, null);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    JSONObject data = new JSONObject();
                    data.put("room", RECENT_ROOM_NUMBER);
                    data.put("message", chattingEditor.getText().toString().trim());
                    data.put("key", whiteApplication.User.user_key);
                    mSocket.emit("send", data);
                    chattingEditor.getText().clear();
                } catch (Exception e) {
                    e.printStackTrace();
                    makeAlertDialog(SweetAlertDialog.WARNING_TYPE, getString(R.string.app_error_json), true,null);
                }
            }
        });
        chattingLeave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSocket.on("leave", onLeave);
                try {
                    JSONObject data = new JSONObject();
                    data.put("room", RECENT_ROOM_NUMBER);
                    data.put("key", whiteApplication.User.user_key);
                    mSocket.emit("leave", data);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void initializePager() {
        mainViewAdapter = new WhitePagerAdapter(getSupportFragmentManager());
        mainViewPager.setAdapter(mainViewAdapter);
        mainViewPager.setOffscreenPageLimit(2);
        final ArrayList<NavigationTabBar.Model> models = new ArrayList<>();
        models.add(
                new NavigationTabBar.Model.Builder(getResources().getDrawable(R.drawable.icon_social), ContextCompat.getColor(this, R.color.colorAccent))
                        .selectedIcon(getResources().getDrawable(R.drawable.icon_social))
                        .title(getString(R.string.footer_social))
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(getResources().getDrawable(R.drawable.icon_room), ContextCompat.getColor(this, R.color.colorAccent))
                        .selectedIcon(getResources().getDrawable(R.drawable.icon_room))
                        .title(getString(R.string.footer_room))
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(getResources().getDrawable(R.drawable.icon_setting), ContextCompat.getColor(this, R.color.colorAccent))
                        .selectedIcon(getResources().getDrawable(R.drawable.icon_setting))
                        .title(getString(R.string.footer_setting))
                        .build()
        );
        mainViewNavigation.setModels(models);
        mainViewNavigation.setBgColor(ContextCompat.getColor(this, android.R.color.white));
        mainViewNavigation.setViewPager(mainViewPager, 0);
        mainViewNavigation.setBehaviorEnabled(true);
    }

    private void showSplashScreen() {
        mainParent.setVisibility(View.GONE);
        chatParent.setVisibility(View.GONE);
        splashParent.setVisibility(View.VISIBLE);
    }

    private void showMainScreen() {
        splashParent.setVisibility(View.GONE);
        chatParent.setVisibility(View.GONE);
        mainParent.setVisibility(View.VISIBLE);
    }

    private void showChatScreen() {
        splashParent.setVisibility(View.GONE);
        mainParent.setVisibility(View.GONE);
        chatParent.setVisibility(View.VISIBLE);
    }

    private void closeChatScreen() {
        showMainScreen();
        chattingHeader.setText(null);
        chattingListView.setAdapter(null);
        try {
            JSONObject data = new JSONObject();
            data.put("room", RECENT_ROOM_NUMBER);
            data.put("key", whiteApplication.User.user_key);
            mSocket.emit("exit", data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mSocket.off("receive");
    }

    private void showLoaderScreen(String message) {
        loaderParent.setVisibility(View.VISIBLE);
    }

    private void hideLoaderScreen() {
        loaderParent.setVisibility(View.GONE);
    }

    public void onSocialFragmentReady(SocialFragment instance) {
        this.socialFragment = instance;
        socialFragment.onLifeStart();
    }

    public void onRoomFragmentReady(RoomFragment instance) {
        this.roomFragment = instance;
        roomFragment.onLifeStart();
    }

    public void onSettingFragmentReady(SettingFragment instance) {
        this.settingFragment = instance;
        settingFragment.onLifeStart();
    }

    public void onLogout() {
        showSplashScreen();
        mSocket.disconnect();
        mSocket.connect();
    }

    public void onRoomClicked(int clickedRoomIndex) {
        showChatScreen();
        RECENT_ROOM_NUMBER = clickedRoomIndex;
        for (Room room : whiteApplication.User.rooms) {
            if (room.room_index == clickedRoomIndex) {
                startChatLife(room);
                break;
            }
        }
    }

    private void startSplashLife() {

    }

    private void startMainLife() {
        Glide.with(this).load(whiteApplication.User.user_thumbnail).apply(new RequestOptions().circleCrop()).into(mainHeaderThumbnail);
        mainHeaderName.setText(whiteApplication.User.user_nickname);
        mainHeaderRank.setText(String.valueOf(whiteApplication.User.user_rank));
        mainHeaderCash.setText(String.valueOf(whiteApplication.User.user_cash));
        mainHeaderCreated.setText(getString(R.string.main_header_since).concat(" ").concat(whiteApplication.User.user_created));
        if (socialFragment != null) socialFragment.onLifeStart();
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                if (!task.isSuccessful()) {
                    Log.w("WCHAT", "FCM INSTANCE FAILED", task.getException());
                    return;
                }
                String token = task.getResult().getToken();
                mSocket.emit("clientKey", token);
            }
        });
    }

    private void startChatLife(Room targetRoom) {
        mSocket.on("retrieve", onRetrieve);
        try {
            JSONObject data = new JSONObject();
            data.put("room", targetRoom.room_index);
            data.put("index", -1);
            data.put("key", whiteApplication.User.user_key);
            mSocket.emit("retrieve", data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showChatData(final Room targetRoom) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (targetRoom == null) {
                    makeAlertDialog(SweetAlertDialog.WARNING_TYPE, getString(R.string.app_unknown_error), true,null);
                    return;
                }
                chattingHeader.setText(targetRoom.room_name);
                chattingIndex.setText(getString(R.string.chatting_index) + targetRoom.room_index);
                chattingLeave.setText(getString(R.string.chatting_leave));
                chattingAdapter = new chatAdapter(MainActivity.this, targetRoom, targetRoom.messages);
                chattingListView.setAdapter(chattingAdapter);
                chattingListView.post(new Runnable() {
                    @Override
                    public void run() {
                        chattingAdapter.notifyDataSetChanged();
                        chattingListView.scrollToPosition(chattingAdapter.getItemCount() - 1);
                    }
                });
            }
        });
        mSocket.on("receive", onReceive);
        try {
            JSONObject data = new JSONObject();
            data.put("room", RECENT_ROOM_NUMBER);
            data.put("key", whiteApplication.User.user_key);
            mSocket.emit("enter", data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void makeAlertDialog(final int ALERT_TYPE, final String message, final boolean isCancelable, final SweetAlertDialog.OnSweetClickListener buttonListener) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(MainActivity.this, ALERT_TYPE);
                sweetAlertDialog.setTitleText(message);
                sweetAlertDialog.setConfirmClickListener(buttonListener);
                sweetAlertDialog.setCancelable(isCancelable);
                sweetAlertDialog.show();
            }
        });
    }

    public void refreshDatas() {
        mSocket.on("refresh", onRefresh);
        try {
            JSONObject data = new JSONObject();
            data.put("key", whiteApplication.User.user_key);
            mSocket.emit("refresh", data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        if (chatParent.getVisibility() == View.VISIBLE) {
            closeChatScreen();
            return;
        }
        super.onBackPressed();
    }

    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d("WCHAT", "SOCKET > onConnect");
        }
    };

    private Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d("WCHAT", "SOCKET > onDisconnect");
        }
    };

    private Emitter.Listener onConnectionError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d("WCHAT", "SOCKET > onConnectionError > " + args[0]);
        }
    };

    private Emitter.Listener onTimeout = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d("WCHAT", "SOCKET > onTimeout");
        }
    };

    private Emitter.Listener onError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d("WCHAT", "SOCKET > onError");
        }
    };

    private Emitter.Listener onLogin = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d("WCHAT", "SOCKET > onLogin");
            try {
                JSONObject receivedData = (JSONObject) args[0];
                int result = receivedData.getInt("result");
                String error = receivedData.getString("error");
                switch (result) {
                    case 1: // USER NOT EXIST
                        makeAlertDialog(SweetAlertDialog.WARNING_TYPE, getString(R.string.splash_message_login_failed_account), true,null);
                        return;
                    case 2: // USER IS BANNED
                        makeAlertDialog(SweetAlertDialog.WARNING_TYPE, getString(R.string.splash_message_login_failed_block), true,null);
                        return;
                    case 3: // PASSWORD INCORRECT
                        makeAlertDialog(SweetAlertDialog.WARNING_TYPE, getString(R.string.splash_message_login_failed_password_incorrect), true,null);
                        return;
                    case 4: // UNKNOWN ERROR
                        makeAlertDialog(SweetAlertDialog.WARNING_TYPE, getString(R.string.splash_message_login_unknown) + " " + error, true,null);
                        return;
                }
                final int num = receivedData.getInt("num");
                final String id = receivedData.getString("id");
                final String nickname = receivedData.getString("nickname");
                final int rank = receivedData.getInt("rank");
                final String thumbnail = receivedData.getString("thumbnail");
                String created = receivedData.getString("created");
                final int cash = receivedData.getInt("cash");
                final String key = receivedData.getString("key");
                JSONArray roomArray = receivedData.getJSONArray("rooms");
                final ArrayList<Room> rooms = new ArrayList<>();
                for (int i = 0; i < roomArray.length(); i ++) {
                    JSONObject roomObject = roomArray.getJSONObject(i);
                    int room_index = roomObject.getInt("room_index");
                    String room_name = roomObject.getString("room_name");
                    int room_notice = roomObject.getInt("room_notice");
                    Room room = new Room(room_index, room_name, room_notice);
                    rooms.add(room);
                }
                JSONArray socialArray = receivedData.getJSONArray("socials");
                final ArrayList<User> socials = new ArrayList<>();
                for (int i = 0; i < socialArray.length(); i ++) {
                    JSONObject socialObject = socialArray.getJSONObject(i);
                    final int social_num = socialObject.getInt("user_index");
                    final String social_id = socialObject.getString("user_id");
                    final String social_nickname = socialObject.getString("user_nickname");
                    final int social_rank = socialObject.getInt("user_rank");
                    final String social_thumbnail = socialObject.getString("user_thumbnail");
                    User friend = new User(social_num, social_id, social_nickname, social_rank);
                    friend.user_thumbnail = social_thumbnail;
                    socials.add(friend);
                }
                if (created.contains("T")) created = created.substring(0, created.indexOf("T"));
                final String fixCreated = created;
                makeAlertDialog(SweetAlertDialog.SUCCESS_TYPE, nickname, false, new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        User user = new User(num, id, nickname, rank);
                        user.user_cash = cash;
                        user.user_thumbnail = thumbnail;
                        user.user_key = key;
                        user.user_created = fixCreated;
                        user.friends = socials;
                        user.rooms = rooms;
                        whiteApplication.User = user;
                        showMainScreen();
                        startMainLife();
                        sweetAlertDialog.cancel();
                    }
                });
                Log.d("WCHAT", "SOCKET > onLogin > " + nickname + " > " + rooms.toString());
                Log.d("WCHAT", "SOCKET > onLogin > " + nickname + " > " + socials.toString());
            } catch (Exception e) {
                e.printStackTrace();
                makeAlertDialog(SweetAlertDialog.ERROR_TYPE, getString(R.string.app_error_json) + e.getMessage(), true,null);
            }

        }
    };

    private Emitter.Listener onRegister = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d("WCHAT", "SOCKET > onRegister");
            try {
                JSONObject receivedData = (JSONObject) args[0];
                int result = receivedData.getInt("result");
                String error = receivedData.getString("error");
                switch (result) {
                    case 1: // ID ALREADY EXISTS
                        makeAlertDialog(SweetAlertDialog.WARNING_TYPE, getString(R.string.splash_message_register_failed_account), true,null);
                        return;
                    case 2: // UNKNOWN ERROR
                        makeAlertDialog(SweetAlertDialog.WARNING_TYPE, getString(R.string.splash_message_register_unknown) + " " + error, true,null);
                        return;
                }
                makeAlertDialog(SweetAlertDialog.SUCCESS_TYPE, getString(R.string.splash_message_register_success), true,null);
            } catch (Exception e) {
                e.printStackTrace();
                makeAlertDialog(SweetAlertDialog.ERROR_TYPE, getString(R.string.app_error_json) + e.getMessage(), true,null);
            }
        }
    };

    private Emitter.Listener onRetrieve = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d("WCHAT", "SOCKET > onRetrieve");
            try {
                JSONObject receivedData = (JSONObject) args[0];
                int result = receivedData.getInt("result");
                String error = receivedData.getString("error");
                switch (result) {
                    case 2: // UNKNOWN
                        makeAlertDialog(SweetAlertDialog.WARNING_TYPE, error, true,null);
                        return;
                }
                final int room = receivedData.getInt("room");
                JSONArray messageArray = receivedData.getJSONArray("messages");
                final ArrayList<Message> messages = new ArrayList<>();
                for (int i = 0; i < messageArray.length(); i ++) {
                    JSONObject messageObject = messageArray.getJSONObject(i);
                    int message_index = messageObject.getInt("message_index");
                    //int message_room = messageObject.getInt("message_room");
                    int message_owner = messageObject.getInt("message_owner");
                    String message_created = messageObject.getString("message_created");
                    String message_content = messageObject.getString("message_content");
                    message_created = message_created.substring(0, message_created.lastIndexOf(".")).replace("T"," ").replace("Z","");
                    Message message = new Message(message_index, message_owner, message_content, message_created);
                    messages.add(message);
                }
                JSONArray participantArray = receivedData.getJSONArray("participants");
                final ArrayList<User> participants = new ArrayList<>();
                for (int i = 0; i < participantArray.length(); i ++) {
                    JSONObject participantObject = participantArray.getJSONObject(i);
                    final int participant_num = participantObject.getInt("user_index");
                    final String participant_id = participantObject.getString("user_id");
                    final String participant_nickname = participantObject.getString("user_nickname");
                    final int participant_rank = participantObject.getInt("user_rank");
                    final String participant_thumbnail = participantObject.getString("user_thumbnail");
                    User participant = new User(participant_num, participant_id, participant_nickname, participant_rank);
                    participant.user_thumbnail = participant_thumbnail;
                    participants.add(participant);
                }
                Log.d("WCHAT", "SOCKET > onRetrieve > #" + room + " > " + messages.toString());
                Log.d("WCHAT", "SOCKET > onRetrieve > #" + room + " > " + participants.toString());
                Room targetRoom = null;
                for (Room currentRoom : whiteApplication.User.rooms) {
                    if (currentRoom.room_index == room) {
                        targetRoom = currentRoom;
                        break;
                    }
                }
                if (targetRoom != null) {
                    targetRoom.messages.clear();
                    targetRoom.messages.addAll(messages);
                    targetRoom.participants.clear();
                    targetRoom.participants.addAll(participants);
                    Log.d("WCHAT", "SOCKET > onRetrieve > #" + room + " > Data Updated");
                    showChatData(targetRoom);
                } else {
                    showChatData(null);
                }
            } catch (Exception e) {
                e.printStackTrace();
                makeAlertDialog(SweetAlertDialog.ERROR_TYPE, getString(R.string.app_error_json) + e.getMessage(), true,null);
                showChatData(null);
            }
        }
    };

    private Emitter.Listener onReceive = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d("WCHAT", "SOCKET > onReceive");
            try {
                JSONObject receivedData = (JSONObject) args[0];
                int result = receivedData.getInt("result");
                String error = receivedData.getString("error");
                final int room = receivedData.getInt("room");
                JSONObject messageObject = receivedData.getJSONObject("message");
                int message_index = messageObject.getInt("message_index");
                //int message_room = messageObject.getInt("message_room");
                int message_owner = messageObject.getInt("message_owner");
                String message_created = messageObject.getString("message_created");
                String message_content = messageObject.getString("message_content");
                message_created = message_created.substring(message_created.lastIndexOf("()") + 5, message_created.lastIndexOf(".")).replace("T"," ").replace("Z","");
                Message message = new Message(message_index, message_owner, message_content, message_created);

                Log.d("WCHAT", "SOCKET > onReceive > #" + room + " > " + message.toString());
                Room targetRoom = null;
                for (Room currentRoom : whiteApplication.User.rooms) {
                    if (currentRoom.room_index == room) {
                        targetRoom = currentRoom;
                        break;
                    }
                }
                if (targetRoom != null) {
                    for (Message msg : targetRoom.messages) {
                        if (msg.message_index == message_index) return;
                    }
                    targetRoom.messages.add(message);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (chattingAdapter != null && RECENT_ROOM_NUMBER == room && chattingListView != null) {
                                    chattingAdapter.notifyItemInserted(chattingAdapter.getItemCount()-1);
                                    chattingListView.smoothScrollToPosition(chattingAdapter.getItemCount() - 1);
                                    chattingListView.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                forceRippleAnimation(chattingListView.getLayoutManager().findViewByPosition(chattingAdapter.getItemCount() - 1));
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    if (roomFragment != null) roomFragment.notifyAdapterDataChanged(whiteApplication.User.rooms.indexOf(targetRoom));
                    Log.d("WCHAT", "SOCKET > onReceive > #" + room + " > Data Added");
                }
            } catch (Exception e) {
                e.printStackTrace();
                makeAlertDialog(SweetAlertDialog.ERROR_TYPE, getString(R.string.app_error_json) + e.getMessage(), true,null);
            }
        }
    };

    private Emitter.Listener onRefresh = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d("WCHAT", "SOCKET > onRefresh");
            try {
                JSONObject receivedData = (JSONObject) args[0];
                int result = receivedData.getInt("result");
                String error = receivedData.getString("error");
                String message = receivedData.getString("message");
                switch (result) {
                    case 1:
                        makeAlertDialog(SweetAlertDialog.WARNING_TYPE, message, true,null);
                        return;
                    case 2:
                        makeAlertDialog(SweetAlertDialog.WARNING_TYPE, error, true,null);
                        return;
                }
                JSONArray roomArray = receivedData.getJSONArray("rooms");
                final ArrayList<Room> rooms = new ArrayList<>();
                for (int i = 0; i < roomArray.length(); i ++) {
                    JSONObject roomObject = roomArray.getJSONObject(i);
                    int room_index = roomObject.getInt("room_index");
                    String room_name = roomObject.getString("room_name");
                    int room_notice = roomObject.getInt("room_notice");
                    Room room = new Room(room_index, room_name, room_notice);
                    rooms.add(room);
                }
                JSONArray socialArray = receivedData.getJSONArray("socials");
                final ArrayList<User> socials = new ArrayList<>();
                for (int i = 0; i < socialArray.length(); i ++) {
                    JSONObject socialObject = socialArray.getJSONObject(i);
                    final int social_num = socialObject.getInt("user_index");
                    final String social_id = socialObject.getString("user_id");
                    final String social_nickname = socialObject.getString("user_nickname");
                    final int social_rank = socialObject.getInt("user_rank");
                    final String social_thumbnail = socialObject.getString("user_thumbnail");
                    User friend = new User(social_num, social_id, social_nickname, social_rank);
                    friend.user_thumbnail = social_thumbnail;
                    socials.add(friend);
                }
                whiteApplication.User.friends.clear();
                whiteApplication.User.friends.addAll(socials);
                whiteApplication.User.rooms.clear();
                whiteApplication.User.rooms.addAll(rooms);
                if (socialFragment != null) socialFragment.notifyAdapterDataChanged(-1);
                if (roomFragment != null) roomFragment.notifyAdapterDataChanged(-1);
                Log.d("WCHAT", "SOCKET > onRefresh > " + whiteApplication.User.user_nickname + " > " + rooms.toString());
                Log.d("WCHAT", "SOCKET > onRefresh > " + whiteApplication.User.user_nickname + " > " + socials.toString());
            } catch (Exception e) {
                e.printStackTrace();
                makeAlertDialog(SweetAlertDialog.ERROR_TYPE, getString(R.string.app_error_json) + e.getMessage(), true,null);
            }
        }
    };

    private Emitter.Listener onLeave = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d("WCHAT", "SOCKET > onLeave");
            try {
                JSONObject receivedData = (JSONObject) args[0];
                int result = receivedData.getInt("result");
                String message = receivedData.getString("message");
                String error = receivedData.getString("error");
                switch (result) {
                    case 1:
                        makeAlertDialog(SweetAlertDialog.WARNING_TYPE, message, true,null);
                        return;
                    case 2:
                        makeAlertDialog(SweetAlertDialog.WARNING_TYPE, error, true,null);
                        return;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showMainScreen();
                    }
                });
                makeAlertDialog(SweetAlertDialog.SUCCESS_TYPE, getString(R.string.chatting_leave_success) + RECENT_ROOM_NUMBER, true,null);
                refreshDatas();
            } catch (Exception e) {
                e.printStackTrace();
                makeAlertDialog(SweetAlertDialog.ERROR_TYPE, getString(R.string.app_error_json) + e.getMessage(), true,null);
            }
        }
    };

    public static class WhitePagerAdapter extends SmartFragmentStatePagerAdapter {
        private static int NUM_ITEMS = 3;

        public WhitePagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return SocialFragment.newInstance(0);
                case 1:
                    return RoomFragment.newInstance(1);
                case 2:
                    return SettingFragment.newInstance(2);
                default:
                    return null;
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return String.valueOf(position);
        }

    }


    protected void forceRippleAnimation(View view)
    {
        if (view == null) return;

        Drawable background = view.getBackground();

        if(Build.VERSION.SDK_INT >= 21 && background instanceof RippleDrawable)
        {
            final RippleDrawable rippleDrawable = (RippleDrawable) background;

            rippleDrawable.setState(new int[]{android.R.attr.state_pressed, android.R.attr.state_enabled});

            Handler handler = new Handler();

            handler.postDelayed(new Runnable()
            {
                @Override public void run()
                {
                    rippleDrawable.setState(new int[]{});
                }
            }, 200);
        }
    }

}
