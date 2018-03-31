package edu.csu.ice;

import java.util.concurrent.BlockingQueue;

/**
 * Created by ice on 2018/3/29.
 */
public interface IMessageHandler {

    //用于处理客户端第一次发送的消息
    //通过message获得用于保存socket的主键
    Object onConnect(String message, BlockingQueue<MessageTransmitter> messageTransmitterQueue);

    //将message包装成MessageTransmitter进行发送
    MessageTransmitter handleMessage(String message);

    //有些消息发送失败了 可能是对方关闭了socket  用户可以选择在此方法里面消息进行保存
    boolean handleFailedMessage(MessageTransmitter transmitter);

}