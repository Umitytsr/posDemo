package test.demo.activity.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import test.demo.activity.R;
import test.demo.activity.utils.PreferencesUtil;


public class ServerInfoConfigDialog extends Dialog {
    private EditText mIPEditView;
    private EditText mPortEditText;
    private Button mConfirmBtn;
    private Context mContext;

    public ServerInfoConfigDialog(Context context) {
        super(context);
        mContext = context;
    }

    public ServerInfoConfigDialog(Context context, int themeResId) {
        super(context, themeResId);
        mContext = context;
    }

    protected ServerInfoConfigDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_server_info_config);
        initView();
    }

    private void initView() {
        mIPEditView = (EditText) findViewById(R.id.ip);
        mIPEditView.setText(PreferencesUtil.getServerIP());
        mPortEditText = (EditText) findViewById(R.id.port);
        mPortEditText.setText(PreferencesUtil.getServerPort());
        mConfirmBtn = (Button) findViewById(R.id.confirm);
        mConfirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ip = mIPEditView.getText().toString().trim();
                String port = mPortEditText.getText().toString().trim();
                if (!TextUtils.isEmpty(ip) && !TextUtils.isEmpty(port)) {
                    saveIPAndPort(ip, port);
                    dismiss();
                } else {
                    Toast.makeText(mContext, R.string.tip_empty_content, Toast.LENGTH_SHORT).show();
                }
            }
        });
        setCancelable(false);
        setCanceledOnTouchOutside(false);
    }

    /**
     * 保存IP和端口
     */
    private void saveIPAndPort(String ip, String port) {
        PreferencesUtil.setServerIP(ip);
        PreferencesUtil.setServerPort(port);
    }
}
