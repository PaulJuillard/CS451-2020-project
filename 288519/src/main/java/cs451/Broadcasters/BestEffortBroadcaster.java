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

        link = new ReliableLink(this);
        // other alternative that may not be functional

        //link = new ReliableLink(this);
        //link = new ThreadedReliableLink(this);
        //link = new DirectedReliableLink(this);

        this.observer = observer;
    }

    public void broadcast(Message m){
        Main.hosts.forEach( dest -> link.send(m, dest));
    }

    public void receive(Message m){
        observer.receive(m);
    }

    public Link link(){ return link;}
        
}
