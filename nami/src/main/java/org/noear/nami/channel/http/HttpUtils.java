package org.noear.nami.channel.http;

import okhttp3.*;
import org.noear.nami.NamiException;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

class HttpUtils {
    private final static Dispatcher dispatcher() {
        Dispatcher temp = new Dispatcher();
        temp.setMaxRequests(3000);
        temp.setMaxRequestsPerHost(600);
        return temp;
    }

    private final static OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(60*5, TimeUnit.SECONDS)
            .writeTimeout(60*5, TimeUnit.SECONDS)
            .readTimeout(60*5, TimeUnit.SECONDS)
            .dispatcher(dispatcher())
            .build();

    public static HttpUtils http(String url){
        return new HttpUtils(url);
    }

    public static String urlEncode(String str) {
        try {
            return URLEncoder.encode(str, "utf-8");
        } catch (UnsupportedEncodingException ex) {
            throw new NamiException(ex);
        }
    }

    private String _url;
    private Charset _charset;
    private Map<String,String> _cookies;
    private RequestBody _body;
    private Map<String,String> _form;
    private MultipartBody.Builder _part_builer;

    private Request.Builder _builder;
    public HttpUtils(String url){
        _url = url;
        _builder = new Request.Builder().url(url);
    }


    //@XNote("设置charset")
    public HttpUtils charset(String charset){
        _charset = Charset.forName(charset);
        return this;
    }

    //@XNote("设置请求头")
    public HttpUtils headers(Map<String,String> headers){
        if (headers != null) {
            headers.forEach((k, v) -> {
                _builder.header(k, v);
            });
        }

        return this;
    }


    //@XNote("设置数据提交")
    public HttpUtils data(Map<String,Object> data) {
        if (data != null) {
            tryInitForm();

            data.forEach((k, v) -> {
                if (v != null) {
                    _form.put(k, v.toString());
                }
            });
        }

        return this;
    }

    public HttpUtils data(String key, String value){
        tryInitForm();
        _form.put(key,value);
        return this;
    }

    //@XNote("设置BODY提交")
    public HttpUtils bodyRaw(byte[] bytes, String contentType) {
        _body = FormBody.create(MediaType.parse(contentType), bytes);

        return this;
    }


    //@XNote("执行请求，返回响应对象")
    public Response exec(String mothod) throws Exception {
        if (_part_builer != null) {
            if (_form != null) {
                _form.forEach((k, v) -> {
                    _part_builer.addFormDataPart(k, v);
                });
            }

            _body = _part_builer.build();
        } else {
            if (_form != null) {
                FormBody.Builder fb = new FormBody.Builder(_charset);

                _form.forEach((k, v) -> {
                    fb.add(k, v);
                });

                _body = fb.build();
            }
        }

        if (_cookies != null) {
            _builder.header("Cookie", getRequestCookieString(_cookies));
        }

        switch (mothod.toUpperCase()){
            case "GET":_builder.method("GET",null);break;
            case "POST":_builder.method("POST",_body);break;
            case "PUT":_builder.method("PUT", _body);break;
            case "DELETE":_builder.method("DELETE",_body);break;
            case "PATCH":_builder.method("PATCH",_body);break;
            case "HEAD":_builder.method("HEAD",null);break;
            case "OPTIONS":_builder.method("OPTIONS",null);break;
            case "TRACE":_builder.method("TRACE",null);break;
            default: throw new RuntimeException("This method is not supported");
        }

        Call call = httpClient.newCall(_builder.build());
        return call.execute();
    }

    //@XNote("执行请求，返回字符串")
    public String exec2(String mothod) throws Exception {
        Response tmp = exec(mothod);
        int code = tmp.code();
        String text = tmp.body().string();
        if (code >= 200 && code <= 300) {
            return text;
        } else {
            throw new RuntimeException(code + "错误：" + text);
        }
    }

    private static String getRequestCookieString(Map<String,String> cookies) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;

        for(Map.Entry<String,String> kv : cookies.entrySet()){
            sb.append(kv.getKey()).append('=').append(kv.getValue());
            if (!first) {
                sb.append("; ");
            } else {
                first = false;
            }
        }

        return sb.toString();
    }

    private void tryInitForm(){
        if(_form ==null){
            _form = new HashMap<>();
        }
    }
}
