package test.demo.activity.emv;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.ctk.sdk.DebugLogUtil;

import test.demo.activity.BaseActivity;
import test.demo.activity.R;
import test.demo.activity.utils.PreferencesUtil;


public class TransAgainActivity extends BaseActivity {
    private String TAG ="TransAgainActivity";

    private Handler handler = new Handler();

    public static final String CardMethod = "CardType";

    public static final String CardOperateResult = "Card_Operate_Result";

    String amt;
    String amt_other;

    TextView tickCounter;
    TextView cardOpeResult;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_cardagain_layout);
        initView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }

    private void initView(){
        int cardMethod = getIntent().getIntExtra(CardMethod,0);
        String dispmsg = getIntent().getStringExtra(CardOperateResult);
        if(dispmsg.isEmpty()){
            JumpToReadCard();
        }else{
            tickCounter = findViewById(R.id.tick_counter);
            cardOpeResult = findViewById(R.id.card_Operate_result);
            cardOpeResult.setText(dispmsg);
            startCountdown(5);
        }
    }
    private void JumpToReadCard(){
        Intent intent = new Intent(this, SwingCardActivity.class);
        if(PreferencesUtil.getFunSetTransactionType().equals("00")){
            amt = PreferencesUtil.getFunSetLastAmount();
            intent.putExtra("amount", amt);
        }else if(PreferencesUtil.getFunSetTransactionType().equals("09")){
            amt_other = PreferencesUtil.getFunSetLastAmount();
            intent.putExtra("amountOther",amt_other);
        }
        DebugLogUtil.d(TAG,"goto SwingCardActivity");
        openActivity(intent, true);
    }

    /** 倒计时关闭页面 */
    private void startCountdown(final int count) {

        //tickCounter.setText(""+ count));
        tickCounter.setText(String.valueOf(count));

        if (count == 0) {
            JumpToReadCard();
        } else {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startCountdown(count - 1);
                }
            }, 1000);
        }
    }
}
