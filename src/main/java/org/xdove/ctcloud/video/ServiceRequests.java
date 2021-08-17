package org.xdove.ctcloud.video;

import com.alibaba.fastjson.JSONObject;
import lombok.NonNull;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * 电信智能视频云服务
 * @author Wszl
 * @date 2020年12月25日
 */
public class ServiceRequests {

    private final static Logger log = LogManager.getLogger();

    private HttpClient client;
    private Config config;
    private MessageDigest messageDigest;

    /** 提供获取区域编码的能力 */
    public static final String PATH_DICT_COMMON_AREA = "/api/dict/common/area";
    /** 获取网络摄像机设备相关信息 */
    public static final String PATH_DICT_DEVICE_SELECT = "/api/dict/device/select";
    /** 媒体预览开启 */
    public static final String PATH_DICT_MEDIA_PLAY = "/api/dict/media/play";
    /** 开启直播能力，并开启获取HTTP-M3U8地址 */
    public static final String PATH_DICT_MEDIA_LIVE = "/api/dict/media/live";
    /** 获取各类设备相关信息 */
    public static final String PATH_DICT_DEVICE_QUERY = "/api/dict/device/query";
    /** 查询设备 */
    public static final String PATH_SYSTEM_ACCESS_SELECT = "/system/access/select";

    public ServiceRequests(Config config) throws NoSuchAlgorithmException, InvalidKeyException {
        this.config = config;
        this.client =  HttpClientBuilder.create().build();
        messageDigest = MessageDigest.getInstance("MD5");
    }

    public ServiceRequests(HttpClient client, Config config) throws NoSuchAlgorithmException, InvalidKeyException {
        this.client = client;
        this.config = config;
        messageDigest = MessageDigest.getInstance("MD5");
    }

    public Map<String, Object> dictCommonArea(String areaCode) {
        if (log.isTraceEnabled()) {
            log.trace("request dictCommonArea [{}]", areaCode);
        }
        Map<String, String> param = new TreeMap<>();
        param.put("areacode", areaCode);
        try {
            final String s = this.postRequest(PATH_DICT_COMMON_AREA, param);
            final JSONObject jsonObject = JSONObject.parseObject(s);
            return jsonObject.getInnerMap();
        } catch (IOException e) {
            log.info(e.getLocalizedMessage());
            throw new RuntimeException(e);
        }

    }

    /**
     * 获取网络摄像机设备相关信息
     * @param memberkey 租户唯一识别码 默认为config中tenantKey
     * @param deviceids 设备编号组，以”,”间隔，默认获取全部设备  默认值为 null
     * @param resulttype 返回类型(0:分页;1:列表) 默认值为 1
     * @param pagesize 显示条数(范围:1-100) 默认值为20
     * @param pagenum 当前页码 默认值为1
     * @return
     */
    public Map<String, Object> dictDeviceSelect(String memberkey, String deviceids, Integer resulttype,
                                                Integer pagesize, Integer pagenum) {
        if (log.isTraceEnabled()) {
            log.trace("request dictDeviceSelect memberkey=[{}], deviceids=[{}], resulttype=[{}], pagesize=[{}], " +
                    "pagenum=[{}]", memberkey, deviceids, resulttype, pagesize, pagenum);
        }
        Map<String, String> param = new TreeMap<>();
        param.put("memberkey", Objects.isNull(memberkey) ? config.getTenantKey() : memberkey);
        param.put("deviceids", deviceids);
        param.put("resulttype", parseIntParam(resulttype));
        param.put("pagesize", parseIntParam(pagesize));
        param.put("pagenum", parseIntParam(pagenum));
        try {
            final String s = this.postRequest(PATH_DICT_DEVICE_SELECT, param);
            final JSONObject jsonObject = JSONObject.parseObject(s);
            return jsonObject.getInnerMap();
        } catch (IOException e) {
            log.info(e.getLocalizedMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取各类设备相关信息
     * @param memberkey 租户唯一识别码 默认为config中tenantKey
     * @param deviceids 设备编号组，默认获取全部设备 默认值 null
     * @param devicetypes 设备类型组(0:IPC;1:Smart;4:NVR)，默认获取全部类型 默认值 null
     * @param resulttype 返回类型(0:分页;1:列表) 默认值 1
     * @param pagesize 显示条数(范围:1-100) 默认值 20
     * @param pagenum 当前页码 默认值 1
     * @return
     */
    public Map<String, Object> dictDeviceQuery(String memberkey, String deviceids, String devicetypes, Integer resulttype,
                                                Integer pagesize, Integer pagenum) {
        if (log.isTraceEnabled()) {
            log.trace("request dictDeviceQuery memberkey=[{}], deviceids=[{}], devicetypes=[{}] resulttype=[{}]," +
                    " pagesize=[{}], pagenum=[{}]", memberkey, deviceids, devicetypes, resulttype, pagesize, pagenum);
        }
        Map<String, String> param = new TreeMap<>();
        param.put("memberkey", Objects.isNull(memberkey) ? config.getTenantKey() : memberkey);
        param.put("deviceids", deviceids);
        param.put("devicetypes", devicetypes);
        param.put("resulttype", parseIntParam(resulttype));
        param.put("pagesize", parseIntParam(pagesize));
        param.put("pagenum", parseIntParam(pagenum));
        try {
            final String s = this.postRequest(PATH_DICT_DEVICE_QUERY, param);
            final JSONObject jsonObject = JSONObject.parseObject(s);
            return jsonObject.getInnerMap();
        } catch (IOException e) {
            log.info(e.getLocalizedMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * 媒体预览开启
     * 获取设备实时播放地址
     * @param memberkey 租户唯一识别码 默认为config中tenantKey
     * @param deviceid 设备编号 无默认值
     * @param networktype 网络协议(0:UDP;1:TCP) 无默认值
     * @param accesstype 接入网络类型(0:内网;1:公网;2:其他) 默认值为 1
     * @param accessaddr 请求API的IP地址，当accesstype为2时为必填项 默认值为 null
     * @return
     */
    public Map<String, Object> dictMediaPlay(String memberkey, @NonNull String deviceid, Integer networktype,
                                             Integer accesstype, String accessaddr) {
        if (log.isTraceEnabled()) {
            log.trace("request dictMediaPlay memberkey=[{}], deviceid=[{}], networktype=[{}], accesstype=[{}], " +
                    "accessaddr=[{}]", memberkey, deviceid, networktype, accesstype, accessaddr);
        }
        Map<String, String> param = new TreeMap<>();
        param.put("memberkey",  Objects.isNull(memberkey) ? config.getTenantKey() : memberkey);
        param.put("deviceid", deviceid);
        param.put("networktype", parseIntParam(networktype));
        param.put("accesstype", parseIntParam(accesstype));
        param.put("accessaddr", accessaddr);
        try {
            final String s = this.postRequest(PATH_DICT_MEDIA_PLAY, param);
            final JSONObject jsonObject = JSONObject.parseObject(s);
            return jsonObject.getInnerMap();
        } catch (IOException e) {
            log.info(e.getLocalizedMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * 开启直播能力，并开启获取HTTP-M3U8地址
     * @param memberkey 租户唯一识别码 默认为config中tenantKey
     * @param deviceid 设备编号 无默认值
     * @param networktype 网络协议(0:UDP;1:TCP) 无默认值
     * @param accesstype 接入网络类型(0:内网;1:公网;2:其他) 默认值为 1
     * @param accessaddr 请求API的IP地址，当accesstype为2时为必填项 默认值为 null
     * @return
     */
    public Map<String, Object> dictMediaLive(String memberkey, @NonNull String deviceid, Integer networktype,
                                                Integer accesstype, String accessaddr) {
        if (log.isTraceEnabled()) {
            log.trace("request dictMediaLive memberkey=[{}], deviceid=[{}], networktype=[{}], accesstype=[{}], " +
                    "accessaddr=[{}]", memberkey, deviceid, networktype, accesstype, accessaddr);
        }
        Map<String, String> param = new TreeMap<>();
        param.put("memberkey",  Objects.isNull(memberkey) ? config.getTenantKey() : memberkey);
        param.put("deviceid", deviceid);
        param.put("networktype", parseIntParam(networktype));
        param.put("accesstype", parseIntParam(accesstype));
        param.put("accessaddr", accessaddr);
        try {
            final String s = this.postRequest(PATH_DICT_MEDIA_LIVE, param);
            final JSONObject jsonObject = JSONObject.parseObject(s);
            return jsonObject.getInnerMap();
        } catch (IOException e) {
            log.info(e.getLocalizedMessage());
            throw new RuntimeException(e);
        }
    }

    public Map<String, Object> systemAccessSelect(String memberkey, String[] deviceids, Integer[] devicetypes,
                                                  Integer resulttype, Integer pagesize, Integer pagenum) {
        if (log.isTraceEnabled()) {
            log.trace("request systemAccessSelect memberkey=[{}], deviceids=[{}], devicetypes=[{}], pagesize=[{}], " +
                    "pagenum=[{}]", memberkey, deviceids, devicetypes, pagesize, pagenum);
        }
        Map<String, String> param = new TreeMap<>();
        param.put("memberkey",  Objects.isNull(memberkey) ? config.getTenantKey() : memberkey);
        param.put("deviceids", Objects.isNull(deviceids) ? null : Arrays.toString(deviceids));
        param.put("devicetypes", Objects.isNull(devicetypes) ? null : Arrays.toString(devicetypes));
        param.put("resulttype", parseIntParam(resulttype));
        param.put("pagesize", parseIntParam(pagesize));
        param.put("pagenum", parseIntParam(pagenum));
        try {
            final String s = this.getRequest(PATH_SYSTEM_ACCESS_SELECT, param);
            final JSONObject jsonObject = JSONObject.parseObject(s);
            return jsonObject.getInnerMap();
        } catch (IOException | URISyntaxException e) {
            log.info(e.getLocalizedMessage());
            throw new RuntimeException(e);
        }
    }



    private String parseIntParam(Integer o) {
        if (Objects.isNull(o)) {
            return null;
        }
        return Integer.toString(o);
    }
    /**
     * 组合url
     * @param path api路径
     * @return
     */
    private String combPath(String path) {
        return config.getApiUrl() + path + "?appkey=" + config.getAppKey();
    }

    /**
     * 对参数进签名，规则参考电信智能视频云文档
     * @return
     */
    private String sign(Map<String, String> p, String httpMethod) {
        switch (httpMethod) {
            case "GET":
                final Map<String, String> sortedGetParam = ParamUtils.asciiSort(p);
                final String ps = ParamUtils.combHttpGetParam(sortedGetParam, true);
                return Hex.encodeHexString(messageDigest.digest((config.getSecret() + "&&" + ps).getBytes())).toUpperCase();
            case "POST":
                final Map<String, String> sortedPostParam = ParamUtils.asciiSort(p);
                final String js = JSONObject.toJSONString(sortedPostParam);
                return Hex.encodeHexString(messageDigest.digest((config.getSecret() + "&&" + js).getBytes())).toUpperCase();
            default:
                log.warn("unsupported http method [{}]", httpMethod);
                throw new UnsupportedOperationException(httpMethod);
        }

    }

    private HttpEntity combBody(Map<String, String> p) {
        Map<String, Object> param = new HashMap<>(2);
        param.put("sign", sign(p, "POST"));
        param.put("parmdata", p);
        return new StringEntity(JSONObject.toJSONString(param), config.getEncoding());
    }

    private String postRequest(String path, Map<String, String> p) throws IOException {
        String url = combPath(path);
        HttpPost post = new HttpPost(url);
        final HttpEntity body = combBody(p);
        if (log.isDebugEnabled()) {
            log.debug("post request path=[{}], body=[{}]", url, readInputStream(body.getContent(), config.getEncoding()));
        }
        post.setEntity(body);

        try {
            final HttpResponse response = client.execute(post);
            if (log.isDebugEnabled()) {
                log.debug("path=[{}], params=[{}], response status=[{}] content=[{}]", path, p,
                        response.getStatusLine().getStatusCode(),
                        readInputStream(response.getEntity().getContent(),
                                Objects.isNull(response.getEntity().getContentEncoding()) ? config.getEncoding() : response.getEntity().getContentEncoding().getValue()));
            }
            return readInputStream(response.getEntity().getContent(),
                            Objects.isNull(response.getEntity().getContentEncoding()) ? config.getEncoding() : response.getEntity().getContentEncoding().getValue());
        } catch (IOException e) {
            log.info("path=[{}], params=[{}] error.", path, p, e);
            throw e;
        }
    }

    private String readInputStream(InputStream i, String encoding) throws IOException {
        return IOUtils.toString(i, Objects.isNull(encoding) ? config.getEncoding() : encoding);
    }

    private String getRequest(String path, Map<String, String> p) throws IOException, URISyntaxException {

        URI url = combParam(p ,combPath(path));
        HttpGet get = new HttpGet(url);
        if (log.isDebugEnabled()) {
            log.debug("get request url=[{}]", url);
        }
        try {
            final HttpResponse response = client.execute(get);
            if (log.isDebugEnabled()) {
                log.debug("url=[{}], response status=[{}] content=[{}]", url,
                        response.getStatusLine().getStatusCode(),
                        readInputStream(response.getEntity().getContent(),
                                Objects.isNull(response.getEntity().getContentEncoding()) ? config.getEncoding() : response.getEntity().getContentEncoding().getValue()));
            }
            return readInputStream(response.getEntity().getContent(),
                    Objects.isNull(response.getEntity().getContentEncoding()) ? config.getEncoding() : response.getEntity().getContentEncoding().getValue());
        } catch (IOException e) {
            log.info("request url=[{}] error.", url, e);
            throw e;
        }
    }

    private URI combParam(Map<String, String> p, String url) throws URISyntaxException {
        URIBuilder b = new URIBuilder(url);
        p.forEach((k, v) -> {
            if (Objects.isNull(v)) return;
            b.addParameter(k, v);
        });
        b.addParameter("sign", sign(p, "GET"));
        return b.build();
    }
}
