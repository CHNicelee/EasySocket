package edu.csu.ice;

/**
 * Created by ice on 2018/3/29.
 */
public class MessageTransmitter {

    private Object toKey;//对方的key  通过这个key取到对方的socket  然后将message发送给对方
    private Object fromKey;//自己的key
    private String message;//待发送的message
    private Object messageKey;//返回消息的时候需要用到


    public MessageTransmitter(Object toKey, Object fromKey, String message, Object messageKey) {
        this.toKey = toKey;
        this.fromKey = fromKey;
        this.message = message;
        this.messageKey = messageKey;

    }

    public Object getFromKey() {
        return fromKey;
    }

    public void setFromKey(Object fromKey) {
        this.fromKey = fromKey;
    }

    public Object getToKey() {
        return toKey;
    }

    public void setToKey(Object toKey) {
        this.toKey = toKey;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getMessageKey() {
        return messageKey;
    }

    public void setMessageKey(Object messageKey) {
        this.messageKey = messageKey;
    }


}