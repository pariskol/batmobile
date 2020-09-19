package gr.kgdev.batmobile.models;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class User {

    private String username;
    private String password;
    private Integer id;
    private Boolean isActive;

    public User(JSONObject json) {
        try {
            this.username = json.getString("name");
            this.id = json.getInt("id");
            this.isActive = json.has("active") ? json.getInt("active") == 1 ? true : false : true;
        } catch (JSONException e) {
            try {
                this.username = json.getString("NAME");
                this.id = json.getInt("ID");
                this.isActive = json.has("ACTIVE") ? json.getInt("ACTIVE") == 1 ? true : false : true;
            } catch (JSONException ex) {
                Log.e(getClass().getName(), "Could not deserilize json");
            }

        }
    }

    public String getUsername() {
        return username;
    }

    public Integer getId() {
        return id;
    }

    public Boolean isActive() {
        return isActive;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

