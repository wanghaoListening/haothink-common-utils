package com.haothink.common.utils;

import com.dianping.cat.Cat;
import com.dianping.cat.CatConstants;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.CharsetUtils;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * http请求工具类 支持https
 * Created by wanghao on 16/3/21.
 */
public class HttpClientUtils {

    private static String EMPTY_STR = "";
    private static String UTF_8 = "UTF-8";
    private static RequestConfig requestConfig;
    /**
     * 设置连接超时时间 单位毫秒
     */
    private static final int CONN_TIME_OUT = 5000;
    /**
     * 请求超时时间
     * 设置从connect Manager获取Connection 超时时间，单位毫秒
     */
    private static final int CONN_REQUEST_TIME_OUT = 5000;
    /**
     * 数据处理超时时间
     * 请求获取数据的超时时间，单位毫秒。 如果访问一个接口，多少时间内无法返回数据
     */
    private static final int SOCKET_TIME_OUT = 5000;
    /**
     * 连接池的最大连接数
     */
    private static final int MAX_TOTAL_CONNECTION = 200;
    /**
     * 每个路由的最大连接数
     */
    private static final int MAX_CONNECTION_PER_ROUTE = 100;
    private static final String HTTP_PREFIX = "HTTP";
    private static final String HTTP_HEADER = "HTTP:";
    private static final String JSONP_CALLBACK = "callback";
    private static CloseableHttpClient closeableHttpClient;
    private static final String HTTP_SPLIT = "//";
    /**
     * 业务方定义上报Cat的URL
     */
    public static ThreadLocal<String> REPORT_URL = new ThreadLocal<>();

    private static final Logger logger = LoggerFactory.getLogger(HttpClientUtils.class);

    static {

        //定义 http连接的 策略  可以允许 http和 https
        ConnectionSocketFactory plainsf = PlainConnectionSocketFactory.getSocketFactory();
        LayeredConnectionSocketFactory sslsf = createSSLConnSocketFactory();
        Registry<ConnectionSocketFactory> registry = RegistryBuilder
                .<ConnectionSocketFactory>create().register("http", plainsf)
                .register("https", sslsf).build();

        //初始化http请求池
        PoolingHttpClientConnectionManager cm;
        cm = new PoolingHttpClientConnectionManager(registry);
        cm.setMaxTotal(MAX_TOTAL_CONNECTION);
        cm.setDefaultMaxPerRoute(MAX_CONNECTION_PER_ROUTE);
        closeableHttpClient = HttpClients
                .custom()
                .setConnectionManager(cm)
                .build();

        //初始化请求参数
        requestConfig = RequestConfig.custom()
                .setConnectTimeout(CONN_TIME_OUT)
                .setConnectionRequestTimeout(CONN_REQUEST_TIME_OUT)
                .setSocketTimeout(SOCKET_TIME_OUT)
                .build();
    }


    /**
     * 通过连接池获取HttpClient
     *
     * @return
     */
    private static CloseableHttpClient getHttpClient() {
        return closeableHttpClient;
    }

    public static String httpGetRequest(String url) throws IOException {
        return httpGetRequest(url, UTF_8);
    }


    public static String httpGetRequest(String url, String charSet) throws IOException {
        HttpGet httpGet = buildHttpGet(url);
        return getResult(httpGet, charSet);
    }

    public static String httpGetRequest(String url, RequestConfig requestConfig) throws IOException {
        HttpGet httpGet = buildHttpGet(url);
        return getResult(httpGet, UTF_8, requestConfig);
    }

    public static String httpGetRequest(String url, Map<String, Object> params) throws URISyntaxException, IOException {
        return httpGetRequest(url, params, UTF_8);
    }

    public static String httpGetRequest(String url, Map<String, Object> params, RequestConfig requestConfig) throws URISyntaxException, IOException {
        URIBuilder ub = new URIBuilder();
        ub.setPath(processURL(url));

        ArrayList<NameValuePair> pairs = covertParams2NVPS(params);
        ub.setParameters(pairs);

        HttpGet httpGet = new HttpGet(ub.build());
        return getResult(httpGet, UTF_8, requestConfig);
    }

    public static String httpGetRequest(String url, Map<String, Object> params, List<BasicClientCookie> cookieList) throws URISyntaxException, IOException {
        return httpGetRequest(url, params, UTF_8, cookieList);
    }

    public static String httpGetRequest(String url, Map<String, Object> params, String charSet) throws URISyntaxException, IOException {
        return httpGetRequest(url, params, charSet, null);
    }

    public static String httpGetRequest(String url, Map<String, Object> params, String charSet, List<BasicClientCookie> cookieList) throws URISyntaxException, IOException {
        URIBuilder ub = new URIBuilder();
        ub.setPath(processURL(url));

        ArrayList<NameValuePair> pairs = covertParams2NVPS(params);
        ub.setParameters(pairs);

        HttpGet httpGet = new HttpGet(ub.build());

        //处理cookie
        HttpContext httpContext = processCookie(cookieList);

        return getResult(httpGet, charSet, httpContext);
    }

    public static String httpGetRequest(String url, Map<String, Object> headers, Map<String, Object> params) throws URISyntaxException, IOException {
        return httpGetRequest(url, headers, params, UTF_8);
    }

    public static String httpJsonpGetRequest(String url, Map<String, Object> headers, Map<String, Object> params) throws URISyntaxException, IOException {
        params.put(JSONP_CALLBACK, JSONP_CALLBACK);
        String result = httpGetRequest(url, headers, params, UTF_8);
        return getJsonString(result, JSONP_CALLBACK);
    }

    public static String httpGetRequest(String url, Map<String, Object> headers, Map<String, Object> params, String charSet, List<BasicClientCookie> cookieList) throws URISyntaxException, IOException {
        URIBuilder ub = new URIBuilder();
        ub.setPath(processURL(url));

        ArrayList<NameValuePair> pairs = covertParams2NVPS(params);
        ub.setParameters(pairs);

        HttpGet httpGet = new HttpGet(ub.build());
        for (Map.Entry<String, Object> param : headers.entrySet()) {
            httpGet.addHeader(param.getKey(), String.valueOf(param.getValue()));
        }

        //处理cookie
        HttpContext httpContext = processCookie(cookieList);

        return getResult(httpGet, charSet, httpContext);
    }

    public static String httpGetRequest(String url, Map<String, Object> headers, Map<String, Object> params, String charSet) throws URISyntaxException, IOException {
        URIBuilder ub = new URIBuilder();
        ub.setPath(processURL(url));

        ArrayList<NameValuePair> pairs = covertParams2NVPS(params);
        ub.setParameters(pairs);

        HttpGet httpGet = new HttpGet(ub.build());
        for (Map.Entry<String, Object> param : headers.entrySet()) {
            httpGet.addHeader(param.getKey(), String.valueOf(param.getValue()));
        }
        return getResult(httpGet, charSet);
    }


    public static String httpGetRequestSSL(String url, Map<String, Object> headers, Map<String, Object> params) throws Exception {
        return httpGetRequestSSL(url, headers, params, UTF_8);
    }

    public static String httpGetRequestSSL(String url, Map<String, Object> headers, Map<String, Object> params, String charSet) throws Exception {
        URIBuilder ub = new URIBuilder();
        ub.setPath(processURL(url));

        ArrayList<NameValuePair> pairs = covertParams2NVPS(params);
        ub.setParameters(pairs);

        HttpGet httpGet = new HttpGet(ub.build());
        for (Map.Entry<String, Object> param : headers.entrySet()) {
            httpGet.addHeader(param.getKey(), String.valueOf(param.getValue()));
        }

        return getSSLResult(httpGet, charSet);
    }

    public static String httpPostRequest(String url) throws IOException {
        HttpPost httpPost = buildHttpPost(url);
        return getResult(httpPost, UTF_8);
    }

    public static String httpPostRequest(String url, Map<String, Object> params) throws IOException {
        return httpPostRequest(url, params, UTF_8);
    }

    public static String httpPostRequest(String url, Map<String, Object> params, RequestConfig requestConfig) throws IOException {
        HttpPost httpPost = buildHttpPost(url);
        ArrayList<NameValuePair> pairs = covertParams2NVPS(params);

        httpPost.setEntity(new UrlEncodedFormEntity(pairs, UTF_8));
        return getResult(httpPost, UTF_8, requestConfig);
    }

    public static String httpPostRequest(String url, Map<String, Object> params, String charSet) throws IOException {
        HttpPost httpPost = buildHttpPost(url);
        ArrayList<NameValuePair> pairs = covertParams2NVPS(params);

        httpPost.setEntity(new UrlEncodedFormEntity(pairs, UTF_8));
        return getResult(httpPost, charSet);
    }


    public static String httpPostRequest(String url, String data, String contentType) throws IOException {
        return httpPostRequest(url, data, contentType, UTF_8);
    }

    public static String httpPostRequest(String url, Map<String, Object> headers, String data, String contentType) throws IOException {
        HttpPost httpPost = buildHttpPost(url);
        for (Map.Entry<String, Object> param : headers.entrySet()) {
            httpPost.addHeader(param.getKey(), String.valueOf(param.getValue()));
        }
        StringEntity stringEntity = new StringEntity(data, ContentType.create(contentType, Consts.UTF_8));
        httpPost.setEntity(stringEntity);
        return getResult(httpPost, UTF_8);
    }


    public static String httpPostRequest(String url, String data, String contentType, String charSet) throws IOException {
        HttpPost httpPost = buildHttpPost(url);
        StringEntity stringEntity = new StringEntity(data, ContentType.create(contentType, Consts.UTF_8));
        httpPost.setEntity(stringEntity);
        return getResult(httpPost, charSet);
    }

    public static String httpPostRequest(String url, String data, String contentType, String charSet, RequestConfig requestConfig) throws IOException {
        HttpPost httpPost = buildHttpPost(url);
        StringEntity stringEntity = new StringEntity(data, ContentType.create(contentType, Consts.UTF_8));
        httpPost.setEntity(stringEntity);
        return getResult(httpPost, charSet, requestConfig);
    }

    public static String httpPostRequest(String url, Map<String, Object> headers, Map<String, Object> params) throws IOException {
        return httpPostRequest(url, headers, params, UTF_8);
    }

    public static String httpJsonpPostRequest(String url, Map<String, Object> headers, Map<String, Object> params) throws IOException {
        params.put(JSONP_CALLBACK, JSONP_CALLBACK);
        String result = httpPostRequest(url, headers, params, UTF_8);
        return getJsonString(result, JSONP_CALLBACK);
    }

    public static String httpPostRequest(String url, Map<String, Object> headers, Map<String, Object> params, List<BasicClientCookie> cookieList) throws IOException {
        return httpPostRequest(url, headers, params, UTF_8, cookieList);
    }

    public static String httpPostRequest(String url, Map<String, Object> headers, Map<String, Object> params, String charSet) throws IOException {
        return httpPostRequest(url, headers, params, charSet, null);
    }

    public static String httpPostRequest(String url, Map<String, Object> headers, Map<String, Object> params, String charSet, List<BasicClientCookie> cookieList) throws IOException {
        HttpPost httpPost = buildHttpPost(url);

        for (Map.Entry<String, Object> param : headers.entrySet()) {
            httpPost.addHeader(param.getKey(), String.valueOf(param.getValue()));
        }

        ArrayList<NameValuePair> pairs = covertParams2NVPS(params);
        httpPost.setEntity(new UrlEncodedFormEntity(pairs, UTF_8));

        //处理cookie
        HttpContext httpContext = processCookie(cookieList);

        return getResult(httpPost, charSet, httpContext);
    }

    public static String httpPostRequestSSL(String url, Map<String, Object> headers, Map<String, Object> params) throws Exception {
        return httpPostRequestSSL(url, headers, params, UTF_8);
    }


    public static String httpPostRequestSSL(String url, Map<String, Object> headers, Map<String, Object> params, String charSet) throws Exception {
        HttpPost httpPost = buildHttpPost(url);

        for (Map.Entry<String, Object> param : headers.entrySet()) {
            httpPost.addHeader(param.getKey(), String.valueOf(param.getValue()));
        }

        ArrayList<NameValuePair> pairs = covertParams2NVPS(params);
        httpPost.setEntity(new UrlEncodedFormEntity(pairs, UTF_8));

        return getSSLResult(httpPost, charSet);
    }

    public static String httpPostRequestSSL(String url, Map<String, Object> headers, String data, String contentType) throws Exception {
        HttpPost httpPost = buildHttpPost(url);

        for (Map.Entry<String, Object> param : headers.entrySet()) {
            httpPost.addHeader(param.getKey(), String.valueOf(param.getValue()));
        }
        StringEntity stringEntity = new StringEntity(data, ContentType.create(contentType, Consts.UTF_8));
        httpPost.setEntity(stringEntity);

        return getSSLResult(httpPost, UTF_8);
    }



    /**
     * Http get请求
     * 使用代理
     *
     * @param url
     * @param headers
     * @param params
     * @param charSet
     * @param proxyHost
     * @param proxyPort
     * @return
     * @throws URISyntaxException
     * @throws IOException
     */
    public static String httpGetRequestWithProxy(String url, Map<String, Object> headers, Map<String, Object> params, String charSet, String proxyHost, Integer proxyPort) throws URISyntaxException, IOException {

        RequestConfig config = buildRequestConfigWithProxy(proxyHost, proxyPort);
        URIBuilder ub = new URIBuilder();
        ub.setPath(processURL(url));

        ArrayList<NameValuePair> pairs = covertParams2NVPS(params);
        ub.setParameters(pairs);

        HttpGet httpGet = new HttpGet(ub.build());
        for (Map.Entry<String, Object> param : headers.entrySet()) {
            httpGet.addHeader(param.getKey(), String.valueOf(param.getValue()));
        }
        return getResult(httpGet, charSet, null, config);
    }
    /**
     * Http get请求
     * 使用代理
     *
     * @param url
     * @param headers
     * @param params
     * @param charSet
     * @param config
     * @return
     * @throws URISyntaxException
     * @throws IOException
     */
    public static String httpGetRequestWithProxy(String url, Map<String, Object> headers, Map<String, Object> params, String charSet,  RequestConfig config) throws URISyntaxException, IOException {
        URIBuilder ub = new URIBuilder();
        ub.setPath(processURL(url));
        ArrayList<NameValuePair> pairs = covertParams2NVPS(params);
        ub.setParameters(pairs);

        HttpGet httpGet = new HttpGet(ub.build());
        for (Map.Entry<String, Object> param : headers.entrySet()) {
            httpGet.addHeader(param.getKey(), String.valueOf(param.getValue()));
        }
        return getResult(httpGet, charSet, null, config);
    }

    /**
     * Http post请求
     * 使用代理
     *
     * @param url
     * @param data
     * @param contentType
     * @param charSet
     * @param proxyHost
     * @param proxyPort
     * @return
     * @throws IOException
     */
    public static String httpPostRequestWithProxy(String url, String data, String contentType, String charSet, String proxyHost, Integer proxyPort) throws IOException {
        RequestConfig config = buildRequestConfigWithProxy(proxyHost, proxyPort);
        HttpPost httpPost = buildHttpPost(url);
        StringEntity stringEntity = new StringEntity(data, ContentType.create(contentType, Consts.UTF_8));
        httpPost.setEntity(stringEntity);
        return getResult(httpPost, charSet, config);
    }

    /**
     * Http post请求
     * 使用代理
     *
     * @param url
     * @param data
     * @param contentType
     * @param proxyHost
     * @param proxyPort
     * @return
     * @throws IOException
     */
    public static String httpPostRequestWithProxy(String url, Map<String, Object> headers, String data, String contentType, String proxyHost, Integer proxyPort) throws IOException {
        RequestConfig config = buildRequestConfigWithProxy(proxyHost, proxyPort);
        HttpPost httpPost = buildHttpPost(url);
        for (Map.Entry<String, Object> param : headers.entrySet()) {
            httpPost.addHeader(param.getKey(), String.valueOf(param.getValue()));
        }
        StringEntity stringEntity = new StringEntity(data, ContentType.create(contentType, Consts.UTF_8));
        httpPost.setEntity(stringEntity);
        return getResult(httpPost, UTF_8, config);
    }

    /**
     * Http post请求
     * 使用代理
     * @param url
     * @param params
     * @param headers
     * @param charSet
     * @param proxyHost
     * @param proxyPort
     * @return
     * @throws IOException
     */
    public static String httpPostRequestWithProxy(String url, Map<String, Object> params, Map<String, Object> headers,String charSet,String proxyHost, Integer proxyPort) throws IOException {
        RequestConfig config = buildRequestConfigWithProxy(proxyHost, proxyPort);
        HttpPost httpPost = buildHttpPost(url);

        for (Map.Entry<String, Object> param : headers.entrySet()) {
            httpPost.addHeader(param.getKey(), String.valueOf(param.getValue()));
        }

        ArrayList<NameValuePair> pairs = covertParams2NVPS(params);
        httpPost.setEntity(new UrlEncodedFormEntity(pairs, UTF_8));
        return getResult(httpPost, charSet,config);
    }

    /**
     * 构建 RequestConfig 并配置proxy参数
     *
     * @param proxyHost
     * @param proxyPort
     * @return
     */
    private static RequestConfig buildRequestConfigWithProxy(String proxyHost, Integer proxyPort) {
        HttpHost proxy = null;
        if (proxyHost != null && proxyPort != null) {
            proxy = new HttpHost(proxyHost, proxyPort);
        }
        return RequestConfig.custom()
                .setConnectTimeout(CONN_TIME_OUT)
                .setConnectionRequestTimeout(CONN_REQUEST_TIME_OUT)
                .setSocketTimeout(SOCKET_TIME_OUT)
                .setProxy(proxy)
                .build();
    }

    private static ArrayList<NameValuePair> covertParams2NVPS(Map<String, Object> params) {
        ArrayList<NameValuePair> pairs = new ArrayList<NameValuePair>();
        for (Map.Entry<String, Object> param : params.entrySet()) {
            pairs.add(new BasicNameValuePair(param.getKey(), String.valueOf(param.getValue())));
        }

        return pairs;
    }

    public static String httpPostRequest(String url, Map<String, Object> params, String charSet, boolean isNullToString) throws IOException {
        HttpPost httpPost = buildHttpPost(url);
        ArrayList<NameValuePair> pairs = covertParams2NVPS(params, isNullToString);

        httpPost.setEntity(new UrlEncodedFormEntity(pairs, UTF_8));
        return getResult(httpPost, charSet);
    }

    private static ArrayList<NameValuePair> covertParams2NVPS(Map<String, Object> params, boolean isNullToString) {
        ArrayList<NameValuePair> pairs = new ArrayList<NameValuePair>();
        for (Map.Entry<String, Object> param : params.entrySet()) {
            Object object = param.getValue();
            if (object == null && !isNullToString) {
                pairs.add(new BasicNameValuePair(param.getKey(), null));
            } else {
                pairs.add(new BasicNameValuePair(param.getKey(), String.valueOf(param.getValue())));
            }
        }

        return pairs;
    }

    /**
     * 创建SSL安全连接
     *
     * @return
     */
    private static SSLConnectionSocketFactory createSSLConnSocketFactory() {
        SSLConnectionSocketFactory sslsf = null;
        try {
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
                public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    return true;
                }
            }).build();
            sslsf = new SSLConnectionSocketFactory(sslContext, new X509HostnameVerifier() {
                @Override
                public boolean verify(String arg0, SSLSession arg1) {
                    return true;
                }

                @Override
                public void verify(String host, SSLSocket ssl) throws IOException {
                }

                @Override
                public void verify(String host, X509Certificate cert) throws SSLException {
                }

                @Override
                public void verify(String host, String[] cns, String[] subjectAlts) throws SSLException {
                }
            });
        } catch (Exception e) {
            logger.error("createSSLConnSocketFactory异常", e);
        }
        return sslsf;
    }


    /**
     * 处理Http请求
     *
     * @param request
     * @return
     */
    private static String getResult(HttpRequestBase request, String charSet) throws IOException {
        return getResult(request, charSet, null, requestConfig);
    }


    /**
     * 处理Http请求
     *
     * @param request
     * @return
     */
    private static String getResult(HttpRequestBase request, String charSet, HttpContext httpContext) throws IOException {
        return getResult(request, charSet, httpContext, requestConfig);
    }


    /**
     * 处理Http请求
     *
     * @param request
     * @return
     */
    private static String getResult(HttpRequestBase request, String charSet, RequestConfig requestConfig) throws IOException {
        return getResult(request, charSet, null, requestConfig);
    }


    /**
     * 处理Http请求
     *
     * @param request
     * @return
     */
    private static String getResult(HttpRequestBase request, String charSet, HttpContext httpContext, RequestConfig requestConfig) throws IOException {
        Transaction t = null;
        try {
            String name = REPORT_URL.get();
            if (StringUtils.isBlank(name)) {
                name = request.getURI().toString();
                if (name.indexOf("?") != -1) {
                    name = name.substring(0, name.indexOf("?"));
                }
            }
            t = Cat.newTransaction(CatConstants.TYPE_REQUEST, name);
        } catch (Exception e) {
            logger.error("cat Transaction init error", e);
        }

        try {
            request.setConfig(requestConfig);
            CloseableHttpClient httpClient = getHttpClient();
            CloseableHttpResponse response = httpClient.execute(request, httpContext);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                String result = EntityUtils.toString(entity, charSet);
                response.close();
                if (null != t) {
                    t.setStatus(Message.SUCCESS);
                }
                return result;
            }
        } catch (Exception e){
            if (null != t) {
                t.setStatus(e);
            }
            throw e;
        } finally {
            if (null != t) {
                t.complete();
            }
            REPORT_URL.remove();
        }
        return EMPTY_STR;
    }

    /**
     * 处理Http请求
     *
     * @param request
     * @return
     */
    private static String getSSLResult(HttpRequestBase request, String charSet) throws Exception {
        return getResult(request, charSet, null, requestConfig);
    }

    private static HttpPost buildHttpPost(String url) {
        return new HttpPost(processURL(url));
    }

    private static HttpGet buildHttpGet(String url) {
        return new HttpGet(processURL(url));
    }

    private static String processURL(String url) {
        if (StringUtils.isBlank(url)) {
            return url;
        }
        String upperUrl = url.toUpperCase();
        if (upperUrl.startsWith(HTTP_PREFIX)) {
            return url;
        } else if (upperUrl.startsWith(HTTP_SPLIT)) {
            return HTTP_HEADER + url;
        } else {
            return HTTP_HEADER + HTTP_SPLIT + url;
        }
    }

    private static HttpContext processCookie(List<BasicClientCookie> cookieList) {
        HttpContext httpContext = null;
        if (CollectionUtils.isNotEmpty(cookieList)) {
            httpContext = new BasicHttpContext();
            BasicCookieStore cookieStore = new BasicCookieStore();
            BasicClientCookie[] array = new BasicClientCookie[cookieList.size()];
            cookieStore.addCookies(cookieList.toArray(array));
            httpContext.setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);
        }
        return httpContext;
    }


    /**
     * 发送 post请求（带文件）
     *
     * @param url        地址
     * @param fileParams 附件
     * @create by zoran.huang
     */
    public static String httpPostFileRequest(String url, Map<String, File> fileParams) throws Exception {
        return httpPostFileRequest(url, null, null, fileParams);
    }

    /**
     * 发送 post请求（带文件）
     *
     * @param url        地址
     * @param maps       参数
     * @param fileParams 附件
     * @create by zoran.huang
     */
    public static String httpPostFileRequest(String url, Map<String, Object> maps, Map<String, File> fileParams) throws Exception {
        return httpPostFileRequest(url, null, maps, fileParams);
    }

    /**
     * 发送 post请求（带文件）
     *
     * @param url        地址
     * @param maps       参数
     * @param headers    头信息
     * @param fileParams 附件
     * @create by zoran.huang
     */
    public static String httpPostFileRequest(String url, Map<String, Object> headers, Map<String, Object> maps, Map<String, File> fileParams) throws Exception {
        HttpPost httpPost = new HttpPost(url);
        MultipartEntityBuilder meBuilder = MultipartEntityBuilder.create();
        meBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        meBuilder.setCharset(CharsetUtils.get("UTF-8"));
        if (maps != null) {
            for (String key : maps.keySet()) {
                meBuilder.addPart(key, new StringBody(String.valueOf(maps.get(key)), ContentType.TEXT_PLAIN));
            }
        }
        if (headers != null) {
            for (Map.Entry<String, Object> param : headers.entrySet()) {
                httpPost.addHeader(param.getKey(), String.valueOf(param.getValue()));
            }
        }

        if (fileParams != null) {
            for (Map.Entry<String, File> file : fileParams.entrySet()) {
                FileBody fileBody = new FileBody(file.getValue());
                meBuilder.addPart(file.getKey(), fileBody);
            }
        }
        HttpEntity reqEntity = meBuilder.build();
        httpPost.setEntity(reqEntity);
        return getResult(httpPost, UTF_8);
    }


    public static HttpPost getPost(String url, Map<String, Object> params) throws Exception {
        HttpPost httpPost = buildHttpPost(url);
        ArrayList<NameValuePair> pairs = covertParams2NVPS(params);
        httpPost.setEntity(new UrlEncodedFormEntity(pairs, UTF_8));

        return httpPost;

    }

    private static String getJsonString(String resultStr, String callback) {
        if (StringUtils.isNotEmpty(resultStr) && StringUtils.isNotEmpty(callback)) {
            int index = callback.indexOf(callback);
            if (index > -1 && resultStr.length() > callback.length()) {
                resultStr = resultStr.substring(callback.length() + 1, resultStr.length() - 1);
            }
        }
        return resultStr;
    }


}
