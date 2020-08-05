package gr.kgdev.batmobile.utils;

import gr.kgdev.batmobile.models.User;

public class AppCache {

    private static User APP_USER;

    public static User getAppUser() {
        return APP_USER;
    }

    public static void setAppUser(User appUser) {
        APP_USER = appUser;
    }
}
