package check.uncondition;

import assistant.LogUtils;
import bac.BacUtils;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.params.ParsedHttpParameter;
import burp.api.montoya.http.message.requests.HttpRequest;
import check.CollectImpl;
import check.ICheck;
import check.ICollect;
import extension.MyExtension;
import orm.AbstractKeyValue;
import orm.UserRel;
import sensitive.SensitiveUtils;

import java.util.HashSet;
import java.util.List;

public class UnCondition2Impl implements ICheck {
    @Override
    public boolean check(HttpRequest httpRequest, UserRel userRel) {
        List<AbstractKeyValue> bacDatas = BacUtils.genList4Str(userRel.getParam());
//        删除键
        HttpRequest httpRequest1 = httpRequest.withAddedHeader("XXX-YYY", "UnCondition");
        List<ParsedHttpParameter> parameters = httpRequest1.parameters();
        for (ParsedHttpParameter p : parameters) {
            for (AbstractKeyValue keyValue : bacDatas) {
                if (keyValue.getName().equals(p.name()) && keyValue.getValue().equals(p.value())) {
                    httpRequest1 = httpRequest1.withRemovedParameters(p);
                }
            }
        }

        HttpRequestResponse httpRequestResponse = MyExtension.Api.http().sendRequest(httpRequest1);
        ICollect collect = new CollectImpl();
        UserRel ur = collect.collect(httpRequestResponse.request(), httpRequestResponse.response());
        LogUtils.log("UnCondition2 Request: " + ur);

        List<AbstractKeyValue> abstractKeyValues = SensitiveUtils.genList4Str(userRel.getSensitive());
        List<AbstractKeyValue> abstractKeyValues2 = SensitiveUtils.genList4Str(ur.getSensitive());

        return !ur.getSensitive().equals(userRel.getSensitive()) && new HashSet<>(abstractKeyValues2).containsAll(abstractKeyValues);
    }
}
