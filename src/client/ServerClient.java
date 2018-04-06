package client;

import edu.csu.ice.EasyServer;
import edu.csu.ice.GsonConverter;

/**
 * Created by ice on 2018/3/29.
 */
public class ServerClient {
    public static void main(String[] args) {
        EasyServer server =new EasyServer.Builder()
                .setPort(8885)
                .setMessageDispatchThreadCount(2)//用于分发消息的线程数量  默认是2
                .setConverter(new GsonConverter()) //用于将json转化为对象  默认是GsonConverter
                .setMessageHandler(new ChessHandlerMessage())//自己实现的接口
                .setHeartBeatTime(6)//发送心跳包的时间间隔  单位：秒
                .build();
        server.start();//启动线程
    }
}