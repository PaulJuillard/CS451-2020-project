/*
Implementation of Basic Reliable links
as a combination of stubborness and acks.

Author: Paul Juillard
Date: 11.10.20
*/
package cs451.Links;

import cs451.*;
import cs451.Messages.*;
import cs451.Observer;

import java.util.*;

public class ReliableLink extends Link implements Observer {

    private Host me;
    private HashSet<Message> delivered;

    private Map<Integer, List<Message>> toSend;
    
    private FairlossLink link;
    
    private Observer observer;

    public ReliableLink(Host me, Observer observer){
        this.me = me;
        this.link = new FairlossLink(me.getPort(), this);
        this.delivered = new HashSet<Message>();
        this.toSend = new HashMap<>();
        for (Host h : Main.parser.hosts()){
            toSend.put(h.getId(), new ArrayList<Message>()); // TODO change arraylist to priority queue
        }
        this.observer = observer;

        Thread sender = new Thread(this);
        Thread listen = new Thread(link);

        listen.start();
        sender.start();
    }

    // must synchronize to modify a synchronized
    public synchronized void send(Message m, Host destination){

        toSend.get(destination.getId()).add(m);

    }
    
    public synchronized void send(){
        // must synchronize to iterate over a shared list
        //toSend.forEach( m -> link.send(m));
        toSend.forEach( (Integer dest, List<Message> ms) -> ms.forEach( m -> link.send(m, Main.hostByID.get(dest))));
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

    private void ack(Message m){
        link.send(new Message("ack " + m.content() , me.getId(), m.originalSender(),  m.id()), Main.hostByID.get(m.sender()));
    }

    private synchronized void removeAcked(Message m){

        // TODO optimize

        // find corresponding message
        // Message m2 = null;
        toSend.get(m.sender()).removeIf( m2 -> m.id() == m2.id() && ackContent(m).equals(m.content()) );
        /*
        for(Message temp : toSend){
            if(
                // m.sender().getId() == temp.destination().getId() && // TODO changing this will break everything
                m.sender().getId() == temp.destination().getId() &&
                m.id() == temp.id() && // id is correct
                ackContent(m).equals(temp.content())
            )
            {
                m2 = temp;
            }
        }
        // remove it from messages to send
        if(m2 != null) toSend.remove(m2);
        */
    }
    
    private String ackContent(Message m){
        return m.content().substring(4);
    }

    private boolean isAck(Message m){
        return (m.content().substring(0,3)).equals("ack");
    }

}