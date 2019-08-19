package cn.ommiao.autotaskexecutor;

import android.app.Application;

import cn.ommiao.base.log.SimpleLogger;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        SimpleLogger.initLogger();
    }
}
