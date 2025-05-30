package check.bac;

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

import java.util.List;

public class BacImpl implements ICheck {
    @Override
    public boolean check(HttpRequest httpRequest, UserRel userRel) {
        HttpRequest httpRequest1 = httpRequest.withAddedHeader("XXX-YYY", "BAC");

        List<AbstractKeyValue> authData = BacUtils.genList4Str(userRel.getAuth());
        if (authData.size() == 0) {
            return false;
        }

        httpRequest1 = httpRequest1.withUpdatedHeader(MyExtension.AuthKey, MyExtension.AuthValue);
        List<HttpHeader> headers = httpRequest1.headers();
        for (HttpHeader head : headers) {
            if (head.name().equalsIgnoreCase("cookie")) {
                String cookieValue = head.value();
                String newCookieValue = cookieValue.replaceAll(authData.get(0).getValue(), MyExtension.AuthValue);
                httpRequest1 = httpRequest1.withUpdatedHeader(head.name(), newCookieValue);
            }
        }

        HttpRequestResponse httpRequestResponse = MyExtension.Api.http().sendRequest(httpRequest1);
        ICollect collect = new CollectImpl();
        UserRel ur = collect.collect(httpRequestResponse.request(), httpRequestResponse.response());
        LogUtils.log("BAC Request: " + ur);

        return !ur.getSensitive().isEmpty() && userRel.getSensitive().equals(ur.getSensitive());
    }

    public static void main(String[] args) {
        System.out.println("1002260927270007");
    }
}
