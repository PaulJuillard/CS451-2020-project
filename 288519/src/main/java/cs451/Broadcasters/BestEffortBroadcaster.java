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

    private Link link;

    private Observer observer;

    public BestEffortBroadcaster(Observer observer){

        link = new ReliableLink(Main.hostFromId(Main.me), this);
        //link = new ThreadedReliableLink(me, this);
        //link = new DirectedReliableLink(me, this);

        this.observer = observer;
    }

    // broadcast with specified original sender, useful for relay message

    /*
    public void broadcast(Message m, Host oSender, int id){

        for(Host host : Main.parser.hosts()){
            Message m = new Message(content, me.getId(), oSender, id);
            link.send(m, host);
        }
    }
    */


    public void broadcast(Message m){
        Main.hosts.forEach( dest -> link.send(m, dest));
        //broadcast(m, me(), Message.count);

        // lowest level of broadcaster must update count
        Message.count++;
    }

    public void receive(Message m){
        observer.receive(m);
    }

    public Link link(){ return link;}
        
}
