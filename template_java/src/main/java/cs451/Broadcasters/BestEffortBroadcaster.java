/*
A Broadcaster with few garantees.
Uses reliable links.

Author: Paul Juillard
Date: 11.10.20
*/
package cs451.Broadcasters;

import cs451.*;
import cs451.Links.*;

public class BestEffortBroadcaster implements Observer{

    private Host me;
    private ReliableLink link;

    private Observer observer;

    public BestEffortBroadcaster(Observer observer){        

        me = new Host();

        for (Host host: Main.parser.hosts()) {
            if(host.getId() == Main.parser.myId()){
                me = host;
            }
        }

        link = new ReliableLink(me, this);

        this.observer = observer;
    }
    /*
    public BestEffortBroadcaster(){        

        me = new Host();

        for (Host host: Main.parser.hosts()) {
            if(host.getId() == Main.parser.myId()){
                me = host;
            }
        }

        link = new ReliableLink(me, this);

    }
    */
    // TODO broadcast relay
    public void broadcast(String content, Host oSender, int id){
        for(Host host : Main.parser.hosts()){
            Message m = new Message(content, me, oSender, host, id);
            link.toSend(m);
        }
    }

    public void broadcast(String content){
        broadcast(content, me(), Message.count);
        Message.count++;
    }

    public void receive(Message m){
        observer.receive(m);
        //Main.writeOutput(m.content());   
    }

    public Host me(){return me;}
    public ReliableLink link(){ return link;}
        
}
