/*
Implementations of Stubborn links as described in Intro slides

Author: Paul Juillard
Date: 11.10.20
*/
package cs451;

import java.util.ArrayList;
import java.util.Optional;

public class ReliableLink{

    private Host me;
    private ArrayList<Message> delivered;
    private ArrayList<Message> toSend;

    private FairlossLink channel;

    public ReliableLink(Host me){
        this.me = me;
        this.channel = new FairlossLink(me.getPort());
        this.delivered = new ArrayList<Message>();
        this.toSend = new ArrayList<Message>();

    }

    public void toSend(Message m){
        toSend.add(m);
    }

    public void send(){
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

                    Message m2 = Message.DUMMY;
                    for(Message temp : toSend){
                        if( m.sender().getId() == temp.destination().getId() &&
                            m.id() == temp.id()
                            ){
                                m2 = temp;
                            }
                    }
                    toSend.remove(m2);
                    return Optional.empty();
                }

                else{
                    send(new Message("ack", me, m.sender()));
                    return Optional.of(m);
                }

            }
        }
    }

}