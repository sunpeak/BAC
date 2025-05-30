package check.unauthorize;

import assistant.LogUtils;
import bac.BacUtils;
import burp.api.montoya.http.message.HttpHeader;
import burp.api.montoya.http.message.HttpRequestResponse;
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

public class UnAuthorizeImpl implements ICheck {
    @Override
    public boolean check(HttpRequest httpRequest, UserRel userRel) {
        HttpRequest httpRequest1 = httpRequest.withAddedHeader("XXX-YYY", "UnAuthorize");

        List<AbstractKeyValue> authData = BacUtils.genList4Str(userRel.getAuth());
        if (authData.size() == 0) {
            return false;
        }

        httpRequest1 = httpRequest1.withRemovedHeader(MyExtension.AuthKey);
        List<HttpHeader> headers = httpRequest1.headers();
        for (HttpHeader head : headers) {
            if (head.name().equalsIgnoreCase("cookie")) {
                String cookieValue = head.value();
                String newCookieValue = cookieValue.replaceAll(authData.get(0).getName() + "=", "").replaceAll(authData.get(0).getValue(), "");
                httpRequest1 = httpRequest1.withUpdatedHeader(head.name(), newCookieValue);
            }
        }

        HttpRequestResponse httpRequestResponse = MyExtension.Api.http().sendRequest(httpRequest1);
        ICollect collect = new CollectImpl();
        UserRel ur = collect.collect(httpRequestResponse.request(), httpRequestResponse.response());
        LogUtils.log("UnAuthorize Request: " + ur);

        List<AbstractKeyValue> abstractKeyValues = SensitiveUtils.genList4Str(userRel.getSensitive());
        List<AbstractKeyValue> abstractKeyValues1 = SensitiveUtils.genList4Str(ur.getSensitive());

        return new HashSet<>(abstractKeyValues1).containsAll(abstractKeyValues);
    }
}
