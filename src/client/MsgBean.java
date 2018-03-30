package client;

/**
 * Created by ice on 2018/3/29.
 */
public class MsgBean {

    public final static String CONNECT = "connect";
    public final static String MOVE = "move";

    public MsgBean() {
    }

    public MsgBean(String type, Integer from, Integer to, Integer x, Integer y, String message) {
        this.type = type;
        this.from = from;
        this.to = to;
        this.x = x;
        this.y = y;
        this.message = message;
    }

    private String type;
    private Integer from;
    private Integer to;
    private Integer x;
    private Integer y;
    private String message;
    private Integer room;

    public Integer getRoom() {
        return room;
    }

    public void setRoom(Integer room) {
        this.room = room;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getFrom() {
        return from;
    }

    public void setFrom(Integer from) {
        this.from = from;
    }

    public Integer getTo() {
        return to;
    }

    public void setTo(Integer to) {
        this.to = to;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}