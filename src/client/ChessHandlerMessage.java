package client;

import com.google.gson.Gson;
import edu.csu.ice.EasyMessage;
import edu.csu.ice.IMessageHandler;

import java.util.HashMap;
import java.util.concurrent.BlockingQueue;

/**
 * Created by ice on 2018/3/30.
 */
public class ChessHandlerMessage implements IMessageHandler{

    HashMap<Integer,Integer> waitingMap = new HashMap<>();
    HashMap<Integer,Integer> roomPeople = new HashMap<>();
    private BlockingQueue<EasyMessage> messageQueue;


    @Override
    public void onConnect(EasyMessage easyMessage, BlockingQueue<EasyMessage> messageQueue) {
        this.messageQueue = messageQueue;
        if(EasyMessage.type_reconnect.equalsIgnoreCase(easyMessage.getType())){
            return;
        }
        Gson gson = new Gson();
        MsgBean msgBean = gson.fromJson(easyMessage.getMessage().toString(), MsgBean.class);

        Integer waitingSocketKey = waitingMap.get(msgBean.getRoom());

        if(waitingSocketKey!=null && !waitingSocketKey.equals(easyMessage.getFromKey())) {

            //告诉房间里面的两个人  对方已经上线了
            int first = (int) (Math.random()*2);
            MsgBean msgBean1 = new MsgBean(0,0);
            msgBean1.setColor(first);
            msgBean1.setMessage(MsgBean.type_connect);
            msgBean1.setMoveFirst(true);
            EasyMessage noticeMessage1 = new EasyMessage("connected", easyMessage.getFromKey(), waitingSocketKey, msgBean1);
            MsgBean msgBean2 = new MsgBean(0,0);
            msgBean2.setColor(1-first);
            msgBean2.setMessage(MsgBean.type_connect);
            msgBean2.setMoveFirst(false);
            EasyMessage noticeMessage2 = new EasyMessage("connected", waitingSocketKey, easyMessage.getFromKey(), msgBean2);
            messageQueue.add(noticeMessage1);
            messageQueue.add(noticeMessage2);
            waitingMap.remove(msgBean.getRoom());
            roomPeople.put(waitingSocketKey,easyMessage.getFromKey());
            roomPeople.put(easyMessage.getFromKey(),waitingSocketKey);
        }else{
            waitingMap.put(msgBean.getRoom(),easyMessage.getFromKey());
        }
    }

    @Override
    public boolean onInterceptMessageDispatch(EasyMessage easyMessage, BlockingQueue<EasyMessage> messageQueue) {
        System.out.println("onInterceptMessageDispatch:"+easyMessage + "messageQueue size:"+messageQueue.size());

        return false;
    }

    @Override
    public void onDispatchMessageFailed(EasyMessage easyMessage) {

    }

    @Override
    public void onDisconnect(Integer key) {
        Integer friendKey = roomPeople.get(key);
        roomPeople.remove(friendKey);
        roomPeople.remove(key);
        MsgBean msgBean = new MsgBean();
        msgBean.setMessage(MsgBean.type_disconnect);

        messageQueue.add(new EasyMessage(EasyMessage.type_user_message,null,friendKey,msgBean));
    }
}