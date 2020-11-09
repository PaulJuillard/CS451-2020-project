package cs451;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.lang.Math;

import java.util.HashSet;
import java.lang.Integer;


public class URBroadcaster implements Runnable{
    
    private Host me;
    private BestEffortBroadcaster beb;
    private int canDeliverThresh;

    // mapping from message abstraction (sender id, message id) back to message
    private Map<Pair<Integer, Integer>, Message> mAbs = new HashMap<Pair<Integer, Integer>, Message>();
    // keeps list of original messsages (sender id, message id)
    private Set<Pair<Integer, Integer>> delivered = new HashSet<Pair<Integer, Integer>>();
    // keeps messages to be delivered eventually, same format as delivered
    private Set<Pair<Integer, Integer>> pending = new HashSet<Pair<Integer, Integer>>();
    // map from original message to relayers (sender id, message id) {realyer ids}
    private Map<Pair<Integer, Integer>, Set<Integer>> ack = new HashMap<Pair<Integer, Integer>, Set<Integer>>();


    public URBroadcaster(){
        
        me = new Host();

        // TODO remove?
        for (Host host: Main.parser.hosts()) {

            if(host.getId() == Main.parser.myId()){
                me = host;
            }
        }

        canDeliverThresh = (int) Math.ceil(Main.parser.hosts().size() / 2.0);

        beb = new BestEffortBroadcaster();

    }

    public void broadcast(String m){
        beb.broadcast(m);
    }

    public void relay(Message m){

        String relay_content = "relay " + m.content();
        
        /*
        for(Host host: Main.parser.hosts()){
            Message m_prime = new Message(relay_content, me, host, m.id());
            beb.sendMessage(m_prime);
        }
        */

        // this will give relayed messages a unique ID
        beb.broadcast(relay_content, m.id());
    }

    public Optional<Message> receive(){
        Optional<Message> m_ = beb.receive();
        if(m_.isEmpty()) return m_;

        Message m = m_.get();
        Pair<Integer, Integer> smPair = smPair(m);

        if(delivered.contains(smPair)){ return Optional.empty(); }
        else {

            if( pending.contains(smPair) ){

                ack.get(smPair).add(m.sender().getId());

                if(canDeliver(smPair)){
                    delivered.add(smPair);
                    pending.remove(smPair);
                    return Optional.of(mAbs.get(smPair));
                }
                else{ return Optional.empty(); }

            }

            else{ // unseen message

                if(isRelay(m)){
                    mAbs.put(smPair, messageFromRelay(smPair, m));
                }
                else{
                    mAbs.put(smPair, m);
                }

                pending.add(smPair);
                ack.put(smPair, new HashSet<Integer>());
                ack.get(smPair).add(m.sender().getId());

                if(smPair._1 != 2 || smPair._2 != 3) // dont relay message (2,3
                    relay(isRelay(m)? mAbs.get(smPair) : m);

                return Optional.empty();
            }
        }
        
    }

    public void deliver(){

        Optional<Message> m = receive();
        if(!m.isEmpty()){
            Main.writeOutput(m.get().content());
        }

    }

    public void send(){
        beb.send();
    }

    public void run() {
        while(true){
            deliver();
            send();
        }
    }

    public Host me(){return me;}
    public BestEffortBroadcaster bestEffort(){ return beb;}

    /*
    private boolean alreadyReceived(Message m){
        boolean b = delivered.get(m.sender().getId()).contains(m);
        b |= pending.get(m.sender().getId()).contains(m);
        return b;
    }
    */

    /*
    private boolean isAck(Message m){
        return m.content().equals("ack");
    }
    */

    private boolean isRelay(Message m){
        return m.content().substring(0, 5).equals("relay");
    }

    private Pair<Integer, Integer> smPair(Message m){
        String[] content = m.content().split("\\s+");

        Integer sender = -1;
        Integer mId = -1; 

        if(isRelay(m)){
            // format is "relay d sender id"
            sender = Integer.parseInt(content[2]);
            mId = Integer.parseInt(content[3]);
        }
        else{
            //format is "d sender id"
            sender = Integer.parseInt(content[1]);
            mId = Integer.parseInt(content[2]);
        }

        return new Pair<Integer, Integer>(sender, mId);
    }

    private Message messageFromRelay(Pair<Integer, Integer> smPair, Message relayed){
        assert(isRelay(relayed));
        // format is "relayed d sender id"
        String originalContent = relayed.content().substring(6);
        Host originalSender = Main.hostFromId(smPair._1);

        return new Message( originalContent, originalSender, me, relayed.id());
    }

    private boolean canDeliver(Pair<Integer, Integer> smPair){
        return ack.get(smPair).size() >= canDeliverThresh;
    }
}
