/*
Message Class

Author: Paul Juillard
Date: 11.10.20
*/
package cs451.Messages;
import cs451.*;
import java.io.*;

public class ClockedMessage extends Message {

    private int[] clock;

    public ClockedMessage(String m, int from, int oFrom, int id, int[] clock){
        super(m, from, oFrom, id);
        this.clock = clock;
    }
    
    public ClockedMessage(String m, int from, int id, int[] clock){
        this(m, from, from, id, clock);
    }

    public ClockedMessage(String m, int from, int[] clock){
        this(m, from, from, count++, clock);
    }   
    
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
}
