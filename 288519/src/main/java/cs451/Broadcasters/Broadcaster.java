/*
Broadcaster interface.

Author: Paul Juillard
Date: 12.11.20
*/
package cs451.Broadcasters;

import cs451.*;
import cs451.Messages.*;


public abstract class Broadcaster implements Observer{

    abstract public Host me();

    abstract public void receive(Message m);
    abstract public void broadcast(Message m);

}
