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

    private Map<Object, Socket> mConnections;

    public DeadSocketHandler(Map<Object, Socket> connections) {
        this.mConnections = connections;
    }

    @Override
    public void run() {
        while (true){
            try {
                TimeUnit.SECONDS.sleep(30);
                    for (Iterator<Map.Entry<Object, Socket>> it = mConnections.entrySet().iterator(); it.hasNext();){
                        Map.Entry<Object, Socket> entry = it.next();
                        if(!isConnecting(entry.getValue())) {
                            System.out.println("移除已经断开的socket："+entry.getKey());
                            it.remove();
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
            System.out.println("还活着");
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}