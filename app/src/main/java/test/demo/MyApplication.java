package test.demo;


import android.app.Application;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Handler;

import com.ciontek.hardware.aidl.AidlErrorCodeV2;
import com.ciontek.hardware.aidl.emv.EMVOptV2;
import com.ciontek.hardware.aidl.ped.PedOpt;
import com.ciontek.hardware.aidl.pinpad.PinpadOpt;
import com.ciontek.hardware.aidl.print.PrinterOpt;
import com.ciontek.hardware.aidl.readcard.ReadCardOptV2;
import com.ciontek.hardware.aidl.sysCard.SysCardOpt;
import com.ciontek.hardware.aidl.system.SysBaseOpt;
import com.ciontek.hardware.aidl.tax.TaxOpt;
import com.ctk.sdk.DebugLogUtil;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import pos.paylib.posPayKernel;
import test.demo.activity.BuildConfig;
import pos.paylib.keypad.PinpadManage;


public class MyApplication extends Application {
    private static final String TAG = "MyApplication";

    public static MyApplication app;

    /**
     * 获取基础操作模块
     */
    public SysBaseOpt basicOpt;

    /**
     * 获取读卡模块
     */
    public ReadCardOptV2 readCardOpt;

    /**
     * 获取PinPad操作模块
     */
    public PinpadOpt pinPadOpt;

    /**
     * 获取Ped操作模块
     */
    public PedOpt pedOpt;

    /**
     * 获取Ped操作模块
     */
    public PrinterOpt printerOpt;
    /**
     * 获取Ped操作模块
     */
    public TaxOpt taxOpt;
    /**
     * 获取EMV操作模块
     */
    public EMVOptV2 emvOpt;


    /**
     * 获取系统卡模块
     * ***/
    public SysCardOpt syscardOpt;

    private posPayKernel mPosPayKernel;

    public volatile boolean isConnect;
    private Set<OnServiceConnectListener> listeners;
    private Handler handler = new Handler();


    public volatile int newTransFlag = 0;

    /**
     * 自动测试
     */
    public boolean autoTest;

    public static byte Tdes = 0;
    public static byte KlkTdes = 1;
    public static byte AESMKSK = 2;
    public static byte TDESMKSK = 3;
    public static byte Dukpt = 4;
    public static byte AESDukpt = 5;
    public static byte TDESDukpt = 6;
    private String mcu_update_path;

    private boolean isSPNFC = true;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        listeners = new CopyOnWriteArraySet<>();
        String curFaluor = BuildConfig.FLAVOR;
        if(curFaluor.startsWith("CM30")){
            mcu_update_path = "/storage/emulated/0/Download/CM30_APP.bin";
            isSPNFC = false;
        }else if(curFaluor.equals("CS50")){
            mcu_update_path = "/storage/emulated/0/Download/CS50_APP.bin";
        }else if(curFaluor.equals("CS50C")){
            mcu_update_path = "/storage/emulated/0/Download/CS50_APP.bin";
            isSPNFC = false;
        }else if(curFaluor.equals("CS20")){
            mcu_update_path = "/storage/emulated/0/Download/CS20_APP.bin";
        }else{
            mcu_update_path = "/storage/emulated/0/Download/CS50_APP.bin";
        }
        sendBroadCast(1);
    }

    /**
     * 绑定支付SDK
     */
    public void connectPayService() {
        DebugLogUtil.e(TAG, "start bind payHardware service...");
        mPosPayKernel = posPayKernel.getInstance();
        mPosPayKernel.initPaySDK(this, mConnectCallback);
        checkServiceConnectivity(3 * 1000);
    }

    /**
     * 连接状态回调
     */
    private posPayKernel.ConnectCallback mConnectCallback = new posPayKernel.ConnectCallback() {

        @Override
        public void onConnectPaySDK() {
            DebugLogUtil.e(TAG, "onConnectPaySDK");
            try {
                isConnect = true;
                emvOpt = mPosPayKernel.mEmvOpt;
                basicOpt = mPosPayKernel.mBasicOpt;
                pinPadOpt = mPosPayKernel.mPinpadOpt;
                readCardOpt = mPosPayKernel.mReadcardOpt;
                pedOpt = mPosPayKernel.mPedOpt;
                taxOpt = mPosPayKernel.mTaxOpt;
                printerOpt = mPosPayKernel.mPrintOpt;
                syscardOpt = mPosPayKernel.mSysCardOpt;

                notifyServiceConnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onDisconnectPaySDK() {
            DebugLogUtil.e(TAG, "onDisconnectPaySDK");
            isConnect = false;
            emvOpt = null;
            basicOpt = null;
            pinPadOpt = null;
            readCardOpt = null;
            pedOpt = null;
            taxOpt = null;
            printerOpt = null;
            syscardOpt = null;

            checkServiceConnectivity(0);
        }

    };

    /**
     * 解绑支付SDK
     */
    public void disconnectPayService() {
        DebugLogUtil.e(TAG, "start unbind payHardware service...");
        if (mPosPayKernel != null) {
            mPosPayKernel.destroyPaySDK();
        }
    }

    /**
     * 检查PayHardwareService是否连接
     */
    private void checkServiceConnectivity(long delayMillis) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isConnect) {
                    connectPayService();
                }
            }
        }, delayMillis);
    }

    private void notifyServiceConnect() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                for (OnServiceConnectListener listener : listeners) {
                    listener.onServiceConnect();
                }
            }
        });
    }

    public void registerServiceConnectListener(OnServiceConnectListener l) {
        if (l != null) {
            listeners.add(l);
        }
    }

    public void unregisterServiceConnectListener(OnServiceConnectListener l) {
        listeners.remove(l);
    }


    private void sendBroadCast(int debugMode) {
        //发送广播:是否显示水印
        Intent intent1 = new Intent("security.action.set_mode_success");
        intent1.setComponent(new ComponentName("com.android.systemui", "com.android.systemui.SecurityModeReceiver"));
        //mode 0:交易  1:调试
        intent1.putExtra("mode", debugMode);
        sendBroadcast(intent1);
        //发送广播:调试是否打开
        Intent intent2 = new Intent("security.action.set_mode_success");
        intent2.setComponent(new ComponentName("com.android.settings", "com.android.settings.SecurityModeReceiver"));
        intent2.putExtra("mode", debugMode);
        sendBroadcast(intent2);
    }


    /**
     * service连接成功回接口
     */
    public interface OnServiceConnectListener {
        /**
         * service连接成功后回调方法(UI线程调用)
         */
        void onServiceConnect();
    }

    public String getMcuUpdataPath(){
        return mcu_update_path;
    }

    public boolean getSpNfc(){
        return this.isSPNFC;
    }

}