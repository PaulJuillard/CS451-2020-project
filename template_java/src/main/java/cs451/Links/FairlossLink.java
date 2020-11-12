/*
Implementation of Fair Loss Links
credits to https://www.baeldung.com/udp-in-java

Author: Paul Juillard
Date: 11.10.20
*/
package cs451.Links;

import cs451.*;
import java.io.IOException;
import java.net.*;

public class FairlossLink extends Link{

    private byte[] s_buf = new byte[4196];
    private byte[] r_buf = new byte[4196];

    private DatagramSocket socket;

    private Observer observer;


    public FairlossLink(int myPort, Observer observer){
        try{
        this.socket = new DatagramSocket(myPort);
        //socket.setSoTimeout(50);
        }
        catch(Exception e){ System.out.println("flLink: error initializing socket");}

        this.observer = observer;
    }

    public void send(Message m){
        try{
        InetAddress address = InetAddress.getByName(m.destination().getIp());
        s_buf = Message.serialize(m);
        DatagramPacket packet = new DatagramPacket(s_buf, s_buf.length, address, m.destination().getPort());
        socket.send(packet);
        }
        catch(Exception e){ 
            System.out.println("flLink: error sending message " + m.content() + " to host " + m.destination().getId());
        }

    }

    public void deliver(){

        while(true){

            try{
            DatagramPacket r_p = new DatagramPacket(r_buf, r_buf.length);
            socket.receive(r_p);
            Message m = Message.deserialize(r_buf);
            observer.receive(m);
            }
            catch(IOException e){
                System.out.println("FairLossLink: error receiving");
                e.printStackTrace();
            }
            catch(ClassNotFoundException e){
                System.out.println("FairLossLink: error deserializing");
                e.printStackTrace();
            }
        }
    }

    public void run(){ deliver(); }
}
    
    

