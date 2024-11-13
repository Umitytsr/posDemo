package test.demo.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import test.demo.MyApplication;


/**
 * Created by Administrator on 2021/03/17.
 */


public class IccActivity extends Activity implements View.OnClickListener {

    private final String TAG = "IccActivity";
    private boolean isIccChecked = false;
    private boolean isPsam1Checked =false;
    private boolean isPsam2Checked =false;
    private boolean isPsam3Checked =false;
    private boolean isPsam4Checked =false;
    private boolean isPsam5Checked = false;
    private boolean isPsam6Checked = false;

    private boolean isAt24Checked = false;
    private boolean is4442Checked = false;
    private boolean is4428Checked = false;

    private boolean isSelected = false;

    private RadioGroup cardTypeRadioGroup = null;
    private Spinner itemSpinner = null;
    private RadioGroup CardTypeRadioGroup1 = null;
    private RadioButton radioButtonIccCard = null;
    private RadioButton radioButtonPsam1 = null;
    private RadioButton radioButtonPsam2 = null;
    private Button TestButton = null;

    private byte ATR[] = new byte[40];
    private byte vcc_mode = 1;
    private int ret;

    private WorkHandler mWorkHandler;
    private HandlerThread mWorkThread;

    TextView tv_msg = null;

    private static byte CARD_SLOT_ICC = 0;
    private static byte CARD_SLOT_PSAM1 = 1;
    private static byte CARD_SLOT_PSAM2 = 2;
    private static byte CARD_SLOT_PSAM3 = 3;
    private static byte CARD_SLOT_PSAM4 = 4;
    private static byte CARD_SLOT_PSAM5 = 5;
    private static byte CARD_SLOT_PSAM6 = 6;

    private static byte CARD_SLOT_AT24 = 3;


    private class WorkHandler extends Handler {
        public static final int MSG_WORK_ICCARD_ACTION = 1 << 0;
        public static final int MSG_WORK_AT24_ACTION = 1 << 1;
        public static final int MSG_WORK_4442_ACTION = 1 << 2;

        public static final int MSG_WORK_PSAM1_ACTION = 1 << 3;
        public static final int MSG_WORK_PSAM2_ACTION = 1 << 4;
        public static final int MSG_WORK_PSAM3_ACTION = 1 << 5;
        public static final int MSG_WORK_PSAM4_ACTION = 1 << 6;
        public static final int MSG_WORK_PSAM5_ACTION = 1 << 7;
        public static final int MSG_WORK_PSAM6_ACTION = 1 << 8;

        public static final int MSG_WORK_4428_ACTION = 1 << 9;

        public WorkHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            switch (what) {
                case MSG_WORK_ICCARD_ACTION:
                    startTestIcc(CARD_SLOT_ICC);
                    break;
                case MSG_WORK_AT24_ACTION:
                    startTestAt24(CARD_SLOT_AT24);
                    break;
                case MSG_WORK_4442_ACTION:
                    startTest4442(CARD_SLOT_ICC);
                    break;
                case MSG_WORK_4428_ACTION:
                    startTestAt4428(CARD_SLOT_ICC);
                    break;
                case MSG_WORK_PSAM1_ACTION:
                    startTestIcc(CARD_SLOT_PSAM1);
                    break;
                case MSG_WORK_PSAM2_ACTION:
                    startTestIcc(CARD_SLOT_PSAM2);
                    break;
                case MSG_WORK_PSAM3_ACTION:
                    startTestIcc(CARD_SLOT_PSAM3);
                    break;
                case MSG_WORK_PSAM4_ACTION:
                    startTestIcc(CARD_SLOT_PSAM4);
                    break;
                case MSG_WORK_PSAM5_ACTION:
                    startTestIcc(CARD_SLOT_PSAM5);
                    break;
                case MSG_WORK_PSAM6_ACTION:
                    startTestIcc(CARD_SLOT_PSAM6);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //无title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //全屏
        getWindow().setFlags(WindowManager.LayoutParams. FLAG_FULLSCREEN ,
                WindowManager.LayoutParams. FLAG_FULLSCREEN);

        setContentView(R.layout.activity_icc);
        initViews();

    }

    private void initViews(){
        cardTypeRadioGroup = (RadioGroup) this.findViewById(R.id.rg_card_type);
        itemSpinner = (Spinner)this.findViewById(R.id.selected_item);
        tv_msg = (TextView) this.findViewById(R.id.tv_msg);

        TestButton = (Button) findViewById(R.id.button_SingleTest);
        TestButton.setOnClickListener(IccActivity.this);

        cardTypeRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                switch (checkedId) {
                    case R.id.radioButton_icc:
                        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getApplicationContext(),R.array.icc_test_item, android.R.layout.simple_spinner_item);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        itemSpinner.setAdapter(adapter);
                        break;
                    case R.id.RadioButton_psam:
                        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(getApplicationContext(),R.array.psam_test_item, android.R.layout.simple_spinner_item);
                        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        itemSpinner.setAdapter(adapter1);
                        break;
                }
            }
        });

        itemSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                initFlag();
                String selectedItem = parent.getItemAtPosition(position).toString();
                Log.d(TAG,"selected item: " + selectedItem);
                if(selectedItem.equals("ICC")){
                    isIccChecked = true;
                    tv_msg.setText("ICC ready to Test");
                }else if(selectedItem.equals("AT24")){
                    isAt24Checked = true;
                    tv_msg.setText("AT24 ready to Test");
                }else if(selectedItem.equals("4442")){
                    is4442Checked = true;
                    tv_msg.setText("4442 ready to Test");
                }else if(selectedItem.equals("4428")){
                    is4428Checked = true;
                    tv_msg.setText("4428 ready to Test");
                }else if(selectedItem.equals("psam1")){
                    isPsam1Checked = true;
                    tv_msg.setText("PSAM1 ready to Test");
                }else if(selectedItem.equals("psam2")){
                    isPsam2Checked = true;
                    tv_msg.setText("PSAM2 ready to Test");
                }else if(selectedItem.equals("psam3")){
                    isPsam3Checked = true;
                    tv_msg.setText("PSAM3 ready to Test");
                }else if(selectedItem.equals("psam4")){
                    isPsam4Checked = true;
                    tv_msg.setText("PSAM4 ready to Test");
                }else if(selectedItem.equals("psam5")){
                    isPsam5Checked = true;
                    tv_msg.setText("PSAM5 ready to Test");
                }else if(selectedItem.equals("psam6")){
                    isPsam6Checked = true;
                    tv_msg.setText("PSAM6 ready to Test");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //star a thread for psam action
        mWorkThread = new HandlerThread("sdk_psam_thread");
        mWorkThread.start();
        mWorkHandler = new WorkHandler(mWorkThread.getLooper());

    }

    private void initFlag(){
        isIccChecked = false;
        isAt24Checked = false;
        is4442Checked = false;
        is4428Checked = false;
        isPsam1Checked = false;
        isPsam2Checked = false;
        isPsam3Checked = false;
        isPsam4Checked = false;
        isPsam5Checked = false;
        isPsam6Checked = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWorkHandler.removeCallbacksAndMessages(null);
        mWorkThread.quitSafely();

    }


    String strInfo = "";
    void startTestIcc(byte slot) {
        ret = 1;
        byte dataIn[] = new byte[512];

        if (slot == 0) {
            try {
                ret = MyApplication.app.syscardOpt.sysIccCheck(slot);
                if (ret != 0) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            tv_msg.setText("CPU Check Failed");
                        }
                    });

                    return ;
                }
            }catch (RemoteException e){
                e.printStackTrace();
            }
        }

        try {
            ret = MyApplication.app.syscardOpt.sysIccOpen(slot,vcc_mode,ATR);
            if (ret != 0) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        tv_msg.setText("Open Failed");
                    }
                });
                Log.e(TAG, "IccOpen failed!");
                return;
            }
        }catch (RemoteException e){
            e.printStackTrace();
        }


        String atrString = "";
        for (int i = 0; i < ATR.length; i++) {
            atrString += Integer.toHexString(Integer.valueOf(String.valueOf(ATR[i]))).replaceAll("f", "");
        }
        Log.d(TAG, "atrString = " + ByteUtil.bytearrayToHexString(ATR, ATR.length));

        byte cmd[] = new byte[4];
        short lc = 0;
        short le = 0;

        if (slot == 0) {
            cmd[0] = (byte) 0x00;   //0-3 cmd
            cmd[1] = (byte) 0xa4;
            cmd[2] = 0x04;
            cmd[3] = 0x00;
            lc = 0x05;
            le = 0x00;

            dataIn[0] = (byte)0x49;
            dataIn[1] = (byte)0x47;
            dataIn[2] = (byte)0x54;
            dataIn[3] = (byte)0x50;
            dataIn[4] = (byte)0x43;
        } else {
            cmd[0] = 0x00;            //0-3 cmd
            cmd[1] = (byte) 0x84;
            cmd[2] = 0x00;
            cmd[3] = 0x00;
            lc = 0x00;
            le = 0x08;
            String sendmsg = "";
            dataIn = sendmsg.getBytes();
            Log.e("liuhao Icc  " ,"PSAM *******");


        }

        ApduSend mApduSend = new ApduSend(cmd, lc, dataIn, le);
        ApduResp mApduResp = null;
        byte[] resp = new byte[516];

        try {
            ret = MyApplication.app.syscardOpt.sysIccCommand(slot,mApduSend.getBytes(),resp);
            if (0 == ret) {
                mApduResp = new ApduResp(resp);
                strInfo = ByteUtil.bytearrayToHexString(mApduResp.DataOut, mApduResp.LenOut) + "SWA:"
                        + ByteUtil.byteToHexString(mApduResp.SWA) + " SWB:" + ByteUtil.byteToHexString(mApduResp.SWB);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv_msg.setText(strInfo);
                    }
                });
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv_msg.setText("Command Failed");
                    }
                });
                Log.e(TAG, "Icc_Command failed!");
            }
        }catch (RemoteException e){
            e.printStackTrace();
        }
//        byte data[] = "00A4040005494754504300".getBytes();
//        byte resplen[] = new byte[4];


//        try {
//            ret = MyApplication.app.syscardOpt.sysIccApduCmd(slot,data,data.length,resp, resplen);
//            if (0 == ret) {
//
//                strInfo = ByteUtil.bytearrayToHexString(resp, resplen[0]) ;
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        tv_msg.setText(strInfo);
//                    }
//                });
//            } else {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        tv_msg.setText("Icc_apduCommand Failed");
//                    }
//                });
//                Log.e(TAG, "Icc_apduCommand failed!");
//            }
//        }catch (RemoteException e){
//            e.printStackTrace();
//        }

       try {
           MyApplication.app.syscardOpt.sysIccClose(slot);
       }catch (RemoteException e){
           e.printStackTrace();
       }

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    void startTestAt24(byte slot) {
        try {
            ret = 1;
            strInfo = "";
            byte[] dataIn = new byte[516];
            if (slot != 0){
                slot = 0;
            }

            ret = MyApplication.app.syscardOpt.sysSleCheckAt24(slot);
            if (ret != 0) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        tv_msg.setText("Check Failed");
                    }
                });
                Log.e(TAG, "Lib_SleCheckAt24 failed!");
                return ;
            }

            ret = MyApplication.app.syscardOpt.sysSleOpenAt24(slot);
            if (ret != 0) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        tv_msg.setText("Open Failed");
                    }
                });
                Log.e(TAG, "Lib_SleOpenAt24 failed!");
                return;
            }
            else{
                Log.d(TAG, "SleOpenAt24 open success!");
            }
            dataIn = new byte[512];

            ret = MyApplication.app.syscardOpt.sysSleReadMemAt24(slot,0,10,dataIn);
            if (0 == ret) {
                strInfo += "\r\nReadFirst:" + ByteUtil.bytearrayToHexString(dataIn, 10);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv_msg.setText(strInfo);
                    }
                });
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv_msg.setText("Lib_SleReadMemAt24 read Failed");
                    }
                });
                Log.e(TAG, "Lib_SleReadMemAt24 failed!");
                MyApplication.app.syscardOpt.sysSleCloseAt24(slot);
                return;
            }

            String sendmsg = ".SYS.DDF01";
            dataIn = sendmsg.getBytes();

            ret = MyApplication.app.syscardOpt.sysSleWriteMemAt24(slot,2,10,dataIn);
            if (0 == ret) {
                strInfo += "\r\nWriteData:" + ByteUtil.bytearrayToHexString(dataIn, 10);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv_msg.setText(strInfo);
                    }
                });
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv_msg.setText("Lib_SleWriteMemAt24 write Failed");
                    }
                });
                Log.e(TAG, "Lib_SleWriteMemAt24 failed!");
                MyApplication.app.syscardOpt.sysSleCloseAt24(slot);
                return;
            }

            ret = MyApplication.app.syscardOpt.sysSleReadMemAt24(slot,2,10,dataIn);
            if (0 == ret) {
                String recvmsg = new String(dataIn);
                strInfo += "\r\nReadData:" + ByteUtil.bytearrayToHexString(dataIn, 10);

                Log.d(TAG, "recvmsg = " + recvmsg);
                Log.d(TAG, "sendmsg = " + sendmsg);

                if(!sendmsg.equals(recvmsg))
                {
                    strInfo += "\r\nData Error";
                }
                else
                {
                    strInfo += "\r\nData Equal";
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv_msg.setText(strInfo);
                    }
                });
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv_msg.setText("Lib_SleReadMemAt24 write Failed");
                    }
                });
                Log.e(TAG, "Lib_SleReadMemAt24 failed!");
            }
            MyApplication.app.syscardOpt.sysSleCloseAt24(slot);

            Thread.sleep(200);
        }catch (RemoteException e){
            e.printStackTrace();
        }catch (InterruptedException e){
            e.printStackTrace();
        }

    }

    void startTest4442(byte slot){
        ret = 1;
        byte[] dataIn = new byte[516];
        if (slot != 0){
            return;
        }
        try {
            ret = MyApplication.app.syscardOpt.sysSleDetectSYN(slot);
        }catch (RemoteException e){
            e.printStackTrace();
        }


        Log.e(TAG, "Lib_IccDetectSYN ret =" + ret );
        if (ret != 0) {
            runOnUiThread(new Runnable() {
                public void run() {
                    tv_msg.setText("Check Failed");
                }
            });

            return ;
        }
        try {
            ret = MyApplication.app.syscardOpt.sysSleInit4442(slot,dataIn);
        }catch (RemoteException e){
            e.printStackTrace();
        }

        Log.e(TAG, "Lib_SleInit4442 ret =" + ret);
        if (ret != 0) {
            runOnUiThread(new Runnable() {
                public void run() {
                    tv_msg.setText("Open Failed");
                }
            });
            return;
        }else {
            strInfo = ByteUtil.bytearrayToHexString(dataIn, 4);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tv_msg.setText(strInfo);
                }
            });
        }
        try {
            ret = MyApplication.app.syscardOpt.sysSleReadErrorCount4442(slot);
        }catch (RemoteException e){
            e.printStackTrace();
        }

        Log.e(TAG, "Lib_SleReadErrorCount4442 ret = " + ret);
        strInfo += "\r\nReadErrorCount:" + ret;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv_msg.setText(strInfo);
            }
        });
        byte[] verdata = new byte[5]; //password, the valid password must be 3 bytes
        verdata[0] = (byte) 0xff;
        verdata[1] = (byte) 0xff;
        verdata[2] = (byte) 0xff;
        verdata[3] = 0;
        verdata[4] = 0;
        try {
            ret = MyApplication.app.syscardOpt.sysSleVerSecCode4442(slot,verdata);
        }catch (RemoteException e){
            e.printStackTrace();
        }

        Log.e(TAG, "Lib_SleVerSecCode4442 ret = " + ret);
        try {
            ret = MyApplication.app.syscardOpt.sysSleReadErrorCount4442(slot);
        }catch (RemoteException e){
            e.printStackTrace();
        }

        Log.e(TAG, "Lib_SleReadErrorCount4442 ret = " + ret);

        if(ret == 7)
        {
            try {
                ret = MyApplication.app.syscardOpt.sysSleReadMem4442(slot,(byte)0x21,0x10,dataIn);
            }catch (RemoteException e){
                e.printStackTrace();
            }

            Log.e(TAG, "Lib_SleReadMem4442 ret = " + ret);
            if(ret == 0) {
                Log.e(TAG, "read:" + ByteUtil.bytearrayToHexString(dataIn, 0x10));
                //dataIn[0] = 0x32;
                //dataIn = ByteUtil.hexStringToBytes("61616161");
                dataIn[0] = 0x61;
                dataIn[1] = 0x61;
                dataIn[2] = 0x61;
                dataIn[3] = 0x61;
                Log.e(TAG, "write data:" + ByteUtil.bytearrayToHexString(dataIn, 0x10));
                try {
                    ret = MyApplication.app.syscardOpt.sysSleWriteMem4442(slot,(byte)0x21,0x10,dataIn);
                    Log.e(TAG, "Lib_SleWriteMem4442 ret = " + ret);
                    ret = MyApplication.app.syscardOpt.sysSleReadMem4442(slot,(byte)0x21,0x10,dataIn);
                    Log.e(TAG, "Lib_SleReadMem4442 ret = " + ret);
                }catch (RemoteException e){
                    e.printStackTrace();
                }

                if(ret == 0) {
                    Log.e(TAG, "read:" + ByteUtil.bytearrayToHexString(dataIn, 0x10));
                    strInfo = "Read success\n";
                    strInfo += ByteUtil.bytearrayToHexString(dataIn, 0x10);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tv_msg.setText(strInfo);
                        }
                    });
                }

            }
        }
        try {
            ret = MyApplication.app.syscardOpt.sysSleClose4442(slot);
        }catch (RemoteException e){
            e.printStackTrace();
        }

        Log.e(TAG, "Lib_SleClose4442 ret =" + ret);

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    //Converting a string of hex character to bytes
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2){
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    void startTestAt4428(byte slot) {
        ret = 1;
        strInfo = "";
        byte[] dataIn = new byte[4];
        if (slot != 0)
            slot = 0;
//            return;
        try {
            ret = MyApplication.app.syscardOpt.sysSleInit4428(slot, dataIn);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        if (ret != 0) {
            runOnUiThread(new Runnable() {
                public void run() {
                    tv_msg.setText("SleInit4428 Failed");
                }
            });
            Log.e(TAG, "SleInit4428 failed!");
            return;
        }
        else
            Log.d(TAG, "SleInit4428 success!");

        try {
            ret = MyApplication.app.syscardOpt.sysIccDetect_4428(slot);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        if (ret != 0) {
            runOnUiThread(new Runnable() {
                public void run() {
                    tv_msg.setText("Check Failed");
                }
            });
            Log.e(TAG, "SleCheck4428 failed!");

            return ;
        }
        dataIn = new byte[4];
        try {
            ret = MyApplication.app.syscardOpt.sysSleOpen4428(slot);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        if (ret != 0) {
            runOnUiThread(new Runnable() {
                public void run() {
                    tv_msg.setText("Open Failed");
                }
            });
            Log.e(TAG, "SleOpen4428 failed!");
            return;
        }
        else
            Log.d(TAG, "SleOpen4428 open success!");

        dataIn = new byte[512];

        try {
            ret = MyApplication.app.syscardOpt.sysSleReadWithoutPB4428(slot, 0, 0x20,dataIn);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        if (0 == ret) {
            strInfo += "\r\nReadFirst:" + ByteUtil.bytearrayToHexString(dataIn, 0x20);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tv_msg.setText(strInfo);
                }
            });
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tv_msg.setText("SleReadWithoutPB4428 read Failed");
                }
            });
            Log.e(TAG, "SleReadWithoutPB4428 failed!");
            try {
                MyApplication.app.syscardOpt.sysSleClose4428(slot);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            return;
        }

        /*verify start *******************************************************************/
        try {
            ret = MyApplication.app.syscardOpt.sysSleReadPinCounter4428(slot, dataIn);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        if (0 == ret) {
            strInfo += "\r\nReadPinCounterFirst:" + ByteUtil.bytearrayToHexString(dataIn, 0x1);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tv_msg.setText(strInfo);
                }
            });
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tv_msg.setText("SleReadWithoutPB4428 read Failed");
                }
            });
            Log.e(TAG, "SleReadWithoutPB4428 failed!");
            try {
                MyApplication.app.syscardOpt.sysSleClose4428(slot);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            return;
        }

        dataIn[0] = (byte) (dataIn[0] / 2);
        try {
            ret = MyApplication.app.syscardOpt.sysSleWritePinCounter4428(slot, dataIn[0]);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        if (0 == ret) {
            strInfo += "\r\nWritePinCounter:" + ByteUtil.bytearrayToHexString(dataIn, 0x1);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tv_msg.setText(strInfo);
                }
            });
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tv_msg.setText("SleReadWithoutPB4428 read Failed");
                }
            });
            Log.e(TAG, "SleReadWithoutPB4428 failed!");
            try {
                MyApplication.app.syscardOpt.sysSleClose4428(slot);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            return;
        }
        dataIn[0] = (byte) 0xff;
        dataIn[1] = (byte) 0xff;
        try {
            ret = MyApplication.app.syscardOpt.sysSleVerifyPin4428(slot, dataIn);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        if (0 == ret) {
            dataIn[0] = (byte) 0xff;
            try {
                ret = MyApplication.app.syscardOpt.sysSleWriteWithoutPB4428(slot, 0x03fd, 1, dataIn);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            if (0 == ret) {
                strInfo += "\r\nWritesecondPinCounter: 0xff" ;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv_msg.setText(strInfo);
                    }
                });
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv_msg.setText("SleReadWithoutPB4428 read Failed");
                    }
                });
                Log.e(TAG, "SleReadWithoutPB4428 failed!");
                try {
                    MyApplication.app.syscardOpt.sysSleClose4428(slot);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                return;
            }

        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tv_msg.setText("SleReadWithoutPB4428 read Failed");
                }
            });
            Log.e(TAG, "SleReadWithoutPB4428 failed!");
            try {
                MyApplication.app.syscardOpt.sysSleClose4428(slot);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            return;
        }

        try {
            ret = MyApplication.app.syscardOpt.sysSleReadPinCounter4428(slot, dataIn);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        if (0 == ret) {
            strInfo += "\r\nReadPinCountersecond:" + ByteUtil.bytearrayToHexString(dataIn, 1);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tv_msg.setText(strInfo);
                }
            });
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tv_msg.setText("Lib_SleReadMemAt24 write Failed");
                }
            });
            Log.e(TAG, "SleReadPinCounter4428 failed!");
        }
        /*verify end *************************************************************************/
//        dataIn[0] = (byte)0xff;
//        dataIn[1] = (byte)0xff;
//        ret = MyApplication.app.syscardOpt.sysSleWritePin4428(slot, dataIn);
//        if (0 == ret) {
//            strInfo += "\r\nSleWritePin4428:" + ByteUtil.bytearrayToHexString(dataIn, 1);
//
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    tv_msg.setText(strInfo);
//                }
//            });
//        } else {
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    tv_msg.setText("SleWritePin4428 write Failed");
//                }
//            });
//            Log.e(TAG, "SleWritePin4428 failed!");
//        }

        dataIn[0] = (byte) 0x12;
        dataIn[1] = (byte) 0x34;
        try {
            ret = MyApplication.app.syscardOpt.sysSleWriteWithoutPB4428(slot, 0x0310, 2, dataIn);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        if (0 == ret) {
            strInfo += "\r\nSleWriteWithoutPB4428: "+ ByteUtil.bytearrayToHexString(dataIn, 2);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tv_msg.setText(strInfo);
                }
            });
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tv_msg.setText("SleReadWithoutPB4428 read Failed");
                }
            });
            Log.e(TAG, "SleReadWithoutPB4428 failed!");
            try {
                MyApplication.app.syscardOpt.sysSleClose4428(slot);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            return;
        }

        try {
            ret = MyApplication.app.syscardOpt.sysSleReadWithoutPB4428(slot, 0x0310, 2, dataIn);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        if (0 == ret) {
            strInfo += "\r\nSleReadWithoutPB4428:" + ByteUtil.bytearrayToHexString(dataIn, 2);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tv_msg.setText(strInfo);
                }
            });
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tv_msg.setText("SleReadWithoutPB4428 read Failed");
                }
            });
            Log.e(TAG, "SleReadWithoutPB4428 failed!");
            try {
                MyApplication.app.syscardOpt.sysSleClose4428(slot);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            return;
        }

        try {
            MyApplication.app.syscardOpt.sysSleClose4428(slot);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public void onClick(View v) {

        tv_msg.setText("");
        switch (v.getId()){
            case R.id.button_SingleTest:
                if(isIccChecked){
                    mWorkHandler.sendEmptyMessage(WorkHandler.MSG_WORK_ICCARD_ACTION);
                }else if(isAt24Checked){
                    mWorkHandler.sendEmptyMessage(WorkHandler.MSG_WORK_AT24_ACTION);
                }else if(is4442Checked){
                    mWorkHandler.sendEmptyMessage(WorkHandler.MSG_WORK_4442_ACTION);
                }else if(is4428Checked){
                    mWorkHandler.sendEmptyMessage(WorkHandler.MSG_WORK_4428_ACTION);
                }else if(isPsam1Checked){
                    mWorkHandler.sendEmptyMessage(WorkHandler.MSG_WORK_PSAM1_ACTION);
                }else if(isPsam2Checked){
                    mWorkHandler.sendEmptyMessage(WorkHandler.MSG_WORK_PSAM2_ACTION);
                }else if(isPsam3Checked){
                    if(BuildConfig.FLAVOR.startsWith("CM30")){
                        mWorkHandler.sendEmptyMessage(WorkHandler.MSG_WORK_PSAM3_ACTION);
                    }else{
                        Toast.makeText(this,"Not support",Toast.LENGTH_SHORT).show();
                    }
                }else if(isPsam4Checked){
                    if(BuildConfig.FLAVOR.startsWith("CM30")){
                        mWorkHandler.sendEmptyMessage(WorkHandler.MSG_WORK_PSAM4_ACTION);
                    }else{
                        Toast.makeText(this,"Not support",Toast.LENGTH_SHORT).show();
                    }
                }else if(isPsam5Checked){
                    if(BuildConfig.FLAVOR.equals("CM30T")){
                        mWorkHandler.sendEmptyMessage(WorkHandler.MSG_WORK_PSAM5_ACTION);
                    }else{
                        Toast.makeText(this,"Not support",Toast.LENGTH_SHORT).show();
                    }
                }else if(isPsam6Checked){
                    if(BuildConfig.FLAVOR.equals("CM30T")){
                        mWorkHandler.sendEmptyMessage(WorkHandler.MSG_WORK_PSAM6_ACTION);
                    }else{
                        Toast.makeText(this,"Not support",Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }


}
