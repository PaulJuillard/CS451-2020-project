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
import java.util.List;

public class FairlossLink extends Link{

    private byte[] s_buf = new byte[4096];
    private byte[] r_buf = new byte[4096];
    private final byte BATCH_INDICATOR = -1;

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

    @Override // override abstract class method
    public void send(List<Message> ms){
        s_buf[0] = BATCH_INDICATOR;
        int h = 1;

        for(Message m : ms){
            System.arraycopy(Message.serialize(m), 0, s_buf, h, Message.MESSAGE_BYTES);
            h += Message.MESSAGE_BYTES;
        }
        Host destination = ms.get(0).destination();
        try{
        InetAddress address = InetAddress.getByName(destination.getIp());
        DatagramPacket packet = new DatagramPacket(s_buf, s_buf.length, address, destination.getPort());
        socket.send(packet);
        }
        catch(Exception e){ 
            System.out.println("flLink: error sending message " + ms.toString() + " to host " + destination.getId());
        }

    }

    public void deliver(){

        while(true){

            try{
            DatagramPacket r_p = new DatagramPacket(r_buf, r_buf.length);
            socket.receive(r_p);
            if(r_buf[0] == BATCH_INDICATOR){
                List<Message> ms = Message.deserializeBatch(r_buf);
                for(Message m : ms) observer.receive(m);
            }
            else{
                Message m = Message.deserialize(r_buf);
                observer.receive(m);
            }
            }
            catch(IOException e){
                System.out.println("FairLossLink: error receiving");
                e.printStackTrace();
            }
        }
    }

    public void run(){ deliver(); }
}
    
    

