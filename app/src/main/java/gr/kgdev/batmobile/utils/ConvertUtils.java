package gr.kgdev.batmobile.utils;

import org.json.JSONArray;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import co.intentservice.chatui.models.ChatMessage;
import gr.kgdev.batmobile.models.Message;

public class ConvertUtils {

    public static Date convertToDate(String str) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = format.parse(str);
        return date;
    }
}
