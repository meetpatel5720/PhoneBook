package com.meet.phonebook.Data;

import android.os.Parcel;
import android.os.Parcelable;

public class PersonData implements Parcelable {
    public String name;
    public String address;
    public long phoneNo;
    public long mobileNo;

    public PersonData(){

    }

    public PersonData(String name, String address, long mobileNo, long phoneNo) {
        this.name = name;
        this.address = address;
        this.phoneNo = phoneNo;
        this.mobileNo = mobileNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public long getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(int phoneNo) {
        this.phoneNo = phoneNo;
    }

    public long getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(int mobileNo) {
        this.mobileNo = mobileNo;
    }

    protected PersonData(Parcel in){
        name = in.readString();
        address = in.readString();
        mobileNo = in.readLong();
        phoneNo = in.readLong();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(address);
        dest.writeLong(mobileNo);
        dest.writeLong(phoneNo);
    }

    public static final Creator<PersonData> CREATOR = new Creator<PersonData>() {
        @Override
        public PersonData createFromParcel(Parcel in) {
            return new PersonData(in);
        }

        @Override
        public PersonData[] newArray(int size) {
            return new PersonData[size];
        }
    };
}
