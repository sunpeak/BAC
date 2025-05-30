package check;

import burp.api.montoya.http.message.requests.HttpRequest;
import orm.UserRel;

public interface ICheck {

    boolean check(HttpRequest httpRequest, UserRel userRel);

}
