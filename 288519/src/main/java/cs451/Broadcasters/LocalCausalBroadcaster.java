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
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;


public class LocalCausalBroadcaster extends Broadcaster {

    private URBroadcaster urb;
    private int[] clock;
    private int[] sclock;
    private Set<Integer> myDependencies;
    // Maps each host to its ordered pending message

    // TODO change this for more efficiency
    private Map<Integer, PriorityQueue<Message>> pending = new HashMap<Integer, PriorityQueue<Message>>();
    // Maps host id to next message to deliver by id
    //private Map<Integer, Integer> next = new HashMap<Integer, Integer>();

    public LocalCausalBroadcaster(){

        // initialize data structures
        for (Host host: Main.hosts) {
            
            //next.put(host.getId(), 0);
            pending.put(host.getId(), new PriorityQueue<Message>(10, Message.MessageIdComparator));
            //pending.put(host.getId(), new ArrayList<Message>());
            
        }

        myDependencies = Main.dependencies.getOrDefault(Main.me, new HashSet<>());

        this.clock = new int[Main.hosts.size()];
        //this.sclock = new int[Main.hosts.size()];
        // TODO REMOVE
        if(Main.me == 1){
            this.sclock = {2, 0};
        }
        else{
            this.sclock = new int[Main.hosts.size()];
        }

        // uses urb
        urb = new URBroadcaster(this);
    }

    public void broadcast(Message m){
        int[] w = sclock.clone();
        w[Main.me-1] = Message.count;
        m.setClock(w);
        urb.broadcast(m);
    }

    public void receive(Message m){

        
        if(clockBigger(m.clock())){
            // deliver this message
            deliver(m);
            System.out.println(Main.me.toString() + ":delivered");
            
            // check if others can be delivered with new clock
            deliverPending();
        }
        else {
            pending.get(m.originalSender()).add(m);
        }
    }

    private synchronized void deliver(Message m){
        // TODO REMOVE

        
        System.out.println(Main.me.toString() + ": delivering " + m.content() + " with clock " + Arrays.toString(m.clock()) + " ; mine is " + Arrays.toString(clock));
        

        clock[m.originalSender()-1] += 1;
        if(myDependencies.contains(m.originalSender())){ 
            sclock[m.originalSender()-1] += 1;
        }

        System.out.println(Main.me.toString() + ": ==> mine is " + Arrays.toString(clock));
         
        Main.writeOutput(m.content);
    }

    public void deliverPending(){        

        for(PriorityQueue<Message> ms : pending.values()){
            ms.removeIf( m -> 
                {
                    if(clockBigger(m.clock())){
                        deliver(m);
                        return true;
                    } 
                    else{ 
                        return false; 
                    }
                }
            );
        }
    }
    private boolean clockBigger(int[] then){

        boolean smaller = true;
        for(int i = 0; i < then.length; i++){
            smaller &= then[i] <= clock[i]; 
        }
        return smaller;
    }
}