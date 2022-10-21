package org.xdove.ctcloud.video;

import com.alibaba.fastjson.JSONObject;
import lombok.NonNull;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.activation.MimeType;
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
    private String urlPrefix;

    /** 提供获取区域编码的能力 */
    public static final String PATH_DICT_COMMON_AREA = "/common/area";
    /** 获取网络摄像机设备相关信息 */
    public static final String PATH_DICT_DEVICE_SELECT = "/device/select";
    /** 媒体预览开启 */
    public static final String PATH_DICT_MEDIA_PLAY = "/media/play";
    /** 开启直播能力，并开启获取HTTP-M3U8地址 */
    public static final String PATH_DICT_MEDIA_LIVE = "/media/live";
    /** 获取各类设备相关信息 */
    public static final String PATH_DICT_DEVICE_QUERY = "/device/query";
    /** 查询设备 */
    public static final String PATH_SYSTEM_ACCESS_SELECT = "/system/access/select";
    /**************************
     *      终端互动接口
     **************************/
    /** 接入账号查询 */
    public static final String PATH_TALK_ACCOUNT_SELECT = "/talk/account/select";
    /** 接入地址获取 */
    private static final String PATH_TALK_ACCESS = "/talk/access";
    /** 终端视频呼叫 */
    private static final String PATH_TALK_PLAY = "/talk/play";
    /** 终端互动开启 */
    private static final String PATH_TALK_START = "/talk/start";
    /** 终端互动关闭 */
    private static final String PATH_TALK_STOP = "/talk/stop";
    /** 查询终端设备 */
    public static final String PATH_SYSTEM_DEVICE_TERMINAL = "/system/deviceterminal";
    /** 语音广播申请 */
    public static final String PATH_SYSTEM_VOICE_APPLY = "/system/voice/apply";
    /** 语音广播确认 */
    public static final String PATH_SYSTEM_VOICE_CONFIRM = "/system/voice/confirm";
    /** 语音广播断开 */
    public static final String PATH_SYSTEM_VOICE_DISCONNECT = "/system/voice/disconnet";

    public ServiceRequests(Config config) throws NoSuchAlgorithmException, InvalidKeyException {
        this.config = config;
        this.client =  HttpClientBuilder.create().build();
        messageDigest = MessageDigest.getInstance("MD5");
        if (Objects.nonNull(config.getUriPrefix())) {
            this.urlPrefix = config.getUriPrefix();
        } else {
            this.urlPrefix = "/api/dict";
        }
    }

    public ServiceRequests(HttpClient client, Config config) throws NoSuchAlgorithmException, InvalidKeyException {
        this.client = client;
        this.config = config;
        messageDigest = MessageDigest.getInstance("MD5");
        if (Objects.nonNull(config.getUriPrefix())) {
            this.urlPrefix = config.getUriPrefix();
        } else {
            this.urlPrefix = "/api/dict";
        }
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

    /**************************************************************************************************
     *                              终端互动接口
     **************************************************************************************************/

    /**
     * 接入账号查询
     * @param memberkey 租户唯一识别码 默认为config中tenantKey
     * @param accountIds 接入编号组，未设置获取全部账号信息
     * @param resultType 返回类型(0:分页;1:列表)
     * @param pageSize 显示条数(范围:1-100)
     * @param pageNum 当前页码
     * @return
     */
    public Map<String, Object> talkAccountSelect(String memberkey, String accountIds, Integer resultType, Integer pageSize,
                                                    Integer pageNum) {
        if (log.isTraceEnabled()) {
            log.trace("request talkAccountSelect memberkey=[{}], accountIds=[{}], resultType=[{}], pageSize=[{}]," +
                    "pageNum=[{}]", memberkey, accountIds, resultType, pageSize, pageNum);
        }
        Map<String, String> param = new TreeMap<>();
        param.put("memberkey",  Objects.isNull(memberkey) ? config.getTenantKey() : memberkey);
        param.put("accountIds",  accountIds);
        param.put("resultType",  parseIntParam(resultType));
        param.put("pageSize",  parseIntParam(pageSize));
        param.put("pageNum",  parseIntParam(pageNum));
        try {
            final String s = this.postRequest(PATH_TALK_ACCOUNT_SELECT, param);
            final JSONObject jsonObject = JSONObject.parseObject(s);
            return jsonObject.getInnerMap();
        } catch (IOException e) {
            log.info(e.getLocalizedMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * 接入地址获取
     * @param memberkey 租户唯一识别码 默认为config中tenantKey
     * @param accesstype 接入网络类型(0:内网;1:公网;2:其他
     * @param accessaddr 请求API的IP地址，当accesstype为2时为必填项
     * @param targetid 目标方终端接入编码
     * @return
     */
    public Map<String, Object> talkAccess(String memberkey, Integer accesstype, String accessaddr, String targetid) {
        if (log.isTraceEnabled()) {
            log.trace("request talkAccountSelect memberkey=[{}], accesstype=[{}], accessaddr=[{}], targetid=[{}]",
                    memberkey, accesstype, accessaddr, targetid);
        }
        Map<String, String> param = new TreeMap<>();
        param.put("memberkey",  Objects.isNull(memberkey) ? config.getTenantKey() : memberkey);
        param.put("accesstype",  parseIntParam(accesstype));
        param.put("accessaddr",  accessaddr);
        param.put("targetid",  targetid);
        try {
            final String s = this.postRequest(PATH_TALK_ACCESS, param);
            final JSONObject jsonObject = JSONObject.parseObject(s);
            return jsonObject.getInnerMap();
        } catch (IOException e) {
            log.info(e.getLocalizedMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * 终端视频呼叫
     * @param memberkey 租户唯一识别码 默认为config中tenantKey
     * @param accesstype 接入网络类型(0:内网;1:公网;2:其他
     * @param accessaddr 请求API的IP地址，当accesstype为2时为必填项
     * @param targetid 目标方终端接入编码
     * @return
     */
    public Map<String, Object> talkPlay(String memberkey, Integer accesstype, String accessaddr, String targetid) {
        if (log.isTraceEnabled()) {
            log.trace("request talkPlay memberkey=[{}], accesstype=[{}], accessaddr=[{}], targetid=[{}]",
                    memberkey, accesstype, accessaddr, targetid);
        }
        Map<String, String> param = new TreeMap<>();
        param.put("memberkey",  Objects.isNull(memberkey) ? config.getTenantKey() : memberkey);
        param.put("accesstype",  parseIntParam(accesstype));
        param.put("accessaddr",  accessaddr);
        param.put("targetid",  targetid);
        try {
            final String s = this.postRequest(PATH_TALK_PLAY, param);
            final JSONObject jsonObject = JSONObject.parseObject(s);
            return jsonObject.getInnerMap();
        } catch (IOException e) {
            log.info(e.getLocalizedMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * 终端视频开启
     * @param memberkey 租户唯一识别码 默认为config中tenantKey
     * @param targetid 目标方终端接入编码*1
     * @param sourceid 发起方终端接入编码*2
     * @return
     */
    public Map<String, Object> talkStart(String memberkey, String targetid, String sourceid) {
        if (log.isTraceEnabled()) {
            log.trace("request talkStart memberkey=[{}], targetid=[{}], sourceid=[{}]",
                    memberkey, targetid, sourceid);
        }
        Map<String, String> param = new TreeMap<>();
        param.put("memberkey",  Objects.isNull(memberkey) ? config.getTenantKey() : memberkey);
        param.put("targetid",  targetid);
        param.put("sourceid",  sourceid);
        try {
            final String s = this.postRequest(PATH_TALK_START, param);
            final JSONObject jsonObject = JSONObject.parseObject(s);
            return jsonObject.getInnerMap();
        } catch (IOException e) {
            log.info(e.getLocalizedMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * 终端视频关闭
     * @param memberkey 租户唯一识别码 默认为config中tenantKey
     * @param targetid 发起方终端接入编码*1
     * @param sourceid 目标方终端接入编码*2
     * @return
     */
    public Map<String, Object> talkStop(String memberkey, String targetid, String sourceid) {
        if (log.isTraceEnabled()) {
            log.trace("request talkStop memberkey=[{}], targetid=[{}], sourceid=[{}]",
                    memberkey, targetid, sourceid);
        }
        Map<String, String> param = new TreeMap<>();
        param.put("memberkey",  Objects.isNull(memberkey) ? config.getTenantKey() : memberkey);
        param.put("targetid",  targetid);
        param.put("sourceid",  sourceid);
        try {
            final String s = this.postRequest(PATH_TALK_STOP, param);
            final JSONObject jsonObject = JSONObject.parseObject(s);
            return jsonObject.getInnerMap();
        } catch (IOException e) {
            log.info(e.getLocalizedMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * 查询终端设备
     * @param memberkey 租户唯一识别码 默认为config中tenantKey
     * @return
     */
    public Map<String, Object> systemDeviceTerminal(String memberkey) {
        if (log.isTraceEnabled()) {
            log.trace("request systemDeviceTerminal memberkey=[{}]", memberkey);
        }
        Map<String, String> param = new TreeMap<>();
        param.put("memberkey",  Objects.isNull(memberkey) ? config.getTenantKey() : memberkey);
        try {
            final String s = this.postRequest(PATH_SYSTEM_DEVICE_TERMINAL, param);
            final JSONObject jsonObject = JSONObject.parseObject(s);
            return jsonObject.getInnerMap();
        } catch (IOException e) {
            log.info(e.getLocalizedMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * 语音广播申请
     * @param memberkey 租户唯一识别码 默认为config中tenantKey
     * @param sourceId 源终端设备号
     * @param ip 平台 ip
     * @return
     */
    public Map<String, Object> systemVoiceApply(String memberkey, @NonNull String sourceId, @NonNull String ip) {
        if (log.isTraceEnabled()) {
            log.trace("request systemVoiceApply memberkey=[{}], sourceId=[{}], ip=[{}]",
                    memberkey, sourceId, ip);
        }
        Map<String, String> param = new TreeMap<>();
        param.put("memberkey",  Objects.isNull(memberkey) ? config.getTenantKey() : memberkey);
        param.put("sourceid", sourceId);
        param.put("ip", ip);
        try {
            final String s = this.postRequest(PATH_SYSTEM_VOICE_APPLY, param);
            final JSONObject jsonObject = JSONObject.parseObject(s);
            return jsonObject.getInnerMap();
        } catch (IOException e) {
            log.info(e.getLocalizedMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * 语音广播确认
     * @param memberkey 租户唯一识别码 默认为config中tenantKey
     * @param sourceId 源终端设备号
     * @param targetId 目标设备号
     * @param serialNum 推流唯一序列号
     * @return
     */
    public Map<String, Object> systemVoiceConfirm(String memberkey, @NonNull String sourceId, @NonNull String targetId,
                                                @NonNull String serialNum) {
        if (log.isTraceEnabled()) {
            log.trace("request systemVoiceConfirm memberkey=[{}], sourceId=[{}], targetId=[{}], serialNum=[{}]",
                    memberkey, sourceId, targetId, serialNum);
        }
        Map<String, String> param = new TreeMap<>();
        param.put("memberkey",  Objects.isNull(memberkey) ? config.getTenantKey() : memberkey);
        param.put("sourceid", sourceId);
        param.put("targetid", targetId);
        param.put("serialnum", serialNum);
        try {
            final String s = this.postRequest(PATH_SYSTEM_VOICE_CONFIRM, param);
            final JSONObject jsonObject = JSONObject.parseObject(s);
            return jsonObject.getInnerMap();
        } catch (IOException e) {
            log.info(e.getLocalizedMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * 语音广播断开
     * @param memberkey 租户唯一识别码 默认为config中tenantKey
     * @param sourceId 源终端设备号
     * @param targetId 目标设备号
     * @param serialNum 推流唯一序列号
     * @return
     */
    public Map<String, Object> systemVoiceDisconnect(String memberkey, @NonNull String sourceId, @NonNull String targetId,
                                                  @NonNull String serialNum) {
        if (log.isTraceEnabled()) {
            log.trace("request systemVoiceDisconnet memberkey=[{}], sourceId=[{}], targetId=[{}], serialNum=[{}]",
                    memberkey, sourceId, targetId, serialNum);
        }
        Map<String, String> param = new TreeMap<>();
        param.put("memberkey",  Objects.isNull(memberkey) ? config.getTenantKey() : memberkey);
        param.put("sourceid", sourceId);
        param.put("targetid", targetId);
        param.put("serialnum", serialNum);
        try {
            final String s = this.postRequest(PATH_SYSTEM_VOICE_DISCONNECT, param);
            final JSONObject jsonObject = JSONObject.parseObject(s);
            return jsonObject.getInnerMap();
        } catch (IOException e) {
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
        return config.getApiUrl() + this.urlPrefix + path + "?appkey=" + config.getAppKey();
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
        post.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        post.setEntity(body);

        try {
            final HttpResponse response = client.execute(post);
            String respContent = readInputStream(response.getEntity().getContent(),
                    Objects.isNull(response.getEntity().getContentEncoding()) ? config.getEncoding() : response.getEntity().getContentEncoding().getValue());
            if (log.isDebugEnabled()) {
                log.debug("path=[{}], params=[{}], response status=[{}] content=[{}]", path, p,
                        response.getStatusLine().getStatusCode(), respContent);
            }
            return respContent;
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
