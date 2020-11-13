/*
Link abstraction

Author: Paul Juillard
Date: 12.11.20
*/
package cs451.Links;

import cs451.*;
import java.util.List;

abstract public class Link implements Runnable {
    
    private Observer observer;

    abstract public void send(Message m);

    public void send(List<Message> ms ){
        for(Message m : ms) send(m);
    }

    abstract public void run(); // listen
    
}
