package org.sonar.common.utils;

import com.alibaba.fastjson.JSON;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

/**
 * @author
 * @description HTTP请求工具类
 */
public class HttpUtils {

    private HttpUtils(){}

    /**
     * 发送GET请求
     *
     * @param url         请求URL
     * @param queryParams 请求参数
     * @return 响应结果
     */
    public static String doGet(String url, Map<String, Object> queryParams)
            throws IOException {
        // 拼接参数
        if (queryParams != null && !queryParams.isEmpty()) {
            url += "?" + urlEncode(queryParams);
        }

        // GET请求
        HttpGet get = new HttpGet(url);
        // 请求配置
        get.setConfig(requestConfig());

        // 执行请求
        return getResult(get);
    }

    /**
     * 发送POST请求
     *
     * @param url        请求URL
     * @param bodyParams Body参数
     * @return 响应结果
     */
    public static String doPost(String url, Map<String, Object> bodyParams)
            throws IOException {
        // POST请求
        HttpPost post = new HttpPost(url);
        // 设置Body参数
        setBodyParams(post, bodyParams);
        // 请求配置
        post.setConfig(requestConfig());

        // 执行请求
        return getResult(post);
    }

    /**
     * 发送带Header的GET请求
     *
     * @param url         请求URL
     * @param headers     请求头
     * @param queryParams 查询参数
     * @return 响应结果
     */
    public static String doGet(String url, Map<String, String> headers, Map<String, Object> queryParams)
            throws IOException {
        // 拼接参数
        if (queryParams != null && !queryParams.isEmpty()) {
            url += "?" + urlEncode(queryParams);
        }

        // GET请求
        HttpGet get = new HttpGet(url);
        // 设置请求头
        setHeaders(get, headers);
        // 请求配置
        get.setConfig(requestConfig());

        // 执行请求
        return getResult(get);
    }

    /**
     * 发送带Header的POST请求
     *
     * @param url        请求URL
     * @param headers    请求头
     * @param bodyParams Body参数
     * @return 响应结果
     */
    public static String doPost(String url, Map<String, String> headers, Map<String, Object> bodyParams)
            throws IOException {
        // POST请求
        HttpPost post = new HttpPost(url);
        // 设置请求头
        setHeaders(post, headers);
        // 设置Body参数
        setBodyParams(post, bodyParams);
        // 请求配置
        post.setConfig(requestConfig());

        // 执行请求
        return getResult(post);
    }

    /**
     * 执行HTTP请求
     *
     * @param requestBase HTTP请求
     * @return 响应结果
     */
    private static String getResult(HttpRequestBase requestBase)
            throws IOException {
        CloseableHttpClient httpClient = null;
        String result = null;
        try {
            // HTTP客户端
            httpClient = HttpClients.createDefault();
            // 响应结果
            result = EntityUtils.toString(httpClient.execute(requestBase).getEntity());
        }finally {
            if (httpClient != null) {
                httpClient.close();
            }
        }

        // 返回响应结果
        return result;
    }

    /**
     * 请求配置
     *
     * @return 请求配置
     */
    private static RequestConfig requestConfig() {
        return RequestConfig
                .custom()
                // 连接主机超时时间
                .setConnectTimeout(35000)
                // 请求超时时间
                .setConnectionRequestTimeout(35000)
                // 数据读取超时时间
                .setSocketTimeout(60000)
                .build();
    }

    /**
     * 设置请求头
     *
     * @param requestBase HTTP请求
     * @param headers     请求头
     */
    private static void setHeaders(HttpRequestBase requestBase, Map<String, String> headers) {
        // 设置请求头
        if (headers != null && !headers.isEmpty()) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                requestBase.setHeader(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * URL参数编码
     *
     * @param queryParams 请求参数
     * @return 参数编码结果
     */
    private static String urlEncode(Map<?, ?> queryParams)
            throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<?, ?> entry : queryParams.entrySet()) {
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(String.format("%s=%s",
                    URLEncoder.encode(entry.getKey().toString(), "UTF-8"),
                    URLEncoder.encode(entry.getValue().toString(), "UTF-8")
            ));
        }
        return sb.toString();
    }

    /**
     * 设置Body参数
     *
     * @param post       POST请求
     * @param bodyParams Body参数
     */
    private static void setBodyParams(HttpPost post, Map<?, ?> bodyParams)
            throws UnsupportedEncodingException {
        HttpEntity entity = new StringEntity(JSON.toJSONString(bodyParams));
        post.setEntity(entity);
    }


    /**
     * 获取用户 cookie
     * @param urlParam url 地址
     * @param path  请求拼接地址信息
     * @throws IOException
     */
    public static String getConnectCookie(String urlParam,String path) throws IOException{
        String cookie = null;
        HttpURLConnection conn = null;
        OutputStream os = null;
        InputStream is = null;
        try {
            URL url = new URL(urlParam);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            os = conn.getOutputStream();
            //这里是把post参数携带上去。
            os.write(path.getBytes("utf-8"));
            is = conn.getInputStream();
            byte[] b = new byte[1024];
            int len = is.read(b);
            while(len != -1){
                len = is.read(b);
            }
            //这里是读取第一次登陆时服务器返回的cookie，然后用一个全局变量cookie接收。因为是服务器往客户端发送cookie，所以名字是Set-Cookie
            cookie = conn.getHeaderField("Set-Cookie");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }   finally{
            if(os != null){
                os.close();
            }
            if(is != null){
                is.close();
            }
            if(conn != null){
                conn.disconnect();
            }
        }
        return cookie;
    }

}