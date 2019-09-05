package cn.ommiao.autotaskexecutor;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiScrollable;
import androidx.test.uiautomator.UiSelector;

import com.orhanobut.logger.Logger;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import cn.ommiao.base.entity.order.Group;
import cn.ommiao.base.entity.order.NotFoundEvent;
import cn.ommiao.base.entity.order.Order;
import cn.ommiao.base.entity.order.Task;
import cn.ommiao.base.util.FileUtil;
import cn.ommiao.base.util.OrderUtil;
import cn.ommiao.base.util.StringUtil;

import static cn.ommiao.base.entity.order.Order.INFINITE;
import static org.junit.Assert.assertNotNull;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class AutoTaskTest {
    private Context context;
    private UiDevice uiDevice;
    private Task task;

    @Before
    public void init() {
        uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        assertNotNull(uiDevice);
        try {
            uiDevice.executeShellCommand("pm grant cn.ommiao.autotaskexecutor android.permission.READ_EXTERNAL_STORAGE");
            uiDevice.executeShellCommand("pm grant cn.ommiao.autotaskexecutor android.permission.WRITE_EXTERNAL_STORAGE");
        } catch (IOException e) {
            e.printStackTrace();
            assert false;
        }
        context = ApplicationProvider.getApplicationContext();
        assertNotNull(context);
        String taskJson = FileUtil.readTask();
        Logger.d(taskJson);
        if (StringUtil.isEmpty(taskJson)){
            taskJson = OrderUtil.readOrders(context);
        }
        task = Task.fromJson(taskJson, Task.class);
        assert task.groups.size() > 0;
    }
    
    @Test
    public void test() {

        while (true){
            if(task.groups.size() == 0){
                break;
            }
            Group group = task.groups.get(0);
            for(int i = 0; i < group.repeatTimes; i++){
                int orderIndex = 0;
                boolean endFlag = false;
                while (true){
                    if(orderIndex == group.orders.size()){
                        break;
                    }
                    Order order = group.orders.get(orderIndex);
                    UiObject uiObject;
                    try {
                        switch (order.findRule) {
                            default:
                            case DEVICE:
                                uiObject = null;
                                break;
                            case DESCRIPTION:
                                uiObject = uiDevice.findObject(new UiSelector().description(order.uiInfo.description));
                                break;
                            case ID:
                                uiObject = uiDevice.findObject(new UiSelector().resourceId(order.uiInfo.id));
                                break;
                            case TEXT:
                                uiObject = uiDevice.findObject(new UiSelector().text(order.uiInfo.text));
                                break;
                            case TEXT_CONTAINS:
                                uiObject = uiDevice.findObject(new UiSelector().textContains(order.uiInfo.text));
                                break;
                            case TEXT_PARENT_ID_SCROLL:
                                UiScrollable scrollableById = new UiScrollable(new UiSelector().resourceId(order.uiInfo.id));
                                UiSelector selectorChildByText = new UiSelector().text(order.uiInfo.child.text);
                                uiObject = scrollableById.getChild(selectorChildByText);
                                scrollableById.scrollIntoView(uiObject);
                                break;
                            case CLASSNAME:
                                uiObject = uiDevice.findObject(new UiSelector().description(order.uiInfo.className));
                                break;
                        }

                        switch (order.action) {
                            default:
                                //nothing to do
                                break;
                            case HOME:
                                uiDevice.pressHome();
                                break;
                            case BACK:
                                uiDevice.pressBack();
                                break;
                            case CLICK:
                                uiObject.click();
                                break;
                            case CLICK_POSITION:
                                int clickPosX = Integer.parseInt(order.uiInfo.position.split(",")[0]);
                                int clickPosY = Integer.parseInt(order.uiInfo.position.split(",")[1]);
                                uiDevice.click(clickPosX, clickPosY);
                                break;
                            case FORCE_STOP:
                                uiDevice.executeShellCommand("am force-stop " + order.uiInfo.targetPackageName);
                                break;
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

                    } catch (UiObjectNotFoundException e) {
                        e.printStackTrace();
                        if(order.alternate != null){
                            order = order.alternate;
                            continue;
                        }
                        if(order.repeatTimes == INFINITE){
                            orderIndex++;
                            continue;
                        }
                        switch (order.notFoundEvent){
                            case ERROR:
                                //stop because error
                                return;
                            case RETRY:
                                order.notFoundEvent = NotFoundEvent.ERROR;
                                continue;
                            case IGNORE:
                                orderIndex++;
                                continue;
                            case IGNORE_GROUP:
                                endFlag = true;
                                break;
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (IOException e){
                        e.printStackTrace();
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

    }
}
