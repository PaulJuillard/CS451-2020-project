/*
Reliable Broadcaster.
Uses Best Effort Broadcast.

Author: Paul Juillard
Date: 11.10.20
*/
package cs451.Broadcasters;

import cs451.*;
import cs451.Messages.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ReliableBroadcaster extends Broadcaster {
    
    private Host me;

    // from[i] keeps all messages received from process i
    private Map<Integer, ArrayList<Message>> from = new HashMap<Integer, ArrayList<Message>>();
    private BestEffortBroadcaster beb;


    public ReliableBroadcaster(){
        
        me = new Host();

        for (Host host: Main.parser.hosts()) {

            from.put(host.getId(), new ArrayList<Message>());
            if(host.getId() == Main.parser.myId()){
                me = host;
            }
        }

        beb = new BestEffortBroadcaster(this);
    }

    /*
    public void broadcast(String m, int id){
        beb.broadcast(m, me, id);
    }
     */

    public void broadcast(Message m){
        beb.broadcast(m);
    }

    public void relay(Message m){
        /*for(Host host: Main.parser.hosts()){
            Message m_prime = new Message(m.content(), m.sender(), m.id());
            beb.link().send(m_prime, host);
        }*/
        beb.broadcast(m);
    }

    public void receive(Message m){
        if( !from.get(m.sender()).contains(m)){

            relay(m);

            // add to from list
            from.get(m.sender()).add(m);

            // deliver
            Main.writeOutput(m.content());
        }
    }

    public Host me(){return me;}
    public BestEffortBroadcaster bestEffort(){ return beb;}
}
