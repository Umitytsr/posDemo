package test.demo.activity;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.ctk.sdk.DebugLogUtil;

import test.demo.MyApplication;

/**
 * Created by Administrator on 2017/8/17.
 */

public class PiccActivity extends Activity implements View.OnClickListener {

    static final String TAG = "NFCTEST";

    static final int TYPE_NFC = 0;
    static final int TYPE_PICC = 1;
    static final int TYPE_M1_WRITE = 2;
    static final int TYPE_M1_READ = 3;
    static final int TYPE_M1_OPERATE = 4;
    static final int TYPE_M1_WRITE_BLOCK = 5;
    static final int TYPE_M1_READ_BLOCK = 6;
    static final int TYPE_PICC_POLL = 7;
    static final int TYPE_PICC_M1_OPERATE = 8;

    byte picc_mode = 'B';
    byte picc_type = 'a';
    byte blkNo = 60;
    byte blkValue[] = new byte[20];
    byte pwd[] = new byte[20];
    byte cardtype[] = new byte[3];
    byte serialNo[] = new byte[50];
    byte dataIn[] = new byte[530];
    byte[] dataM1 = new byte[16];

    TextView textViewMsg = null,tvOpereteType = null;
    LinearLayout bankcardoperlayout;
    LinearLayout M1cardoperlayout;

    RadioButton bankcardTest,m1cardTest;
    Button btnStart, btnNfc,btnReadM1,btnWriteM1,btnOperateM1 ,btnReadM1Block ,btnWriteM1Block ,btnPiccPoll;
    EditText editBlkNo , editWriteData ;
    EditText editM1OperateData , editM1OperateBlkNo ,editM1OperateUpdateNo ;
    RadioGroup rg_operate = null;
    RadioButton rb_add ,rb_equal,rb_Subtraction;

    String strBlkNo = "" , strWriteData = "";

    static byte m1OpereteType = (byte)'+';

    private boolean bIsBack = false;

    IFinishCall iFinishCall;
    interface IFinishCall{
        void isFinish(boolean bIsFinish);
    }

    void setIFinishCall(IFinishCall iFinishCall){
        this.iFinishCall = iFinishCall;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //无title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_picc);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        textViewMsg = (TextView) this.findViewById(R.id.textView_picc);
        bankcardTest = (RadioButton)this.findViewById(R.id.bankcard);
        m1cardTest = (RadioButton)this.findViewById(R.id.M1Card);

        bankcardoperlayout = (LinearLayout)this.findViewById(R.id.bankcard_operation);
        M1cardoperlayout = (LinearLayout)this.findViewById(R.id.m1_operation);

        bankcardTest.setOnCheckedChangeListener(null);
        m1cardTest.setOnCheckedChangeListener(null);

        bankcardTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"bankcardTest: "+ bankcardTest.isChecked());
                Log.d(TAG,"m1cardTest: "+ m1cardTest.isChecked());
                if(bankcardTest.isChecked()){
                    bankcardTest.setChecked(true);
                    m1cardTest.setChecked(false);
                    bankcardoperlayout.setVisibility(View.VISIBLE);
                    M1cardoperlayout.setVisibility(View.GONE);
                    initBankCardView();
                }else{
                    bankcardTest.setChecked(false);
                    m1cardTest.setChecked(true);
                    bankcardoperlayout.setVisibility(View.GONE);
                    M1cardoperlayout.setVisibility(View.VISIBLE);
                    initM1CardView();
                }
            }
        });
        m1cardTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"bankcardTest: "+ bankcardTest.isChecked());
                Log.d(TAG,"m1cardTest: "+ m1cardTest.isChecked());
                if(m1cardTest.isChecked()){
                    bankcardTest.setChecked(false);
                    m1cardTest.setChecked(true);
                    bankcardoperlayout.setVisibility(View.GONE);
                    M1cardoperlayout.setVisibility(View.VISIBLE);
                    initM1CardView();
                }else{
                    bankcardTest.setChecked(true);
                    m1cardTest.setChecked(false);
                    bankcardoperlayout.setVisibility(View.VISIBLE);
                    M1cardoperlayout.setVisibility(View.GONE);
                    initBankCardView();
                }
            }
        });

        //主动获取焦点
        textViewMsg.requestFocus();
    }

    private void initBankCardView(){
        btnNfc = (Button) findViewById(R.id.btnNfc);
        btnPiccPoll =(Button) findViewById(R.id.btnPiccTest);

        btnNfc.setOnClickListener(this);
        btnPiccPoll.setOnClickListener(this);

    }
    private void initM1CardView(){
        btnReadM1 = (Button) findViewById(R.id.btnReadM1);
        btnWriteM1 =(Button) findViewById(R.id.btnWriteM1);
        btnReadM1.setOnClickListener(this);
        btnWriteM1.setOnClickListener(this);

        btnReadM1Block =(Button) findViewById(R.id.btnReadM1Block);
        btnWriteM1Block =(Button) findViewById(R.id.btnWriteM1Block);
        btnReadM1Block.setOnClickListener(this);
        btnWriteM1Block.setOnClickListener(this);

        btnOperateM1 =(Button) findViewById(R.id.btnOperateM1);
        btnOperateM1.setOnClickListener(this);

        editBlkNo  = (EditText) findViewById(R.id.editBlkNo);
        editWriteData  = (EditText) findViewById(R.id.editWriteData);

        editM1OperateData  = (EditText) findViewById(R.id.editM1OperateData);
        editM1OperateBlkNo  = (EditText) findViewById(R.id.editM1OperateBlkNo);
        editM1OperateUpdateNo  = (EditText) findViewById(R.id.editM1OperateUpdateNo);

        btnStart = (Button)findViewById(R.id.btnM1Check);
        btnStart.setOnClickListener(this);

        tvOpereteType = (TextView)findViewById(R.id.tvOpereteType);

        rg_operate = (RadioGroup) findViewById(R.id.rg_operate);
        rb_add= (RadioButton) findViewById(R.id.rb_add);
        rb_add.setChecked(true);
        rb_Subtraction= (RadioButton) findViewById(R.id.rb_Subtraction);
        rb_equal= (RadioButton) findViewById(R.id.rb_equal);

        rg_operate.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.rb_add:
                        m1OpereteType = (byte)'+';
                        tvOpereteType.setText(" + ");
                        break;
                    case R.id.rb_Subtraction:
                        m1OpereteType = (byte)'-';
                        tvOpereteType.setText(" - ");
                        break;
                    case R.id.rb_equal:
                        m1OpereteType = (byte)'=';
                        tvOpereteType.setText(" = ");
                        break;
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    protected void onPause() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onPause();
//        isQuit = true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        try {
            MyApplication.app.syscardOpt.sysPiccClose();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        bIsBack = true;
    }

    public int readNfcCard() {

        synchronized (this) {
            Log.e(TAG, "heyp nfc Picc_Open start!");
            byte[] NfcData_Len = new byte[5];
            final byte[] Technology = new byte[25];
            byte[] NFC_UID = new byte[56];
            byte[] NDEF_message = new byte[500];

            try {
                int ret = MyApplication.app.syscardOpt.sysNfc(NfcData_Len, Technology, NFC_UID, NDEF_message);

                final int TechnologyLength = NfcData_Len[0] & 0xFF;
                int NFC_UID_length = NfcData_Len[1] & 0xFF;
                int NDEF_message_length = (NfcData_Len[3] & 0xFF) + (NfcData_Len[4] & 0xFF);
                byte[] NDEF_message_data = new byte[NDEF_message_length];
                byte[] NFC_UID_data = new byte[NFC_UID_length];
                System.arraycopy(NFC_UID, 0, NFC_UID_data, 0, NFC_UID_length);
                System.arraycopy(NDEF_message, 0, NDEF_message_data, 0, NDEF_message_length);
                String NDEF_message_data_str = new String(NDEF_message_data);
                String NDEF_str = null;
                if (!TextUtils.isEmpty(NDEF_message_data_str)) {
                    NDEF_str = NDEF_message_data_str.substring(NDEF_message_data_str.indexOf("en") + 2, NDEF_message_data_str.length());
                }
                if (ret == 0) {
                    MyApplication.app.basicOpt.SpBeep();
                    if (!TextUtils.isEmpty(NDEF_str)) {

                        final String tmpStr = "TYPE: " + new String(Technology).substring(0, TechnologyLength) + "\n"
                                + "UID: " + ByteUtil.bytearrayToHexString(NFC_UID_data, NFC_UID_data.length) + "\n"
                                + NDEF_str;
                        runOnUiThread(new Runnable() {
                            public void run() {
                                textViewMsg.setText(tmpStr);
                            }
                        });
                    } else {
                        final String str="TYPE: " + new String(Technology).substring(0, TechnologyLength) + "\n"
                                + "UID: " + ByteUtil.bytearrayToHexString(NFC_UID_data, NFC_UID_data.length) + "\n"
                                + NDEF_str;

                        runOnUiThread(new Runnable() {
                            public void run() {textViewMsg.setText(str);}
                        });
                    }
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            textViewMsg.setText("Read Card Failed !..");
                            return;
                        }
                    });
                }
                m_bThreadFinished = true;
                return ret;
            }catch (RemoteException e){
                e.printStackTrace();
            }
           return 0;
        }
    }

    public int ByteArrayToInt(byte[] bArr) {
        return bArr.length != 4 ? -1 : (bArr[3] & 255) << 24 | (bArr[2] & 255) << 16 | (bArr[1] & 255) << 8 | (bArr[0] & 255) << 0;
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnNfc:
                if (null != piccThread && !piccThread.isThreadFinished()) {
                    Log.e(TAG,"onClickNfc");
                    return;
                }
                piccThread = new PICC_Thread(TYPE_PICC);
                piccThread.start();
                break;
            case R.id.btnPiccTest:
                if (null != piccThread && !piccThread.isThreadFinished()) {
                    Log.e(TAG,"onClickTest");
                    return;
                }
                piccThread = new PICC_Thread(TYPE_NFC);
                piccThread.start();
                break;
            case R.id.btnM1Check:
                if (null != piccThread && !piccThread.isThreadFinished()) {
                    Log.e(TAG,"onClickM1Test");
                    return;
                }
                piccThread = new PICC_Thread(TYPE_NFC);
                piccThread.start();
                break;
            case R.id.btnWriteM1:
                if (null != piccThread && !piccThread.isThreadFinished()) {
                    Log.e(TAG,"onClickWriteM1");
                    return;
                }
                piccThread = new PICC_Thread(TYPE_M1_WRITE);
                piccThread.start();
                break;
            case R.id.btnReadM1:
                if (null != piccThread && !piccThread.isThreadFinished()) {
                    Log.e(TAG,"onClickReadM1");
                    return;
                }
                piccThread = new PICC_Thread(TYPE_M1_READ);
                piccThread.start();
                break;
            case R.id.btnOperateM1:
                if (null != piccThread && !piccThread.isThreadFinished()) {
                    Log.e(TAG,"onClickReadM1");
                    return;
                }
                piccThread = new PICC_Thread(TYPE_M1_OPERATE);
                piccThread.start();
                break;
            case R.id.btnReadM1Block:
                if (null != piccThread && !piccThread.isThreadFinished()) {
                    Log.e(TAG,"onClickReadM1 Block");
                    return;
                }
                piccThread = new PICC_Thread(TYPE_M1_READ_BLOCK);
                piccThread.start();
                break;
            case R.id.btnWriteM1Block:
                if (null != piccThread && !piccThread.isThreadFinished()) {
                    Log.e(TAG,"onClickWriteM1 Block");
                    return;
                }
                piccThread = new PICC_Thread(TYPE_M1_WRITE_BLOCK);
                piccThread.start();
                break;
        }
    }

    PICC_Thread piccThread = null;
    private boolean m_bThreadFinished = false;

    public class PICC_Thread extends Thread {

        int type;
        int ret;

        public PICC_Thread(int type) {

            this.type = type;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    textViewMsg.setText("");
                }
            });
        }

        public boolean isThreadFinished() {
            return m_bThreadFinished;
        }

        public void run() {

            synchronized (this) {
                m_bThreadFinished = false;

                switch (type){
                    case TYPE_NFC:
                        try {
                            ret = MyApplication.app.syscardOpt.sysPiccOpen();

                            final byte PCardType[]= new byte[4];
                            final byte PMode ='A';
                            final byte PserialNo[] = new byte[40];

                            if (ret == 0){
                                long time = System.currentTimeMillis();
                                while (System.currentTimeMillis() < time + 10000) {

                                    if (bIsBack) {
                                        Log.e(TAG, "*****************loop bIsBack true");
                                        m_bThreadFinished = true;
                                        break;
                                    }
                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            textViewMsg.setText(getResources().getString(R.string.wait_time));
                                        }
                                    });
                                    Log.e(TAG, "NFC = " + System.currentTimeMillis());
                                    ret = MyApplication.app.syscardOpt.sysPiccCheck(PMode,PCardType, PserialNo);
                                    if(ret == 0){
                                        runOnUiThread(new Runnable() {
                                            public void run() {
                                                try {
                                                    MyApplication.app.basicOpt.SpBeep();
                                                }catch (RemoteException e){
                                                    e.printStackTrace();
                                                }

                                                textViewMsg.setText("PiccCheck() test ok\n "+
                                                        "\nserialNo : " + ByteUtil.bytearrayToHexString(PserialNo, 4)
                                                );
                                            }
                                        });
                                        m_bThreadFinished = true;
                                        break;
                                    }else{
                                        runOnUiThread(new Runnable() {public void run() {textViewMsg.setText("Picc Check Test Failed...");}});
                                    }
                                }

                            } else {
                                m_bThreadFinished = true;
                                break;
                            }
                        }catch (RemoteException e){
                            e.printStackTrace();
                        }
                        break;
                    case TYPE_PICC:
                        try {
                            ret = MyApplication.app.syscardOpt.sysPiccOpen();
                            if (0 != ret) {
                                runOnUiThread(new Runnable() {
                                    public void run() {textViewMsg.setText("Picc_Open Error");}
                                });
                                Log.e(TAG, "Picc_Open error!");
                                return;
                            }
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    textViewMsg.setText("Checking card...");
                                }
                            });
                            boolean bPICCCheck = false;
                            long curtime = System.currentTimeMillis();
                            while (System.currentTimeMillis() < curtime + 10000) {

                                if (bIsBack) {
                                    Log.e(TAG, "*****************loop bIsBack true");
                                    m_bThreadFinished = true;
                                    break;
                                }
                                Log.e(TAG, "NFC = " + System.currentTimeMillis());
                                ret = MyApplication.app.syscardOpt.sysPiccCheck(picc_mode, cardtype, serialNo);
                                if(ret == 0){
                                    Log.e(TAG, "Picc_Check succeed!");
                                    bPICCCheck = true;
                                    break;
                                }
                            }

                            if (bPICCCheck) {
                                if ('M' == picc_mode) {
                                    pwd[0] = (byte) 0xff;
                                    pwd[1] = (byte) 0xff;
                                    pwd[2] = (byte) 0xff;
                                    pwd[3] = (byte) 0xff;
                                    pwd[4] = (byte) 0xff;
                                    pwd[5] = (byte) 0xff;
                                    pwd[6] = (byte) 0x00;

                                    picc_type = 'A';
                                    ret = MyApplication.app.syscardOpt.sysPiccM1Authority(picc_type, blkNo, pwd, serialNo);
                                    if (0 == ret) {
                                        runOnUiThread(new Runnable() {
                                            public void run() {textViewMsg.setText("Picc_M1Authority Succeed");}
                                        });

                                        blkValue[0] = (byte) 0x22;
                                        blkValue[1] = (byte) 0x00;
                                        blkValue[2] = (byte) 0x00;
                                        blkValue[3] = (byte) 0x00;
                                        blkValue[4] = (byte) 0xbb;
                                        blkValue[5] = (byte) 0xff;
                                        blkValue[6] = (byte) 0xff;
                                        blkValue[7] = (byte) 0xff;
                                        blkValue[8] = (byte) 0x44;
                                        blkValue[9] = (byte) 0x00;
                                        blkValue[10] = (byte) 0x00;
                                        blkValue[11] = (byte) 0x00;
                                        blkValue[12] = (byte) blkNo;
                                        blkValue[13] = (byte) ~blkNo;
                                        blkValue[14] = (byte) blkNo;
                                        blkValue[15] = (byte) ~blkNo;
                                        //ret = posApiHelper.PiccM1ReadBlock(blkNo, blkValue);
                                        ret = MyApplication.app.syscardOpt.sysPiccM1ReadBlock(blkNo,blkValue);
                                        if (0 == ret) {
                                            //ret = posApiHelper.PiccM1ReadBlock(blkNo, blkValue);
                                            Log.e(TAG, "ret = " + ret + ",  blkValue = " + blkValue.toString());
                                            runOnUiThread(new Runnable() {
                                                public void run() {
                                                    try {
                                                        MyApplication.app.basicOpt.SpBeep();
                                                    }catch (RemoteException e){
                                                        e.printStackTrace();
                                                    }
                                                    textViewMsg.setText("Picc_M1WriteBlock read blkValue :" + ByteUtil.bytearrayToHexString(blkValue, 20));
                                                }
                                            });
                                            //posApiHelper.SysBeep();
                                            MyApplication.app.basicOpt.SpBeep();
                                        } else {
                                            runOnUiThread(new Runnable() {
                                                public void run() {textViewMsg.setText("Picc_M1WriteBlock Error    return " + ret);}
                                            });
                                        }
                                    } else {
                                        runOnUiThread(new Runnable() {
                                            public void run() {textViewMsg.setText("Picc_M1Authority Error    return " + ret);}
                                        });
                                    }
                                } else if('A' == picc_mode){
                                    byte cmd[] = new byte[4];
                                    cmd[0] = 0x00;            //0-3 cmd
                                    cmd[1] = (byte) 0x84;
                                    cmd[2] = 0x00;
                                    cmd[3] = 0x00;
                                    short lc = 0x00;
                                    short le = 0x08;
                                    dataIn = "1PAY.SYS.DDF01".getBytes();
                                    ApduSend ApduSend = new ApduSend(cmd, lc, dataIn, le);
                                    ApduResp ApduResp = null;
                                    byte[] resp = new byte[516];

                                    ret = MyApplication.app.syscardOpt.sysPiccCommand(ApduSend.getBytes(),resp);
                                    if (0 == ret) {
                                        //  Lib_Beep();
                                        String strInfo = "";
                                        ApduResp = new ApduResp(resp);
                                        strInfo = ByteUtil.bytearrayToHexString(ApduResp.DataOut, ApduResp.LenOut) + "SWA:" + ByteUtil.byteToHexString(ApduResp.SWA) + " SWB:" + ByteUtil.byteToHexString(ApduResp.SWB);
                                        final String finalStrInfo = strInfo;
                                        runOnUiThread(new Runnable() {
                                            public void run() {
                                                try {
                                                    MyApplication.app.basicOpt.SpBeep();
                                                }catch (RemoteException e){
                                                    e.printStackTrace();
                                                }
                                                textViewMsg.setText(finalStrInfo);
                                            }
                                        });
                                    } else {
                                        runOnUiThread(new Runnable() {
                                            public void run() {textViewMsg.setText("Picc_Command Error    return " + ret);}
                                        });
                                        Log.e(TAG, "Picc_Command failed! return " + ret);
                                    }
                                }else{
                                        byte sendCmd[] = new byte[48];
                                        byte recvBuf[] = new byte[128];
                                        int sendBufLen = 0;
                                        int recvBufLen[] = new int[2];
                                        dataIn = "1TIC.ICA".getBytes();

                                        sendCmd[0] = 0x00;            //0-3 cmd
                                        sendCmd[1] = (byte) 0xA4;
                                        sendCmd[2] = (byte) 0x04;
                                        sendCmd[3] = (byte) 0x00;
                                        sendCmd[4] = (byte) dataIn.length;
                                        System.arraycopy(dataIn,0,sendCmd,5,(byte)dataIn.length);
                                        sendBufLen = 5 + dataIn.length;
                                        //sendCmd[sendBufLen++] = 0x00;

                                        Log.d("apdu-send:",ByteUtil.bytearrayToHexString(sendCmd,sendBufLen));
                                        ret = MyApplication.app.syscardOpt.sysPiccApduCmd(sendCmd,sendBufLen,recvBuf,recvBufLen);
                                        if (0 == ret) {
                                            try {
                                                MyApplication.app.basicOpt.SpBeep();
                                            }catch (RemoteException e){
                                                e.printStackTrace();
                                            }
                                            String strInfo = "";
                                            //ApduResp = new ApduResp(resp);
                                            Log.d(TAG,"recvBufLen: " + recvBufLen[0]);
                                            strInfo = ByteUtil.bytearrayToHexString(recvBuf, recvBufLen[0]) ;
                                            Log.d(TAG,"apdu-recv:"+ strInfo);
                                            final String finalStrInfo = strInfo;
                                            runOnUiThread(new Runnable() {
                                                public void run() {textViewMsg.setText(finalStrInfo);}
                                            });
                                        } else {
                                            runOnUiThread(new Runnable() {
                                                public void run() {textViewMsg.setText("Picc_Command Error    return " + ret);}
                                            });
                                            Log.e(TAG, "Picc_Command failed! return " + ret);
                                        }
                                }
                            } else {
                                runOnUiThread(new Runnable() {
                                    public void run() {textViewMsg.setText(" Looking for cards ");}
                                });
                                Log.e(TAG, "Time Out!");
                            }
                            //MyApplication.app.syscardOpt.sysPiccClose();
                            Log.e(TAG, "posApiHelperPiccClose()!");
                        }catch (RemoteException e){
                            e.printStackTrace();
                        }
                        break;
                    case TYPE_M1_READ:
                        try {
                            ret = MyApplication.app.syscardOpt.sysPiccOpen();
                            if (0 != ret) {
                                runOnUiThread(new Runnable() {
                                    public void run() {textViewMsg.setText("Picc_Open Error");}
                                });
                                Log.e(TAG, "Picc_Open error!");
                                break;
                            }
                        }catch (RemoteException e){
                            e.printStackTrace();
                        }

                        strBlkNo = editBlkNo.getText().toString().trim();
                        if(strBlkNo == null || strBlkNo.length() < 1){
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    textViewMsg.setText(getResources().getString(R.string.blockTips));
                                    Toast.makeText(PiccActivity.this,getResources().getString(R.string.blockTips),Toast.LENGTH_SHORT).show();
                                }
                            });
                            m_bThreadFinished = true;
                            break;
                        }

                        blkNo = (byte) Integer.parseInt(strBlkNo);

                        pwd[0] = (byte) 0xff;
                        pwd[1] = (byte) 0xff;
                        pwd[2] = (byte) 0xff;
                        pwd[3] = (byte) 0xff;
                        pwd[4] = (byte) 0xff;
                        pwd[5] = (byte) 0xff;
                        pwd[6] = (byte) 0x00;
                        picc_type = 'A';

                        try {
                            ret = MyApplication.app.syscardOpt.sysPiccM1Authority(picc_type, blkNo, pwd, serialNo);
                            if(ret != 0){
                                runOnUiThread(new Runnable() {
                                    public void run() { textViewMsg.setText("M1 Read failed~\n Authority -- ret = " +ret); }
                                });
                                m_bThreadFinished = true;
                                break;
                            }

                            ret = MyApplication.app.syscardOpt.sysPiccM1ReadValue(Integer.parseInt(strBlkNo) ,dataM1);
                            if(ret == 0){
                                runOnUiThread(new Runnable() {
                                    public void run() { textViewMsg.setText("M1 Read Success~\n" + ByteUtil.bytearrayToHexString(dataM1,4)); }
                                });
                            }else {
                                runOnUiThread(new Runnable() {
                                    public void run() { textViewMsg.setText("M1 Read failed~ \nret = " + ret); }
                                });
                            }
                        }catch (RemoteException e){
                            e.printStackTrace();
                        }

                        break;

                    case TYPE_M1_WRITE:
                        try {
                            ret = MyApplication.app.syscardOpt.sysPiccOpen();
                            if (0 != ret) {
                                runOnUiThread(new Runnable() {
                                    public void run() {textViewMsg.setText("Picc_Open Error");}
                                });
                                Log.e(TAG, "Picc_Open error!");
                                break;
                            }
                        }catch (RemoteException e){
                            e.printStackTrace();
                        }
                        strBlkNo = editBlkNo.getText().toString().trim();
                        strWriteData = editWriteData.getText().toString().trim();
                        if(strBlkNo == null || strBlkNo.length() < 1 || strWriteData.length() < 1 ){
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    textViewMsg.setText(getResources().getString(R.string.blockTips) + "\nand " + getResources().getString(R.string.writeTips) );
                                    Toast.makeText(PiccActivity.this,getResources().getString(R.string.blockTips) + "and " + getResources().getString(R.string.writeTips),Toast.LENGTH_SHORT).show();
                                }
                            });
                            m_bThreadFinished = true;
                            break;
                        }
                        blkNo = (byte) Integer.parseInt(strBlkNo);

                        pwd[0] = (byte) 0xff;
                        pwd[1] = (byte) 0xff;
                        pwd[2] = (byte) 0xff;
                        pwd[3] = (byte) 0xff;
                        pwd[4] = (byte) 0xff;
                        pwd[5] = (byte) 0xff;
                        pwd[6] = (byte) 0x00;
                        picc_type = 'A';
                        try {
                            ret = MyApplication.app.syscardOpt.sysPiccM1Authority(picc_type,blkNo, pwd, serialNo);
                            if(ret != 0){
                                runOnUiThread(new Runnable() {
                                    public void run() { textViewMsg.setText("M1 Write failed~\n Authority -- ret = " +ret); }
                                });
                                m_bThreadFinished = true;
                                break;
                            }

                            ret = MyApplication.app.syscardOpt.sysPiccM1WriteValue(Integer.parseInt(strBlkNo) ,strWriteData.getBytes());
                            if(ret == 0){
                                runOnUiThread(new Runnable() {
                                    public void run() { textViewMsg.setText("M1 Write Success~\n"); }
                                });
                            }else {
                                runOnUiThread(new Runnable() {
                                    public void run() { textViewMsg.setText("M1 Write failed~ \nret = " + ret); }
                                });
                                m_bThreadFinished = true;
                                break;
                            }
                        }catch (RemoteException e){
                            e.printStackTrace();
                        }
                        break;

                    case TYPE_M1_READ_BLOCK:
                        try {
                            ret = MyApplication.app.syscardOpt.sysPiccOpen();
                            if (0 != ret) {
                                runOnUiThread(new Runnable() {
                                    public void run() {textViewMsg.setText("Picc_Open Error");}
                                });
                                Log.e(TAG, "Picc_Open error!");
                                break;
                            }
                        }catch (RemoteException e){
                            e.printStackTrace();
                        }
                        strBlkNo = editBlkNo.getText().toString().trim();
                        if(strBlkNo == null || strBlkNo.length() < 1){
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    textViewMsg.setText(getResources().getString(R.string.blockTips));
                                    Toast.makeText(PiccActivity.this,getResources().getString(R.string.blockTips),Toast.LENGTH_SHORT).show();
                                }
                            });
                            m_bThreadFinished = true;
                            break;
                        }

                        blkNo = (byte) Integer.parseInt(strBlkNo);

                        pwd[0] = (byte) 0xff;
                        pwd[1] = (byte) 0xff;
                        pwd[2] = (byte) 0xff;
                        pwd[3] = (byte) 0xff;
                        pwd[4] = (byte) 0xff;
                        pwd[5] = (byte) 0xff;
                        pwd[6] = (byte) 0x00;
                        picc_type = 'A';
                        try {
                            ret = MyApplication.app.syscardOpt.sysPiccM1Authority(picc_type, blkNo, pwd, serialNo);
                            if(ret != 0){
                                runOnUiThread(new Runnable() {
                                    public void run() { textViewMsg.setText("M1 Read failed~\n Authority -- ret = " +ret); }
                                });
                                m_bThreadFinished = true;
                                break;
                            }

                            ret = MyApplication.app.syscardOpt.sysPiccM1ReadBlock((byte)Integer.parseInt(strBlkNo) ,dataM1);
                            if(ret == 0){
                                runOnUiThread(new Runnable() {
                                    public void run() { textViewMsg.setText("M1 Read Block Success~\n" + ByteUtil.bytearrayToHexString(dataM1,16)); }
                                });
                            }else {
                                runOnUiThread(new Runnable() {
                                    public void run() { textViewMsg.setText("M1 Read Block failed~ \nret = " + ret); }
                                });
                            }
                        }catch (RemoteException e){
                            e.printStackTrace();
                        }

                        break;

                    case TYPE_M1_WRITE_BLOCK:
                        try {
                            ret = MyApplication.app.syscardOpt.sysPiccOpen();
                            if (0 != ret) {
                                runOnUiThread(new Runnable() {
                                    public void run() {textViewMsg.setText("Picc_Open Error");}
                                });
                                Log.e(TAG, "Picc_Open error!");
                                break;
                            }
                        }catch (RemoteException e){
                            e.printStackTrace();
                        }
                        strBlkNo = editBlkNo.getText().toString().trim();
                        strWriteData = editWriteData.getText().toString().trim();
                        if(strBlkNo == null || strBlkNo.length() < 1 || strWriteData.length() < 1 ){
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    textViewMsg.setText(getResources().getString(R.string.blockTips) + "\nand " + getResources().getString(R.string.writeTips) );
                                    Toast.makeText(PiccActivity.this,getResources().getString(R.string.blockTips) + "and " + getResources().getString(R.string.writeTips),Toast.LENGTH_SHORT).show();
                                }
                            });
                            m_bThreadFinished = true;
                            break;
                        }

                        blkNo = (byte) Integer.parseInt(strBlkNo);

                        pwd[0] = (byte) 0xff;
                        pwd[1] = (byte) 0xff;
                        pwd[2] = (byte) 0xff;
                        pwd[3] = (byte) 0xff;
                        pwd[4] = (byte) 0xff;
                        pwd[5] = (byte) 0xff;
                        pwd[6] = (byte) 0x00;
                        picc_type = 'A';
                        try {
                            ret = MyApplication.app.syscardOpt.sysPiccM1Authority(picc_type, blkNo, pwd, serialNo);
                            if(ret != 0){
                                runOnUiThread(new Runnable() {
                                    public void run() { textViewMsg.setText("M1 Write Block failed~\n Authority -- ret = " +ret); }
                                });
                                m_bThreadFinished = true;
                                break;
                            }
                            ret = MyApplication.app.syscardOpt.sysPiccM1WriteBlock((byte)Integer.parseInt(strBlkNo) ,strWriteData.getBytes());
                            if(ret == 0){
                                runOnUiThread(new Runnable() {
                                    public void run() { textViewMsg.setText("M1 Write Block Success~\n"); }
                                });
                            }else {
                                runOnUiThread(new Runnable() {
                                    public void run() { textViewMsg.setText("M1 Write Block failed~ \nret = " + ret); }
                                });
                                m_bThreadFinished = true;
                                break;
                            }
                        }catch (RemoteException e){
                            e.printStackTrace();
                        }
                        break;

                    case TYPE_M1_OPERATE:
                        try {
                            ret = MyApplication.app.syscardOpt.sysPiccOpen();
                            DebugLogUtil.e(TAG,"sysPiccOpen: "+ ret);
                            if (0 != ret) {
                                runOnUiThread(new Runnable() {
                                    public void run() {textViewMsg.setText("Picc_Open Error");}
                                });
                                Log.e(TAG, "Picc_Open error!");
                                break;
                            }
                        }catch (RemoteException e){
                            e.printStackTrace();
                        }
                        if((editM1OperateBlkNo.getText().toString().trim().length()<1)
                                ||(editM1OperateUpdateNo.getText().toString().trim().length()<1)
                                ||(editM1OperateData.getText().toString().trim().length()<1)){
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    textViewMsg.setText("M1 Operate failed~\n Please Input start blkNO and update blkNO~"); }
                            });
                            m_bThreadFinished = true;
                            break;
                        }
                        strBlkNo = editM1OperateBlkNo.getText().toString().trim();

                        Log.e(TAG,"strBlkNo: " + strBlkNo);
                        blkNo = (byte) Integer.parseInt(strBlkNo);
                        //blkNo = 16;

                        pwd[0] = (byte) 0xff;
                        pwd[1] = (byte) 0xff;
                        pwd[2] = (byte) 0xff;
                        pwd[3] = (byte) 0xff;
                        pwd[4] = (byte) 0xff;
                        pwd[5] = (byte) 0xff;
                        pwd[6] = (byte) 0x00;
                        picc_type = 'A';
                        try {
                            ret = MyApplication.app.syscardOpt.sysPiccM1Authority(picc_type, blkNo, pwd, serialNo);
                            DebugLogUtil.d(TAG,"sysPiccM1Authority: "+ ret);
                            if(ret != 0){
                                runOnUiThread(new Runnable() {
                                    public void run() { textViewMsg.setText("M1 Operate Authority failed~\n Authority -- ret = " +ret); }
                                });
                                m_bThreadFinished = true;
                                break;
                            }
                            ret = MyApplication.app.syscardOpt.sysPiccM1Operate(m1OpereteType,(byte)Integer.parseInt(editM1OperateBlkNo.getText().toString().trim()),
                                    editM1OperateData.getText().toString().trim().getBytes(),(byte)Integer.parseInt(editM1OperateUpdateNo.getText().toString().trim()));

                            if(ret!=0){
                                runOnUiThread(new Runnable() {
                                    public void run() { textViewMsg.setText("M1 Operate Operate failed~\n Operate -- ret = " +ret); }
                                });
                                m_bThreadFinished = true;
                                break;
                            }else{
                                runOnUiThread(new Runnable() {
                                    public void run() { textViewMsg.setText("M1 Operate Operate success\n Operate -- ret = " +ret); }
                                });
                                m_bThreadFinished = true;
                            }
                        }catch (RemoteException e){
                            e.printStackTrace();
                        }
                        break;
                    case TYPE_PICC_POLL:
                        try {
                            ret = MyApplication.app.syscardOpt.sysPiccOpen();

                            final byte CardType[]= new byte[4];
                            final byte UID[] =new byte[10];
                            final byte ucUIDLen[] = new byte[1];
                            final byte ATS[] = new byte[40];
                            final byte ucATSLen[] = new byte[1];
                            final byte SAK[] = new byte[1];

                            if (ret == 0){
                                long time = System.currentTimeMillis();
                                while (System.currentTimeMillis() < time + 10000) {

                                    if (bIsBack) {
                                        Log.e(TAG, "*****************loop bIsBack true");
                                        m_bThreadFinished = true;
                                        break;
                                    }

                                    Log.e(TAG, "NFC = " + System.currentTimeMillis());

                                    ret = MyApplication.app.syscardOpt.sysPiccPolling(CardType, UID, ucUIDLen, ATS, ucATSLen, SAK);

                                    if(ret == 0){

                                        textViewMsg.setText("CardType :" + new String(CardType)
                                                + "\nUID : " + ByteUtil.bytearrayToHexString(UID, ucUIDLen[0])
                                                + "\nATS :" + ByteUtil.bytearrayToHexString(ATS, ucATSLen[0])
                                                + "\nSAK :" + ByteUtil.bytearrayToHexString(SAK, 1)
                                        );

                                        m_bThreadFinished = true;

                                        break;
                                    }else{
                                        textViewMsg.setText("Picc Poll Test ...");
                                    }
                                }
                                if(ret !=0){
                                    textViewMsg.setText("Picc Poll Test failed");
                                }
                                m_bThreadFinished = true;

                            } else {
                                m_bThreadFinished = true;
                                return;
                            }
                        }catch (RemoteException e){
                            e.printStackTrace();
                        }


                        break;
                }

               if(MyApplication.app.getSpNfc() == false){
                    closeReader();
                }

                m_bThreadFinished = true;
            }
        }
    }

    private void closeReader(){
        try {
           int  ret = MyApplication.app.syscardOpt.sysPiccClose();
            if (0 != ret) {
                runOnUiThread(new Runnable() {
                    public void run() {textViewMsg.setText("Picc_close Error");}
                });
                DebugLogUtil.e(TAG, "Picc_close failed!\n ret =  "+ ret);
            }
        }catch (RemoteException e){
            e.printStackTrace();
        }
    }

}
