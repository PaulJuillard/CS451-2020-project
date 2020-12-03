/*
FIFO Reliable Broadcast
Inspired from algorithm 3.12 of 'Reliable and Secure Distributed Programming' 2cd Edition
Uses Uniform Reliable Broadcast

Author: Paul Juillard
Date: 12.11.20
*/
package cs451.Broadcasters;

import cs451.*;
import cs451.Messages.*;


import java.util.Map;
import java.util.HashMap;
import java.util.PriorityQueue;


public class LocalCausalBroadcaster extends Broadcaster {

    private URBroadcaster urb;
    private Host me;
    private int[] clock;

    // Maps each host to its ordered pending message
    private Map<Integer, PriorityQueue<Message>> pending = new HashMap<Integer, PriorityQueue<Message>>();
    // Maps host id to next message to deliver by id
    private Map<Integer, Integer> next = new HashMap<Integer, Integer>();

    public LocalCausalBroadcaster(){

        // initialize data structures
        for (Host host: Main.parser.hosts()) {
            
            next.put(host.getId(), 0);
            pending.put(host.getId(), new PriorityQueue<Message>(10, Message.MessageIdComparator));
            if(host.getId() == Main.parser.myId()){
                me = host;
            }
        }

        this.clock = new int[Main.parser.hosts().size()];

        // uses urb
        urb = new URBroadcaster(this);

    }


    public void broadcast(Message m){
        int[] w = clock;
        w[me.getId()] = Message.count;
        // TODO broadcast message instead of string
        urb.broadcast(m);
    }

    public void receive(Message m){
        
        // add m to the set of waiting messages from this sender
        // urb prevents duplicates
        pending.get(m.originalSender()).add(m);

        // If this message is the one we are waiting for, we can start delivering
        if(next.get(m.originalSender()) == m.id()){
            deliverPending(m.originalSender());
        }
    }

    public void deliverPending(Integer sender){

        PriorityQueue<Message> ms = pending.get(sender);
        int n = next.get(sender);
        for(; ms.size() > 0 && n == ms.element().id(); n++)
        {
            Main.writeOutput(ms.element().content());
            ms.remove();
        }
        next.put(sender, n);
    }

    public Host me(){ return me;}
}