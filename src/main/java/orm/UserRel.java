package orm;

public class UserRel {
    private String url;

    private String auth;

    private String param;

//    private String field;

    private String sensitive;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAuth() {
        return auth;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public String getSensitive() {
        return sensitive;
    }

    public void setSensitive(String sensitive) {
        this.sensitive = sensitive;
    }

    @Override
    public String toString() {
        return
                "-> " + url + '\n' +
                        "-> " + auth + '\n' +
                        "-> " + param + '\n' +
                        "<- " + sensitive + '\n';
    }
}
