package gr.kgdev.batmobile.activities;

import androidx.lifecycle.ViewModel;

import gr.kgdev.batmobile.models.User;

public class MainViewModel extends ViewModel {


    private User appUser;

    public void setAppUser(User user) {
        this.appUser = user;
    }
}
