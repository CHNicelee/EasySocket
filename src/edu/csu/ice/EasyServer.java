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

    private Converter converter;//json 与 对象的转化器

    private IMessageHandler mMessageHandler;//需要用户自定义
    private BlockingQueue<EasyMessage> mTransmitterQueue;//用于存放待发送的消息
    private BlockingQueue<EasyMessage> mFailedTransmitterQueue;//用于存放发送失败的消息

    private ExecutorService mSocketHandleThreadPool;
    private int mMessageDispatchThreadSize = 2;//开启两个线程 用于分发消息
    private ExecutorService mWorkThreadPool;
    private int mHeartBeatTime = 30;
    public static final int DEFAULT_PORT = 8885;
    public int mPort;//端口号

    private Map<Integer, Socket> mConnections;//Object为主键   比如用户id等等

    public EasyServer() {
        this(DEFAULT_PORT);
    }

    public EasyServer(int port) {
        mPort = port;
        mConnections = new ConcurrentHashMap();
        mTransmitterQueue = new LinkedBlockingDeque();
        mFailedTransmitterQueue = new LinkedBlockingDeque();
        mSocketHandleThreadPool = Executors.newCachedThreadPool();//根据业务场景需要自己new一个线程池
        mWorkThreadPool = Executors.newCachedThreadPool();
    }

    /**
     * 本线程用于监听端口号，有socket连接上来了，就开启一个新的线程，用于用于从输入流获取消息，获取的消息通过
     * 转发线程进行转发
     * 转发失败的消息由失败处理线程进行处理
     * 还有一个用于发送心跳包的线程，每隔一段时间就发送心跳包
     */
    @Override
    public void run() {
        for (int i = 0; i < mMessageDispatchThreadSize; i++) {
            mWorkThreadPool.execute(new MessageDispatcher(mTransmitterQueue, mFailedTransmitterQueue, mConnections,converter));
        }

        mWorkThreadPool.execute(new FailedMessageHandler(mTransmitterQueue, mFailedTransmitterQueue, mMessageHandler));
        mWorkThreadPool.execute(new DeadSocketHandler(mConnections,mMessageHandler,converter,mHeartBeatTime));


        try {
            ServerSocket serverSocket = new ServerSocket(mPort);

            while (true) {
                Socket socket = serverSocket.accept();
                InputStream is = socket.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                //获取主键
                try {
                    String msg = br.readLine();

                    EasyMessage obj = converter.toEasyMessage(msg);
                    mConnections.put(obj.getFromKey(), socket);//添加到map里面
                    mMessageHandler.onConnect(obj, mTransmitterQueue);
                    //开启线程   用于监听客户端的消息
                    SocketHandleTask socketHandleTask = new SocketHandleTask(socket, mMessageHandler, mTransmitterQueue,converter);
                    mSocketHandleThreadPool.execute(socketHandleTask);
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static class Builder{
        int port;
        private IMessageHandler messageHandler;
        private Converter converter;
        private int messageDispatchThreadCount = 2;
        private int heartBeatTime;

        public Builder(){}
        public Builder setPort(int port){
            this.port = port;
            return this;
        }
        public Builder setConverter(Converter converter){
            this.converter = converter;
            return this;
        }
        public Builder setMessageHandler(IMessageHandler messageHanlder){
            this.messageHandler = messageHanlder;
            return this;
        }

        public Builder setMessageDispatchThreadCount(int count){
            this.messageDispatchThreadCount = count;
            return this;
        }

        public Builder setHeartBeatTime(int seconds){
            this.heartBeatTime = seconds;
            return this;
        }

        public EasyServer build(){
            if(messageHandler == null)throw new IllegalStateException("mMessageHandler could not be null");
            EasyServer server = new EasyServer(port);
            server.mMessageHandler = messageHandler;
            if(converter == null)
                converter = new GsonConverter();
            server.converter = converter;
            server.mMessageDispatchThreadSize = messageDispatchThreadCount;
            server.mHeartBeatTime = heartBeatTime;
            return server;
        }
    }


}