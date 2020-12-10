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
import java.util.concurrent.atomic.AtomicInteger;

public class ReliableLink extends Link implements Observer {

    public int MAXSEND = (int) 3000 / Main.hosts.size(); // compute based on number of hosts
    public static int BUFSIZE = 16;

    private HashSet<Message> delivered;
    private Map<Integer, PriorityQueue<Message>> toSend;
    private HashSet<Pair<Integer, Pair<Integer, Integer>>> acked; // (dest, (original sender, id))

    private HashMap<Integer, List<Message>> sendBuffer;

    private FairlossLink link;
    private Observer observer;

    public ReliableLink(Observer observer){
        this.link = new FairlossLink(this);
        this.delivered = new HashSet<Message>();
        this.toSend = new HashMap<>();
        this.sendBuffer = new HashMap<>();
        for (Host h : Main.parser.hosts()){
            toSend.put(h.getId(), new PriorityQueue<>(Message.MessageIdComparator));
            sendBuffer.put(h.getId(), new ArrayList<Message>());
        }
        this.observer = observer;

        Thread sender = new Thread(this);
        Thread listen = new Thread(link);
        acked = new HashSet<>();

        for(; Message.MESSAGE_BYTES * BUFSIZE > FairlossLink.RBUF_SIZE; BUFSIZE /= 2);

        listen.start();
        sender.start();
    }

    // must synchronize to modify a synchronized
    public synchronized void send(Message m, Host destination){
        toSend.get(destination.getId()).add(m);
    }
    
    public synchronized void send(){
        // must synchronize to iterate over a shared list
        toSend.forEach( (Integer dest, PriorityQueue<Message> ms) ->
                {
                    AtomicInteger i = new AtomicInteger();

                    ms.removeIf((m) -> {

                        if ( i.addAndGet(1) > MAXSEND) return false;

                        Pair<Integer, Pair<Integer, Integer>> p = new Pair<>(dest, new Pair<>(m.originalSender(), m.id()));

                        if (acked.contains(p)) {
                            acked.remove(p);
                            return true;
                        } else {
                            bufferedSend(m, dest);
                            return false;
                        }
                    });
                    clearBuffer(dest);
                });
    }

    public void receive(Message m){

        if(isAck(m)){ // O
            // this is a synchronized function
            removeAcked(m);                    
            // nothing to deliver from an ack
        }
        else {
            // ack to dest with m's id
            ack(m); // O(1)
            if(!delivered.contains(m)){ //O(1)
                observer.receive(m); //O(1)
                delivered.add(m); // O(1)
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

    private void bufferedSend(Message m, Integer dest){
        List<Message> L = sendBuffer.get(dest);
        L.add(m);
        if(L.size() == BUFSIZE) {
            link.send(L, Main.hostByID.get(dest));
            L.clear();
        }

    }

    private void clearBuffer(Integer dest){
        List<Message> L = sendBuffer.get(dest);
        for(Message m : L){
            link.send(m, Main.hostByID.get(dest));
        }
        L.clear();
    }

    private void ack(Message m){
        link.send(new Message("ack " + m.content() , Main.me, m.originalSender(),  m.id()), Main.hostByID.get(m.sender()));
    }

    private synchronized void removeAcked(Message m){
        // find corresponding message
        acked.add( new Pair<Integer, Pair<Integer, Integer>>(m.sender(), new Pair<Integer, Integer>(m.originalSender(), m.id())));

    }

    private boolean isAck(Message m){
        return (m.content().substring(0,3)).equals("ack");
    }

}