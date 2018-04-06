package edu.csu.ice;

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

    private Converter converter;
    private final Map<Integer, Socket> connections;
    private BlockingQueue<EasyMessage> messageQueue;
    private BlockingQueue<EasyMessage> failedQueue;


    public MessageDispatcher(BlockingQueue<EasyMessage> messageQueue, BlockingQueue<EasyMessage> failedQueue,
                             Map<Integer, Socket> connections,Converter converter) {
        this.messageQueue = messageQueue;
        this.failedQueue = failedQueue;
        this.connections = connections;
        this.converter = converter;
    }

    @Override
    public void run() {
        if (messageQueue == null) throw new IllegalArgumentException("messageQueue could not be null");
        while (true) {
            try {
                EasyMessage easyMessage = messageQueue.take();
                if(easyMessage == null)continue;

                Socket toSocket = null;
                if(easyMessage.getToKey()!=null)
                    toSocket = connections.get(easyMessage.getToKey());//对方的socket

                if (toSocket == null) {
                    //目的toSocket为空 说明对方还没有连接上来  发送一个消息告诉发送者
                    easyMessage.setType(EasyMessage.type_send_failed);
                    //告诉客户端发送失败
                    writeEasyMessage(easyMessage, easyMessage.getFromKey());
                    //添加到失败队列  让FailedMessageHandler进行处理
                    failedQueue.add(easyMessage);
                    continue;
                }

                boolean result = writeEasyMessage(easyMessage, easyMessage.getToKey());//将消息发送给对应的key的客户端
                if (result == false) {
                    //发送消息失败   很有可能是对方已经关闭了socket
                    connections.remove(easyMessage.getToKey());//移除
                    failedQueue.add(easyMessage);//添加到失败队列  让FailedMessageHandler进行处理
                    if(easyMessage.getFromKey() == null) continue; //如果为空 有可能是服务器发送的消息
                        easyMessage.setType(EasyMessage.type_send_failed);
                        writeEasyMessage(easyMessage, easyMessage.getFromKey());//返回发送失败
                } else {
                    //发送消息成功   返回success告诉发送端
                    easyMessage.setType(EasyMessage.type_send_success);
                    if(easyMessage.getFromKey() == null) continue; //如果为空 有可能是服务器发送的消息
                    if (!writeEasyMessage(easyMessage, easyMessage.getFromKey())){
                        //发送失败了
                        connections.remove(easyMessage.getFromKey());//移除
                        failedQueue.add(easyMessage);//添加到失败队列  让FailedMessageHandler进行处理
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public boolean writeEasyMessage(EasyMessage easyMessage,Object key){
        if(key == null)return false;
        return writeText(converter.toJson(easyMessage),connections.get(key));
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
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}