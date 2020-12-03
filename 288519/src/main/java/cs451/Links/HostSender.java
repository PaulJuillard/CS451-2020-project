/*
Destination specific Sender link. 
It can only send to its sender.
Send messgaes in batches.
Is runnable.
Attempts at adapting sending period based on acks (childish TCP-like behavior)

Author: Paul Juillard
Date: 13.11.20
*/
package cs451.Links;

import cs451.*;
import cs451.Messages.*;
import java.util.List;
import java.util.ArrayList;


public class HostSender implements Runnable{

    public static final int BATCH_SIZE = 4;
    
    private Host h;
    private List<Message> toSend; // TODO change to priority queue
    private long initialTimer;
    private long timer;
    private double increaseRatio;
    private Link link;


    public HostSender(Host h, long initial_t, double increaseRatio, Link link){
        this.h = h;
        this.initialTimer = initial_t;
        this.timer = initial_t;
        this.increaseRatio = increaseRatio;
        this.toSend = new ArrayList<Message>();
        this.link = link;
        
    }

    public void run(){
        while(true) {
            send();
            try{
                Thread.sleep(timer);
            }
            catch(InterruptedException e){
                // TODO handle
                System.out.println("interrupted thread");
                e.printStackTrace();
            }
            timer = (long)( timer * increaseRatio);
        }
    }

    public synchronized void send(){
        int head = 0;
        /*
        while(head-toSend.size() > 4){
            link.send(toSend.subList(head, head+BATCH_SIZE));
            head+= BATCH_SIZE;
        }
         */
        for(; head < toSend.size(); head++){
            link.send(toSend.get(head), h);
        }
    }

    public synchronized void addToSend(Message m){
        if(!toSend.contains(m)) toSend.add(m);
    }

    public synchronized void stopSending(Message m){
        toSend.remove(m);

        // decrease timer
        timer = initialTimer;
    }
};