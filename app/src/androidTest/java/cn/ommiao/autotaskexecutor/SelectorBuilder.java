package cn.ommiao.autotaskexecutor;

import androidx.test.uiautomator.By;
import androidx.test.uiautomator.BySelector;
import androidx.test.uiautomator.UiSelector;

import cn.ommiao.base.entity.order.FindRule;
import cn.ommiao.base.entity.order.UiInfo;

public class SelectorBuilder {

    private UiInfo uiInfo;

    public static SelectorBuilder getInstance(){
        return new SelectorBuilder();
    }

    public void bind(UiInfo uiInfo){
        this.uiInfo = uiInfo;
    }

    public UiSelector buildUiSelector(){
        UiSelector uiSelector = new UiSelector();
        for (FindRule findRule : uiInfo.findRules.keySet()) {
            uiSelector = findRule.bindSelector(uiSelector, uiInfo.findRules.get(findRule));
        }
        return uiSelector;
    }

    public BySelector buildBySelector(){
        BySelector bySelector = By.descContains("");
        for (FindRule findRule : uiInfo.findRules.keySet()) {
            bySelector = findRule.bindSelector(bySelector, uiInfo.findRules.get(findRule));
        }
        return bySelector;
    }

}
