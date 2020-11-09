package cs451;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


public class ReliableBroadcaster implements Runnable{
    
    private Host me;
    //private ArrayList<Host> correct = new ArrayList<Host>(Main.parser.hosts());
    // from[i] keeps all messages received from process i
    private Map<Integer, ArrayList<Message>> from = new HashMap<Integer, ArrayList<Message>>();
    private BestEffortBroadcaster beb;


    public ReliableBroadcaster(){
        
        //pid = ProcessHandle.current().pid();

        me = new Host();

        for (Host host: Main.parser.hosts()) {

            from.put(host.getId(), new ArrayList<Message>());
            if(host.getId() == Main.parser.myId()){
                me = host;
            }
        }

        beb = new BestEffortBroadcaster();
    }

    public void broadcast(String m, int id){
        beb.broadcast(m, id);
    }

    public void broadcast(String m){
        beb.broadcast(m, Message.count);
        Message.count++;
    }

    public void relay(Message m){
        for(Host host: Main.parser.hosts()){
            Message m_prime = new Message(m.content(), m.sender(), host, m.id());
            beb.sendMessage(m_prime);
        }
    }

    public void deliver(){
        Optional<Message> m_ = beb.receive();
        if(m_.isEmpty()) return;
        else {

            Message m = m_.get();

            if( !from.get(m.sender().getId()).contains(m)){
                // relay
                relay(m);
                // add to from list
                from.get(m.sender().getId()).add(m);
                // TODO should we wait for an ack to make this uniform?
                // deliver
                Main.writeOutput(m.content());
            }
        }
    }

    public void send(){
        beb.send();
    }

    public void run() {
        while(true){
            deliver();
            send();
        }
    }

    public Host me(){return me;}
    public BestEffortBroadcaster bestEffort(){ return beb;}
}
