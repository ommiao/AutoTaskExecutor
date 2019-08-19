package cn.ommiao.autotaskexecutor;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;

import com.orhanobut.logger.Logger;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import cn.ommiao.base.entity.order.Order;
import cn.ommiao.base.entity.order.Task;
import cn.ommiao.base.util.FileUtil;
import cn.ommiao.base.util.OrderUtil;
import cn.ommiao.base.util.StringUtil;

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
        assert task.orders.size() > 0;
    }

    @Test
    public void test() {


        while (true) {
            if (task.orders.size() == 0) {
                break;
            }
            Order order = task.orders.get(0);
            UiObject uiObject;

            switch (order.findRule) {
                default:
                case "device":
                    uiObject = null;
                    break;
                case "description":
                    uiObject = uiDevice.findObject(new UiSelector().description(order.uiInfo.description));
                    break;
                case "id":
                    uiObject = uiDevice.findObject(new UiSelector().resourceId(order.uiInfo.id));
                    break;
                case "text":
                    uiObject = uiDevice.findObject(new UiSelector().text(order.uiInfo.text));
                    break;
            }

            try {

                switch (order.action) {
                    default:
                        //nothing to do
                        break;
                    case "home":
                        uiDevice.pressHome();
                        break;
                    case "back":
                        uiDevice.pressBack();
                        break;
                    case "click":
                        uiObject.click();
                        break;
                    case "clickPosition":
                        int clickPosX = Integer.parseInt(order.uiInfo.position.split(",")[0]);
                        int clickPosY = Integer.parseInt(order.uiInfo.position.split(",")[1]);
                        uiDevice.click(clickPosX, clickPosY);
                        break;
                }

                if (order.delay > 0) {
                    Thread.sleep(order.delay);
                }

                if (order.repeatTimes > 1) {
                    order.repeatTimes--;
                } else {
                    task.orders.remove(0);
                }

            } catch (UiObjectNotFoundException e) {
                e.printStackTrace();
                if(order.uiInfo.alternate != null){
                    order.uiInfo = order.uiInfo.alternate;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }


    }
}
