package cs451;

import cs451.Parsers.*;
import cs451.Broadcasters.*;
import java.io.FileWriter;
import java.lang.StringBuilder;

public class Main {

    public static Parser parser;
    private static StringBuilder output = new StringBuilder();

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
        }
        
        Coordinator coordinator = new Coordinator(parser.myId(), parser.barrierIp(), parser.barrierPort(), parser.signalIp(), parser.signalPort());
        
        coordinator.waitOnBarrier();
        
        Broadcaster broadcaster = new FifoBroadcaster();

        for(int m = 1; m <= nMessages; m++){
            broadcaster.broadcast("d " + broadcaster.me().getId() + " " + m);
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
