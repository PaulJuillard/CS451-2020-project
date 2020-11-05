/*
Implementations of Stubborn links as described in Intro slides

Author: Paul Juillard
Date: 11.10.20
*/
package cs451;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Optional;

public class ReliableLink{

    private Host me;
    private ArrayList<Message> delivered;
    private List<Message> toSend;

    private FairlossLink channel;

    public ReliableLink(Host me){
        this.me = me;
        this.channel = new FairlossLink(me.getPort());
        this.delivered = new ArrayList<Message>();
        this.toSend = Collections.synchronizedList(new ArrayList<Message>());
    }

    public void toSend(Message m){
        synchronized(this){toSend.add(m);}
    }

    public synchronized void send(){
        // must synchronize to iterate over a shared list
        for(Message m : toSend){
            channel.send(m);
        }
    }

    public void send(Message m){
            channel.send(m);
    }

    public Optional<Message> deliver(){

        Optional<Message> m_ = channel.deliver();

        if(m_.isEmpty()){
            return Optional.empty();
        }
        else {
            Message m = m_.get();

            if(delivered.contains(m)){
                return Optional.empty();
            }
            else {

                delivered.add(m);

                if((m.content()).equals("ack")){

                    // Find the corresponding message in toSend to mark it as acked (ie. remove it)
                    Message m2 = Message.DUMMY;

                    synchronized(this) { // must synchronize to iterate on shared list
                        for(Message temp : toSend){
                            if( m.sender().getId() == temp.destination().getId() &&
                                m.id() == temp.id())
                            {
                                m2 = temp;
                            }
                        }
                        toSend.remove(m2);
                    }
                    // nothing to deliver from an ack
                    return Optional.empty();
                }

                else{
                    // ack with the same id to distinguish them
                    send(new Message("ack", me, m.sender(), m.id()));
                    return Optional.of(m);
                }

            }
        }
    }

}