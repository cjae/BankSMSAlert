package com.android.cjae.smsalert.model;

/**
 * Created by Jedidiah on 06/06/2017.
 */

public class SMSData {

    private String body;
    private String number;
    private String date;

    public SMSData(String body, String number, String date) {
        this.body = body;
        this.number = number;
        this.date = date;
    }

    public String getBody() {
        return body;
    }

    public String getNumber() {
        return number;
    }

    public String getDate() {
        return date;
    }
}
