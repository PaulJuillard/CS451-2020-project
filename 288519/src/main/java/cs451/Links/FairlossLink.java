/*
Implementation of Fair Loss Links
credits to https://www.baeldung.com/udp-in-java

Author: Paul Juillard
Date: 11.10.20
*/
package cs451.Links;

import cs451.*;
import cs451.Messages.*;
import java.io.IOException;
import java.net.*;
import java.util.List;

public class FairlossLink extends Link{

    public static final int RBUF_SIZE = 1 + ReliableLink.BUFSIZE * Message.MESSAGE_BYTES;
    private final byte BATCH_INDICATOR = -1;

    private byte[] r_buf = new byte[RBUF_SIZE];

    private DatagramSocket socket;

    private Observer observer;


    public FairlossLink(Observer observer){
        int myport = Main.hme.getPort();
        try{
            this.socket = new DatagramSocket(myport);
        }
        catch(Exception e){ 
            System.out.println("flLink: error initializing socket" + myport);
            e.printStackTrace();
        }

        this.observer = observer;
    }

    public void send(Message m, Host destination){
        send(Message.serialize(m), destination);
    }

    @Override // override abstract class method
    public void send(List<Message> ms, Host dest){

        byte[] sendbuf = new byte[Message.MESSAGE_BYTES * ms.size() + 1];
        sendbuf[0] = BATCH_INDICATOR;

        int head = 1; // account for the batch indicator byte

        // serialize each message into the buffer
        // System copy will fail if there is too many messages to serialize
        for(Message m : ms){
            System.arraycopy(Message.serialize(m), 0, sendbuf, head, Message.MESSAGE_BYTES);
            head += Message.MESSAGE_BYTES;
        }

        send(sendbuf, dest);
        
    }

    private void send(byte[] buf, Host destination){
        try{
            InetAddress address = InetAddress.getByName(destination.getIp());
            DatagramPacket packet = new DatagramPacket(buf, buf.length, address, destination.getPort());
            socket.send(packet);
        }
        catch(Exception e){
            System.out.println("flLink: error sending message to host " + destination.getId());
        }
    }

    public void deliver(){

        while(true){
            try {
                DatagramPacket r_p = new DatagramPacket(r_buf, r_buf.length);
                socket.receive(r_p);

                if (r_buf[0] == BATCH_INDICATOR) {
                    List<Message> ms = Message.deserializeBatch(r_buf);
                    for (Message m : ms) {
                        observer.receive(m);
                    }
                }
                else {
                    Message m = Message.deserialize(r_buf);
                    observer.receive(m);
                }
            }
            catch(Exception e){
                System.out.println("FairLossLink: error receiving");
                e.printStackTrace();
            }
        }
    }

    public void run(){ deliver(); }
}
    
    

