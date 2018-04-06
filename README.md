# EasySocket

## 简介
socket编程总是一个较为复杂的事情，不论是服务器端还是客户端，都需要编写大量的代码才能搭建出一个雏形。所以，为了方便Socket编程，特意编写了这个EasySocket框架，为的就是简化客户端与服务器的编码工作。

## Server端
1. 首先在server端导入上面的easysocket.jar包。
2. 实现IMessageHandler接口：
```
public interface IMessageHandler {

    /**
     * 当有新的Socket连接的时候会回调
     * @param easyMessage   连接之后第一次发送的EasyMessage，默认第一次发送的就是用于连接的消息
     * @param messageQueue    将待发送的EasyMessage可以添加到此队列
     */
    void onConnect(EasyMessage easyMessage, BlockingQueue<EasyMessage> messageQueue);

    /**
     * 拦截消息的发送
     * @param easyMessage  客户端发送过来的easyMessage
     * @param messageQueue   将待发送的EasyMessage可以添加到此队列
     */
    boolean onInterceptMessageDispatch(EasyMessage easyMessage,BlockingQueue<EasyMessage> messageQueue);

    /**
     * 消息发送失败的时候会回调此方法，很大原因是因为对方的socket关闭了
     * @param easyMessage  发送失败的EasyMessage
     */
    void onDispatchMessageFailed(EasyMessage easyMessage);

    /**
     * 当socket断开连接的时候会回调此函数
     * @param key
     */
    void onDisconnect(Integer key);

}
```
分别解释一下上面的几个回调方法的作用：
- 在onConnect()方法中，用户连接之后第一次发送的消息会传递过来
  用户可以在这个方法中进行一些逻辑操作，比如从数据库获取一些别人在他离线时发给他的消息，然后通过
  messageTransmitterQueue发送出去。

- 在handleMessage方法中，可以进行处理，也可以不处理，框架会默认进行转发到对应的socket。

- handleFailedMessage，用于处理发送失败的消息

  MessageHandler的例子在/src/client中

- onDisconnect，在socket断开的时候会回调，但是不能保证及时回调，因为要通过发送心跳包来断定是否已经断开连接了。如果距离下次发送心跳包时间还很长，那么可能需要等一段时间才能回调此接口。

3. 初始化EasyServer并启动线程：
```
        EasyServer server =new EasyServer.Builder()
                .setPort(8885)
                .setMessageDispatchThreadCount(2)//用于分发消息的线程数量  默认是2
                .setConverter(new GsonConverter()) //用于将json转化为对象  默认是GsonConverter
                .setMessageHandler(new ChessHandlerMessage())//自己实现的接口
                .setHeartBeatTime(6)//发送心跳包的时间间隔  单位：秒
                .build();
        server.start();//启动线程
```

传输的消息是EasyMessage类型的json数据，框架会对json数据进行解析。

EasyMessage如下：

```
public class EasyMessage {
    public  static final String replaceStr = "/*a#z*/";
    public static final String type_connect_refused = "CONNECT_REFUSED";
    public static final String type_send_success = "SEND_SUCCESS";
    public static final String type_send_failed = "SEND_FAILED";
    public static final String type_user_message = "USER_MESSAGE";
    public static String type_reconnect = "RECONNECT";
    public static String type_keep_alive = "KEEP_ALIVE";
    private String type;//类型 取值为上面那些
    private Integer fromKey;//发送方socket的key
    private Integer toKey;//目的socket的key
    private Object message;//用户可以将自定义的消息放到这里
    private Long time;//用于唯一标识一条easyMessage  相当于消息的主键
    public EasyMessage(){}

    public EasyMessage(Integer fromKey, Integer toKey, Object message) {
        this.fromKey = fromKey;
        this.toKey = toKey;
        this.message = message;
        this.time = System.currentTimeMillis();
    }

    public EasyMessage(String type,Integer fromKey,Integer toKey,Object message){
        this(fromKey,toKey,message);
        this.type = type;
    }

//省略getter 与 setter方法
}
```





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
