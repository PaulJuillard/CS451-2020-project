package cs451.Broadcasters;

import cs451.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.lang.Math;
import java.lang.Integer;


public class URBroadcaster implements Observer{
    
    private Host me;
    private BestEffortBroadcaster beb;
    private double canDeliverThresh;
    private Observer observer;

    // mapping from message abstraction (sender id, message id) back to message
    //private Map<Pair<Integer, Integer>, Message> mAbs = new HashMap<Pair<Integer, Integer>, Message>();
    
    // keeps list of original messsages (sender id, message id)
    private Set<Pair<Integer, Integer>> delivered = new HashSet<Pair<Integer, Integer>>();
    // keeps messages to be delivered eventually, same format as delivered
    private Set<Pair<Integer, Integer>> pending = new HashSet<Pair<Integer, Integer>>();
    // map from original message to relayers (sender id, message id) {realyer ids}
    private Map<Pair<Integer, Integer>, Set<Integer>> ack = new HashMap<Pair<Integer, Integer>, Set<Integer>>();

    // TODO rename smPair
    
    public URBroadcaster(Observer obs){
        
        me = new Host();

        // TODO remove?
        for (Host host: Main.parser.hosts()) {

            if(host.getId() == Main.parser.myId()){
                me = host;
            }
        }

        canDeliverThresh = Main.parser.hosts().size() / 2.0;

        beb = new BestEffortBroadcaster(this);

        observer = obs;
    }
     // TODO remove
    public URBroadcaster(){
        
        me = new Host();

        // TODO remove?
        for (Host host: Main.parser.hosts()) {

            if(host.getId() == Main.parser.myId()){
                me = host;
            }
        }

        canDeliverThresh = (int) Math.ceil(Main.parser.hosts().size() / 2.0);

        beb = new BestEffortBroadcaster(this);

    }

    public void broadcast(String m){
        beb.broadcast(m);
    }


    public void receive(Message m){
        
        Pair<Integer, Integer> smPair = smPair(m);

        if(delivered.contains(smPair)){ return;}
        else {

            ack.computeIfAbsent(smPair, empty -> new HashSet<>()).add(m.sender().getId());
            if( pending.contains(smPair) ){
                // add this sender to acked for this message
                // set prevents duplicates

                if(canDeliver(smPair)){

                    // update data structures
                    delivered.add(smPair);
                    pending.remove(smPair);

                    // receive
                    observer.receive(m);
                    //Main.writeOutput(m.content());
                    
                }

                // else nothing to do
            }

            else{ // unseen message

                // update data structures
                pending.add(smPair);

                // relay
                relay(m);
            }
        }
        
    }

    public Host me(){ return me;}


    private void relay(Message m){

        //String relay_content = "relay " + m.content();

        // use the same id to be able to recognize it
        beb.broadcast(m.content(), m.originalSender(), m.id());
    }

    private boolean isRelay(Message m){
        //return m.sender() != m.originalSender();
        return m.content().substring(0, 5).equals("relay");
    }

    private Pair<Integer, Integer> smPair(Message m){
        // credits to multiple stack overflow pages

        /*
        String[] content = m.content().split("\\s+");

        Integer sender = -1;
        Integer mId = -1; 

        if(isRelay(m)){
            // format is "relay d sender id"
            sender = m.originalSender();
            mId = m.id();
        }
        else{
            //format is "d sender id"
            sender = m.sender();

            
            sender = Integer.parseInt(content[1]);
            mId = Integer.parseInt(content[2]);
            
        }
        */

        // if the message is not a relay, then sender == originalSender
        Integer sender = m.originalSender().getId();
        Integer mId = m.id();

        return new Pair<Integer, Integer>(sender, mId);
    }

    private Message messageFromRelay(Pair<Integer, Integer> smPair, Message relayed){
        // format is "relayed d sender id"
        String originalContent = relayed.content().substring(6);
        Host originalSender = Main.hostFromId(smPair._1);

        return new Message(originalContent, originalSender, me, relayed.id());
    }

    private boolean canDeliver(Pair<Integer, Integer> smPair){
        return ack.get(smPair).size() > canDeliverThresh;
    }

}
