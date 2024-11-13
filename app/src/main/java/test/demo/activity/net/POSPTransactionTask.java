package test.demo.activity.net;

import android.os.Handler;
import android.os.Looper;

import test.demo.activity.utils.DebugLogUtil;
import test.demo.activity.utils.Utils;

import java.util.Arrays;

public final class POSPTransactionTask {
    private static final String TAG = "POSPTransactionTask";

    private Handler handler = new Handler(Looper.getMainLooper());

    public POSPTransactionTask() {
    }

    public void sendToPOSP(final byte[] data, final POSPTransactionListener listener) {
        ThreadPoolManager.executeInSinglePool(new Runnable() {
            @Override
            public void run() {
                try {
                    onTransactionStart(listener);

                    byte[] recBuff = new byte[2048];
                    int[] recvLen = new int[1];
                    POSPAccess pospAccess = new POSPAccess();
                    DebugLogUtil.e(TAG, "POSP transaction send data:" + Utils.byte2HexStr(data));
                    pospAccess.transaction(data, data.length, recBuff, recvLen);
                    DebugLogUtil.e(TAG, "POSP transaction receive data:" + Utils.byte2HexStr(recBuff));
                    DebugLogUtil.e(TAG, "POSP transaction receive valid length:" + recvLen[0]);

                    onTransactionSuccess(recBuff, recvLen[0], listener);
                } catch (Exception e) {
                    e.printStackTrace();
                    DebugLogUtil.e(TAG, "POSP transaction error:" + e.getMessage());
                    onTransactionError(e, listener);
                }
            }
        });
    }

    /**
     * 同步上送数据，在调用者线程执行
     *
     * @param data 要发送的数据
     * @return 接收到的数据
     */
    public byte[] syncSendToPOSP(final byte[] data) {
        try {
            byte[] recBuff = new byte[2048];
            int[] recvLen = new int[1];
            POSPAccess pospAccess = new POSPAccess();
            DebugLogUtil.e(TAG, "POSP transaction send data:" + Utils.byte2HexStr(data));
            pospAccess.transaction(data, data.length, recBuff, recvLen);
            DebugLogUtil.e(TAG, "POSP transaction receive data:" + Utils.byte2HexStr(recBuff));
            DebugLogUtil.e(TAG, "POSP transaction receive valid length:" + recvLen[0]);

            if (recvLen[0] > 0) {
                return Arrays.copyOf(recBuff, recvLen[0]);
            }
        } catch (Exception e) {
            e.printStackTrace();
            DebugLogUtil.e(TAG, "POSP transaction error:" + e.getMessage());
        }
        return new byte[0];
    }

    public static int getPOSPSocketTimeout() {
        return (POSPAccess.DEFAULT_CONN_TIMEOUT + POSPAccess.DEFAULT_SOCKET_TIMEOUT) / 1000;
    }

    /** 交易开始 */
    private void onTransactionStart(final POSPTransactionListener listener) {
        if (listener != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onStart();
                }
            });
        }
    }

    /** 交易成功 */
    private void onTransactionSuccess(final byte[] recvBuf, final int recvLen, final POSPTransactionListener listener) {
        if (listener != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onSuccess(recvBuf, recvLen);
                }
            });
        }
    }

    /** 交易出错 */
    private void onTransactionError(final Throwable t, final POSPTransactionListener listener) {
        if (listener != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onError(t);
                }
            });
        }
    }
}
