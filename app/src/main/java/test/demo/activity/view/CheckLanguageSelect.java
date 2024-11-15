package test.demo.activity.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;



public class CheckLanguageSelect {

    int dialogResult;
    public static Handler mHandler;
    public Context context;

    public static boolean showComfirmDialog(Activity context, String title, String msg) {
        mHandler = new MyHandler();
        return new CheckLanguageSelect(context, title,msg).getResult() == 1;
    }

    public CheckLanguageSelect(Activity context, String title, String msg) {
        //显示Dialog;
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        dialogBuilder.setPositiveButton("english", new DialogButtonOnClick(1));
        dialogBuilder.setNegativeButton("chinese", new DialogButtonOnClick(0));
        dialogBuilder.setTitle(title).setMessage(msg).create().show();
        //系统线程会阻塞
        try {
            Looper.loop();}catch (Exception e) {}
    }
    public int getResult() {return dialogResult;}

    private static class  MyHandler extends Handler{
        public void handleMessage(Message mesg) {
            throw new RuntimeException();
        }
    }

    private final class DialogButtonOnClick implements OnClickListener {
        int type;
        public DialogButtonOnClick(int type){
            this.type = type;
        }
        /**
         * 用户点击确定或取消按钮后,设置点击按钮,发送消息;
         * mHandler收到消息后抛出RuntimeException()异常;
         * 阻塞会消失,主线程继续执行
         */
        public void onClick(DialogInterface dialog, int which) {
            CheckLanguageSelect.this.dialogResult = type;
            Message m = mHandler.obtainMessage();
            mHandler.sendMessage(m);
        }
    }
}
