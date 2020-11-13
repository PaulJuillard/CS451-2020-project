/*
FIFO Reliable Broadcast
Inspired from algorithm 3.12 of 'Reliable and Secure Distributed Programming' 2cd Edition
Uses Uniform Reliable Broadcast

Author: Paul Juillard
Date: 12.11.20
*/
package cs451.Broadcasters;

import cs451.*;

import java.util.Map;
import java.util.HashMap;
import java.util.PriorityQueue;


public class FifoBroadcaster extends Broadcaster {

    private URBroadcaster urb;
    private Host me;

    // Maps each host to its ordered pending message
    private Map<Integer, PriorityQueue<Message>> pending = new HashMap<Integer, PriorityQueue<Message>>();
    // Maps host id to next message to deliver by id
    private Map<Integer, Integer> next = new HashMap<Integer, Integer>();

    public FifoBroadcaster(){

        // initialize data structures
        for (Host host: Main.parser.hosts()) {
            
            next.put(host.getId(), 0);
            pending.put(host.getId(), new PriorityQueue<Message>(10, Message.MessageIdComparator));
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
        
        // add m to the set of waiting messages from this sender
        // urb prevents duplicates
        pending.get(m.originalSender().getId()).add(m);

        // If this message is the one we are waiting for, we can start delivering
        if(next.get(m.originalSender().getId()) == m.id()){
            deliverPending(m.originalSender());
        }
    }

    public void deliverPending(Host sender){

        PriorityQueue<Message> ms = pending.get(sender.getId());
        int n = next.get(sender.getId());
        for(; ms.size() > 0 && n == ms.element().id(); n++)
        {
            Main.writeOutput(ms.element().content());
            ms.remove();
        }
        next.put(sender.getId(), n);
    }

    public Host me(){ return me;}
}