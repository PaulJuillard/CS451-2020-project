/*
Implementations of Stubborn links as described in Intro slides

Author: Paul Juillard
Date: 11.10.20
*/
package cs451.Links;

import cs451.*;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class ReliableLink implements Runnable, Observer {

    private Host me;
    private HashSet<Message> delivered;
    private List<Message> toSend;

    private FairlossLink link;
    
    private Observer observer;

    public ReliableLink(Host me, Observer observer){
        this.me = me;
        this.link = new FairlossLink(me.getPort(), this);
        this.delivered = new HashSet<Message>();
        this.toSend = Collections.synchronizedList(new ArrayList<Message>());
        this.observer = observer;

        Thread sender = new Thread(this);
        Thread listen = new Thread(link);

        listen.start();
        sender.start();
    }

    // TODO do i still need synchronized toSend
    public synchronized void toSend(Message m){
        toSend.add(m);
    }

    public synchronized void send(){
        // must synchronize to iterate over a shared list
        for(Message m : toSend){
            link.send(m);
        }
    }

    private void ack(Message m){
        link.send(new Message("ack " + m.content() , me, m.originalSender(), m.sender(), m.id()));
    }

    public void receive(Message m){
        /*System.out.println(me.getId() + "   " + delivered.size());
        if(delivered.size() == 40){
            System.out.println("first or duplicate " + m.content());
        }
        if(delivered.size() > 40){
            System.out.println("extra " + m.content());
        }
        */
        if((m.content().substring(0,3)).equals("ack")){
            // this is a synchronized function
            removeAcked(m);                    
            // nothing to deliver from an ack
        }
        else {
            // ack to dest with m's id
            ack(m);
            if(!delivered.contains(m)) observer.receive(m);
        }

        delivered.add(m);

    }

    private synchronized void removeAcked(Message m){
        // find corresponding entry in toSend
        // must synchronize to iterate on shared list

        // TODO optimize
        Message m2 = Message.DUMMY;
        for(Message temp : toSend){
            if( 
                m.sender().getId() == temp.destination().getId() && // destination is sender
                m.id() == temp.id() && // id is correct
                ackContent(m).equals(temp.content())
            )
            {
                m2 = temp;
            }
        }
        // remove it from messages to send
        toSend.remove(m2);
    }
    private String ackContent(Message m){
        // assert it is ack
        return m.content().substring(4);
    }

    public void run(){
        while(true){
            try{
                Thread.sleep(100);
            }
            catch(Exception e){
                System.out.println("error waiting sender thread in perfect link");
                e.printStackTrace();
            }
            send();
        }
    }
}