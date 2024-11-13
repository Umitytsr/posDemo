package test.demo.activity.emv;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import com.ciontek.hardware.aidl.bean.TransDataV2;
import com.ctk.sdk.DebugLogUtil;


import test.demo.MyApplication;
import test.demo.activity.BaseActivity;
import test.demo.activity.R;
import test.demo.activity.utils.MoneyUtils;
import test.demo.activity.utils.PreferencesUtil;
import test.demo.activity.view.AmountInputView;
import test.demo.activity.view.NumberKeyboard;

/**
 * 消费输入消费金额
 */
public class InputMoneyActivity extends BaseActivity implements View.OnClickListener {
    private AmountInputView amountInputView;
    private AmountInputView amountBackInputView;

    private Boolean inputCashbackFlag = false;
    private String amountAuth;
    private String amountOther;
    private static final String TAG = "InputMoneyActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inputmoney);
        initView();
        MyApplication.app.newTransFlag = 1;
    }

    private void initView() {
        RelativeLayout rl_bottom = findViewById(R.id.rl_bottom);
        amountInputView = findViewById(R.id.amountInputView);
        NumberKeyboard numberKeyboard = findViewById(R.id.keyboard);

        //showToast(R.string.entrymoney);
        rl_bottom.setOnClickListener(this);
        numberKeyboard.setKeyClickCallback(new NumberKeyboard.KeyClickCallback() {
            @Override
            public void onNumClick(int keyNum) {
                amountInputView.addText(keyNum + "");
            }

            @Override
            public void onDelClick() {
                amountInputView.delLast();
            }

            @Override
            public void onCleanClick() {
                amountInputView.clean();
            }
        });

    }

    private void inputCashbackMoney(){
        showToast(getString(R.string.input_cashback_amount));
        inputCashbackFlag = true;
        amountBackInputView = findViewById(R.id.amountInputView);
        NumberKeyboard numberKeyboard1 = findViewById(R.id.keyboard);
        numberKeyboard1.setKeyClickCallback(new NumberKeyboard.KeyClickCallback() {
            @Override
            public void onNumClick(int keyNum) {
                amountBackInputView.addText(keyNum + "");
            }

            @Override
            public void onDelClick() {
                amountBackInputView.delLast();
            }

            @Override
            public void onCleanClick() {
                amountBackInputView.clean();
            }
        });

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.rl_bottom) {
            if ((PreferencesUtil.getFunSetTransactionType().equals("09")) && (!inputCashbackFlag)) { // 未输入返现金额，提示输入
                amountAuth = amountInputView.getAmountText().toString();
                amountInputView.clean();
                inputCashbackMoney();
            } else {  // 非返现交易或已输入返现金额的返现交易
                if (!inputCashbackFlag) {
                    amountAuth = amountInputView.getAmountText().toString();
                } else {
                    amountOther = amountBackInputView.getAmountText().toString();
                }
                DebugLogUtil.e(TAG,"amountAuth: "+amountAuth);
                DebugLogUtil.e(TAG,"amountOther: "+amountOther);
                btnPayClick();
            }
        }
    }

    private void btnPayClick() {
        String amount = amountAuth;
        long amountOther = 0;
        long amountAuth;
        if(amount != null) {
            amountAuth = MoneyUtils.stringMoney2LongCent(amount);
        }
        else{
            amountAuth = 1;
            amount = "0.01";
        }
        if(PreferencesUtil.getFunSetTransactionType().equals("09")){ //  cashback
            String amountBack = this.amountOther;
            if(amountBack != null) {
                amountOther = MoneyUtils.stringMoney2LongCent(amountBack);
            }
            else{
                amountOther = 1;
            }
            amount = MoneyUtils.longCent2DoubleMoneyStr(amountOther+amountAuth);
        }

        TransDataV2 TransData = new TransDataV2();
        DebugLogUtil.d(TAG, "btnPayClick amount: " + amount);
        if (amount.equals("0.00") && PreferencesUtil.getFunSetZeroAmtSupport().equals("00")) {
            showToast(R.string.entrymoney);
        }else {
            TransData.amount = MoneyUtils.stringMoney2LongCent(amount) + "";
            Intent intent = new Intent(this, SwingCardActivity.class);
            intent.putExtra("TransData", TransData);
            intent.putExtra("amount", TransData.amount);
            intent.putExtra("amountAuth", amountAuth + amountOther);
            intent.putExtra("amountOther", amountOther);
            DebugLogUtil.d(TAG,"goto SwingCardActivity");
            openActivity(intent, true);
        }
    }

}
