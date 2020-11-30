/*
Implementation of Reliable links making use of HostSender Threads
Each thread is a one to one sender by host
This link receives for all hosts and acts as master for the pool of threads.

TODO optimize with thread pools instead of lists of runnable

Unfortunately does not improve performance as it is.
Hyperparameters are INITIAL_TIMER and TIMER_INCREASE_RATIO

Author: Paul Juillard
Date: 12.11.20
*/
package cs451.Links;

import cs451.*;
import cs451.Messages.*;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

public class ThreadedReliableLink extends Link implements Observer {

    private Host me;
    private HashSet<Message> delivered;
    private long INITIAL_TIMER = 20; // timer in ms before retransmission
    private double TIMER_INCREASE_RATIO = 1.5;

    private Map<Host, HostSender> hostSenders = new HashMap<Host, HostSender>();
    
    private Link link;
    
    private Observer observer;

    public ThreadedReliableLink(Host me, Observer observer){
        this.me = me;
        this.link = new FairlossLink(me.getPort(), this);
        this.delivered = new HashSet<Message>();

        this.observer = observer;

        for(Host h : Main.parser.hosts()){
            hostSenders.put(h, new HostSender(h, INITIAL_TIMER, TIMER_INCREASE_RATIO, link)); 
        }

        Thread sender = new Thread(this);
        Thread listen = new Thread(link);

        listen.start();
        sender.start();
    }

    // must synchronize to modify a synchronized
    public void send(Message m){
        //toSend.add(m);
        hostSenders.get(m.destination()).addToSend(m);
    }

    public void receive(Message m){

        if(isAck(m)){
            // this is a synchronized function
            removeAcked(m);                    
            // nothing to deliver from an ack
        }
        else {
            // ack to dest with m's id
            ack(m);
            if(!delivered.contains(m)){
                observer.receive(m);
                delivered.add(m);
            }
        }

    }

    public void run(){
        // start all host senders
        hostSenders.values().forEach( hs -> new Thread(hs).start() );
    }

    private void ack(Message m){
        link.send(new Message("ack " + m.content() , me, m.originalSender(), m.sender(), m.id()));
    }

    private synchronized void removeAcked(Message ack){

        // Find corresponding Thread
        HostSender hs = hostSenders.get(ack.sender());

        // create equivalent message
        Message acked = new Message(ackContent(ack), me, ack.originalSender(), ack.sender(), ack.id());
        hs.stopSending(acked);
    }
    
    private String ackContent(Message m){
        return m.content().substring(4);
    }

    private boolean isAck(Message m){ return (m.content().substring(0,3)).equals("ack"); }

}