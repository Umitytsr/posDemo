package test.demo.activity.net;

/** 与POSP通信回调接口 */
public interface POSPTransactionListener {
    /** 交易开始 */
    void onStart();

    /** 交易成功 */
    void onSuccess(byte[] recvBuff, int recvLen);

    /** 交易出错 */
    void onError(Throwable t);
}
