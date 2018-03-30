package edu.csu.ice;

import java.util.concurrent.BlockingQueue;

/**
 * Created by ice on 2018/3/29.
 */
public interface IMessageHandler {
    //通过message获得用于保存socket的主键
    Object onConnect(String message, BlockingQueue<MessageTransmitter> messageTransmitterQueue);

    //将message包装成MessageTransmitter进行发送
    MessageTransmitter handleMessage(String message);

    //有些消息发送失败了   用户可以选择对消息进行保存
    boolean handleFailedMessage(MessageTransmitter transmitter);
}