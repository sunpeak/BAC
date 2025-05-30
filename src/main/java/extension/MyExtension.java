package extension;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ByteArray;
import burp.api.montoya.core.HighlightColor;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.proxy.http.*;
import check.CollectImpl;
import check.ICheck;
import check.ICollect;
import check.bac.BacImpl;
import check.unauthorize.UnAuthorizeImpl;
import check.uncondition.UnCondition2Impl;
import check.uncondition.UnConditionImpl;
import orm.Req;
import orm.UserRel;

import javax.swing.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyExtension implements BurpExtension {
    public static MontoyaApi Api;

    public static String Domain;
    public static String AuthKey = "jsessionid";

    public static String AuthValue;

    public static boolean Start = false;

    public static List<String> DelFields = Collections.singletonList("pagenum");

    public static List<String> DefaultFields = Arrays.asList("id", "no", "code", "num");

    private static final List<String> staticRes = Arrays.asList(".js", ".css", ".gif", ".png", ".bmp", ".jpeg", ".jpg");

    private static final ExecutorService executorService = Executors.newFixedThreadPool(10);

    @Override
    public void initialize(MontoyaApi montoyaApi) {
        Api = montoyaApi;
        montoyaApi.extension().setName("BAC");

        JPanel myUI = new JPanel();
        BAC bac = new BAC();
        myUI.add(bac.BAC_JPanel);
        Api.userInterface().registerSuiteTab("BAC", myUI);
        Api.userInterface().applyThemeToComponent(myUI);

        Api.proxy().registerRequestHandler(new ProxyRequestHandler() {

            @Override
            public ProxyRequestReceivedAction handleRequestReceived(InterceptedRequest interceptedRequest) {
                if (!Start) {
                    return null;
                }
                if (!interceptedRequest.url().startsWith(Domain)) {
                    return null;
                }
//                过滤静态资源
                if (staticRes.stream().anyMatch(e -> interceptedRequest.path().split("\\?")[0].toLowerCase().endsWith(e))) {
                    return null;
                }

//                解密请求入库
                if (interceptedRequest.body() != null && interceptedRequest.body().length() > 0) {
                    Req.insertOne(interceptedRequest.messageId(), interceptedRequest.body().getBytes());
                }

                return null;
            }

            @Override
            public ProxyRequestToBeSentAction handleRequestToBeSent(InterceptedRequest interceptedRequest) {
                return null;
            }
        });

        Api.proxy().registerResponseHandler(new ProxyResponseHandler() {
            @Override
            public ProxyResponseReceivedAction handleResponseReceived(InterceptedResponse interceptedResponse) {
                if (!Start) {
                    return null;
                }
                if (!interceptedResponse.initiatingRequest().url().startsWith(Domain)) {
                    return null;
                }
                HttpRequest httpRequest = interceptedResponse.initiatingRequest();
//                过滤静态资源

                if (staticRes.stream().anyMatch(e -> httpRequest.path().split("\\?")[0].toLowerCase().endsWith(e))) {
                    return null;
                }

//                从DB中获取解密后的reqBody
                byte[] body = Req.findOne(interceptedResponse.messageId());
                HttpRequest httpRequest1 = interceptedResponse.initiatingRequest();
                if (body != null && body.length > 0) {
                    httpRequest1.withBody(ByteArray.byteArray(body));
                }

//                先采集
                ICollect collect = new CollectImpl();
                UserRel userRel = collect.collect(httpRequest1, interceptedResponse);

//                原始请求直接入库
                if (userRel.getSensitive() == null || "".equals(userRel.getSensitive())) {
                    return null;
                } else {
                    interceptedResponse.annotations().setNotes("敏感数据");
                }
//                Dao.insertDB(userRel);

                checkBac(interceptedResponse, httpRequest1, userRel);
                checkUnAuthorize(interceptedResponse, httpRequest1, userRel);
                checkUnCondition(interceptedResponse, httpRequest1, userRel);
                return null;
            }

            @Override
            public ProxyResponseToBeSentAction handleResponseToBeSent(InterceptedResponse interceptedResponse) {
                return null;
            }
        });


    }

    public void checkBac(InterceptedResponse interceptedResponse, HttpRequest httpRequest, UserRel userRel) {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                if (userRel.getParam() != null && !userRel.getParam().equals("") &&
                        userRel.getAuth() != null && !userRel.getAuth().equals("")) {
                    ICheck iCheck = new BacImpl();
                    if (iCheck.check(httpRequest, userRel)) {
                        String note = interceptedResponse.annotations().notes();
                        if (note == null || note.isEmpty()) {
                            interceptedResponse.annotations().setNotes("越权");
                        } else {
                            if (!note.contains("越权")) {
                                interceptedResponse.annotations().setNotes(note + ",越权");
                            }
                        }
                        interceptedResponse.annotations().setHighlightColor(HighlightColor.YELLOW);
                    }
                }
            }
        });
    }

    public void checkUnAuthorize(InterceptedResponse interceptedResponse, HttpRequest httpRequest, UserRel userRel) {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                if (userRel.getAuth() != null && !userRel.getAuth().equals("")) {
                    ICheck iCheck = new UnAuthorizeImpl();
                    if (iCheck.check(httpRequest, userRel)) {
                        String note = interceptedResponse.annotations().notes();
                        if (note == null || note.isEmpty()) {
                            interceptedResponse.annotations().setNotes("未授权");
                        } else {
                            if (!note.contains("未授权")) {
                                interceptedResponse.annotations().setNotes(note + ",未授权");
                            }
                        }
                        interceptedResponse.annotations().setHighlightColor(HighlightColor.YELLOW);
                    }
                }
            }
        });
    }

    public void checkUnCondition(InterceptedResponse interceptedResponse, HttpRequest httpRequest, UserRel userRel) {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                if (userRel.getParam() != null && !userRel.getParam().equals("")) {
                    boolean flag;
                    ICheck iCheck = new UnConditionImpl();
                    flag = iCheck.check(httpRequest, userRel);
                    if (!flag) {
                        iCheck = new UnCondition2Impl();
                        flag = iCheck.check(httpRequest, userRel);
                    }
                    if (flag) {
                        String note = interceptedResponse.annotations().notes();
                        if (note == null || note.isEmpty()) {
                            interceptedResponse.annotations().setNotes("空条件");
                        } else {
                            if (!note.contains("空条件")) {
                                interceptedResponse.annotations().setNotes(note + ",空条件");
                            }
                        }
                        interceptedResponse.annotations().setHighlightColor(HighlightColor.YELLOW);
                    }
                }
            }
        });
    }

}


