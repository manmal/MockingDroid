package com.petpet.mockingdroid.monitor;

public class Metric {

    private long time;
    
    private int count;
    
    public void incrCount() {
        this.count++;
    }
    
    public void incrTimeBy(long incr) {
        this.time += incr;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getTime() {
        return time;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getCount() {
        return count;
    }
}
