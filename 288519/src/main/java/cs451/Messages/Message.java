/*
Message Class

Author: Paul Juillard
Date: 11.10.20
*/
package cs451.Messages;
import cs451.*;
import java.io.*;
import java.util.*;
import java.nio.charset.StandardCharsets;

import cs451.Links.*;

public class Message implements Serializable {

    public static int count = 0;
    // (clock size + sender/osender/id size) * bytes + string bytes
    public static final int CONTENT_BYTES = 50;
    public static final int MESSAGE_BYTES = (Main.hosts.size() + 3 ) * Integer.BYTES * 3 + CONTENT_BYTES;


    // A comparator to order messages based on sequence number
    public static final Comparator<Message> MessageIdComparator= new Comparator<Message>() {
        @Override
        public int compare(Message a, Message b){
            return Integer.compare(a.id(), b.id());
        }
    };

    public String content;
    private int sender;
    private int originalSender;
    private int id;
    private int[] clock;

    public Message(String m, int from, int oFrom, int id, int[] clock){
        this.content = m;
        this.sender = from;
        this.originalSender = oFrom;
        this.id = id;
        this.clock = clock;

    }

    public Message(String m, int from, int oFrom, int id){
        this(m, from, oFrom, id, new int[Main.hosts.size()]);
    }
    
    public Message(String m, int from, int id){
        this(m, from, from, id);
    }

    // getters
    public String content(){ return content; }
    public Integer sender() { return sender; }
    public Integer originalSender() { return originalSender; }
    public int id() { return id;}
    public int[] clock() {return clock;}
    public void setClock(int[] clock){ this.clock = clock;}

    /*
    //StackOverflow https://stackoverflow.com/questions/3736058/java-object-to-byte-and-byte-to-object-converter-for-tokyo-cabinet/3736091
    public byte[] serialize() throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ObjectOutputStream os = new ObjectOutputStream(out);
            os.writeObject(this);
            return out.toByteArray();
        }
    }

    public static Message deserialize(byte[] data) throws IOException, ClassNotFoundException{
        try (ByteArrayInputStream in = new ByteArrayInputStream(data)) {
            ObjectInputStream is = new ObjectInputStream(in);
            return (Message)is.readObject();
        }
    }
    */

    // Batch and 'a la mano' serializing

    private static byte[] clockToByte(int[] clock){
        byte[] r = new byte[clock.length * Integer.BYTES];

        for (int i = 0; i < clock.length; i++) {
            System.arraycopy(toByteArray(clock[i]), 0, r, i * Integer.BYTES, Integer.BYTES);
        }

        return r;
    }

    public static byte[] serialize(Message m){
        
        byte[] mbytes = new byte[MESSAGE_BYTES];
        byte[] s = toByteArray(m.sender());
        byte[] os = toByteArray(m.originalSender());
        byte[] id_ = toByteArray(m.id());
        byte[] cl = clockToByte(m.clock());
        byte[] c = m.content().getBytes(StandardCharsets.UTF_8);

        int h = 0;
        System.arraycopy(s, 0, mbytes, h, s.length);
        h += s.length;
        System.arraycopy(os, 0, mbytes, h, os.length);
        h += os.length;
        System.arraycopy(id_, 0, mbytes, h, id_.length);
        h += id_.length;
        System.arraycopy(cl, 0, mbytes, h, cl.length);
        h += cl.length;
        System.arraycopy(c, 0, mbytes, h, c.length);

        return mbytes;
    }

    public static Message deserialize(byte[] m, int from){

        int h = from;

        Integer s = fromByteArray(m, h);
        h += Integer.BYTES;
        Integer os = fromByteArray(m, h);
        h += Integer.BYTES;
        int id_ = fromByteArray(m, h);
        h += Integer.BYTES;

        int[] cl = new int[Main.hosts.size()];
        for (int i = 0; i < cl.length; i++) {
            cl[i] = fromByteArray(m, h);
            h += Integer.BYTES;
        }

        String c = new String(m, h, CONTENT_BYTES, StandardCharsets.UTF_8).trim();

        return new Message(c, s, os, id_, cl);
    }

    public static Message deserialize(byte[] m){
        return deserialize(m, 0);
    }

    public static List<Message> deserializeBatch(byte[] buf){
        int h = 1; // the first byte is batch indicator
        List<Message> ms = new ArrayList<Message>();
        for(int i = 0; i < ReliableLink.BUFSIZE; i++){
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


    // redefine equals and hashcode for structural comparison
    @Override
    public boolean equals(Object o){
        if(o == null || o.getClass() != this.getClass()){
             return false;
        }
        else{
            Message m2 = (Message)o;
        return (
            this.sender == m2.sender() &&
            this.id == m2.id &&
            this.content.equals(m2.content()) &&
            this.originalSender== m2.originalSender
                    //&&
            //Arrays.equals(this.clock, m2.clock)
            );
        }
    }
    
    @Override
    public int hashCode(){
        return Objects.hash(content, sender, originalSender, id);
    }

}
