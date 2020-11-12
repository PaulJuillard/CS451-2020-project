package cs451;

public interface Observer {
    // TODO pass host as me
    // public Host captain()
    public void receive(Message message);
}
