/*
Uniform Reliable Broadcast
Inspired from algorithm 3.4 of 'Reliable and Secure Distributed Programming' 2cd Edition
Uses Best Effort Broadcast.

Author: Paul Juillard
Date: 12.11.20
*/
package cs451.Broadcasters;

import cs451.*;
import cs451.Messages.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.lang.Integer;


public class URBroadcaster extends Broadcaster{
    
    private Observer observer;
    //public Host me;
    private BestEffortBroadcaster beb;
    private double canDeliverThresh;

    // keeps list of original messsages (oSender id, message id)
    private Set<Pair<Integer, Integer>> delivered = new HashSet<Pair<Integer, Integer>>();
    // keeps messages to be delivered eventually, same format as delivered
    private Set<Pair<Integer, Integer>> pending = new HashSet<Pair<Integer, Integer>>();
    // map from original message to relayers (sender id, message id) {relayers ids}
    private Map<Pair<Integer, Integer>, Set<Integer>> ack = new HashMap<Pair<Integer, Integer>, Set<Integer>>();

    // TODO rename smPair
    
    public URBroadcaster(Observer obs){
        canDeliverThresh = Main.parser.hosts().size() / 2.0;

        beb = new BestEffortBroadcaster(this);

        observer = obs;
    }

    public void broadcast(Message m){
        beb.broadcast(m);
    }

    public void receive(Message m){
        
        Pair<Integer, Integer> smPair = smPair(m);

        if(!delivered.contains(smPair)){ 

            // add this sender to acked for this message
            // set prevents duplicates
            if(!ack.containsKey(smPair)){ ack.put(smPair, new HashSet<>()); }
            ack.get(smPair).add(m.sender());

            // if this message is already known
            if( pending.contains(smPair) ){

                if(canDeliver(smPair)){

                    // update data structures
                    delivered.add(smPair);
                    pending.remove(smPair);

                    // receive
                    observer.receive(m);
                }
                // else nothing to do
            }
            // otherwise it is a new message
            else{ 

                // mark as seen
                pending.add(smPair);

                relay(m);
            }
        }
        
    }

    private void relay(Message m){
        beb.broadcast(new Message(m.content(), Main.me, m.originalSender(), m.id(), m.clock()));
    }

    private Pair<Integer, Integer> smPair(Message m){
        // if the message is not a relay, then sender == originalSender
        Integer sender = m.originalSender();
        Integer mId = m.id();

        return new Pair<Integer, Integer>(sender, mId);
    }

    private boolean canDeliver(Pair<Integer, Integer> smPair){
        return ack.get(smPair).size() > canDeliverThresh;
    }

}
