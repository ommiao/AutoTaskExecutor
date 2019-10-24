package cn.ommiao.autotaskexecutor.utils;

import android.os.Looper;
import android.widget.Toast;

import androidx.test.core.app.ApplicationProvider;

public class ToastUtil {

    public static void shortToast(String msg){
        Looper.prepare();
        Toast.makeText(ApplicationProvider.getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
        Looper.loop();
    }

}
