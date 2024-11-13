package test.demo.activity.emv;


import test.demo.MyApplication;
import test.demo.activity.R;

/** EMV交易结果 */
public enum EMVTransResult {
    /** 联机批准 */
    ONLINE_APPROVE(MyApplication.app.getString(R.string.online_approve)),
    /** 联机拒绝 */
    ONLINE_DECLINE(MyApplication.app.getString(R.string.online_decline)),
    /** 继续执行联机操作 */
    ONLINE_REQUEST(MyApplication.app.getString(R.string.online_request)),
    /** 脱机批准 */
    OFFLINE_APPROVE(MyApplication.app.getString(R.string.offline_approve)),
    /** 脱机拒绝 */
    OFFLINE_DECLINE(MyApplication.app.getString(R.string.offline_decline)),
    /** 请使用其他界面 */
    USE_OTHER_INTERFACE(MyApplication.app.getString(R.string.another_interface)),
    /** 交易终止 */
    TRANSACTION_TERMINATED(MyApplication.app.getString(R.string.transaction_terminate)),
    /** 交易终止(服务不接受) */
    NOT_ACCEPTED(MyApplication.app.getString(R.string.terminal_service_not_accepted)),
    /** 此卡为芯片卡,不可降级交易 */
    READ_CARD_FALLBACK(MyApplication.app.getString(R.string.use_ic_card)),
    /** 此卡为芯片卡,不可降级交易 */
    SWIPE_CARD(MyApplication.app.getString(R.string.swipe_card)),
    /** 联机失败 */
    ONLINE_FAILED(MyApplication.app.getString(R.string.online_failed)),

    /** 看手机 */
    SEE_PHONE(MyApplication.app.getString(R.string.see_phone));

    EMVTransResult(String depicter) {
        this.depicter = depicter;
    }

    public String getDepicter() {
        return depicter;
    }

    private String depicter;
}
