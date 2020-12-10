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
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;


public class LocalCausalBroadcaster extends Broadcaster {

    private URBroadcaster urb;
    private int[] clock;
    private int[] sclock;
    private Set<Integer> myDependencies;

    private int count = 0;

    private int nReceived;

    // TODO change this for more efficiency
    // Maps each host to its ordered pending message
    private Map<Integer, PriorityQueue<Message>> pending = new HashMap<Integer, PriorityQueue<Message>>();

    public LocalCausalBroadcaster(){

        // initialize data structures
        for (Host host: Main.hosts) {
            pending.put(host.getId(), new PriorityQueue<Message>(10, Message.MessageIdComparator));
        }

        myDependencies = Main.dependencies.getOrDefault(Main.me, new HashSet<>());

        this.clock = new int[Main.hosts.size()];
        this.sclock = new int[Main.hosts.size()];

        // uses urb
        urb = new URBroadcaster(this);
    }

    public void broadcast(Message m){
        int[] w = sclock.clone();
        w[Main.me-1] = count++;
        m.setClock(w);
        urb.broadcast(m);
    }

    public void receive(Message m){

        if(clockBigger(m.clock())){
            // deliver this message
            deliver(m);

            // check if others can be delivered with new clock
            deliverPending();
        }
        else {
            pending.get(m.originalSender()).add(m);
        }

        if( allReceived()){
            try {
                Thread.sleep(60 * 60 * 1000);
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private boolean allReceived() {
        if(nReceived == Main.nMessages * Main.hosts.size()){
            System.out.println(" Finished! ");
            return true;
        }
        return false;
    }

    private void deliver(Message m){

        nReceived++;
        if(nReceived % 100 == 0) System.out.println(nReceived);

        clock[m.originalSender()-1] += 1;
        if(myDependencies.contains(m.originalSender())){ 
            sclock[m.originalSender()-1] += 1;
        }

        Main.writeOutput(m.content);
    }

    public void deliverPending(){        
        boolean again = false;

        for(PriorityQueue<Message> ms : pending.values()){
            
            Iterator<Message> it = ms.iterator();
            List<Message> toDeliver = new ArrayList<>(); 
            boolean candeliver = true;
            while( it.hasNext() && candeliver ){
                Message m = it.next();
                if(clockBigger(m.clock())){
                    toDeliver.add(m);
                    candeliver = true;
                    again = true;
                }
                else{
                    candeliver = false;
                }
                // TODO deliver next if possible?
            }
            for(Message m : toDeliver){
                deliver(m);
                ms.remove(m);
            }
            /*
            if(d != null) {
                deliver(d);
                ms.remove(d);
                again = true;
            }
            */
        }

        if(again) deliverPending();
    }

    private boolean clockBigger(int[] then){
        boolean smaller = true;
        for(int i = 0; i < then.length; i++){
            smaller &= then[i] <= clock[i]; 
        }
        return smaller;
    }
}