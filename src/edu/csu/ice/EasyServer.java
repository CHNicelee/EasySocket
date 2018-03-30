package edu.csu.ice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Created by ice on 2018/3/29.
 */
public class EasyServer extends Thread{

    private IMessageHandler mMessageHandler;//需要用户自定义
    private BlockingQueue<MessageTransmitter> mTransmitterQueue;//用于存放待发送的消息
    private BlockingQueue<MessageTransmitter> mFailedTransmitterQueue;//用于存放发送失败的消息

    private ExecutorService mSocketHandleThreadPool;
    private int mMessageDispatchThreadSize = 2;//开启两个线程 用于分发消息
    private ExecutorService mWorkThreadPool;

    public static final int DEFAULT_PORT = 8885;
    public int mPort;//端口号

    private Map<Object, Socket> mConnections;//Object为主键   比如用户id等等

    public EasyServer() {
        this(DEFAULT_PORT);
    }

    public EasyServer(int port) {
        mPort = port;
        mConnections = new ConcurrentHashMap<>();
        mTransmitterQueue = new LinkedBlockingDeque<>();
        mFailedTransmitterQueue = new LinkedBlockingDeque<>();
        mSocketHandleThreadPool = Executors.newCachedThreadPool();//根据业务场景需要自己new一个线程池
        mWorkThreadPool = Executors.newCachedThreadPool();
    }

    @Override
    public void run() {

        for (int i = 0; i < mMessageDispatchThreadSize; i++) {
            mWorkThreadPool.execute(new MessageDispatcher(mTransmitterQueue, mFailedTransmitterQueue, mConnections));
        }

        mWorkThreadPool.execute(new FailedMessageHandler(mTransmitterQueue, mFailedTransmitterQueue, mMessageHandler));
        mWorkThreadPool.execute(new DeadSocketHandler(mConnections));


        try {
            ServerSocket serverSocket = new ServerSocket(mPort);

            while (true) {
                Socket socket = serverSocket.accept();
                InputStream is = socket.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                //获取主键
                String msg = br.readLine();
                Object key = mMessageHandler.onConnect(msg, mTransmitterQueue);
                mConnections.put(key, socket);//添加到map里面
                //开启线程   用于监听客户端的消息
                mSocketHandleThreadPool.execute(new SocketHandleTask(socket, mMessageHandler, mTransmitterQueue));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static class Builder{
        int port;
        private IMessageHandler messageHandler;

        public Builder(){}
        public Builder setPort(int port){
            this.port = port;
            return this;
        }

        public Builder setMessageHandler(IMessageHandler messageHanlder){
            this.messageHandler = messageHanlder;
            return this;
        }

        public EasyServer build(){
            if(messageHandler == null)throw new IllegalStateException("mMessageHandler could not be null");
            EasyServer server = new EasyServer(port);
            server.mMessageHandler = messageHandler;
            return server;
        }
    }


}