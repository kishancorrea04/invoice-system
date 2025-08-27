package com.invoice.system.invoice_system.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "db")
public class DataSourceProperties {
    private String url;
    private String driverClassName;
    private String username;
    private String password;
    private String ddlAuto = "create";
    private String dialect;
}
