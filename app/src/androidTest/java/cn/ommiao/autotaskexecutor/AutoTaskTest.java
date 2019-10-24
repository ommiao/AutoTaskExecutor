package cn.ommiao.autotaskexecutor;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;
import androidx.test.uiautomator.BySelector;
import androidx.test.uiautomator.Configurator;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;

import com.orhanobut.logger.Logger;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import cn.ommiao.base.entity.actionhelper.BaseActionHelper;
import cn.ommiao.base.entity.order.ExceptionEvent;
import cn.ommiao.base.entity.order.ExecuteResult;
import cn.ommiao.base.entity.order.Group;
import cn.ommiao.base.entity.order.Order;
import cn.ommiao.base.entity.order.Task;
import cn.ommiao.base.entity.order.UiInfo;
import cn.ommiao.base.exception.InjectEventException;
import cn.ommiao.base.exception.ShellCommandException;
import cn.ommiao.base.util.FileUtil;
import cn.ommiao.base.util.OrderUtil;
import cn.ommiao.base.util.StringUtil;

import static cn.ommiao.base.entity.order.Order.INFINITE;
import static cn.ommiao.base.util.Constant.URI_EXECUTE_RESULT;
import static org.junit.Assert.assertNotNull;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class AutoTaskTest {

    private static final long DEFAULT_TIMEOUT = 5000;

    private Context context;
    private UiDevice uiDevice;
    private Task task;
    private ExecuteResult executeResult;

    @Before
    public void init() {
        uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        assertNotNull(uiDevice);
        try {
            uiDevice.executeShellCommand(ShellCommands.getReadExternalStorageCommand());
            uiDevice.executeShellCommand(ShellCommands.getWriteExternalStorageCommand());
        } catch (IOException e) {
            e.printStackTrace();
            assert false;
        }
        context = ApplicationProvider.getApplicationContext();
        assertNotNull(context);
        String taskJson = FileUtil.readTask();
        if (StringUtil.isEmpty(taskJson)){
            Logger.d("No task, execute test task.");
            taskJson = OrderUtil.readOrders(context);
        }
        Logger.d(taskJson);
        task = Task.fromJson(taskJson, Task.class);
        executeResult = new ExecuteResult();
        executeResult.taskId = task.taskId;
        executeResult.taskName = task.taskName;
        assert task.groups.size() > 0;
    }
    
    @Test
    public void test() {
        executeResult.startTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.CHINA).format(new Date());
        while (true){
            if(task.groups.size() == 0){
                break;
            }
            Group group = task.groups.get(0);
            for(int i = 0; i < group.repeatTimes; i++){
                int orderIndex = 0;
                boolean endFlag = false;
                while (true){
                    if(orderIndex >= group.orders.size()){
                        break;
                    }
                    Order order = group.orders.get(orderIndex);
                    long timeout = order.timeout == 0 ? DEFAULT_TIMEOUT : order.timeout;
                    Configurator.getInstance().setWaitForSelectorTimeout(timeout < 1000 ? 1000 : timeout);
                    SelectorBuilder builder = SelectorBuilder.getInstance();
                    UiObject uiObject;
                    UiObject2 uiObject2;
                    UiSelector uiSelector, uiSelectorP;
                    BySelector bySelector, bySelectorP;
                    try {

                        UiInfo uiInfo = order.uiInfo;
                        builder.bind(uiInfo);
                        uiSelector = builder.buildUiSelector();
                        bySelector = builder.buildBySelector();

                        if(order.uiInfo.parent != null){
                            UiInfo parent = order.uiInfo.parent;
                            builder.bind(parent);
                            uiSelectorP = builder.buildUiSelector();
                            bySelectorP = builder.buildBySelector();
                            uiObject = uiDevice.findObject(uiSelectorP).getChild(uiSelector);
                            uiObject2 = uiDevice.findObject(bySelectorP).findObject(bySelector);
                        } else {
                            uiObject = uiDevice.findObject(uiSelector);
                            uiObject2 = uiDevice.findObject(bySelector);
                        }

                        BaseActionHelper actionHelper = order.action.getActionHelper().with(order);
                        if(actionHelper.isGlobalAction()){
                            actionHelper.performGlobalAction(uiDevice);
                        } else {
                            actionHelper.performWidgetAction(uiObject, uiObject2);
                        }

                        if (order.delay > 0) {
                            Thread.sleep(order.delay);
                        }

                        if (order.repeatTimes > 1) {
                            order.repeatTimes--;
                            continue;
                        } else if(order.repeatTimes == INFINITE) {
                            continue;
                        } else {
                            orderIndex += 1;
                            continue;
                        }

                    } catch (Exception e){
                        e.printStackTrace();
                        if(order.alternate != null){
                            order = order.alternate;
                            continue;
                        }
                        if(order.repeatTimes == INFINITE){
                            orderIndex++;
                            continue;
                        }
                        switch (order.exceptionEvent){
                            case RETRY:
                                order.exceptionEvent = ExceptionEvent.ERROR;
                                continue;
                            case IGNORE_ORDER:
                                orderIndex += order.exceptionParam.IGNORE_ORDER_COUNT;
                                continue;
                            case IGNORE_GROUP:
                                endFlag = true;
                                break;
                        }
                        if(e instanceof UiObjectNotFoundException){
                            execTaskFail(e.getMessage());
                            return;
                        } else if(e instanceof InjectEventException){
                            execTaskFail("无法执行USB模拟点击：" + e.getMessage());
                            return;
                        } else if(e instanceof ShellCommandException){
                            execTaskFail("无法执行命令：" + e.getMessage());
                            return;
                        }  else if(e instanceof InterruptedException || e instanceof IOException){
                            execTaskFail("未知错误：" + e.getMessage());
                            return;
                        }
                    }
                    if(endFlag){
                        break;
                    }
                }
                if(endFlag){
                    break;
                }
            }
            task.groups.remove(0);
        }

        execTaskSuccess();

    }

    private void execTaskSuccess(){
        executeResult.endTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.CHINA).format(new Date());
        executeResult.success = true;
        Logger.d("Task [id:" + task.taskId + ", name:" + task.taskName + "] executed successfully.");
        reportExecuteResult();
    }

    private void execTaskFail(String errorReason){
        executeResult.endTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.CHINA).format(new Date());
        executeResult.success = false;
        executeResult.errorReason = errorReason;
        Logger.d("Task [id:" + task.taskId + ", name:" + task.taskName + "] executed failed.");
        reportExecuteResult();
    }

    private void reportExecuteResult(){
        Uri uri = Uri.parse(URI_EXECUTE_RESULT);
        ContentValues values = new ContentValues();
        values.put("executeResult", executeResult.toJson());
        context.getContentResolver().insert(uri, values);
    }


}
