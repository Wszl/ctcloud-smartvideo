package org.xdove.ctcloud.video;


import org.apache.http.impl.client.HttpClients;
import org.junit.Before;
import org.junit.Test;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

public class ServiceRequestsTest {

    private ServiceRequests serviceRequests;

    @Before
    public void init() throws InvalidKeyException, NoSuchAlgorithmException {
        Config config = new Config();
        config.setApiUrl(System.getenv("APP_URL"));
        config.setAppKey(System.getenv("APP_KEY"));
        config.setSecret(System.getenv("APP_SECRET"));
        config.setTenantKey(System.getenv("APP_TEN"));
        config.setUriPrefix("/api/dict");
        config.setEncoding("UTF8");
        this.serviceRequests = new ServiceRequests(HttpClients.createDefault(), config);
    }

    @Test
    public void testDictCommonArea() {
        final Map<String, Object> stringObjectMap = this.serviceRequests.dictCommonArea(null);
        System.out.print(stringObjectMap);
    }

    @Test
    public void testDictDeviceSelect() {
        final Map<String, Object> stringObjectMap = this.serviceRequests.dictDeviceSelect(null, null, 1, 10000, 1);
        System.out.print(stringObjectMap);
    }

    @Test
    public void testDictMediaPlay() {
        final String deviceId = System.getenv("DEVICE_ID");
        final String protocolType = System.getenv("PROTOCOL_TYPE");
        final Map<String, Object> stringObjectMap = this.serviceRequests.dictMediaPlay(null, deviceId, 1, null, null, protocolType);
        System.out.print(stringObjectMap);
    }

    @Test
    public void testDictMediaLive() {
        final String deviceId = System.getenv("DEVICE_ID");
        final String protocolType = System.getenv("M3U8_TYPE");
        final Map<String, Object> stringObjectMap = this.serviceRequests.dictMediaLive(null, deviceId, 1, null, null, protocolType);
        System.out.print(stringObjectMap);
    }

    @Test
    public void testDictDeviceQuery() {
        final Map<String, Object> stringObjectMap = this.serviceRequests.dictDeviceQuery(null, null, null, null, null, null);
        System.out.println(stringObjectMap);
    }

    @Test
    public void testSystemAccessSelect() {
        final Map<String, Object> stringObjectMap = this.serviceRequests.systemAccessSelect(null, null, null, null, null, null);
        System.out.println(stringObjectMap);
    }

    /**************************
     *      终端互动接口测试
     **************************/

    @Test
    public void testTalkAccountSelect() {
        final Map<String, Object> stringObjectMap = this.serviceRequests.talkAccountSelect(null, null, null, null, null);
        System.out.println(stringObjectMap);
    }

    @Test
    public void testTalkAccess() {
        final String targetId = System.getenv("TARGET_ID");
        final Map<String, Object> stringObjectMap = this.serviceRequests.talkAccess(null, null, null, targetId);
        System.out.println(stringObjectMap);
    }

    @Test
    public void testTalkPlay() {
        final String targetId = System.getenv("TARGET_ID");
        final Map<String, Object> stringObjectMap = this.serviceRequests.talkPlay(null, null, null, targetId);
        System.out.println(stringObjectMap);
    }

    @Test
    public void testTalkStart() {
        final String targetId = System.getenv("TARGET_ID");
        final String sourceId = System.getenv("SOURCE_ID");
        final Map<String, Object> stringObjectMap = this.serviceRequests.talkStart(null, targetId, sourceId);
        System.out.println(stringObjectMap);
    }

    @Test
    public void testTalkStop() {
        final String targetId = System.getenv("TARGET_ID");
        final String sourceId = System.getenv("SOURCE_ID");
        final Map<String, Object> stringObjectMap = this.serviceRequests.talkStop(null, targetId, sourceId);
        System.out.println(stringObjectMap);
    }

    @Test
    public void testSystemDeviceTerminal() {
        final Map<String, Object> stringObjectMap = this.serviceRequests.systemDeviceTerminal(null);
        System.out.println(stringObjectMap);
    }

    @Test
    public void testSystemVoiceApply() {
        final String ip = System.getenv("IP");
        final String sourceId = System.getenv("SOURCE_ID");
        final Map<String, Object> stringObjectMap = this.serviceRequests.systemVoiceApply(null, sourceId, ip);
        System.out.println(stringObjectMap);
    }

    @Test
    public void testSystemVoiceConfirm() {
        final String sourceId = System.getenv("SOURCE_ID");
        final String targetId = System.getenv("TARGET_ID");
        final String serial_num = System.getenv("SERIAL_NUM");
        final Map<String, Object> stringObjectMap = this.serviceRequests.systemVoiceConfirm(null, sourceId, targetId, serial_num);
        System.out.println(stringObjectMap);
    }

    @Test
    public void testSystemVoiceDisconnect() {
        final Map<String, Object> stringObjectMap = this.serviceRequests.systemVoiceDisconnect(null, null, null, null);
        System.out.println(stringObjectMap);
    }
}