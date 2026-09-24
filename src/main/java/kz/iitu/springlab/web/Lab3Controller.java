package kz.iitu.springlab.web;

import kz.iitu.springlab.config.*;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/lab3")
public class Lab3Controller {

    private final AppProperties props;
    private final EnvironmentBanner banner;
    private final Environment environment;

    public Lab3Controller(AppProperties props, EnvironmentBanner banner,
                          Environment environment) {
        this.props = props;
        this.banner = banner;
        this.environment = environment;
    }

    @GetMapping("/config")
    public Map<String, Object> config() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("owner",           props.owner());
        map.put("group",           props.group());
        map.put("mailFrom",        props.mail().from());
        map.put("mailRetryCount",  props.mail().retryCount());
        map.put("mailTimeout",     props.mail().timeout().toString());
        map.put("mailEnabled",     props.mail().enabled());
        map.put("localeDefault",   props.locale().defaultLocale());
        map.put("localeSupported", props.locale().supported());
        map.put("serverPort",      environment.getProperty("server.port"));
        map.put("activeProfiles",  Arrays.asList(environment.getActiveProfiles()));
        map.put("banner",          banner.describe());
        return map;
    }
}