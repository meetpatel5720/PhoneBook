package com.meet.phonebook.Misc;

public class Constants {
    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String IS_FIRST_RUN = "firstRunFlag";
    public static final String IS_NUMBER_VERIFIED = "isNumberVerified";
    public static final String SIM_SERIAL = "simSerial";
    public static final String SIM_SLOT = "simSlotIndex";
    public static final String MOBILE_NO = "mobileNo";
    public static final String DATABASE_NAME = "phone_book.db";

    // global topic to receive app wide push notifications
    public static final String TOPIC_GLOBAL = "global";

    // broadcast receiver intent filters
    public static final String REGISTRATION_COMPLETE = "registrationComplete";
    public static final String PUSH_NOTIFICATION = "pushNotification";

    // id to handle the notification in the notification tray
    public static final int NOTIFICATION_ID = 100;
    public static final int NOTIFICATION_ID_BIG_IMAGE = 101;

    public static final String IS_SUBSCRIBED = "isSubscribed";

    public static final String IS_DATABASE_UPDATED = "isDatabaseUpdated";
    public static final String DATABASE_URL = "databaseUrl";
}


