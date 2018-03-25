package com.shuzhongchen.mortgagecalculator.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.UUID;

/**
 * Created by shuzhongchen on 3/25/18.
 */

public class BasicInfo implements Parcelable{

    public String id;

    public String propertyType;

    public String streetAddress;

    public String city;

    public String state;

    public String zipcode;

    public String propertyPrice;

    public String downPayment;

    public String apr;

    public String terms;

    public BasicInfo() {
        id = UUID.randomUUID().toString();
    }


    protected BasicInfo(Parcel in) {
        id = in.readString();
        propertyType = in.readString();
        streetAddress = in.readString();
        city = in.readString();
        state = in.readString();
        zipcode = in.readString();
        propertyPrice = in.readString();
        downPayment = in.readString();
        apr = in.readString();
        terms = in.readString();
    }

    public static final Creator<BasicInfo> CREATOR = new Creator<BasicInfo>() {
        @Override
        public BasicInfo createFromParcel(Parcel in) {
            return new BasicInfo(in);
        }

        @Override
        public BasicInfo[] newArray(int size) {
            return new BasicInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(propertyType);
        parcel.writeString(streetAddress);
        parcel.writeString(city);
        parcel.writeString(state);
        parcel.writeString(zipcode);
        parcel.writeString(propertyPrice);
        parcel.writeString(downPayment);
        parcel.writeString(apr);
        parcel.writeString(terms);
    }
}
