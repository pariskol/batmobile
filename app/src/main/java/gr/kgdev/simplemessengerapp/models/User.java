package gr.kgdev.simplemessengerapp.models;

import org.json.JSONException;
import org.json.JSONObject;

public class User {

    private String username;
    private Integer id;
    private Boolean isActive;

    public User(JSONObject json) {
        try {
            this.username = json.getString("USERNAME");
            this.id = json.getInt("ID");
            this.isActive = json.getInt("ACTIVE") == 1 ? true : false;
        } catch (JSONException e) {
            e.printStackTrace();
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
}

