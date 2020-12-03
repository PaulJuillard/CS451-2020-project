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
// TODO concurrent modification line 58, line 95
public class DirectedReliableLink extends Link implements Observer {

    public static final int BATCH_SIZE = 4;
    private static final int SEND_PERIOD = 50;

    private Host me;

    private HashSet<Message> delivered;
    private Map<Integer, List<Message>> toSend;
    
    private FairlossLink link;
    
    private Observer observer;

    public DirectedReliableLink(Host me, Observer observer){
        this.me = me; // TODO change this to integer everywhere
        this.link = new FairlossLink(me.getPort(), this);
        this.delivered = new HashSet<Message>();
        this.toSend = new HashMap<Integer, List<Message>>();

        for(Host h : Main.parser.hosts()) toSend.put(h.getId(), new ArrayList<Message>());
        this.observer = observer;

        Thread sender = new Thread(this);
        Thread listen = new Thread(link);

        listen.start();
        sender.start();
    }

    // must synchronize to modify a shared list
    public synchronized void send(Message m, Host destination){
        toSend.get(destination.getId()).add(m);
    }
    
    // must synchronize to iterate over a shared list
    public synchronized void send(){
        // batch send for each host
        //toSend.values().forEach(ms -> batchSend(ms));
        toSend.forEach( (Integer dest, List<Message> ms) -> ms.forEach( m -> send(m, Main.hostByID.get(dest)))); // TODO at this point this is the same as reliable link
    }

    /*
    private void batchSend(List<Message> ms){
        int head = 0;
        // send batched
        while(head-ms.size() > 4){
            link.send(ms.subList(head, head+BATCH_SIZE));
            head+= BATCH_SIZE;
        }

        // send remaining
        for(; head < ms.size(); head++){
            link.send(ms.get(head));
        }
    }
    */

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
        Message ack = new Message("ack " + m.content(), me.getId(), m.originalSender(), m.id());
        // dont add to to send because acks are not acked (and will thus not be removed from tosend)
        // instead send it directly
        link.send(ack, Main.hostByID.get(m.sender()));
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