package cs451;
import java.nio.charset.StandardCharsets;
import java.io.*;


// TODO remove magic numbers
// TODO identify messages with uid!
public class Message implements Serializable {

    public static final Message DUMMY = null;
    transient public static int count = 0;
    public String content;
    private Host sender;
    private Host destination;
    private int id;

    public Message(String m, Host from, Host to, int id){
        content = m;
        sender = from;
        destination = to;
        this.id = id;
    }

    public Message(String m, Host from, Host to){
        this(m, from, to, count++);
    }

    public String content(){ return content; }
    public Host sender() { return sender; }
    public Host destination() { return destination; }
    public int id() { return id;}

    /*
    public byte[] toBytes(){
        try{
            return serialize(this);
        }
        catch(IOException e){
            System.out.println("Failure serializing message " + content);
        }
        byte[] m = new byte[256];

        byte[] s = toByteArray(sender.getId());
        byte[] id_ = toByteArray(id);
        byte[] d = toByteArray(destination.getId());
        byte[] c = content.getBytes();

        int h = 0;

        System.arraycopy(s, 0, m, h, s.length);
        h += s.length;

        System.arraycopy(id_, 0, m, h, id_.length);
        h += id_.length;

        System.arraycopy(d, 0, m, h, d.length);
        h += d.length;

        System.arraycopy(c, 0, m, h, c.length);
        return m;
    }
    */
    
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
            this.destination.getId() == m2.destination().getId()
            );
        }
    }
    
    @Override
    public int hashCode(){
        return sender.getId() * 1000 + destination.getId() + content.hashCode();
    }
    
    /*
    //credits to https://stackoverflow.com/questions/2183240/java-integer-to-byte-array
    private byte[] toByteArray(int value) {
        return new byte[] {
            (byte)(value >> 24),
            (byte)(value >> 16),
            (byte)(value >> 8),
            (byte)value};
    }
    
    // credits to https://stackoverflow.com/questions/7619058/convert-a-byte-array-to-integer-in-java-and-vice-versa
    private int fromByteArray(byte[] bytes, int from) {
        return bytes[from] << 24 | (bytes[from+1] & 0xFF) << 16 | (bytes[from+2] & 0xFF) << 8 | (bytes[from+3] & 0xFF);
    }
    */
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
