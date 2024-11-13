package test.demo.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;

import test.demo.MyApplication;


/**
 * Created by Administrator on 2021/03/17.
 */


public class SysActivity extends Activity implements OnClickListener {

    public static final int OPCODE_SET_SN = 0;
    public static final int OPCODE_GET_SN = 1;
    public static final int OPCODE_GET_CHIP_ID =2;
    public static final int OPCODE_GET_BEEP =3;


    public static final int OPCODE_GET_MDB =4;
    public static String[] MY_PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.MOUNT_UNMOUNT_FILESYSTEMS"};

    public static final int REQUEST_EXTERNAL_STORAGE = 1;

    private final String TAG = "SysActivity";

    byte SN[] = new byte[32];
    String snString = "";
    byte version[] = new byte[9];

    EditText editSn = null;
    TextView tvMsg = null;

    Button btnUpdate,btnSetSN, btnGetSN, btnGetChipID, btnVersion , btnBeep,btnMdbtest, btnSetmcuTime,btnGetmcuTime;
    int ret = 0;

    private static final String CTRL_FILE = "/sys/devices/platform/mdb_sw/mdb_sw";
    private void writeToCtrlFile(String data) {
        try {
            FileOutputStream fps = new FileOutputStream(new File(CTRL_FILE));
            fps.write(data.getBytes());
            fps.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //无title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_sys);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        tvMsg = (TextView) findViewById(R.id.textview);

        editSn = (EditText) findViewById(R.id.editSn);

        btnUpdate = (Button) findViewById(R.id.btnUpdate);
        btnSetSN = (Button) findViewById(R.id.btnSetSN);
        btnGetSN = (Button) findViewById(R.id.btnGetSN);
        btnGetChipID = (Button) findViewById(R.id.btnGetChipID);
        btnVersion = (Button) findViewById(R.id.btnVersion);
        btnBeep = (Button) findViewById(R.id.btnBeep);

        btnSetmcuTime = (Button) findViewById(R.id.btnSetmcuTime);
        btnGetmcuTime = (Button) findViewById(R.id.btnGetMcuTime);

        btnMdbtest =(Button) findViewById(R.id.btnTestMdb);

        if(BuildConfig.FLAVOR.equals("CM30")){
            btnMdbtest.setVisibility(View.VISIBLE);
            btnMdbtest.setOnClickListener(this);
        }
        btnUpdate.setOnClickListener(this);
        btnSetSN.setOnClickListener(this);
        btnGetSN.setOnClickListener(this);
        btnGetChipID.setOnClickListener(this);
        btnVersion.setOnClickListener(this);
        btnBeep.setOnClickListener(this);
        btnGetmcuTime.setOnClickListener(this);
        btnSetmcuTime.setOnClickListener(this);

    }

    protected void onResume() {
        // TODO Auto-generated method stub
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onResume();
    }

    @Override
    protected void onPause() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        synchronized (this) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    void startTestSys(int OpCode) {
        switch (OpCode) {
            case OPCODE_GET_SN:
                tvMsg.setText("Get SN...");
                try {
                    ret = MyApplication.app.basicOpt.SpReadSN(SN);
                }catch (RemoteException e){
                    e.printStackTrace();
                }

                if (ret == 0) {
                    tvMsg.setText("Read SN Success: " + ByteUtil.bytesToString(SN));
                } else {
                    tvMsg.setText("Read SN Failed");
                }
                break;
            case OPCODE_SET_SN:
                tvMsg.setText("Set SN...");
                snString = editSn.getText().toString();
                try {
                    ret = MyApplication.app.basicOpt.SpWriteSN(snString.getBytes());
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
                if (ret == 0) {
                    tvMsg.setText("Write SN Success\n" + "setSN : " + snString);
                } else {
                    tvMsg.setText("Write SN Failed");
                }
                break;
            case OPCODE_GET_CHIP_ID:
                byte chipIdBuf[] = new byte[16];
                try {
                    ret = MyApplication.app.basicOpt.SpReadChipID(chipIdBuf,16);
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
                if (ret == 0) {
                    tvMsg.setText("Read ChipID Success: " + ByteUtil.bytearrayToHexString(chipIdBuf, 16));
                } else {
                    tvMsg.setText("Read ChipID Failed");
                }
                break;
            case OPCODE_GET_BEEP:
                try {
                    ret = MyApplication.app.basicOpt.SpMutiBeep((byte) 15,(byte) 200);
                }catch (RemoteException e){
                    e.printStackTrace();
                }

                break;
            case OPCODE_GET_MDB:

                writeToCtrlFile("9");  //--- gpio7 拉高

                String dispMsg ="";
                Log.e(TAG, "enter mdb test: ");
                tvMsg.setText("Enter mdb test...");
                byte buf[] = new byte[256];
                byte buf1[] = new byte[256];
                int len = 12;
                for(int i=0;i <len;i++){
                    char temp = 0;
                    int key = (int) (Math.random() * 2);
                    switch (key) {
                        case 0:
                            temp = (char) (Math.random() * 10 + 48);//产生随机数字
                            break;
                        case 1:
                            temp = (char) (Math.random()*6 + 'a');//产生a-f
                            break;
                        default:
                            break;
                    }
                    buf[i] = (byte) temp;
                }

                try {
                    ret = MyApplication.app.basicOpt.SpMdbTest(buf, buf1,len);
                }catch (RemoteException e){
                    e.printStackTrace();
                }

                if (ret == 0) {
                    tvMsg.setText("test MDB Failed: ");
                } else {
                    dispMsg +="recvData: " + ByteUtil.bytearrayToHexString(buf1,ret);
                    dispMsg +="\n";
                    if(Arrays.equals(buf,buf1)){
                        dispMsg +="result: success";
                    }else{
                        dispMsg +="result: send receive succee, but receive data error";
                    }
                    tvMsg.setText(dispMsg);
                    //tvMsg.setText("Recv MDb :" + ByteUtil.bytearrayToHexString(buf1, ret));
                }
                writeToCtrlFile("8");  //-- gpio7 拉低

                break;
            default:
                break;
        }
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    void getSysVersionInfo() {
        try {
            ret = MyApplication.app.basicOpt.SpGetVersion(version);
            String Sn = Build.getSerial();
            Log.e(TAG, "getSysVersionInfo Sn = " + Sn);
        }catch (RemoteException e){
            e.printStackTrace();
        }

        Log.e(TAG, "getSysVersionInfo ret = " + ret);
        Log.d(TAG,"getSysVersionInfo: "+ com.ctk.sdk.ByteUtil.bytearrayToHexString(version,9));
        if (ret == 0) {
            if (version[6] == -1 && version[7] == -1 && version[8] == -1) {
                tvMsg.setText(" SP Version: CS50-" + version[0] + "." + version[1] + "." + version[2] +
                        "\nSO Lib Version: V" + version[3] + "." + version[4] + "." + version[5] +
                        "\nSecurity Boot Version: NULL" + "\nSucceed"
                );
            }
            else {
                tvMsg.setText("Security App Version: V" + version[0] + "." + version[1] + "." + version[2] +
                        "\nSO Lib Version: V" + version[3] + "." + version[4] + "." + version[5] +
                        "\nSecurity Boot Version: V" + version[6] + "." + version[7] + "." + version[8]
                        + "\nSucceed");
            }
        }else {
            tvMsg.setText("Get_Version Failed");
        }
    }

    private void restartApp() {
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    ProgressDialog updateDlg = null;

    private void startUpdate(String binpath) {
        //byte[] path = binpath.getBytes();
        Log.e(TAG, "startUpdate  ........ 00");
        Log.e(TAG,"path: "+ binpath);

        //Sys.Lib_LogSwitch(1);
        updateDlg = ProgressDialog.show(this, null, getString(R.string.isUpdating), false, false);
        new Thread() {
            @Override
            public void run() {
                super.run();
                    try {
                        int ret = MyApplication.app.basicOpt.SpUpdate_32550(binpath);
                    }catch (RemoteException e){
                        e.printStackTrace();
                    }


                Log.e(TAG, "SysUpdate ret = " + ret);
                if (ret == 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateDlg.cancel();
                            //升级成功 重启应用
                            tvMsg.setText(R.string.update_finish);
                        }
                    });

                    new Thread() {
                        public void run() {
                            try {
                                sleep(2000);
                                restartApp();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();

                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateDlg.cancel();
                            tvMsg.setText(R.string.update_fail);
                        }
                    });
                }
            }
        }.start();
    }

    /**
     * @Description: Request permission
     * 申请权限
     */
    private void requestPermission() {
        //检测是否有写的权限
        //Check if there is write permission
        int checkCallPhonePermission = ContextCompat.checkSelfPermission(SysActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (checkCallPhonePermission != PackageManager.PERMISSION_GRANTED) {
            // 没有写文件的权限，去申请读写文件的权限，系统会弹出权限许可对话框
            //Without the permission to Write, to apply for the permission to Read and Write, the system will pop up the permission dialog
            ActivityCompat.requestPermissions(SysActivity.this, MY_PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
        } else {
            updateMcu();
        }
    }

    /**
     * a callback for request permission
     * 注册权限申请回调
     *
     * @param requestCode  申请码
     * @param permissions  申请的权限
     * @param grantResults 结果
     */
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_EXTERNAL_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                updateMcu();
            }
        }
    }

    private void updateMcu() {

        tvMsg.setText("Update...");
        //String path = "/storage/emulated/0/Download/CS50_APP.bin";
        String path = MyApplication.app.getMcuUpdataPath();
        File file = null , file1 = null;
        Log.d(TAG,"update path: "+ path);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //file = new File("/storage/emulated/0/Download/CS50_APP.bin");
            file = new File(path);
        } else {
            file = new File(path);
            //file = new File("/storage/emulated/0/Download/CS50_APP.bin");
        }

        if (!file.exists()) {
            Toast.makeText(getApplicationContext(), getString(R.string.file_not_found), Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this).setTitle(R.string.update)
                .setMessage(R.string.update_or_not)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startUpdate(path);
                        dialog.cancel();
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                dialog.cancel();
            }
        }).show();
    }


    private void SetTime() {

        int ret = 0;

        Calendar calendar= Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month= calendar.get(Calendar.MONTH)+1 ;
        int dayofmonth= calendar.get(Calendar.DAY_OF_MONTH);
        int hour= calendar.get(Calendar.HOUR_OF_DAY);
        int minuter= calendar.get(Calendar.MINUTE);
        int second= calendar.get(Calendar.SECOND);
        Log.d("vpos","Current Set Time:"+year +":" + month +":" + dayofmonth+":" + hour+":" + minuter+":" + second);


        byte [] time = new byte[6];
        time[0] = (byte)(year - 2000);
        time[1] = (byte)month;
        time[2] = (byte)(dayofmonth );
        time[3] = (byte)hour;
        time[4] = (byte)minuter;
        time[5] = (byte)second;

        try {
            ret = MyApplication.app.basicOpt.sysSetTime(time);
        }catch (RemoteException e){
            e.printStackTrace();
        }
        String currentTime = String.format("%02d-%02d-%02d %02d:%02d:%02d", time[0], time[1],time[2], time[3],time[4], time[5]);
        tvMsg.setText("settime:" + ret +" :"+currentTime);
    }

    private void GetTime() {
        int ret = 0;
        Calendar calendar= Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month= calendar.get(Calendar.MONTH)+1 ;
        int dayofmonth= calendar.get(Calendar.DAY_OF_MONTH);
        int hour= calendar.get(Calendar.HOUR_OF_DAY);
        int minuter= calendar.get(Calendar.MINUTE);
        int second= calendar.get(Calendar.SECOND);
        Log.d("vpos","Current Get Time:"+year +":" + month +":" + dayofmonth+":" + hour+":" + minuter+":" + second);
        String sysTime = String.format("%02d-%02d-%02d %02d:%02d:%02d", year, month,dayofmonth, hour,minuter, second);
        byte [] time = new byte[6];
        try {
            ret = MyApplication.app.basicOpt.sysGetTime(time);
        }catch (RemoteException e){
            e.printStackTrace();
        }
        String currentTime = String.format("%02d-%02d-%02d %02d:%02d:%02d", time[0], time[1],time[2], time[3],time[4], time[5]);
        tvMsg.setText("android time:" +sysTime + "\r\ngettime:" + currentTime);
    }

    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btnUpdate:
                //Determine if the current Android version is >=23
                // 判断Android版本是否大于23
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermission();
                } else {
                    updateMcu();
                }
                break;

            case R.id.btnSetSN:
                startTestSys(OPCODE_SET_SN);
                break;

            case R.id.btnGetSN:
                startTestSys(OPCODE_GET_SN);
                break;

            case R.id.btnGetChipID:
                startTestSys(OPCODE_GET_CHIP_ID);
                break;

            case R.id.btnVersion:
                getSysVersionInfo();
                break;

            case R.id.btnBeep:
                startTestSys(OPCODE_GET_BEEP);
                break;
            case R.id.btnTestMdb:
                startTestSys(OPCODE_GET_MDB);
                break;
            case R.id.btnSetmcuTime:
                SetTime();
                break;
            case R.id.btnGetMcuTime:
                GetTime();
                break;
        }
    }
}
