package org.xdove.ctcloud.video;


import org.apache.http.impl.client.HttpClients;
import org.junit.Before;
import org.junit.BeforeClass;
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
        this.serviceRequests = new ServiceRequests(HttpClients.createDefault(), config);
    }

    @Test
    public void testDictCommonArea() {
        final Map<String, Object> stringObjectMap = this.serviceRequests.dictCommonArea(null);
        System.out.print(stringObjectMap);
    }

    @Test
    public void testDictDeviceSelect() {
        final Map<String, Object> stringObjectMap = this.serviceRequests.dictDeviceSelect(null, null, 0, null, null);
        System.out.print(stringObjectMap);
    }

    @Test
    public void testDictMediaLive() {
        final String deviceId = System.getenv("DEVICE_ID");
        final Map<String, Object> stringObjectMap = this.serviceRequests.dictMediaLive(null, deviceId, 1, null, null);
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
}