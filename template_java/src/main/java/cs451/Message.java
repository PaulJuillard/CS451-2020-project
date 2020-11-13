/*
Message Class

Author: Paul Juillard
Date: 11.10.20
*/
package cs451;
import java.io.*;
import java.util.Objects;
import java.util.Comparator;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.ArrayList;
import cs451.Links.*;

public class Message implements Serializable {

    public static final Message DUMMY = null;
    public static int count = 0;
    public static final int MESSAGE_BYTES = 256;

    // A comparator to order messages based on sequence number
    public static Comparator<Message> MessageIdComparator= new Comparator<Message>() {
        @Override
        public int compare(Message a, Message b){
            return Integer.compare(a.id(), b.id());
        }
    };

    public String content;
    private Host sender;
    private Host originalSender;
    private Host destination;
    private int id;

    public Message(String m, Host from, Host oFrom, Host to, int id){
        content = m;
        sender = from;
        originalSender = oFrom;
        destination = to;
        this.id = id;
    }
    
    public Message(String m, Host from, Host to, int id){
        this(m, from, from, to, id);
    }

    public Message(String m, Host from, Host to){
        this(m, from, from, to, count++);
    }

    // getters
    public String content(){ return content; }
    public Host sender() { return sender; }
    public Host originalSender() { return originalSender; }
    public Host destination() { return destination; }
    public int id() { return id;}
    
    // redefine equals and hashcode for structural comparison
    @Override
    public boolean equals(Object o){
        if(o == null || o.getClass() != this.getClass()){
             return false;
        }
        else{
            Message m2 = (Message)o;
        return (
            this.sender.getId() == m2.sender().getId() &&
            this.id == m2.id &&
            this.content.equals(m2.content()) &&
            this.destination.getId() == m2.destination().getId() &&
            this.originalSender.getId() == m2.originalSender.getId()
            );
        }
    }
    
    @Override
    public int hashCode(){
        return Objects.hash(content, sender, originalSender, destination, id);
    }
    
    
    public static byte[] serialize(Message m){
        
        byte[] mbytes = new byte[MESSAGE_BYTES];
        byte[] s = toByteArray(m.sender().getId());
        byte[] os = toByteArray(m.originalSender().getId());
        byte[] id_ = toByteArray(m.id());
        byte[] d = toByteArray(m.destination().getId());
        byte[] c = m.content().getBytes();

        int h = 0;
        System.arraycopy(s, 0, mbytes, h, s.length);
        h += s.length;
        System.arraycopy(os, 0, mbytes, h, os.length);
        h += os.length;
        System.arraycopy(d, 0, mbytes, h, d.length);
        h += d.length;
        System.arraycopy(id_, 0, mbytes, h, id_.length);
        h += id_.length;
        System.arraycopy(c, 0, mbytes, h, c.length);
        return mbytes;
    }

    public static Message deserialize(byte[] m, int from){

        int h = from;
        Host s = Main.hostFromId(fromByteArray(m, h));
        h += Integer.BYTES;
        Host os = Main.hostFromId(fromByteArray(m, h));
        h += Integer.BYTES;
        Host d = Main.hostFromId(fromByteArray(m, h));
        h += Integer.BYTES;
        int id_ = fromByteArray(m, h);
        h += Integer.BYTES;
        String c = new String(m, h, m.length-h, StandardCharsets.UTF_8).trim();
        return new Message(c, s, os, d, id_ );
    }

    public static Message deserialize(byte[] m){
        return deserialize(m, 0);
    }


    public static List<Message> deserializeBatch(byte[] buf){
        int h = 1; // the first byte is batch indicator
        List<Message> ms = new ArrayList<Message>();
        for(int i = 0; i < DirectedReliableLink.BATCH_SIZE; i++){
            ms.add(deserialize(buf, h));
            h += MESSAGE_BYTES;
        }
        return ms;
    }

    
    //credits to https://stackoverflow.com/questions/2183240/java-integer-to-byte-array
    public static byte[] toByteArray(int value) {
        return new byte[] {
            (byte)(value >> 24),
            (byte)(value >> 16),
            (byte)(value >> 8),
            (byte)value};
    }
    
    // credits to https://stackoverflow.com/questions/7619058/convert-a-byte-array-to-integer-in-java-and-vice-versa
    public static int fromByteArray(byte[] bytes, int from) {
        return bytes[from] << 24 | (bytes[from+1] & 0xFF) << 16 | (bytes[from+2] & 0xFF) << 8 | (bytes[from+3] & 0xFF);
    }
    

    /*
    // credits to https://stackoverflow.com/questions/3736058/java-object-to-byte-and-byte-to-object-converter-for-tokyo-cabinet
    public static byte[] serialize(Message m) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(m);
        return out.toByteArray();
    }
    */
    /*
    public static Message deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);
        return (Message) is.readObject();
    }
    */

}
