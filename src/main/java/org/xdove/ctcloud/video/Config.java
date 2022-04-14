package org.xdove.ctcloud.video;

import lombok.Data;

/**
 * 相关配置参数
 */
@Data
public class Config {
    private String tenantKey;
    private String apiUrl;
    private String appKey;
    private String secret;
    private String encoding = "utf8";
    private String uriPrefix;
}
