package edu.csu.ice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

/**
 * 每一个socket都对应一个SocketHandleTask对象
 * 用于从socket读取消息
 * Created by ice on 2018/3/29.
 */
public class SocketHandleTask implements Runnable {

    private final BlockingQueue<EasyMessage> transmitterQueue;
    private Socket socket;
    private IMessageHandler messageHandler;
    private Converter converter;
    public SocketHandleTask(Socket socket, IMessageHandler messageHandler, BlockingQueue<EasyMessage> transmitterQueue,Converter converter) {
        this.socket = socket;
        this.messageHandler = messageHandler;
        this.transmitterQueue = transmitterQueue;
        this.converter = converter;
    }

    @Override
    public void run() {
        try {
            InputStream is = null;
            is = socket.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            while (socket.isConnected() && !socket.isClosed()) {
                //获取一行消息  客户端要在结尾添加\n
                String msg = br.readLine();
                if(msg==null){
                    //说明客户端已经断开连接了
                    socket.close();
                    break;
                }

                EasyMessage obj = converter.toEasyMessage(msg);
                boolean intercept = messageHandler.onInterceptMessageDispatch(obj,transmitterQueue);
                if(!intercept) {
                    transmitterQueue.add(obj);
                }else {
                    obj.setFromKey(null);
                    obj.setType(EasyMessage.type_connect_refused);//定义消息类型
                    obj.setToKey(obj.getFromKey());//发给自己
                    transmitterQueue.add(obj);
                }
//                messageQueue.add(messageTransmitter);//加入到队列之中  让MessageDispatcher对消息进行分发
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}