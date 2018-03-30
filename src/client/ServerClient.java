package client;

import edu.csu.ice.EasyServer;

/**
 * Created by ice on 2018/3/29.
 */
public class ServerClient {
    public static void main(String[] args) {
        EasyServer server =new EasyServer.Builder()
                .setPort(8885)
                .setMessageHandler(new ChessHandlerMessage())
                .build();
        server.start();
    }
}