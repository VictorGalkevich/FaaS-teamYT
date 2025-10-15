package com.example.domain;

import java.util.List;
import java.util.Map;

public class FunctionRequest {
    private String name;
    private String image;
    private int port;
    private List<String> args;
    private Map<String, String> env;
    private Integer minScale;
    private Integer maxScale;
    private String metric;
    private Integer target;
    private Integer timeoutSeconds;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }

    public List<String> getArgs() { return args; }
    public void setArgs(List<String> args) { this.args = args; }

    public Map<String, String> getEnv() { return env; }
    public void setEnv(Map<String, String> env) { this.env = env; }

    public Integer getMinScale() { return minScale; }
    public void setMinScale(Integer minScale) { this.minScale = minScale; }

    public Integer getMaxScale() { return maxScale; }
    public void setMaxScale(Integer maxScale) { this.maxScale = maxScale; }

    public String getMetric() { return metric; }
    public void setMetric(String metric) { this.metric = metric; }

    public Integer getTarget() { return target; }
    public void setTarget(Integer target) { this.target = target; }

    public Integer getTimeoutSeconds() { return timeoutSeconds; }
    public void setTimeoutSeconds(Integer timeoutSeconds) { this.timeoutSeconds = timeoutSeconds; }
}
