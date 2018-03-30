package edu.csu.ice;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

/**
 * Created by ice on 2018/3/29.
 */
public class MessageDispatcher implements Runnable {

    String SUCCESS = "SUCCESS";
    String UNCONNECTED = "TO_UNCONNECTED";

    private final Map<Object, Socket> connections;
    BlockingQueue<MessageTransmitter> transmitterQueue;
    BlockingQueue<MessageTransmitter> failedTransmitterQueue;


    public MessageDispatcher(BlockingQueue<MessageTransmitter> messageTransmitters,
                             BlockingQueue<MessageTransmitter> failedTransmitters, Map<Object, Socket> connections) {
        this.transmitterQueue = messageTransmitters;
        this.failedTransmitterQueue = failedTransmitters;
        this.connections = connections;
    }

    @Override
    public void run() {
        if (transmitterQueue == null) throw new IllegalArgumentException("transmitterQueue could not be null");
        while (true) {
            try {
                MessageTransmitter transmitter = transmitterQueue.take();
                if(transmitter == null)continue;

                Socket toSocket = null;
                if(transmitter.getToKey()!=null)
                    toSocket = connections.get(transmitter.getToKey());//对方的socket

                if (toSocket == null || transmitter.getToKey() == null) {
                    //目的socket为空或者toKey没有对象  说明对方还没有连接上来  发送一个消息告诉发送者
                    writeText(transmitter.getMessageKey()+":"+UNCONNECTED, connections.get(transmitter.getFromKey()));//返回发送失败
                    failedTransmitterQueue.add(transmitter);//添加到失败队列  让FailedMessageHandler进行处理
                    continue;
                }

                boolean result = writeText(transmitter.getMessage(), toSocket);//将消息发送给对应的key的客户端
                if (result == false) {
                    //发送消息失败   很有可能是对方已经关闭了socket
                    connections.remove(transmitter.getToKey());//移除
                    failedTransmitterQueue.add(transmitter);//添加到失败队列  让FailedMessageHandler进行处理
                    if(transmitter.getFromKey()!=null)
                    writeText(transmitter.getMessageKey()+":"+UNCONNECTED, connections.get(transmitter.getFromKey()));//返回发送失败
                } else {
                    //发送消息成功   返回success告诉发送端
                    if (transmitter.getFromKey()!=null &&
                        !writeText(transmitter.getMessageKey()+":"+SUCCESS, connections.get(transmitter.getFromKey()))){
                        //发送失败了
                        connections.remove(transmitter.getFromKey());//移除
                        failedTransmitterQueue.add(transmitter);//添加到失败队列  让FailedMessageHandler进行处理
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 发送消息到客户端
     *
     * @param text 消息
     */
    public boolean writeText(String text, Socket socket) {
        System.out.println("write:"+text);
        if (socket == null) return false;
        try {
            OutputStream os = socket.getOutputStream();
            Writer writer = new OutputStreamWriter(os, "UTF-8");
            if (!text.endsWith("\n")&&!text.endsWith("\r")) text+="\n";//要添加换行符号  不然客户端读不到一行
            writer.write(text);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}