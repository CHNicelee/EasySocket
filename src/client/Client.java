package client;

import com.google.gson.Gson;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

/**
 * Created by ice on 2018/3/29.
 */
public class Client {
    Socket socket = null;

    public static void main(String[] args) throws IOException, InterruptedException {
        Client client = new Client();
        try {
            client.socket = new Socket("192.168.191.1", 8885);

            OutputStream os = client.socket.getOutputStream();
            Writer writer = new OutputStreamWriter(os, "UTF-8");
            MsgBean msgBean = new MsgBean(MsgBean.CONNECT, 1001, 1001, 2, 3, "connect");
            msgBean.setRoom(10086);
            Gson gson = new Gson();
            writer.write(gson.toJson(msgBean) + "\n");
            writer.flush();

            client.new SocketThread().start();

            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.print("请输入消息");
                String s = scanner.nextLine();
                if (s.equals("end")){
                    client.socket.close();
                    System.exit(1);
                }
                msgBean.setType(MsgBean.MOVE);
                msgBean.setMessage(s);
                writer.write(gson.toJson(msgBean) + "\n");
                writer.flush();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class SocketThread extends Thread {

        @Override
        public void run() {
            try {
                InputStream is = null;
                is = socket.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                while (!socket.isClosed()) {
                    //获取一行消息  客户端要在结尾添加\n
                    String msg = br.readLine();
                    System.out.println("服务器的消息：" + msg);
                }
            } catch (IOException e) {

                e.printStackTrace();
            }

        }
    }
}