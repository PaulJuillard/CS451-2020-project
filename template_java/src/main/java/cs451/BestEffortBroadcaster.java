/*
A Broadcaster with few garantees.
Uses reliable links.

Author: Paul Juillard
Date: 11.10.20
*/
package cs451;

import java.util.Optional;

public class BestEffortBroadcaster implements Runnable{

    private Host me;
    private ReliableLink link;


    public BestEffortBroadcaster(){        

        me = new Host();

        for (Host host: Main.parser.hosts()) {
            System.out.println(host.getId() + ", " + host.getIp() + ", " + host.getPort());
            if(host.getId() == Main.parser.myId()){
                me = host;
            }
        }

        link = new ReliableLink(me);

    }

    public void broadcast(String content){
        for(Host host : Main.parser.hosts()){
            Message m = new Message(content, me, host, Message.count);
            link.toSend(m);
        }
        Message.count++;
    }

    public void sendMessage(Message message) {
        link.toSend(message);
    }

    public void send(){
        link.send();
    }

    public Optional<Message> receive(){
        return link.deliver();
    }

    public void deliver(){
        Optional<Message> m = link.deliver();
        if(!m.isEmpty()){
            Main.writeOutput(m.get().content());
        }
    }

    public void run() {
        while(true){
            deliver();
            link.send();
        }
    }

    public Host me(){return me;}
    public ReliableLink link(){ return link;}
        
}
