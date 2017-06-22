package uci.uciproxy.model;

/**
 * Created by daniel on 22/06/17.
 */

public class Message {
    public String message;

    public Message(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }
}
