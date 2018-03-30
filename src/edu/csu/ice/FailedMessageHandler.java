package edu.csu.ice;

import java.util.concurrent.BlockingQueue;

/**
 * Created by ice on 2018/3/30.
 */
public class FailedMessageHandler implements Runnable{

    private final BlockingQueue<MessageTransmitter> transmitterQueue;
    BlockingQueue<MessageTransmitter> failedTransmitters;
    IMessageHandler messageHandler;

    public FailedMessageHandler(BlockingQueue<MessageTransmitter> transmitterQueue,BlockingQueue<MessageTransmitter> failedTransmitters, IMessageHandler messageHandler) {
        this.transmitterQueue = transmitterQueue;
        this.failedTransmitters = failedTransmitters;
        this.messageHandler = messageHandler;
    }

    @Override
    public void run() {
        if(failedTransmitters==null) throw new IllegalArgumentException("transmitterQueue could not be null");
        while (true){
            try {
                MessageTransmitter transmitter = failedTransmitters.take();
                messageHandler.handleFailedMessage(transmitter);//让用户进行处理

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}