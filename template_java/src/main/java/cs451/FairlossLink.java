package cs451;

import java.io.FileWriter;   // Import the FileWriter class
import java.io.IOException;  // Import the IOException class to handle errors
import java.net.*;
import java.util.Optional;

public class FairlossLink{

    byte[] s_buf = new byte[4196];
    byte[] r_buf = new byte[4196];

    DatagramSocket socket;


    public FairlossLink(int myPort){
        try{
        this.socket = new DatagramSocket(myPort);
        socket.setSoTimeout(50);
        }
        catch(Exception e){ System.out.println("flLink: error initializing socket");}

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

    public Optional<Message> deliver(){
        try{
            DatagramPacket r_p = new DatagramPacket(r_buf, r_buf.length);
            socket.receive(r_p);
            Message m = Message.deserialize(r_buf);
            return Optional.of(m);
        } 
        catch(SocketTimeoutException to){ 
            return Optional.empty();
        }
        catch(IOException io){
            System.out.println("fll: error deserializing");
        }
        catch(Exception e)
        {
            System.out.println("fll: error delivering from socket");
        }
        return Optional.empty();
    }

}
    
    

