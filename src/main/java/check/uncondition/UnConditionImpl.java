package check.uncondition;

import assistant.LogUtils;
import bac.BacUtils;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.params.HttpParameter;
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

public class UnConditionImpl implements ICheck {
    @Override
    public boolean check(HttpRequest httpRequest, UserRel userRel) {
        List<AbstractKeyValue> bacDatas = BacUtils.genList4Str(userRel.getParam());
//        值置空
        HttpRequest httpRequest1 = httpRequest.withAddedHeader("XXX-YYY", "UnCondition");
        List<ParsedHttpParameter> parameters = httpRequest1.parameters();
        for (ParsedHttpParameter p : parameters) {
            for (AbstractKeyValue keyValue : bacDatas) {
                if (keyValue.getName().equals(p.name()) && keyValue.getValue().equals(p.value())) {
                    HttpParameter parameter = HttpParameter.parameter(p.name(), "", p.type());
                    httpRequest1 = httpRequest1.withUpdatedParameters(parameter);
                }
            }
        }


        HttpRequestResponse httpRequestResponse = MyExtension.Api.http().sendRequest(httpRequest1);
        ICollect collect = new CollectImpl();
        UserRel ur = collect.collect(httpRequestResponse.request(), httpRequestResponse.response());
        LogUtils.log("UnCondition1 Request: " + ur);

        List<AbstractKeyValue> abstractKeyValues = SensitiveUtils.genList4Str(userRel.getSensitive());
        List<AbstractKeyValue> abstractKeyValues1 = SensitiveUtils.genList4Str(ur.getSensitive());

        return !ur.getSensitive().equals(userRel.getSensitive()) && new HashSet<>(abstractKeyValues1).containsAll(abstractKeyValues);
    }

}
