package test.demo.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;

import com.zhy.m.permission.MPermissions;
import java.util.ArrayList;
import java.util.List;

import test.demo.activity.emv.InputMoneyActivity;
import test.demo.activity.pinkeypad.PinpadCustomLayoutActivity;
import test.demo.activity.setting.FuncSettingActivity;



/**
 * Created by Administrator on 2017/12/12.
 */

public class Emvl2TestActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "Emvl2TestActivity";
    private final List<Button> funcBtns = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emv_test);
        initView();
        //connectPayService();
        //MyApplication.app.registerServiceConnectListener(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        MPermissions.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void initView() {
        funcBtns.add((Button) findViewById(R.id.transaction));
        funcBtns.add((Button) findViewById(R.id.function_setting));
        funcBtns.add((Button) findViewById(R.id.other_setting));
        funcBtns.add((Button) findViewById(R.id.pinkeypadtest));

        ProgressDialog mProgressDialog = new ProgressDialog(Emvl2TestActivity.this);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setMessage("Loading data");

        for (Button btn : funcBtns) {
            btn.setOnClickListener(this);
        }
        switchButton(true);
    }

    /**
     * 开关
     *
     * @param onOff: TRUE:show button
     */
    private void switchButton(boolean onOff) {
        for (Button btn : funcBtns) {
//            if (btn.getId() == R.id.conn) {
//                continue;
//            }
            btn.setEnabled(onOff);
        }
    }

    //@SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
//            case R.id.conn:
//                connectPayService();
//                break;
            case R.id.transaction:
                Log.e(TAG,"entry transaction");
                openActivity(InputMoneyActivity.class);
                break;

            case R.id.function_setting:
                //openActivity(FunctionSettingActivity.class);
                openActivity(FuncSettingActivity.class);
                break;

            case R.id.other_setting:
                openActivity(OtherSettingActivity.class);
                break;

            case R.id.pinkeypadtest:
                int pinType =10;
                int pinPadType =0;
                Intent intent = new Intent(this,PinpadCustomLayoutActivity.class);
                intent.putExtra("pinType",pinType);
                intent.putExtra("pinPadType", pinPadType);
                openActivity(intent);

                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        //MyApplication.app.unregisterServiceConnectListener(this);
        //MyApplication.app.disconnectPayService();
        super.onDestroy();
    }

}
