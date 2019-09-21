package kr.devx.whitechat;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;

import org.json.JSONObject;

import de.psdev.licensesdialog.LicensesDialog;
import de.psdev.licensesdialog.licenses.ApacheSoftwareLicense20;
import de.psdev.licensesdialog.licenses.GnuLesserGeneralPublicLicense21;
import de.psdev.licensesdialog.licenses.MITLicense;
import de.psdev.licensesdialog.model.Notice;
import de.psdev.licensesdialog.model.Notices;
import io.socket.emitter.Emitter;

public class SettingFragment extends Fragment {

    private WhiteApplication whiteApplication;
    private int currentPage;
    private SharedPreferences appPreferences;

    private Switch notificationForegroundSwitch, notificationBackgroundSwitch;
    private LinearLayout logoutView, withdrawView;
    private TextView opensourceView;

    public static SettingFragment newInstance(int currentPage) {
        SettingFragment fragment = new SettingFragment();
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
        View rootView = inflater.inflate(R.layout.fragment_setting, container, false);
        initializeId(rootView);

        ((MainActivity)getActivity()).onSettingFragmentReady(this);

        return rootView;
    }

    private void initializeId(View rootView) {
        notificationForegroundSwitch = rootView.findViewById(R.id.setting_notification_foreground_switch);
        notificationBackgroundSwitch = rootView.findViewById(R.id.setting_notification_background_switch);
        logoutView = rootView.findViewById(R.id.setting_logout);
        withdrawView = rootView.findViewById(R.id.setting_withdraw);
        opensourceView = rootView.findViewById(R.id.setting_opensource);
    }

    public void onLifeStart() {
        appPreferences = getContext().getSharedPreferences("DATA", Context.MODE_PRIVATE);
        boolean isNotificationForegroundActive = appPreferences.getBoolean("NOTIFICATION_FOREGROUND", true);
        boolean isNotificationBackgroundActive = appPreferences.getBoolean("NOTIFICATION_BACKGROUND", true);
        notificationForegroundSwitch.setChecked(isNotificationForegroundActive);
        notificationBackgroundSwitch.setChecked(isNotificationBackgroundActive);
        notificationForegroundSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                whiteApplication.IS_NOTIFICATION_FOREGROUND_ACTIVE = isChecked;
                appPreferences.edit().putBoolean("NOTIFICATION_FOREGROUND", isChecked).apply();
            }
        });
        notificationBackgroundSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                whiteApplication.IS_NOTIFICATION_BACKGROUND_ACTIVE = isChecked;
                appPreferences.edit().putBoolean("NOTIFICATION_BACKGROUND", isChecked).apply();
            }
        });
        logoutView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).onLogout();
            }
        });
        withdrawView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).mSocket.on("withdraw", onWithdraw);
                showWithdrawDialog();
            }
        });
        opensourceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOpensourceDialog();
            }
        });
    }

    private void showWithdrawDialog() {
        final EditText idEditor = new EditText(getContext());
        idEditor.setHint(getString(R.string.setting_withdraw_dialog_hint_id));
        final EditText passwordEditor = new EditText(getContext());
        passwordEditor.setHint(getString(R.string.setting_withdraw_dialog_hint_pw));
        passwordEditor.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        SweetAlertDialog withdrawDialog = new SweetAlertDialog(getContext(), SweetAlertDialog.NORMAL_TYPE)
                .setTitleText(getString(R.string.setting_withdraw_dialog_hint_id))
                .setContentText(getString(R.string.setting_withdraw_dialog_content))
                .setConfirmText(getString(R.string.setting_withdraw_dialog))
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        if (idEditor.getText().toString().trim().length() < 1 || passwordEditor.getText().toString().trim().length() < 1) {
                            Toast.makeText(getContext(), getString(R.string.setting_withdraw_dialog_content), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (!whiteApplication.User.user_id.equals(idEditor.getText().toString().trim())) {
                            Toast.makeText(getContext(), getString(R.string.setting_withdraw_dialog_failed_id), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        try {
                            JSONObject data = new JSONObject();
                            data.put("confirmPW", passwordEditor.getText().toString().trim());
                            data.put("key",whiteApplication.User.user_key);
                            ((MainActivity)getActivity()).mSocket.emit("join", data);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        sweetAlertDialog.dismiss();
                    }
                });
        withdrawDialog.show();
        LinearLayout linearLayout = withdrawDialog.findViewById(R.id.loading);
        int index = linearLayout.indexOfChild(linearLayout.findViewById(R.id.content_text));
        linearLayout.addView(idEditor, index + 1);
        linearLayout.addView(passwordEditor, index + 2);
    }

    private void showOpensourceDialog() {
        final Notices notices = new Notices();
        notices.addNotice(new Notice("SweetAlert Dialog", "https://jitpack.io/p/thomper/sweet-alert-dialog", "Pedant", new MITLicense()));
        notices.addNotice(new Notice("NavigationTabBar", "https://github.com/Devlight/NavigationTabBar", "Basil Miller", new ApacheSoftwareLicense20()));
        notices.addNotice(new Notice("socket.io-client-java", "https://github.com/socketio/socket.io-client-java", "Naoyuki Kanezawa", new GnuLesserGeneralPublicLicense21()));
        notices.addNotice(new Notice("Glide", "https://github.com/bumptech/glide", "Google", new ApacheSoftwareLicense20()));
        notices.addNotice(new Notice("Firebase", "http://example.org", "Example Person 2", new ApacheSoftwareLicense20()));
        new LicensesDialog.Builder(getActivity())
                .setNotices(notices)
                .setIncludeOwnLicense(true)
                .build()
                .show();
    }

    private Emitter.Listener onWithdraw = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d("WCHAT", "SOCKET > onWithdraw");
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
                ((MainActivity)getActivity()).makeAlertDialog(SweetAlertDialog.SUCCESS_TYPE, getString(R.string.setting_withdraw_success), false, new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        ((MainActivity)getActivity()).onLogout();
                    }
                });
                ((MainActivity)getActivity()).refreshDatas();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

}
