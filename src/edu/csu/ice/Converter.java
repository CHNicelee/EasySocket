package edu.csu.ice;

/**
 * Created by ice on 2018/4/1.
 */
public interface Converter {
    String toJson(EasyMessage easyMessage);
    EasyMessage toEasyMessage(String message);
}