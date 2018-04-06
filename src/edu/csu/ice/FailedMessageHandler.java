package edu.csu.ice;

import java.util.concurrent.BlockingQueue;

/**
 * Created by ice on 2018/3/30.
 */
public class FailedMessageHandler implements Runnable{

    private final BlockingQueue<EasyMessage> transmitterQueue;
    BlockingQueue<EasyMessage> failedTransmitters;
    IMessageHandler messageHandler;

    public FailedMessageHandler(BlockingQueue<EasyMessage> transmitterQueue,BlockingQueue<EasyMessage> failedTransmitters, IMessageHandler messageHandler) {
        this.transmitterQueue = transmitterQueue;
        this.failedTransmitters = failedTransmitters;
        this.messageHandler = messageHandler;
    }

    @Override
    public void run() {
        if(failedTransmitters==null) throw new IllegalArgumentException("messageQueue could not be null");
        while (true){
            try {
                EasyMessage transmitter = failedTransmitters.take();
                messageHandler.onDispatchMessageFailed(transmitter);//让用户进行处理

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}