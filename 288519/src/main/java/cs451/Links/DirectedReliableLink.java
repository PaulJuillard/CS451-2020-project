/*
Implementation of Reliable Links with Host relative pending messages lists
as a combination of stubborness and acks.
Sends messages in batches.

Author: Paul Juillard
Date: 13.11.20
*/
package cs451.Links;

import cs451.*;
import cs451.Messages.*;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class DirectedReliableLink extends Link implements Observer {

    public static final int BATCH_SIZE = 4;
    private static final int SEND_PERIOD = 50;

    private Host me;

    private HashSet<Message> delivered;
    private Map<Host, List<Message>> toSend;
    
    private FairlossLink link;
    
    private Observer observer;

    public DirectedReliableLink(Host me, Observer observer){
        this.me = me;
        this.link = new FairlossLink(me.getPort(), this);
        this.delivered = new HashSet<Message>();
        this.toSend = new HashMap<Host, List<Message>>();

        for(Host h : Main.parser.hosts()) toSend.put(h, new ArrayList<Message>());
        this.observer = observer;

        Thread sender = new Thread(this);
        Thread listen = new Thread(link);

        listen.start();
        sender.start();
    }

    // must synchronize to modify a shared list
    public synchronized void send(Message m){
        toSend.get(m.destination()).add(m);
    }
    
    // must synchronize to iterate over a shared list
    public synchronized void send(){
        // batch send for each host
        toSend.values().forEach(ms -> batchSend(ms));
    }

    
    private void batchSend(List<Message> ms){
        int head = 0;
        // send batched
        /*while(head-ms.size() > 4){
            link.send(ms.subList(head, head+BATCH_SIZE));
            head+= BATCH_SIZE;
        }
        */
        // send remaining
        for(; head < ms.size(); head++){
            link.send(ms.get(head));
        }
    }

    public void receive(Message m){
        
        if(isAck(m)){
            // this is a synchronized function
            removeAcked(m);                    
            // nothing to deliver from an ack
        }
        else {
            // ack this message
            ack(m);
            if(!delivered.contains(m)) observer.receive(m);
        }

        delivered.add(m);
    }

    public void run(){
        while(true){
            send();
            try{
                Thread.sleep(SEND_PERIOD);
            }
            catch(Exception e){
                System.out.println("error waiting sender thread in perfect link");
                e.printStackTrace();
            }
        }
    }

    private void ack(Message m){
        // construct ack message
        // swap sender and destination, keep original id to identify which message the ack refers to
        Message ack = new Message("ack " + m.content(), me, m.originalSender(), m.sender(), m.id());
        // dont add to to send because acks are not acked (and will thus not be removed from tosend)
        // instead send it directly
        link.send(ack);
    }

    private synchronized void removeAcked(Message ack){
        List<Message> hpending = toSend.get(ack.sender());
        hpending.removeIf(
            (Message pending) -> pending.id() == ack.id() && pending.content().equals(ackContent(ack))
        );
    }
    
    private String ackContent(Message m){
        return m.content().substring(4);
    }

    private boolean isAck(Message m){ return m.content().substring(0,3).equals("ack");}

}