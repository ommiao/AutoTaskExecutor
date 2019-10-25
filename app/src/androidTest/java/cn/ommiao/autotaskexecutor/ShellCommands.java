package cn.ommiao.autotaskexecutor;

public class ShellCommands {

    private static final String PACKAGE_MAIN = "cn.ommiao.autotask";

    private static final String ACTIVITY_MAIN = "cn.ommiao.autotask.ui.SplashActivity";

    private static final String PREFIX_AM_START = "am start -n ";

    private static final String PREFIX_GRANT = "pm grant cn.ommiao.autotaskexecutor ";

    private static final String READ_EXTERNAL_STORAGE = "android.permission.READ_EXTERNAL_STORAGE";

    private static final String WRITE_EXTERNAL_STORAGE = "android.permission.WRITE_EXTERNAL_STORAGE";

    public static String getReadExternalStorageCommand(){
        return PREFIX_GRANT + READ_EXTERNAL_STORAGE;
    }

    public static String getWriteExternalStorageCommand(){
        return PREFIX_GRANT + WRITE_EXTERNAL_STORAGE;
    }

    public static String getMainPackage(){
        return PACKAGE_MAIN;
    }

    public static String getStartMainCommand(){
        return PREFIX_AM_START + PACKAGE_MAIN + "/" + ACTIVITY_MAIN;
    }

}
