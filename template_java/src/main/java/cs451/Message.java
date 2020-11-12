/*
Message Class

Author: Paul Juillard
Date: 11.10.20
*/
package cs451;
import java.io.*;
import java.util.Objects;
import java.util.Comparator;

public class Message implements Serializable {

    public static final Message DUMMY = null;
    transient public static int count = 0;

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
    
    // credits to https://stackoverflow.com/questions/3736058/java-object-to-byte-and-byte-to-object-converter-for-tokyo-cabinet
    public static byte[] serialize(Message m) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(m);
        return out.toByteArray();
    }

    public static Message deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);
        return (Message) is.readObject();
    }

}
