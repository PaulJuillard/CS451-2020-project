package cs451;
import cs451.Messages.*;

public interface Observer {
    public void receive(Message message);
}
