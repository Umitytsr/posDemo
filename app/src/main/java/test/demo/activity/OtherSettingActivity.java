package test.demo.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;


import test.demo.activity.constants.UpdateDataConstant;
import test.demo.activity.listener.UpdateDataListener;
import test.demo.activity.utils.UpdateDataController;


/**
 * 其他设置
 */
public class OtherSettingActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "OtherSettingActivity";
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_setting);
        initView();
        UpdateDataController.getInstance().setUpdateDataListener(listener);
    }

    private UpdateDataListener listener = new UpdateDataListener() {
        @Override
        public void onStart() {
            showProgressDialog(mProgressDialog);
        }

        @Override
        public void onSucess() {
            dismissProgressDialog(mProgressDialog);
            Toast.makeText(OtherSettingActivity.this, "update_success", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onError(String message) {
            dismissProgressDialog(mProgressDialog);
            Toast.makeText(OtherSettingActivity.this, message, Toast.LENGTH_LONG).show();
        }
    };

    private void showProgressDialog(Dialog dialog) {
        if (dialog != null && !dialog.isShowing()) {
            dialog.show();
        }
    }

    private void dismissProgressDialog(Dialog dialog) {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }
    private void initView() {
        Button updatePubKey = (Button) findViewById(R.id.update_pub_key);
        Button updateAID = (Button) findViewById(R.id.update_aid);
        Button updateTerminalParams = (Button) findViewById(R.id.update_terminal_params);
        Button updateRecoveryPublicKey = (Button) findViewById(R.id.update_recovery_public_key);
        Button updateBlacklist = (Button) findViewById(R.id.update_blacklist);

        updatePubKey.setOnClickListener(this);
        updateAID.setOnClickListener(this);
        updateTerminalParams.setOnClickListener(this);
        updateRecoveryPublicKey.setOnClickListener(this);
        updateBlacklist.setOnClickListener(this);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setMessage("load_update_data");

    }

    public void onBackPressed() {
        finish();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.update_pub_key:
                UpdateDataController.getInstance().updateData(UpdateDataConstant.UPDATE_PUB_KEY);
                break;
            case R.id.update_aid:
                UpdateDataController.getInstance().updateData(UpdateDataConstant.UPDATE_AID);
                break;
            case R.id.update_terminal_params:
                UpdateDataController.getInstance().updateData(UpdateDataConstant.UPDATE_TERM_PARAM);
                break;
            case R.id.update_blacklist:
                UpdateDataController.getInstance().updateData(UpdateDataConstant.UPDATE_BLACK_LIST);
                break;
            case R.id.update_recovery_public_key:
                UpdateDataController.getInstance().updateData(UpdateDataConstant.UPDATE_RECYCLE_PUBKEY);
                break;
        }
    }
}
