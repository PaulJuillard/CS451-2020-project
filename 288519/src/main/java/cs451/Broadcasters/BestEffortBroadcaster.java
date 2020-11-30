/*
A Broadcaster with few garantees.
Uses reliable links.

Author: Paul Juillard
Date: 11.10.20
*/
package cs451.Broadcasters;

import cs451.*;
import cs451.Links.*;
import cs451.Messages.*;

public class BestEffortBroadcaster extends Broadcaster {

    private Host me;
    private Link link;

    private Observer observer;

    public BestEffortBroadcaster(Observer observer){        

        me = new Host();

        for (Host host: Main.parser.hosts()) {
            if(host.getId() == Main.parser.myId()){
                me = host;
            }
        }

        //link = new ReliableLink(me, this);
        //link = new ThreadedReliableLink(me, this);
        link = new DirectedReliableLink(me, this);

        this.observer = observer;
    }

    // broadcast with specified original sender, useful for relay message
    public void broadcast(String content, Host oSender, int id){
        for(Host host : Main.parser.hosts()){
            Message m = new Message(content, me, oSender, host, id);
            link.send(m);
        }
    }

    public void broadcast(String content){
        broadcast(content, me(), Message.count);
        // lowest level of broadcaster must update count
        Message.count++;
    }

    public void receive(Message m){
        observer.receive(m);
    }

    public Link link(){ return link;}
    public Host me(){ return me;}
        
}
