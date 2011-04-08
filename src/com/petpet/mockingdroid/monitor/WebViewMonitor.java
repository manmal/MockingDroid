package com.petpet.mockingdroid.monitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebViewMonitor extends WebViewClient {
    
    private List<String> history;
    
    private Map<String, Metric> metrics;
    
    private long current = -1;
    
    private String lastUrl;
    
    public WebViewMonitor(String url) {
        this.history = new ArrayList<String>();
        this.metrics = new HashMap<String, Metric>();
        this.monitor(url);
    }
    
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        this.monitor(url);
        return super.shouldOverrideUrlLoading(view, url);
    }
    
    public void backButtonPressed(String url) {
        this.monitor(url);
    }
    
    private void monitor(String url) {
        System.out.println("com.petpet.mockingdroid.monitor: " + url);
        
        if (this.current == -1) {
            this.current = System.currentTimeMillis();
            this.lastUrl = url;
            this.history.add(url);
        } else {
            long old = this.current;
            this.current = System.currentTimeMillis();
            
            Metric metric = this.metrics.get(this.lastUrl);
            long time = current - old;
            if (metric == null) {
                metric = new Metric();
                metric.setCount(1);
                metric.setTime(time);
                System.out.println("com.petpet.mockingdroid.monitor " + time);
                this.metrics.put(this.lastUrl, metric);
            } else {
                metric.incrCount();
                System.out.println("com.petpet.mockingdroid.monitor " + time);
                metric.incrTimeBy(time);
            }
            this.lastUrl = url;
        }
    }
    
    public String getMetrics() {
        Set<String> pages = new HashSet<String>(this.history);
        String res = "History: ";
        for (String h : pages) {
            res += h;
            Metric metric = this.metrics.get(h);
            if (metric != null) {
                res += " count: [" + metric.getCount() + "] time: [" + metric.getTime()+"] -->";
            }
        }
        
        return res;
        
    }
    
    

}
