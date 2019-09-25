package cn.ommiao.autotaskexecutor;

public class ShellCommands {

    private static final String PREFIX_GRANT = "pm grant cn.ommiao.autotaskexecutor ";

    private static final String READ_EXTERNAL_STORAGE = "android.permission.READ_EXTERNAL_STORAGE";

    private static final String WRITE_EXTERNAL_STORAGE = "android.permission.WRITE_EXTERNAL_STORAGE";

    public static String getReadExternalStorageCommand(){
        return PREFIX_GRANT + READ_EXTERNAL_STORAGE;
    }

    public static String getWriteExternalStorageCommand(){
        return PREFIX_GRANT + WRITE_EXTERNAL_STORAGE;
    }

}
