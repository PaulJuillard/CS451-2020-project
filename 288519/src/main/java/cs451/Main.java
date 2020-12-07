package cs451;

import cs451.Messages.Message;
import cs451.Parsers.*;
import cs451.Broadcasters.*;
import java.io.FileWriter;
import java.lang.StringBuilder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Main {

    public static Parser parser;
    private static StringBuilder output = new StringBuilder();

    public static List<Host> hosts;
    public static Integer me;
    public static Map<Integer, Host> hostByID = new HashMap<>();
    public static Map<Integer, Set<Integer>> dependencies = new HashMap<>();


    private static void handleSignal() {
        System.out.println("Immediately stopping network packet processing.");
        try {

            FileWriter myWriter = new FileWriter(parser.output(), true);
            myWriter.write(output.toString());
            myWriter.close();
          } catch (Exception e) {
            System.out.println("failed wrote to the file.");
            e.printStackTrace();
          }
    }

    private static void initSignalHandlers() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                handleSignal();
            }
        });
    }

    public static void main(String[] args) throws InterruptedException {

        parser = new Parser(args);
        parser.parse();

        initSignalHandlers();

        // if config is defined; always check before parser.config()
        int nMessages = 1;
        if (parser.hasConfig()) {
            nMessages = parser.config();
            dependencies = parser.dependencies();
            System.out.println(dependencies);
        }

        hosts = parser.hosts();
        hosts.forEach( h -> hostByID.put(h.getId(), h));
        me = parser.myId();
        
        Coordinator coordinator = new Coordinator(parser.myId(), parser.barrierIp(), parser.barrierPort(), parser.signalIp(), parser.signalPort());
        
        coordinator.waitOnBarrier();
        
        Broadcaster broadcaster = new LocalCausalBroadcaster();
        
        for(int m = 1; m <= nMessages; m++){
            broadcaster.broadcast( new Message("d " + me + " " + m, me));
            writeOutput("b " + m);
        }
        
        coordinator.finishedBroadcasting();
        
        while (true) {
            // Sleep for 1 hour
            Thread.sleep(60 * 60 * 1000);
        }  
         
    }

    synchronized public static void writeOutput(String content){
        output.append(content + "\n");
    }

    public static Host hostFromId(int id){
        for( Host h : parser.hosts()){
            if(h.getId() == id) return h;
        }
        return new Host();
    }
}
