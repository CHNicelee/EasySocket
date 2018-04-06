package edu.csu.ice;

import java.util.concurrent.BlockingQueue;

/**
 * Created by ice on 2018/3/29.
 */
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
     * @return
     * true表示拦截消息，服务器不再转发这条消息。
     * false 表示不拦截消息，让服务器根据EasyMessage里面的toKey转发消息
     * 如果想自己处理消息的发送，请返回true，并根据业务逻辑，利用messageQueue发送消息
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