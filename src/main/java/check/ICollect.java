package check;

import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;
import orm.UserRel;

public interface ICollect {

    UserRel collect(HttpRequest httpRequest, HttpResponse httpResponse);
}
