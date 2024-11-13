package test.demo.activity.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
//import android.support.v4.app.ActivityCompat;

import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;

public class PermissionsUtil {
    //权限
    public static final String[] COMMON_PERMISSION = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            };
    public static final int REQUEST_CODE_PERMISSION = 5555;

    public static void requstPermissions(Activity activity) {
        List<String> ps = new ArrayList<>();
        for (String s : COMMON_PERMISSION) {
            if (ActivityCompat.checkSelfPermission(activity, s) != PackageManager.PERMISSION_GRANTED) {
                ps.add(s);
            }
        }

        if (!ps.isEmpty()) {
            String[] p = new String[ps.size()];
            for (int i = 0; i < ps.size(); i++) {
                p[i] = ps.get(i);
            }
            ActivityCompat.requestPermissions(activity, p, REQUEST_CODE_PERMISSION);
        }
    }

    public static boolean isGrantedAllPermissions(Activity activity){
        for (String s : COMMON_PERMISSION) {
            return ActivityCompat.checkSelfPermission(activity, s) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

}
