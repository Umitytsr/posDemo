package test.demo.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;


import java.util.Timer;

import test.demo.MyApplication;


/**
 * Created by Administrator on 2017/8/17.
 */

public class PrintActivity extends Activity {

    public String tag = "PrintActivity-Robert2";

    final int PRINT_TEST = 0;
    final int PRINT_UNICODE = 1;
    final int PRINT_BMP = 2;
    final int PRINT_BARCODE = 4;
    final int PRINT_CYCLE = 5;
    final int PRINT_LONGER = 7;
    final int PRINT_OPEN = 8;


    final int PRINT_LAB_SINGLE = 9;
    final int PRINT_LAB_CONTINUE = 10;
    final int PRINT_LAB_BAR = 11;
    final int PRINT_LAB_BIT = 12;

    private RadioGroup rg = null;
    private RadioGroup rg_mode = null;
    private RadioButton rb_mode1 = null;
    private Timer timer;
    private Timer timer2;
    private BroadcastReceiver receiver;
    private IntentFilter filter;
    private int voltage_level;
    private int BatteryV;
    private int mode_flag = 0;
    SharedPreferences preferences;
    SharedPreferences sp;
    SharedPreferences.Editor editor;
    private RadioButton rb_high;
    private RadioButton rb_middle;
    private RadioButton rb_low;
    private RadioButton radioButton_4;
    private RadioButton radioButton_5;
    private Button gb_test;
    private Button gb_unicode;
    private Button gb_barcode;
    private Button btnBmp;

    private Button gb_open;
    private Button gb_long;
    private Button gb_printCycle;

    private Button gb_single;
    private Button gb_continue;
    private Button gb_bar;
    private Button gb_bitm;


    private final static int ENABLE_RG = 10;
    private final static int DISABLE_RG = 11;

    TextView textViewMsg = null;
    TextView textViewGray = null;
    int ret = -1;
    private boolean m_bThreadFinished = true;

    private boolean is_cycle = false;
    private int cycle_num = 0;

    private int RESULT_CODE = 0;
    //private Pos pos;
    int IsWorking = 0;

    Intent mPrintServiceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //无title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_print);
        textViewMsg = (TextView) this.findViewById(R.id.textView_msg);
        textViewGray = (TextView) this.findViewById(R.id.textview_Gray);
        rg = (RadioGroup) this.findViewById(R.id.rg_Gray_type);
        rb_high = (RadioButton) findViewById(R.id.RadioButton_high);
        rb_middle = (RadioButton) findViewById(R.id.RadioButton_middle);
        rb_low = (RadioButton) findViewById(R.id.radioButton_low);
        radioButton_4 = (RadioButton) findViewById(R.id.radioButton_4);
        radioButton_5 = (RadioButton) findViewById(R.id.radioButton_5);
        gb_test = (Button) findViewById(R.id.button_test);
        gb_unicode = (Button) findViewById(R.id.button_unicode);
        gb_barcode = (Button) findViewById(R.id.button_barcode);
        btnBmp = (Button) findViewById(R.id.btnBmp);
        gb_printCycle = (Button) findViewById(R.id.printCycle);

        //----------
        gb_open = (Button) findViewById(R.id.btn_open);
        gb_long = (Button) findViewById(R.id.btnLong);
        gb_single = (Button) findViewById(R.id.btnLabal_single);
        gb_continue = (Button) findViewById(R.id.btnLabal_continue);
        gb_bar = (Button) findViewById(R.id.btnLabal_barcode);
        gb_bitm = (Button) findViewById(R.id.btnLabal_bitmap);

        /*printer mode*/
        rg_mode = (RadioGroup) this.findViewById(R.id.rg_Gray_mode2);
        rb_mode1 = (RadioButton) this.findViewById(R.id.RadioButton_mode1);

        init_Gray();

        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {

                if (printThread != null && !printThread.isThreadFinished()) {

                    Log.e(tag, "Thread is still running...");
                    return;
                }

                String strGray=getResources().getString(R.string.selectGray);

                switch (checkedId) {
                    case R.id.radioButton_low:
                        try {
                            textViewGray.setText(strGray+"3");
                            MyApplication.app.printerOpt.PrnSetGray(3+mode_flag);
                            setValue(3);
                        }catch (RemoteException e){
                            e.printStackTrace();
                        }
                        break;
                    case R.id.RadioButton_middle:
                        try {
                            textViewGray.setText(strGray+"2");
                            MyApplication.app.printerOpt.PrnSetGray(2+mode_flag);
                            setValue(2);
                        }catch (RemoteException e){
                            e.printStackTrace();
                        }


                        break;
                    case R.id.RadioButton_high:
                        try {
                            textViewGray.setText(strGray+"1");
                            MyApplication.app.printerOpt.PrnSetGray(1+mode_flag);
                            setValue(1);
                        }catch (RemoteException e){
                            e.printStackTrace();
                        }

                        break;

                    case R.id.radioButton_4:
                        try {
                            textViewGray.setText(strGray+"4");
                            MyApplication.app.printerOpt.PrnSetGray(4+mode_flag);
                            setValue(4);
                        }catch (RemoteException e){
                            e.printStackTrace();
                        }

                        break;
                    case R.id.radioButton_5:
                        try {
                            textViewGray.setText(strGray+"5");
                            MyApplication.app.printerOpt.PrnSetGray(5+mode_flag);
                            setValue(5);
                        }catch (RemoteException e){
                            e.printStackTrace();
                        }

                        break;
                }
            }
        });

        /*print mode*/
        rb_mode1.setChecked(true);

        handler.sendEmptyMessage(0x34);
        rg_mode.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {

                if (printThread != null && !printThread.isThreadFinished()) {

                    Log.e(tag, "Thread is still running...");
                    return;
                }

                switch (checkedId) {
                    case R.id.RadioButton_mode1:
                        try {
                            MyApplication.app.printerOpt.PrnSetMode(0);
                            handler.sendEmptyMessage(0x34);
                            mode_flag = 0;
                            MyApplication.app.printerOpt.PrnSetGray(getValue()+mode_flag);
                        }catch (RemoteException e){
                            e.printStackTrace();
                        }
                        break;
                    case R.id.RadioButton_mode2:
                        try {
                            MyApplication.app.printerOpt.PrnSetMode(1);
                            mode_flag = 2;
                            MyApplication.app.printerOpt.PrnSetGray(getValue()+mode_flag);
                            handler.sendEmptyMessage(0x56);
                        }catch (RemoteException e){
                            e.printStackTrace();
                        }
                        break;
                    default:
                        try {
                            handler.sendEmptyMessage(0x34);
                            MyApplication.app.printerOpt.PrnSetMode(0);
                        }catch (RemoteException e){
                            e.printStackTrace();
                        }
                        break;
                }
            }
        });

    }

    private void setValue(int val) {
        sp = getSharedPreferences("Gray", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("value", val);
        editor.commit();
    }

    private int getValue() {
        sp = getSharedPreferences("Gray", MODE_PRIVATE);
        int value = sp.getInt("value", 2);
        return value;
    }

    private void init_Gray() {
        int flag = getValue();
        try {
            MyApplication.app.printerOpt.PrnSetGray(flag);
        }catch (RemoteException e){
            e.printStackTrace();
        }
        String strGray=getResources().getString(R.string.selectGray);

        if (flag == 3) {
            rb_low.setChecked(true);
            textViewGray.setText(strGray+"3");
        }else if(flag == 2){
            rb_middle.setChecked(true);
            textViewGray.setText(strGray+"2");
        }else if(flag == 1){
            rb_high.setChecked(true);
            textViewGray.setText(strGray+"1");
        }else if(flag == 4){
            radioButton_4.setChecked(true);
            textViewGray.setText(strGray+"4");
        }else if(flag == 5){
            radioButton_5.setChecked(true);
            textViewGray.setText(strGray+"5");
        }
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onResume();
        filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        super.onPause();
        QuitHandler();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d("onKeyDown", "keyCode = " + keyCode);

        Log.d("ROBERT2 onKeyDown", "keyCode = " + keyCode);
        Log.d("ROBERT2 onKeyDown", "IsWorking== " + IsWorking);
        if (keyCode == event.KEYCODE_BACK) {
            if (IsWorking == 1)
                return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    public void onClickTest(View v) {
        if (printThread != null && !printThread.isThreadFinished()) {
            Log.e(tag, "Thread is still running...");
            return;
        }

        printThread = new Print_Thread(PRINT_TEST);
        printThread.start();
    }

    public void onClickUnicodeTest(View v) {
        if (printThread != null && !printThread.isThreadFinished()) {
            Log.e(tag, "Thread is still running...");
            return;
        }

        printThread = new Print_Thread(PRINT_UNICODE);
        printThread.start();

    }

    public void OnClickBarcode(View view) {
        if (printThread != null && !printThread.isThreadFinished()) {
            Log.e(tag, "Thread is still running...");
            return;
        }

        printThread = new Print_Thread(PRINT_BARCODE);
        printThread.start();
    }

    public void onClickBmp(View view) {
        if (printThread != null && !printThread.isThreadFinished()) {
            Log.e(tag, "Thread is still running...");
            return;
        }

        printThread = new Print_Thread(PRINT_BMP);
        printThread.start();

    }

    public void onClickCycle(View v) {
        if (printThread != null && !printThread.isThreadFinished()) {
            Log.e(tag, "Thread is still running...");
            return;
        }

        if (is_cycle == false) {
            is_cycle = true;
            preferences = getSharedPreferences("count", MODE_PRIVATE);

            cycle_num = preferences.getInt("count", 0);
            SendMsg("total cycle num =" + cycle_num);
            Log.e(tag, "Thread is still 3000ms...");
            handlers.postDelayed(runnable, 3000);

            gb_printCycle.setText("Stop");

        }else{
            handlers.removeCallbacks(runnable);
            gb_printCycle.setText("Cycle");
            is_cycle = false;
        }
    }

    public void onClickClean(View v) {
        textViewMsg.setText("");
        preferences = getSharedPreferences("count", MODE_PRIVATE);
        cycle_num = preferences.getInt("count", 0);
        editor = preferences.edit();
        cycle_num = 0;
        editor.putInt("count", cycle_num);
        editor.commit();
        QuitHandler();
    }

    public void onClickPrnOpen(View v) {
        if (printThread != null && !printThread.isThreadFinished()) {
            Log.e(tag, "Thread is still running...");
            return;
        }

        printThread = new Print_Thread(PRINT_OPEN);
        printThread.start();
    }

    public void onClickLong(View v) {

        if (printThread != null && !printThread.isThreadFinished()) {
            Log.e(tag, "Thread is still running...");
            return;
        }
        printThread = new Print_Thread(PRINT_LONGER);
        printThread.start();
    }

    public void onClick_single(View v) {
        if (printThread != null && !printThread.isThreadFinished()) {
            Log.e(tag, "Thread is still running...");
            return;
        }

        printThread = new Print_Thread(PRINT_LAB_SINGLE);
        printThread.start();
    }

    public void onClick_continue(View v) {
        if (printThread != null && !printThread.isThreadFinished()) {
            Log.e(tag, "Thread is still running...");
            return;
        }

        printThread = new Print_Thread(PRINT_LAB_CONTINUE);
        printThread.start();
    }

    public void onClick_barcode(View v) {
        if (printThread != null && !printThread.isThreadFinished()) {
            Log.e(tag, "Thread is still running...");
            return;
        }

        printThread = new Print_Thread(PRINT_LAB_BAR);
        printThread.start();
    }

    public void onClick_bitmap(View v) {
        if (printThread != null && !printThread.isThreadFinished()) {
            Log.e(tag, "Thread is still running...");
            return;
        }

        printThread = new Print_Thread(PRINT_LAB_BIT);
        printThread.start();
    }

    public void QuitHandler() {
        is_cycle = false;
        gb_test.setEnabled(true);
        gb_barcode.setEnabled(true);
        btnBmp.setEnabled(true);
        gb_unicode.setEnabled(true);
        handlers.removeCallbacks(runnable);
    }

    Handler handlers = new Handler();
    Runnable runnable = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub

            Log.e(tag, "TIMER log...");
            printThread = new Print_Thread(PRINT_CYCLE);
            printThread.start();

            Log.e(tag, "TIMER log2...");
            if (RESULT_CODE == 0) {
                editor = preferences.edit();
                editor.putInt("count", ++cycle_num);
                editor.commit();
                Log.e(tag, "cycle num=" + cycle_num);
                SendMsg("cycle num =" + cycle_num);
            }
            handlers.postDelayed(this, 15000);

        }
    };

    Print_Thread printThread = null;

    public class Print_Thread extends Thread {

        String content = "1234567890";
        Bitmap bmp1 = BitmapFactory.decodeResource(PrintActivity.this.getResources(), R.mipmap.test001);
        int type;

        public boolean isThreadFinished() {
            return m_bThreadFinished;
        }

        public Print_Thread(int type) {
            this.type = type;
        }

        public void run() {
            Log.d("Robert2", "Print_Thread[ run ] run() begin");
            Message msg = Message.obtain();
            Message msg1 = new Message();

            {

                m_bThreadFinished = false;
                try {
                    ret = MyApplication.app.printerOpt.PrnInit();
                    if(ret == -1101){
                        Log.d(tag,"printer module not support");
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Log.e(tag, "init code:" + ret);

                ret = getValue();

                Log.e(tag, "getValue():" + ret);
                if(mode_flag > 0){
                    try {
                        MyApplication.app.printerOpt.PrnSetMode(1);
                    }catch (RemoteException e){
                        e.printStackTrace();
                    }

                }
                else{
                    try {
                        MyApplication.app.printerOpt.PrnSetMode(0);
                    }catch (RemoteException e){
                        e.printStackTrace();
                    }

                }
                try {
                    MyApplication.app.printerOpt.PrnSetGray(ret+mode_flag);
                }catch (RemoteException e){
                    e.printStackTrace();
                }

                Log.e(tag, "PrintSetGray():" );

                try {
                    ret = MyApplication.app.printerOpt.PrnCheckStatus();
                }catch (RemoteException e){
                    e.printStackTrace();
                }

                Log.e(tag, "PrintCheckStatus():" );
                if (ret == -1) {
                    RESULT_CODE = -1;
                    Log.e(tag, "Lib_PrnCheckStatus fail, ret = " + ret);
                    SendMsg("Error, No Paper ");
                    m_bThreadFinished = true;
                    return;
                } else if (ret == -2) {
                    RESULT_CODE = -1;
                    Log.e(tag, "Lib_PrnCheckStatus fail, ret = " + ret);
                    SendMsg("Error, Printer Too Hot ");
                    m_bThreadFinished = true;
                    return;
                } else if (ret == -3) {
                    RESULT_CODE = -1;
                    Log.e(tag, "voltage = " + (BatteryV * 2));
                    SendMsg("Battery less :" + (BatteryV * 2));
                    m_bThreadFinished = true;
                    return;
                }
                else
                {
                    RESULT_CODE = 0;
                }

                Log.d("Robert2", "Lib_PrnStart type= "+type );
                switch (type) {

                    case PRINT_LONGER:
                        SendMsg("PRINT LONG");

                        msg.what = DISABLE_RG;
                        handler.sendMessage(msg);

                        //String stringg = " a b c d e f g h i j k l m n o p q r s t u v w x z A B C D E F G H I J K L M N O P Q R S T U V W X Z 1 2 3 4 5 6 7 8 9 ! @ # $ % ^ & * () _ + ~   [ ] , . / ; ' { } : : | < >?";
                        try {
                            MyApplication.app.printerOpt.PrnSetFont((byte) 24, (byte) 24, (byte) 0x00);
                            MyApplication.app.printerOpt.PrnStr("a b c d e f g h i j k l m n o p q r s t u v w x z A B C D E F G H I J K L M N O P Q R S T U V W X Z 1 2 3 4 5 6 7 8 9 ! @ # $ % ^ & * () _ + ~   [ ] , . / ; ' { } : : | < >?a b c d e f g h i j k l m n o p q r s t u v w x z A B C D E F G H I J K L M N O P Q R S T U V W X Z 1 2 3 4 5 6 7 8 9 ! @ # $ % ^ & * () _ + ~   [ ] , . / ; ' { } : : | < >?a b c d e f g h i j k l m n o p q r s t u v w x z A B C D E F G H I J K L M N O P Q R S T U V W X Z 1 2 3 4 5 6 7 8 9 ! @ # $ % ^ & * () _ + ~   [ ] , . / ; ' { } : : | < >?a b c d e f g h i j k l m n o p q r s t u v w x z A B C D E F G H I J K L M N O P Q R S T U V W X Z 1 2 3 4 5 6 7 8 9 ! @ # $ % ^ & * () _ + ~   [ ] , . / ; ' { } : : | < >?a b c d e f g h i j k l m n o p q r s t u v w x z A B C D E F G H I J K L M N O P Q R S T U V W X Z 1 2 3 4 5 6 7 8 9 ! @ # $ % ^ & * () _ + ~   [ ] , . / ; ' { } : : | < >? a b c d e f g h i j k l m n o p q r s t u v w x z A B C D E F G H I J K L M N O P Q R S T U V W X Z 1 2 3 4 5 6 7 8 9 ! @ # $ % ^ & * () _ + ~   [ ] , . / ; ' { } : : | < >? a b c d e f g h i j k l m n o p q r s t u v w x z A B C D E F G H I J K L M N O P Q R S T U V W X Z 1 2 3 4 5 6 7 8 9 ! @ # $ % ^ & * () _ + ~   [ ] , . / ; ' { } : : | < >?a b c d e f g h i j k l m n o p q r s t u v w x z A B C D E F G H I J K L M N O P Q R S T U V W X Z 1 2 3 4 5 6 7 8 9 ! @ # $ % ^ & * () _ + ~   [ ] , . / ; ' { } : : | < >?a b c d e f g h i j k l m n o p q r s t u v w x z A B C D E F G H I J K L M N O P Q R S T U V W X Z 1 2 3 4 5 6 7 8 9 ! @ # $ % ^ & * () _ + ~   [ ] , . / ; ' { } : : | < >?a b c d e f g h i j k l m n o p q r s t u v w x z A B C D E F G H I J K L M N O P Q R S T U V W X Z 1 2 3 4 5 6 7 8 9 ! @ # $ % ^ & * () _ + ~   [ ] , . / ; ' { } : : | < >?a b c d e f g h i j k l m n o p q r s t u v w x z A B C D E F G H I J K L M N O P Q R S T U V W X Z 1 2 3 4 5 6 7 8 9 ! @ # $ % ^ & * () _ + ~   [ ] , . / ; ' { } : : | < >?a b c d e f g h i j k l m n o p q r s t u v w x z A B C D E F G H I J K L M N O P Q R S T U V W X Z 1 2 3 4 5 6 7 8 9 ! @ # $ % ^ & * () _ + ~   [ ] , . / ; ' { } : : | < >?a b c d e f g h i j k l m n o p q r s t u v w x z A B C D E F G H I J K L M N O P Q R S T U V W X Z 1 2 3 4 5 6 7 8 9 ! @ # $ % ^ & * () _ + ~   [ ] , . / ; ' { } : : | < >?a b c d e f g h i j k l m n o p q r s t u v w x z A B C D E F G H I J K L M N O P Q R S T U V W X Z 1 2 3 4 5 6 7 8 9 ! @ # $ % ^ & * () _ + ~   [ ] , . / ; ' { } : : | < >?a b c d e f g h i j k l m n o p q r s t u v w x z A B C D E F G H I J K L M N O P Q R S T U V W X Z 1 2 3 4 5 6 7 8 9 ! @ # $ % ^ & * () _ + ~   [ ] , . / ; ' { } : : | < >?a b c d e f g h i j k l m n o p q r s t u v w x z A B C D E F G H I J K L M N O P Q R S T U V W X Z 1 2 3 4 5 6 7 8 9 ! @ # $ % ^ & * () _ + ~   [ ] , . / ; ' { } : : | < >?a b c d e f g h i j k l m n o p q r s t u v w x z A B C D E F G H I J K L M N O P Q R S T U V W X Z 1 2 3 4 5 6 7 8 9 ! @ # $ % ^ & * () _ + ~   [ ] , . / ; ' { } : : | < >?a b c d e f g h i j k l m n o p q r s t u v w x z A B C D E F G H I J K L M N O P Q R S T U V W X Z 1 2 3 4 5 6 7 8 9 ! @ # $ % ^ & * () _ + ~   [ ] , . / ; ' { } : : | < >?a b c d e f g h i j k l m n o p q r s t u v w x z A B C D E F G H I J K L M N O P Q R S T U V W X Z 1 2 3 4 5 6 7 8 9 ! @ # $ % ^ & * () _ + ~   [ ] , . / ; ' { } : : | < >?a b c d e f g h i j k l m n o p q r s t u v w x z A B C D E F G H I J K L M N O P Q R S T U V W X Z 1 2 3 4 5 6 7 8 9 ! @ # $ % ^ & * () _ + ~   [ ] , . / ; ' { } : : | < >?a b c d e f g h i j k l m n o p q r s t u v w x z A B C D E F G H I J K L M N O P Q R S T U V W X Z 1 2 3 4 5 6 7 8 9 ! @ # $ % ^ & * () _ + ~   [ ] , . / ; ' { } : : | < >?a b c d e f g h i j k l m n o p q r s t u v w x z A B C D E F G H I J K L M N O P Q R S T U V W X Z 1 2 3 4 5 6 7 8 9 ! @ # $ % ^ & * () _ + ~   [ ] , . / ; ' { } : : | < >?a b c d e f g h i j k l m n o p q r s t u v w x z A B C D E F G H I J K L M N O P Q R S T U V W X Z 1 2 3 4 5 6 7 8 9 ! @ # $ % ^ & * () _ + ~   [ ] , . / ; ' { } : : | < >?a b c d e f g h i j k l m n o p q r s t u v w x z A B C D E F G H I J K L M N O P Q R S T U V W X Z 1 2 3 4 5 6 7 8 9 ! @ # $ % ^ & * () _ + ~   [ ] , . / ; ' { } : : | < >?a b c d e f g h i j k l m n o p q r s t u v w x z A B C D E F G H I J K L M N O P Q R S T U V W X Z 1 2 3 4 5 6 7 8 9 ! @ # $ % ^ & * () _ + ~   [ ] , . / ; ' { } : : | < >?a b c d e f g h i j k l m n o p q r s t u v w x z A B C D E F G H I J K L M N O P Q R S T U V W X Z 1 2 3 4 5 6 7 8 9 ! @ # $ % ^ & * () _ + ~   [ ] , . / ; ' { } : : | < >?\n");

                            MyApplication.app.printerOpt.PrnBarcode(content, 360, 120, "CODE_128");

                            MyApplication.app.printerOpt.PrnStr("CODE_128 : " + content + "\n\n");
                            MyApplication.app.printerOpt.PrnBarcode(content, 240, 240, "QR_CODE");
                            MyApplication.app.printerOpt.PrnStr("QR_CODE : " + content + "\n\n");
                            MyApplication.app.printerOpt.PrnStr("发卡行(ISSUER):01020001 工商银行\n");
                            MyApplication.app.printerOpt.PrnStr("卡号(CARD NO):\n");
                            MyApplication.app.printerOpt.PrnStr("    9558803602109503920\n");
                            MyApplication.app.printerOpt.PrnStr("收单行(ACQUIRER):03050011民生银行\n");
                            MyApplication.app.printerOpt.PrnStr("交易类型(TXN. TYPE):消费/SALE\n");
                            MyApplication.app.printerOpt.PrnStr("卡有效期(EXP. DATE):2013/08\n");
                            MyApplication.app.printerOpt.PrnStr("- - - - - - - - - - - - - - - -\n");
                            MyApplication.app.printerOpt.PrnStr("                                         ");
                            MyApplication.app.printerOpt.PrnStr("\n");
                            MyApplication.app.printerOpt.PrnStr("\n");
                            MyApplication.app.printerOpt.PrnStr("\n");

                            SendMsg("Printing... ");
                            final long starttime_long = System.currentTimeMillis();
                            ret = MyApplication.app.printerOpt.PrnStart();
                            Log.e(tag, "PrintStart ret = " + ret);
                            msg1.what = ENABLE_RG;
                            handler.sendMessage(msg1);
                            if (ret != 0) {
                                RESULT_CODE = -1;
                                Log.e("liuhao", "Lib_PrnStart fail, ret = " + ret);
                                if (ret == -1) {
                                    SendMsg("No Print Paper ");
                                } else if(ret == -2) {
                                    SendMsg("too hot ");
                                }else if(ret == -3) {
                                    SendMsg("low voltage ");
                                }else{
                                    SendMsg("Print fail ");
                                }
                            } else {
                                RESULT_CODE = 0;
                                SendMsg("Print Finish ");

                                final long endttime_long = System.currentTimeMillis();
                                final long totaltime_long = starttime_long - endttime_long;
                                SendMsg("Print finish " );
                            }
                        }catch (RemoteException e){
                            e.printStackTrace();
                        }
                        break;
                    case PRINT_TEST:
                        Log.d("Robert2", "Lib_PrnStart ret START0 " );
                        SendMsg("PRINT_TEST");
                        msg.what = DISABLE_RG;
                        handler.sendMessage(msg);
                        try {
                            MyApplication.app.printerOpt.PrnSetFont((byte) 24, (byte) 24, (byte) 0x00);
                            MyApplication.app.printerOpt.PrnSetLineSpace(2);
                            MyApplication.app.printerOpt.PrnStr("جدارة للحلول الرقمية");
                            MyApplication.app.printerOpt.PrnStr("فاتورة ضريبية مبسطة");
                            MyApplication.app.printerOpt.PrnStr("من چین رو دوست دارم، مردم چینی ها رو زندگی میکنن");
                            MyApplication.app.printerOpt.PrnStr("الفبای فارسی گروه سی‌ودوگانهٔ حروف (اَشکال نوشتاری) در خط فارسی است که نمایندهٔ نگاشتن (همخوان‌ها یا صامت‌ها) در زبان فارسی است و");//阿拉伯语
                            MyApplication.app.printerOpt.PrnStr("                                         \n");
                            MyApplication.app.printerOpt.PrnStr("中文:你好，好久不见。\n");
                            MyApplication.app.printerOpt.PrnStr("英语:Hello, Long time no see   ￡ ：2089.22\n");
                            MyApplication.app.printerOpt.PrnStr("意大利语Italian :Ciao, non CI vediamo da Molto Tempo.\n");
                            MyApplication.app.printerOpt.PrnStr("西班牙语:España, ¡Hola! Cuánto tiempo sin verte!\n");
                            MyApplication.app.printerOpt.PrnStr("法语:Bonjour! Ça fait longtemps!\n");
                            MyApplication.app.printerOpt.PrnStr("ABCDEFGHIJKLMNHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNHIJKLMNOPQRSTUVWXYZ\n");
                            MyApplication.app.printerOpt.PrnStr("abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz\n");
                            MyApplication.app.printerOpt.PrnStr("12345678901234567890123456789012345678901234567890+_)(*&^%$#@!~\n");
                            ret = MyApplication.app.printerOpt.PrintSetFontTTF("/storage/emulated/0/Download/NotoSansDevanagari-Regular.ttf", (byte)24, (byte)24);
                            MyApplication.app.printerOpt.PrnStr("यह थोड़ा विचित्र है, हिंदी। ठीक है.");
                            MyApplication.app.printerOpt.PrnStr("\n");
                            MyApplication.app.printerOpt.PrnSetFont((byte) 24, (byte) 24, (byte) 0x00);
                            MyApplication.app.printerOpt.PrintTableText(new String[]{"Bonjour! Ça fait longtemps!","4567"},new int[]{8,2},new int[]{0,2});
                            MyApplication.app.printerOpt.PrintTableText(new String[]{"Bonjour! Ça !","4567"},new int[]{8,2},new int[]{0,2});
                            MyApplication.app.printerOpt.PrnStr("\n");
                            MyApplication.app.printerOpt.PrnStr("\n");
                            MyApplication.app.printerOpt.PrnStr("\n");
                            MyApplication.app.printerOpt.PrnStr("                                         \n");


                            SendMsg("Printing... ");
                            ret = MyApplication.app.printerOpt.PrnStart();

                            msg1.what = ENABLE_RG;
                            handler.sendMessage(msg1);

                            Log.d("Robert2", "Lib_PrnStart ret = " + ret);

                            if (ret != 0) {
                                RESULT_CODE = -1;
                                Log.e("liuhao", "Lib_PrnStart fail, ret = " + ret);
                                if (ret == -1) {
                                    SendMsg("No Print Paper ");
                                } else if(ret == -2) {
                                    SendMsg("too hot ");
                                }else if(ret == -3) {
                                    SendMsg("low voltage ");
                                }else{
                                    SendMsg("Print fail ");
                                }
                            } else {
                                RESULT_CODE = 0;
                                SendMsg("Print Finish ");
                            }
                            Log.d("Robert2", "Lib_PrnStart ret9 " );
                        }catch (RemoteException e){
                            e.printStackTrace();
                        }
                        break;
                    case PRINT_CYCLE:
                        msg.what = DISABLE_RG;
                        handler.sendMessage(msg);

                        try {
                            MyApplication.app.printerOpt.PrnSetFont((byte) 16, (byte) 16, (byte) 0x33);
                            for (int i = 1; i < 3; i++) {
                                MyApplication.app.printerOpt.PrnSetFont((byte) 24, (byte) 24, (byte) 0x33);
                                MyApplication.app.printerOpt.PrnStr("打印第：" + i + "次\n");
                                MyApplication.app.printerOpt.PrnStr("商户存根MERCHANT COPY\n");
                                MyApplication.app.printerOpt.PrnStr("- - - - - - - - - - - - - - - - - - - - - - - -\n");
                                MyApplication.app.printerOpt.PrnSetFont((byte) 24, (byte) 24, (byte) 0x00);
                                MyApplication.app.printerOpt.PrnStr("商户名称(MERCHANT NAME):\n");
                                MyApplication.app.printerOpt.PrnStr("中国银联直连测试\n");
                                MyApplication.app.printerOpt.PrnStr("商户编号(MERCHANT NO):\n");
                                MyApplication.app.printerOpt.PrnStr("    001420183990573\n");
                                MyApplication.app.printerOpt.PrnStr("终端编号(TERMINAL NO):00026715\n");
                                MyApplication.app.printerOpt.PrnStr("操作员号(OPERATOR NO):12345678\n");
                                MyApplication.app.printerOpt.PrnStr("- - - - - - - - - - - - - - - -\n");
                                MyApplication.app.printerOpt.PrnStr("发卡行(ISSUER):01020001 工商银行\n");
                                MyApplication.app.printerOpt.PrnStr("卡号(CARD NO):\n");
                                MyApplication.app.printerOpt.PrnStr("    9558803602109503920\n");
                                MyApplication.app.printerOpt.PrnStr("收单行(ACQUIRER):03050011民生银行\n");
                                MyApplication.app.printerOpt.PrnStr("交易类型(TXN. TYPE):消费/SALE\n");
                                MyApplication.app.printerOpt.PrnStr("卡有效期(EXP. DATE):2013/08\n");
                                MyApplication.app.printerOpt.PrnStr("- - - - - - - - - - - - - - - -\n");
                                MyApplication.app.printerOpt.PrnStr("批次号(BATCH NO)  :000023\n");
                                MyApplication.app.printerOpt.PrnStr("凭证号(VOUCHER NO):000018\n");
                                MyApplication.app.printerOpt.PrnStr("授权号(AUTH NO)   :987654\n");
                                MyApplication.app.printerOpt.PrnStr("日期/时间(DATE/TIME):\n");
                                MyApplication.app.printerOpt.PrnStr("    2008/01/28 16:46:32\n");
                                MyApplication.app.printerOpt.PrnStr("交易参考号(REF. NO):200801280015\n");
                                MyApplication.app.printerOpt.PrnStr("金额(AMOUNT):  RMB:2.55\n");
                                MyApplication.app.printerOpt.PrnStr("- - - - - - - - - - - - - - - -\n");
                                MyApplication.app.printerOpt.PrnStr("备注/REFERENCE\n");
                                MyApplication.app.printerOpt.PrnStr("- - - - - - - - - - - - - - - -\n");
                                MyApplication.app.printerOpt.PrnSetFont((byte) 16, (byte) 16, (byte) 0x00);
                                MyApplication.app.printerOpt.PrnStr("持卡人签名(CARDHOLDER SIGNATURE)\n");
                                MyApplication.app.printerOpt.PrnStr("\n");
                                MyApplication.app.printerOpt.PrnStr("- - - - - - - - - - - - - - - - - - - - - - - -\n");
                                MyApplication.app.printerOpt.PrnStr("  本人确认以上交易，同意将其计入本卡帐户\n");
                                MyApplication.app.printerOpt.PrnStr("  I ACKNOWLEDGE SATISFACTORY RECEIPT\n");
                                MyApplication.app.printerOpt.PrnStr("\n\n\n\n\n\n\n\n\n\n");
                                ret = MyApplication.app.printerOpt.PrnStart();
                            }

                            msg1.what = ENABLE_RG;
                            handler.sendMessage(msg1);
                            Log.d("", "Lib_PrnStart ret = " + ret);
                            if (ret != 0) {
                                RESULT_CODE = -1;
                                Log.e("liuhao", "Lib_PrnStart fail, ret = " + ret);
                                if (ret == -1) {
                                    SendMsg("No Print Paper ");
                                } else if(ret == -2) {
                                    SendMsg("too hot ");
                                }else if(ret == -3) {
                                    SendMsg("low voltage ");
                                }
                            } else {
                                RESULT_CODE = 0;
                            }
                        }catch (RemoteException e){
                            e.printStackTrace();
                        }
                        break;
                    case PRINT_UNICODE:
                        Log.d("Robert2", "Lib_PrnStart ret START11 " );
                        final long starttime = System.currentTimeMillis();
                        Log.e("Robert2", "PRINT_UNICODE starttime = " + starttime);

                        SendMsg("PRINT_UNICODE");
                        msg.what = DISABLE_RG;
                        handler.sendMessage(msg);

                        try {
/*                            MyApplication.app.printerOpt.PrnSetFont((byte) 24, (byte) 24, (byte) 0x00);

                            MyApplication.app.printerOpt.PrnStr("中文:你好，好久不见。\n");
                            MyApplication.app.printerOpt.PrnStr("英语: ￡20.00 ，￡20.00 ，￡20.00 Hello, Long time no see\n");
                            MyApplication.app.printerOpt.PrnStr("西班牙语:España, ¡Hola! Cuánto tiempo sin verte!\n");
                            MyApplication.app.printerOpt.PrnStr("法语:Bonjour! Ça fait longtemps!\n");
                            MyApplication.app.printerOpt.PrnStr("Italian :Ciao, non CI vediamo da Molto Tempo.\n");


                            SendMsg("Printing... ");
                            //ret = posApiHelper.PrintCtnStart();
                            ret = MyApplication.app.printerOpt.PrnStart();

                            MyApplication.app.printerOpt.PrnSetFont((byte) 16, (byte) 16, (byte) 0x33);*/
                            for (int i = 1; i < 3; i++) {
                                MyApplication.app.printerOpt.PrnSetFont((byte) 24, (byte) 24, (byte) 0x33);
                                MyApplication.app.printerOpt.PrnStr("打印第：" + i + "次\n");
                                MyApplication.app.printerOpt.PrnStr("商户存根MERCHANT COPY\n");
                                MyApplication.app.printerOpt.PrnStr("- - - - - - - - - - - - - - - - - - - - - - - -\n");
                                MyApplication.app.printerOpt.PrnSetFont((byte) 24, (byte) 24, (byte) 0x00);
                                MyApplication.app.printerOpt.PrnStr("商户名称(MERCHANT NAME):\n");
                                MyApplication.app.printerOpt.PrnStr("中国银联直连测试\n");
                                MyApplication.app.printerOpt.PrnStr("商户编号(MERCHANT NO):\n");
                                MyApplication.app.printerOpt.PrnStr("    001420183990573\n");
                                MyApplication.app.printerOpt.PrnStr("终端编号(TERMINAL NO):00026715\n");
                                MyApplication.app.printerOpt.PrnStr("操作员号(OPERATOR NO):12345678\n");
                                MyApplication.app.printerOpt.PrnStr("- - - - - - - - - - - - - - - -\n");
                                MyApplication.app.printerOpt.PrnStr("发卡行(ISSUER):01020001 工商银行\n");
                                MyApplication.app.printerOpt.PrnStr("卡号(CARD NO):\n");
                                MyApplication.app.printerOpt.PrnStr("    9558803602109503920\n");
                                MyApplication.app.printerOpt.PrnStr("收单行(ACQUIRER):03050011民生银行\n");
                                MyApplication.app.printerOpt.PrnStr("交易类型(TXN. TYPE):消费/SALE\n");
                                MyApplication.app.printerOpt.PrnStr("卡有效期(EXP. DATE):2013/08\n");
                                MyApplication.app.printerOpt.PrnStr("- - - - - - - - - - - - - - - -\n");
                                MyApplication.app.printerOpt.PrnStr("批次号(BATCH NO)  :000023\n");
                                MyApplication.app.printerOpt.PrnStr("凭证号(VOUCHER NO):000018\n");
                                MyApplication.app.printerOpt.PrnStr("授权号(AUTH NO)   :987654\n");
                                MyApplication.app.printerOpt.PrnStr("日期/时间(DATE/TIME):\n");
                                MyApplication.app.printerOpt.PrnStr("    2008/01/28 16:46:32\n");
                                MyApplication.app.printerOpt.PrnStr("交易参考号(REF. NO):200801280015\n");
                                MyApplication.app.printerOpt.PrnStr("金额(AMOUNT):  RMB:2.55\n");
                                MyApplication.app.printerOpt.PrnStr("- - - - - - - - - - - - - - - -\n");
                                MyApplication.app.printerOpt.PrnStr("备注/REFERENCE\n");
                                MyApplication.app.printerOpt.PrnStr("- - - - - - - - - - - - - - - -\n");
                                MyApplication.app.printerOpt.PrnSetFont((byte) 16, (byte) 16, (byte) 0x00);
                                MyApplication.app.printerOpt.PrnStr("持卡人签名(CARDHOLDER SIGNATURE)\n");
                                MyApplication.app.printerOpt.PrnStr("\n");
                                MyApplication.app.printerOpt.PrnStr("- - - - - - - - - - - - - - - - - - - - - - - -\n");
                                MyApplication.app.printerOpt.PrnStr("  本人确认以上交易，同意将其计入本卡帐户\n");
                                MyApplication.app.printerOpt.PrnStr("  I ACKNOWLEDGE SATISFACTORY RECEIPT\n");
                                MyApplication.app.printerOpt.PrnStr("\n\n\n\n\n\n\n\n\n\n");
                                ret = MyApplication.app.printerOpt.PrnStart();
                            }

                            msg1.what = ENABLE_RG;
                            handler.sendMessage(msg1);
                            Log.d("", "Lib_PrnStart ret = " + ret);
                            if (ret != 0) {
                                RESULT_CODE = -1;
                                Log.e("liuhao", "Lib_PrnStart fail, ret = " + ret);
                                if (ret == -1) {
                                    SendMsg("No Print Paper ");
                                } else if(ret == -2) {
                                    SendMsg("too hot ");
                                }else if(ret == -3) {
                                    SendMsg("low voltage ");
                                }else{
                                    SendMsg("Print fail ");
                                }
                            } else {
                                RESULT_CODE = 0;
                                SendMsg("Print Finish ");

                                final long endttime = System.currentTimeMillis();
                                Log.e("printtime", "PRINT_UNICODE endttime = " + endttime);
                                final long totaltime = starttime - endttime;
                                SendMsg("Print finish" );
                            }
                        }catch (RemoteException e){
                            e.printStackTrace();
                        }
                        break;

                    case PRINT_OPEN:
                        SendMsg("PRINT_OPEN");
                        msg.what = DISABLE_RG;
                        handler.sendMessage(msg);
                        SendMsg("Print Open... ");
                        try {
                            MyApplication.app.printerOpt.PrnStr("                                         \n");
                            ret = MyApplication.app.printerOpt.PrnStart();
                            msg1.what = ENABLE_RG;
                            handler.sendMessage(msg1);

                            Log.d("", "Lib_PrnStart ret = " + ret);
                            if (ret != 0) {
                                RESULT_CODE = -1;
                                Log.e("liuhao", "Lib_PrnStart fail, ret = " + ret);
                                if (ret == -1) {
                                    SendMsg("No Print Paper ");
                                } else if (ret == -2) {
                                    SendMsg("too hot ");
                                } else if (ret == -3) {
                                    SendMsg("low voltage ");
                                } else {
                                    SendMsg("Print fail ");
                                }
                            } else {
                                RESULT_CODE = 0;
                                SendMsg("Print Finish ");
                            }
                        }catch (RemoteException e){
                            e.printStackTrace();
                        }
                        break;
                    case PRINT_BMP:
                        SendMsg("PRINT_BMP");
                        msg.what = DISABLE_RG;
                        handler.sendMessage(msg);

                        try {
                            final long start_BmpD = System.currentTimeMillis();

                            //Bitmap bmp1 = BitmapFactory.decodeResource(PrintActivity.this.getResources(), R.mipmap.test001);
                            final long end_BmpD = System.currentTimeMillis();
                            final long decodetime = end_BmpD - start_BmpD;
                            final long start_PrintBmp = System.currentTimeMillis();
                            ret = MyApplication.app.printerOpt.PrnBmp(bmp1);
                            MyApplication.app.printerOpt.PrnStr("                                         \n");
                            if (ret == 0) {
                                MyApplication.app.printerOpt.PrnStr("\n\n\n");
                                MyApplication.app.printerOpt.PrnStr("                                         \n");
                                MyApplication.app.printerOpt.PrnStr("                                         \n");

                                SendMsg("Printing... ");
                                // ret = posApiHelper.PrintCtnStart();
                                ret = MyApplication.app.printerOpt.PrnStart();
                                msg1.what = ENABLE_RG;
                                handler.sendMessage(msg1);

                                Log.d("", "Lib_PrnStart ret = " + ret);
                                if (ret != 0) {
                                    RESULT_CODE = -1;
                                    Log.e("liuhao", "Lib_PrnStart fail, ret = " + ret);
                                    if (ret == -1) {
                                        SendMsg("No Print Paper ");
                                    } else if(ret == -2) {
                                        SendMsg("too hot ");
                                    }else if(ret == -3) {
                                        SendMsg("low voltage ");
                                    }else{
                                        SendMsg("Print fail ");
                                    }
                                } else {
                                    final long end_PrintBmp = System.currentTimeMillis();

                                    RESULT_CODE = 0;
                                    final long PrintTime = start_PrintBmp - end_PrintBmp;
                                    SendMsg("Print Finish");
                                    // SendMsg("Print Finish BMP decodetime="+decodetime + "PrintBmpTime"+PrintTime);
                                }
                            } else {
                                RESULT_CODE = -1;
                                SendMsg("Lib_PrnBmp Failed");
                            }
                        }catch (RemoteException e){
                            e.printStackTrace();
                        }
                        break;

                    case PRINT_BARCODE:
                        SendMsg("PRINT_BARCODE");
                        msg.what = DISABLE_RG;
                        handler.sendMessage(msg);
                        try {
                            content = "com.chips.ewallet.scheme://{\"PayeeMemberUuid\":\"a3d7fe8e-873d-499b-9f11-000000000000\",\"PayerMemberUuid\":null,\"TotalAmount\":\"900\",\"PayeeSiteUuid\":null,\"PayeeTransId\":\"100101-084850-6444\",\"PayeeSiteReference\":\"\",\"PayeeDescription\":null,\"ConfirmationUuid\":null,\"StpReference\":null}";
                            MyApplication.app.printerOpt.PrnStr("QR_CODE display " );
                            MyApplication.app.printerOpt.PrnBarcode(content, 360, 360, "QR_CODE");
                            MyApplication.app.printerOpt.PrnStr("PrintQrCode_Cut display " );
                            //posApiHelper.PrintQrCode_Cut(content, 360, 360, "QR_CODE");
                            MyApplication.app.printerOpt.PrintCutQrCode(content, 360, 360, "QR_CODE");
                            MyApplication.app.printerOpt.PrnStr("PrintCutQrCode_Str display " );
                            //posApiHelper.PrintCutQrCode_Str(content,"PK TXT adsad adasd sda",5, 300, 300, "QR_CODE");
                            MyApplication.app.printerOpt.PrintCutQrCodeStr(content,"PK TXT adsad adasd sda",5, 300, 300, "QR_CODE");
                            MyApplication.app.printerOpt.PrnStr("QR_CODE : " + content + "\n\n");
                            MyApplication.app.printerOpt.PrnStr("                                        \n");
                            MyApplication.app.printerOpt.PrnStr("                                        \n");

                            SendMsg("Printing... ");
                            //ret = posApiHelper.PrintCtnStart();
                            ret = MyApplication.app.printerOpt.PrnStart();
                            msg1.what = ENABLE_RG;
                            handler.sendMessage(msg1);

                            Log.d("", "Lib_PrnStart ret = " + ret);
                            if (ret != 0) {
                                RESULT_CODE = -1;

                                if (ret == -1) {
                                    SendMsg("No Print Paper ");
                                } else if(ret == -2) {
                                    SendMsg("too hot ");
                                }else if(ret == -3) {
                                    SendMsg("low voltage ");
                                }else{
                                    SendMsg("Print fail ");
                                }
                            } else {
                                RESULT_CODE = 0;
                                SendMsg("Print Finish ");
                            }
                        }catch (RemoteException e){
                            e.printStackTrace();
                        }
                        break;
                    case PRINT_LAB_SINGLE:
                        SendMsg("SINGLE");
                        msg.what = DISABLE_RG;
                        handler.sendMessage(msg);
                        SendMsg("Print SINGLE... ");
                        try {
                            MyApplication.app.printerOpt.PrnSetFont((byte) 24, (byte) 24, (byte) 0x00);

                            MyApplication.app.printerOpt.PrnStr("Alibaba taobao shop");
                            MyApplication.app.printerOpt.PrnStr("amount:30 dollars");
                            MyApplication.app.printerOpt.PrnStr("weight:30 kg");
                            MyApplication.app.printerOpt.PrnStr("中文:你好，好久不见。");
                            MyApplication.app.printerOpt.PrnStr("中文:你好，好久不见。");
                            MyApplication.app.printerOpt.PrnStr("中文:你好，好久不见。");
                            MyApplication.app.printerOpt.PrnStr("中文:你好，好久不见。");

                            MyApplication.app.printerOpt.PrnStr("中文:你好，好久不见。");
                            MyApplication.app.printerOpt.PrnStr("thank you\n");
                            MyApplication.app.printerOpt.PrnStr("\n\n\n\n");

                            ret = MyApplication.app.printerOpt.PrnStart();
                            ret = MyApplication.app.printerOpt.PrnFeedPaper(100);
                            msg1.what = ENABLE_RG;
                            handler.sendMessage(msg1);

                            Log.d("", "Lib_PrnStart ret = " + ret);
                            if (ret != 0) {
                                RESULT_CODE = -1;

                                if (ret == -1) {
                                    SendMsg("No Print Paper ");
                                } else if (ret == -2) {
                                    SendMsg("too hot ");
                                } else if (ret == -3) {
                                    SendMsg("label  fail ");
                                } else {
                                    SendMsg("Print fail ");
                                }
                            } else {
                                RESULT_CODE = 0;
                                SendMsg("Print Finish ");
                            }
                        }catch (RemoteException e){
                            e.printStackTrace();
                        }
                        break;
                    case PRINT_LAB_CONTINUE:
                        int j = 0;
                        SendMsg("continue");
                        msg.what = DISABLE_RG;
                        handler.sendMessage(msg);
                        try {
                            SendMsg("Print continue... ");
                            for(j = 0; j <3; j++)
                            {
                                MyApplication.app.printerOpt.PrnSetFont((byte) 24, (byte) 24, (byte) 0x00);
                                MyApplication.app.printerOpt.PrnStr("Shopping");
                                MyApplication.app.printerOpt.PrnStr("amount:00 dollars");
                                MyApplication.app.printerOpt.PrnStr("weight:00 kg");
                                MyApplication.app.printerOpt.PrnStr("the unit price:00");
                                MyApplication.app.printerOpt.PrnStr("time...  day...");
                                MyApplication.app.printerOpt.PrnStr("Have a nice day\n");
                                MyApplication.app.printerOpt.PrnStr("中文:你好，好久不见。");
                                MyApplication.app.printerOpt.PrnStr("中文:你好，好久不见。");
                                // ret =  posApiHelper.PrintCtnStart();
                                ret = MyApplication.app.printerOpt.PrnStart();
                                ret = MyApplication.app.printerOpt.PrnStart();
                                ret = MyApplication.app.printerOpt.PrnFeedPaper(100);
                                if(ret != 0)
                                    break;
                            }

                            msg1.what = ENABLE_RG;
                            handler.sendMessage(msg1);

                            Log.d("", "Lib_PrnStart ret = " + ret);
                            if (ret != 0) {
                                RESULT_CODE = -1;

                                if (ret == -1) {
                                    SendMsg("No Print Paper ");
                                } else if (ret == -2) {
                                    SendMsg("too hot ");
                                } else if (ret == -3) {
                                    SendMsg("label  fail ");
                                } else {
                                    SendMsg("Print fail ");
                                }
                            } else {
                                RESULT_CODE = 0;
                                SendMsg("Print Finish ");
                            }
                        }catch (RemoteException e){
                            e.printStackTrace();
                        }
                        break;
                    case PRINT_LAB_BAR:
                        msg.what = DISABLE_RG;
                        handler.sendMessage(msg);
                        try {
                            content = "www.baidu.com/123456789123456789123456789";
                            MyApplication.app.printerOpt.PrintCutQrCode(content, 200, 200, "QR_CODE");
                            SendMsg("Printing... ");
                            //ret = posApiHelper.PrintCtnStart();
                            ret = MyApplication.app.printerOpt.PrnStart();
                            ret = MyApplication.app.printerOpt.PrnFeedPaper(100);
                            msg1.what = ENABLE_RG;
                            handler.sendMessage(msg1);

                            Log.d("", "Lib_PrnStart ret = " + ret);
                            if (ret != 0) {
                                RESULT_CODE = -1;

                                if (ret == -1) {
                                    SendMsg("No Print Paper ");
                                } else if (ret == -2) {
                                    SendMsg("too hot ");
                                } else if (ret == -3) {
                                    SendMsg("label  fail");
                                } else {
                                    SendMsg("Print fail ");
                                }
                            } else {
                                RESULT_CODE = 0;
                                SendMsg("Print Finish ");
                            }
                        }catch (RemoteException e){
                            e.printStackTrace();
                        }
                        break;
                    case PRINT_LAB_BIT:
                        msg.what = DISABLE_RG;
                        handler.sendMessage(msg);
                        SendMsg("Print bitmap... ");

                        try {
                            bmp1 = BitmapFactory.decodeResource(PrintActivity.this.getResources(), R.mipmap.test001);
                            ret = MyApplication.app.printerOpt.PrnBmp(bmp1);
                            SendMsg("Printing... ");

                            ret = MyApplication.app.printerOpt.PrnStart();
                            ret = MyApplication.app.printerOpt.PrnFeedPaper(100);

                            msg1.what = ENABLE_RG;
                            handler.sendMessage(msg1);

                            Log.d("", "Lib_PrnStart ret = " + ret);
                            if (ret != 0) {
                                RESULT_CODE = -1;

                                if (ret == -1) {
                                    SendMsg("No Print Paper ");
                                } else if (ret == -2) {
                                    SendMsg("too hot ");
                                } else if (ret == -3) {
                                    SendMsg("label  fail");
                                } else {
                                    SendMsg("Print fail ");
                                }
                            } else {
                                RESULT_CODE = 0;
                                SendMsg("Print Finish ");
                            }
                        }catch (RemoteException e){
                            e.printStackTrace();
                        }
                        break;
                    default:
                        break;
                }
                m_bThreadFinished = true;
            }
        }
    }


    public class BatteryReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            voltage_level = intent.getExtras().getInt("level");
            Log.e("wbw", "current  = " + voltage_level);
            BatteryV = intent.getIntExtra("voltage", 0);  //电池电压
            Log.e("wbw", "BatteryV  = " + BatteryV);
            Log.e("wbw", "V  = " + BatteryV * 2 / 100);
            //	m_voltage = (int) (65+19*voltage_level/100); //放大十倍
            //   Log.e("wbw","m_voltage  = " + m_voltage );
        }
    }

    public void SendMsg(String strInfo) {
        Message msg = new Message();
        Bundle b = new Bundle();
        b.putString("MSG", strInfo);
        msg.setData(b);
        handler.sendMessage(msg);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case DISABLE_RG:
                    IsWorking = 1;
                    rb_high.setEnabled(false);
                    rb_middle.setEnabled(false);
                    rb_low.setEnabled(false);
                    radioButton_4.setEnabled(false);
                    radioButton_5.setEnabled(false);
                    break;

                case ENABLE_RG:
                    IsWorking = 0;
                    rb_high.setEnabled(true);
                    rb_middle.setEnabled(true);
                    rb_low.setEnabled(true);
                    radioButton_4.setEnabled(true);
                    radioButton_5.setEnabled(true);
                    break;

                case 0x34:
                    gb_single.setEnabled(false);
                    gb_continue.setEnabled(false);
                    gb_bar.setEnabled(false);
                    gb_bitm.setEnabled(false);

                    gb_single.setBackgroundColor(Color.GRAY);
                    gb_continue.setBackgroundColor(Color.GRAY);
                    gb_bar.setBackgroundColor(Color.GRAY);
                    gb_bitm.setBackgroundColor(Color.GRAY);

                    gb_test.setEnabled(true);
                    gb_unicode.setEnabled(true);
                    gb_barcode.setEnabled(true);
                    btnBmp.setEnabled(true);
                    gb_open.setEnabled(true);
                    gb_long.setEnabled(true);
                    gb_printCycle.setEnabled(true);

                    gb_test.setBackgroundColor(getResources().getColor(R.color.item_image_select));
                    gb_unicode.setBackgroundColor(getResources().getColor(R.color.item_image_select));
                    gb_barcode.setBackgroundColor(getResources().getColor(R.color.item_image_select));
                    btnBmp.setBackgroundColor(getResources().getColor(R.color.item_image_select));
                    gb_open.setBackgroundColor(getResources().getColor(R.color.item_image_select));
                    gb_long.setBackgroundColor(getResources().getColor(R.color.item_image_select));
                    gb_printCycle.setBackgroundColor(getResources().getColor(R.color.item_image_select));

                    break;

                case 0x56:
                    //gb_unicode.setVisibility(View.INVISIBLE);
                    gb_test.setBackgroundColor(Color.GRAY);
                    gb_unicode.setBackgroundColor(Color.GRAY);
                    gb_barcode.setBackgroundColor(Color.GRAY);
                    btnBmp.setBackgroundColor(Color.GRAY);
                    gb_open.setBackgroundColor(Color.GRAY);
                    gb_long.setBackgroundColor(Color.GRAY);
                    gb_printCycle.setBackgroundColor(Color.GRAY);

                    gb_test.setEnabled(false);
                    gb_unicode.setEnabled(false);
                    gb_barcode.setEnabled(false);
                    btnBmp.setEnabled(false);
                    gb_open.setEnabled(false);
                    gb_long.setEnabled(false);
                    gb_printCycle.setEnabled(false);

                    gb_single.setEnabled(true);
                    gb_continue.setEnabled(true);
                    gb_bar.setEnabled(true);
                    gb_bitm.setEnabled(true);

                    gb_single.setBackgroundColor(getResources().getColor(R.color.item_image_select));
                    gb_continue.setBackgroundColor(getResources().getColor(R.color.item_image_select));
                    gb_bar.setBackgroundColor(getResources().getColor(R.color.item_image_select));
                    gb_bitm.setBackgroundColor(getResources().getColor(R.color.item_image_select));
                    break;

                default:
                    Bundle b = msg.getData();
                    String strInfo = b.getString("MSG");
                    textViewMsg.setText(strInfo);
                    break;
            }
        }
    };

}
