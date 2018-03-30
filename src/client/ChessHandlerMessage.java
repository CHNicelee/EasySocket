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