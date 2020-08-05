package gr.kgdev.batmobile.models;

import org.json.JSONException;
import org.json.JSONObject;

public class User {

    private String username;
    private Integer id;
    private Boolean isActive;

    public User(JSONObject json) {
        try {
            this.username = json.getString("NAME");
            this.id = json.getInt("ID");
            this.isActive = json.has("ACTIVE") ? json.getInt("ACTIVE") == 1 ? true : false : true;
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

    public void setUsername(String username) {
        this.username = username;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

}

