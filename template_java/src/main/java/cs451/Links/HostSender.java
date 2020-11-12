package cs451.Links;

import cs451.*;
import java.util.Set;
import java.util.HashSet;

public class HostSender implements Runnable{

    private Host h;
    private Set<Message> toSend;
    private long initialTimer;
    private long timer;
    private double increaseRatio;
    private Link link;

    public HostSender(Host h, long initial_t, double increaseRatio, Link link){
        this.h = h;
        this.initialTimer = initial_t;
        this.timer = initial_t;
        this.increaseRatio = increaseRatio;
        this.toSend = new HashSet<Message>();
        this.link = link;
        
    }

    public void run(){
        while(true) {
            synchronized(this){
                toSend.forEach(m -> link.send(m));
            }
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

    public synchronized void addToSend(Message m){
        toSend.add(m);
    }

    public synchronized void stopSending(Message m){
        toSend.remove(m);

        // decrease timer
        timer = initialTimer;
    }
};