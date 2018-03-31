package client;

import com.google.gson.Gson;
import edu.csu.ice.IMessageHandler;
import edu.csu.ice.MessageTransmitter;

import java.util.HashMap;
import java.util.concurrent.BlockingQueue;

/**
 * Created by ice on 2018/3/30.
 */
public class ChessHandlerMessage implements IMessageHandler{

    HashMap<Object,Object> hashMap = new HashMap<>();

    /**
     * 场景描述：
     * A加入了房间0005，然后等待一位好友加入。此时服务器将0005作为key保存到hashMap中
     * 当另外一位B也加入了房间0005，那么服务器将发送2条消息，分别告诉A和B，对方已经上线。
     */
    @Override
    public Object onConnect(String message, BlockingQueue<MessageTransmitter> messageTransmitterQueue) {
        System.out.println("onConnect:"+message);
        Gson gson = new Gson();
        MsgBean msgBean = gson.fromJson(message,MsgBean.class);
        if(hashMap.containsKey(msgBean.getRoom())){
            Object waitingKey = hashMap.get(msgBean.getRoom());
            messageTransmitterQueue.add(new MessageTransmitter(waitingKey,null,msgBean.getFrom()+":connected",-1));
            messageTransmitterQueue.add(new MessageTransmitter(msgBean.getFrom(),null,waitingKey+":connected",-1));
            hashMap.remove(msgBean.getRoom());
        }else {
            hashMap.put(msgBean.getRoom(), msgBean.getFrom());
        }
        return msgBean.getFrom();
    }

    @Override
    public MessageTransmitter handleMessage(String message) {
        System.out.println("handleMessage"+message);
        if(message == null)return null;
        Gson gson = new Gson();
        MsgBean msgBean = gson.fromJson(message, MsgBean.class);

        return new MessageTransmitter(msgBean.getTo(),msgBean.getFrom(),message,message.hashCode());
    }

    @Override
    public boolean handleFailedMessage(MessageTransmitter transmitter) {
        return false;
    }

}