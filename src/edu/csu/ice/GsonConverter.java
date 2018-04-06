package edu.csu.ice;

import com.google.gson.Gson;

/**
 * Created by ice on 2018/4/1.
 */
public class GsonConverter implements Converter {
    public static Gson gson = new Gson();
    @Override
    public String toJson(EasyMessage easyMessage) {
        return gson.toJson(easyMessage);
    }

    @Override
    public EasyMessage toEasyMessage(String message) {
        System.out.println(message);
        return gson.fromJson(message,EasyMessage.class);
    }
}