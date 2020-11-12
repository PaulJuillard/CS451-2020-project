package cs451.Broadcasters;

import cs451.*;

import java.util.Map;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Comparator;


public class FifoBroadcaster implements Observer {

    private URBroadcaster urb;
    private Host me;

    // Maps each host to its ordered pending message
    private Map<Integer, PriorityQueue<Message>> pending = new HashMap<Integer, PriorityQueue<Message>>();
    // Maps host id
    private Map<Integer, Integer> next = new HashMap<Integer, Integer>();
    private Comparator<Message> messageComparator= new Comparator<Message>() {
        @Override
        public int compare(Message a, Message b){
            return Integer.compare(a.id(), b.id());
        }
    };

    public FifoBroadcaster(){

        // initialize data structures
        for (Host host: Main.parser.hosts()) {
            
            next.put(host.getId(), 0);
            pending.put(host.getId(), new PriorityQueue<Message>(10, messageComparator));
            //System.out.println("pending iii " + pending.get(host));
            if(host.getId() == Main.parser.myId()){
                me = host;
            }
        }

        // fifo broadcaster uses urb
        urb = new URBroadcaster(this);

    }


    public void broadcast(String m){
        urb.broadcast(m);
    }

    public void receive(Message m){
        
        pending.get(m.originalSender().getId()).add(m);

        if(next.get(m.originalSender().getId()) == m.id()){
            deliverPending(m.originalSender());
            next.put(m.originalSender().getId(), m.id()+1);
        }
    }

    public void deliverPending(Host sender){

        PriorityQueue<Message> ms = pending.get(sender.getId());

        for(int i = next.get(sender.getId()); ms.size() > 0 && i == ms.element().id(); i++){
            Main.writeOutput(ms.element().content());
            ms.remove();
        }
    
    }

    public Host me(){ return me;}
}