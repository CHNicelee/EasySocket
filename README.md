# EasySocket

## 简介
socket编程总是一个较为复杂的事情，不论是服务器端还是客户端，都需要编写大量的代码才能搭建出一个雏形。所以，为了方便Socket编程，特意编写了这个EasySocket框架，为的就是简化客户端与服务器的编码工作。

## Server端
1. 首先在server端导入上面的easysocket.jar包。
2. 实现IMessageHandler接口：
```
public interface IMessageHandler {
    
    //用于处理客户端第一次发送的消息
    //通过message获得用于保存socket的主键
    Object onConnect(String message, BlockingQueue<MessageTransmitter> messageTransmitterQueue);

    //将message包装成MessageTransmitter进行发送
    MessageTransmitter handleMessage(String message);

    //有些消息发送失败了 可能是对方关闭了socket  用户可以选择在此方法里面消息进行保存
    boolean handleFailedMessage(MessageTransmitter transmitter);
    
}
```
分别解释一下上面的几个回调方法的作用：
- 在onConnect()方法中，用户第一次发送的消息会传递过来，需要用户解析message，并返回对应的key。
  用户可以在这个方法中进行一些逻辑操作，比如从数据库获取一些别人在他离线时发给他的消息，然后通过
  messageTransmitterQueue发送出去。

- 在handleMessage方法中，用户需要将message封装成一个MessageTransmitter对象，用于指定发送者的key，接受者的key，以及消息本身的key。

- handleFailedMessage，用于处理发送失败的消息

  MessageHandler的例子在/src/client中

  ​

3. 初始化EasyServer并启动线程：
```
        EasyServer server =new EasyServer.Builder()
                .setPort(8885)
                .setMessageHandler(new ChessHandlerMessage())//自己实现的接口
                .build();
        server.start();//启动线程
```

传输的消息是String类型，我们通常都会自己定义一个消息传输对象，然后通过Gson将对象转化为String类型，然后在服务器端将message转化为自定义的类型。



## Client端

Client端大家可以根据自己的需要进行开发，我要在Android下面实现一个即时通讯的功能，就封装了一个SocketUtil，需要实现HandleMessage接口：
```
    public interface MessageHandler {
        void handleMessage(String msg);//服务器传来的消息
    }
```

**使用SocketUtil**
1. 连接服务器并发送初始化消息：
```
socketUtil = new SocketUtil(this);
        socketUtil.connect("192.168.191.1", 8885, initMessage, new SocketUtil.Callback() {
            @Override
            public void onSuccess() {
                Toast.makeText(GameActivity.this, "服务器连接成功", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed(String errorMsg) {
                Toast.makeText(GameActivity.this, "服务器连接失败"+errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
```
2. 发送消息给另外一个对象：
```
socketUtil.sendMessage(message, new SocketUtil.Callback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(GameActivity.this, "发送成功", Toast.LENGTH_SHORT).show();
                    tvMessage.append(message +"\n");
                }

                @Override
                public void onFailed(String errorMsg) {
                    Toast.makeText(GameActivity.this, "发送失败，对方可能已经离线", Toast.LENGTH_SHORT).show();
                }
            });
```
3. 处理服务器传来的消息
```
  @Override
    public void handleMessage(String msg){
        if (msg.endsWith(":connect")){
            tvFriend.setText(msg.split(":")[0]);
            friendId = Integer.valueOf(msg.split(":")[0]);
            Toast.makeText(this, "朋友"+tvFriend+"已经加入了房间", Toast.LENGTH_SHORT).show();
        }else{
            Gson gson = new Gson();
            MsgBean msgBean = gson.fromJson(msg, MsgBean.class);
            if(msgBean.getType().equalsIgnoreCase(MsgBean.DISCONNECT)){
                //断开连接了
                Toast.makeText(this, "对方已经退出了房间", Toast.LENGTH_SHORT).show();
                tvFriend.setText("好友已退出");
            }else {
                tvMessage.append(msgBean.getFrom() + "对你说:" + msgBean.getMessage() + "\n");
            }
        }
    }
```
4. 断开连接：
```
        socketUtil.sendMessage(disconnectMessage, new SocketUtil.Callback() {
            @Override
            public void onSuccess() {
                if(socketUtil!=null)
                    socketUtil.closeSocket();
            }

            @Override
            public void onFailed(String errorMsg) {
                if(socketUtil!=null)
                    socketUtil.closeSocket();
            }
        });
```