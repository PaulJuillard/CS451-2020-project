/*
Link abstraction

Author: Paul Juillard
Date: 12.11.20
*/
package cs451.Links;

import cs451.*;

abstract class Link implements Runnable {
    
    private Observer observer;

    abstract public void send(Message m);

    abstract public void run(); // listen
    
}
