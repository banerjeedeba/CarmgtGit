package com.carmgt.com.carmgt.ds;

/**
 * Created by DEBA on 04-Apr-16.
 */
public class Journey {

    private String driver;
    private String dateTime;
    private String fare;

    public String getFare() {
        return fare;
    }

    public void setFare(String fare) {
        this.fare = fare;
    }

    public Journey(String driver, String dateTime, String fare) {
        this.driver = driver;
        this.dateTime = dateTime;
        this.fare = fare;
    }

    public String getDriver() {
        return driver;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }
}
