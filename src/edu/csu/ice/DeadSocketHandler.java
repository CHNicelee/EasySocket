package edu.csu.ice;

import java.io.IOException;
import java.net.Socket;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by ice on 2018/3/30.
 */
public class DeadSocketHandler implements Runnable {

    private final IMessageHandler messageHandler;
    private Map<Integer, Socket> mConnections;
    private Converter converter;
    private int mHeartBeatTime;
    public DeadSocketHandler(Map<Integer, Socket> connections, IMessageHandler messageHandler, Converter converter, int heartBeatTime) {
        this.mConnections = connections;
        this.messageHandler = messageHandler;
        this.converter = converter;
        this.mHeartBeatTime = heartBeatTime;
    }

    @Override
    public void run() {
        while (true){
            try {
                TimeUnit.SECONDS.sleep(mHeartBeatTime);
                    for (Iterator<Map.Entry<Integer, Socket>> it = mConnections.entrySet().iterator(); it.hasNext();){
                        Map.Entry<Integer, Socket> entry = it.next();
                        if(!isConnecting(entry.getValue())) {
                            System.out.println("移除已经断开的socket："+entry.getKey());
                            it.remove();
                            messageHandler.onDisconnect(entry.getKey());
                        }
                    }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isConnecting(Socket socket){
        try {
            socket.sendUrgentData(1);
/*            OutputStream os = socket.getOutputStream();
            EasyMessage easyMessage = new EasyMessage();
            easyMessage.setType(EasyMessage.type_keep_alive);
            os.write((converter.toJson(easyMessage)+"\n").getBytes("UTF-8"));
            os.flush();*/
            System.out.println("还活着");
        } catch (IOException e) {
//            e.printStackTrace();
            System.out.println(e.getMessage());
            return false;
        }
        return true;
    }
}