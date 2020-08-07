package gr.kgdev.batmobile.models;

import org.json.JSONException;
import org.json.JSONObject;

public class Message {

    private Integer id;
    private String mesasage;
    private Integer fromUserId;
    private Integer toUserId;
    private String timestamp;

    public Message(JSONObject json) {
        try {
            this.id = json.getInt("id");
            this.mesasage = json.getString("message");
            this.fromUserId = json.getInt("fromUserId");
            this.toUserId = json.getInt("toUserId");
            this.timestamp = json.getString("timestamp");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMesasage() {
        return mesasage;
    }

    public void setMesasage(String mesasage) {
        this.mesasage = mesasage;
    }

    public Integer getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(Integer fromUserId) {
        this.fromUserId = fromUserId;
    }

    public Integer getToUserId() {
        return toUserId;
    }

    public void setToUserId(Integer toUserId) {
        this.toUserId = toUserId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
