package check;

import bac.BacData;
import bac.BacUtils;
import burp.api.montoya.http.message.Cookie;
import burp.api.montoya.http.message.HttpHeader;
import burp.api.montoya.http.message.params.HttpParameterType;
import burp.api.montoya.http.message.params.ParsedHttpParameter;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import extension.MyExtension;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import orm.AbstractKeyValue;
import orm.UserRel;
import sensitive.SensitiveData;
import sensitive.SensitiveUtils;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CollectImpl implements ICollect {

    @Override
    public UserRel collect(HttpRequest httpRequest, HttpResponse httpResponse) {
        UserRel ur = collectReq(httpRequest);
        collectResp(httpResponse, ur);
        return ur;
    }


    private UserRel collectReq(HttpRequest httpRequest) {
        String url = httpRequest.method() + " " + httpRequest.path().split("\\?")[0];

        String auth = "";
        List<HttpHeader> headers = httpRequest.headers();
        for (HttpHeader head : headers) {
            if (head.name().equals(MyExtension.AuthKey)) {
                if (head.value() != null && head.value().length() > 0) {
                    auth = head.name() + "=" + head.value();
                    break;
                }
            }
            if (head.name().equalsIgnoreCase("cookie")) {
                String[] cookieKV = head.value().split(";");
                for (String kv : cookieKV) {
                    String[] kvs = kv.trim().split("=");
                    if (kvs.length >= 2) {
                        String key = kvs[0].trim();
                        String value = kvs[1].trim();
                        if (key.equals(MyExtension.AuthKey)) {
                            auth = key + "=" + value;
                            break;
                        }
                    }
                }
            }
        }

        List<AbstractKeyValue> params = new ArrayList<>();
        List<ParsedHttpParameter> parameters = httpRequest.parameters();
        if (parameters != null) {
            for (ParsedHttpParameter p : parameters) {
                if (p.type().equals(HttpParameterType.COOKIE)) {
                    continue;
                }
                SensitiveData data = SensitiveUtils.collect(p.name(), p.value());
                if (data != null && data.getType().isIdType()) {
                    params.add(data);
                } else {
                    BacData bacData = BacUtils.collect(p.name(), p.value());
                    if (bacData != null) {
                        params.add(bacData);
                    }
                }
            }
        }

        UserRel ur = new UserRel();
        ur.setUrl(url);
        ur.setAuth(auth);
        ur.setParam(AbstractKeyValue.genStr4List(params));
        return ur;
    }


    private void collectResp(HttpResponse httpResponse, UserRel ur) {
//                过滤响应中auth
        List<Cookie> cookies = httpResponse.cookies();
        for (Cookie c : cookies) {
            if (c.name().equals(MyExtension.AuthKey)) {
                ur.setAuth(c.name() + "=" + c.value());
                break;
            }
        }

        String s;
        if (httpResponse.headers().stream().anyMatch(h -> h.value().toLowerCase().contains("charset=utf-8"))) {
            s = new String(httpResponse.body().getBytes(), StandardCharsets.UTF_8);
        } else {
            s = httpResponse.bodyToString();
        }

//                采集关键字段
        List<AbstractKeyValue> fields = new ArrayList<>();
        if (isValidJson(s)) {
            Object jsonObject;
            if (s.startsWith("[")) {
                jsonObject = JSON.parseArray(s);
            } else {
                jsonObject = JSON.parseObject(s);
            }
            traverseJson(jsonObject, fields);
        } else if (isHtmlByJsoup(s)) {
            Document doc = Jsoup.parse(s);
            Element body = doc.body();

            Elements elements = body.getAllElements(); // 获取 body 下所有元素
            for (Element el : elements) {
                String ownText = el.ownText().trim();
                if (el.tagName().equals("input") ||
                        el.tagName().equals("textarea") ||
                        el.tagName().equals("select")) {
                    ownText = el.val().trim();
                }
                if (!ownText.isEmpty()) {
                    SensitiveData data = SensitiveUtils.collect(ownText);
                    if (data != null) {
                        fields.add(data);
                    }
                }
            }
        }

        if (fields.size() > 0) {
            ur.setSensitive(AbstractKeyValue.genStr4List(fields));
        }
    }


    private void traverseJson(Object json, List<AbstractKeyValue> fields) {
        if (json instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) json;
            for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();

                if (value instanceof String || value instanceof Number) {
//                    优先提取敏感数据
                    SensitiveData data = SensitiveUtils.collect(key, value.toString());
                    if (data != null) {
                        fields.add(data);
                    }
                } else {
                    // 递归处理嵌套的 JSON 对象或数组
                    traverseJson(value, fields);
                }
            }
        } else if (json instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray) json;
            for (Object item : jsonArray) {
                traverseJson(item, fields);
            }
        }
    }

    private boolean isValidJson(String json) {
        try {
            JSON.parse(json);
            return true;
        } catch (JSONException e) {
            return false;
        }
    }

    public static boolean isHtmlByJsoup(String input) {
        if (input == null || input.trim().isEmpty()) return false;

        Document doc = Jsoup.parse(input);
        String bodyHtml = doc.body().html().trim();

        Pattern pattern = Pattern.compile(".*\\<[^>]+>.*", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(bodyHtml);

        // 如果解析后仍包含标签，说明可能是 HTML
        return matcher.matches();
    }
}
