package cs451;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.FileWriter;
import java.lang.StringBuilder;

import java.util.Optional;

public class Main {

    public static Parser parser;
    private static StringBuilder output = new StringBuilder();

    private static void handleSignal() {
        //immediately stop network packet processing
        System.out.println("Immediately stopping network packet processing.");

        try {
            FileWriter myWriter = new FileWriter(parser.output(), true);
            myWriter.write(output.toString());
            myWriter.close();
            //System.out.println("Successfully wrote to the file.");
          } catch (IOException e) {
            System.out.println("An error occurred writing to output file");
            e.printStackTrace();
          }
        //write/flush output file if necessary
        //System.out.println("Writing output.");
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

        long pid = ProcessHandle.current().pid();

        /*
        System.out.println("My id is " + Main.parser.myId() + ".");
        System.out.println("Barrier: " + parser.barrierIp() + ":" + parser.barrierPort());
        System.out.println("Signal: " + parser.signalIp() + ":" + parser.signalPort());
        System.out.println("Output: " + parser.output());
        */
        // if config is defined; always check before parser.config()
        int nMessages = 1;
        if (parser.hasConfig()) {
            // System.out.println("Config: " + parser.config());
            nMessages = parser.config();
        }
        
        Coordinator coordinator = new Coordinator(parser.myId(), parser.barrierIp(), parser.barrierPort(), parser.signalIp(), parser.signalPort());
        
        //System.out.println("Waiting for all processes for finish initialization");
        coordinator.waitOnBarrier();
        
        //System.out.println("Broadcasting messages...");
        
        ReliableBroadcaster broadcaster = new ReliableBroadcaster();
        Thread process = new Thread(broadcaster);
        process.start();

        for(int m = 0; m < nMessages; m++){
            broadcaster.broadcast("d " + broadcaster.me().getId() + " " + m);
            writeOutput("b " + m);
        }
        // broadcaster.broadcast("Hey, i'm process number " + broadcaster.me().getId());
        // broadcaster.broadcast(broadcaster.me().getId() + ":Do you like foo?" + broadcaster.me().getId());
        
        //System.out.println("Signaling end of broadcasting messages");
        coordinator.finishedBroadcasting();
        
        while (true) {
            // Sleep for 1 hour
            Thread.sleep(60 * 60 * 1000);
        }  
         
    }

    synchronized public static void writeOutput(String content){
        //System.out.println("writing { " + content + " } to " + parser.output());
        output.append(content + "\n");
    }

    public static Host hostFromId(int id){
        for( Host h : parser.hosts()){
            if(h.getId() == id) return h;
        }
        return new Host();
    }
}
