package test.demo.activity.view;

import android.app.Dialog;
import android.content.Context;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import test.demo.activity.R;


/**
 * Created by Kobi on 2023/10/08.
 */

public class CenterDialog {
    // 因为多次按键出现问题
    private static CenterDialog mInstance;

    private CenterDialog() {

    }

    public static CenterDialog getInstance() {
        if (mInstance == null)
            mInstance = new CenterDialog();
        return mInstance;
    }

    Dialog pd;

    public Dialog show(Context mContext, int resID, int root) {
        if (pd != null) {
            pd.dismiss();
            pd = null;
        }
        pd = new Dialog(mContext, R.style.CenterDialog);
        pd.setContentView(resID);
        LinearLayout layout = (LinearLayout) pd.findViewById(root);
        layout.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.up2down));
        pd.setCancelable(false);
        pd.setCanceledOnTouchOutside(false);
        pd.show();
        return pd;
    }

    public static Dialog show(Context context, int resID) {
        Dialog dialog = new Dialog(context, R.style.DialogStyle);
        dialog.setContentView(resID);
//        dialog.setCancelable(false);
        dialog.show();
        return dialog;
    }
}
