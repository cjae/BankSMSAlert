package com.android.cjae.smsalert.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Jedidiah on 07/06/2017.
 */

public class Bank implements Parcelable {

    private String bankName;
    private String bankAccount;

    public Bank() {
        this.bankName = "";
        this.bankAccount = "";
    }

    public Bank(String bankName, String bankAccount) {
        this.bankName = bankName;
        this.bankAccount = bankAccount;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(String bankAccount) {
        this.bankAccount = bankAccount;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.bankName);
        dest.writeString(this.bankAccount);
    }

    private Bank(Parcel in) {
        this.bankName = in.readString();
        this.bankAccount = in.readString();
    }

    public static final Creator<Bank> CREATOR = new Creator<Bank>() {
        @Override
        public Bank createFromParcel(Parcel source) {
            return new Bank(source);
        }

        @Override
        public Bank[] newArray(int size) {
            return new Bank[size];
        }
    };
}
