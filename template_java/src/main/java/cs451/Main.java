package cs451;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.FileWriter;

import java.util.Optional;

public class Main {

    public static Parser parser;

    private static void handleSignal() {
        //immediately stop network packet processing
        System.out.println("Immediately stopping network packet processing.");

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
        Parser parser = new Parser(args);
        parser.parse();

        initSignalHandlers();

        long pid = ProcessHandle.current().pid();

        //System.out.println("My PID is " + pid + ".");
        //System.out.println("Use 'kill -SIGINT " + pid + " ' or 'kill -SIGTERM " + pid + " ' to stop processing packets.");
        System.out.println("My id is " + Main.parser.myId() + ".");
        //System.out.println("Output: " + Main.parser.output());
        
        ReliableBroadcaster broadcaster = new ReliableBroadcaster();

        System.out.println("Barrier: " + parser.barrierIp() + ":" + parser.barrierPort());
        System.out.println("Signal: " + parser.signalIp() + ":" + parser.signalPort());
        System.out.println("Output: " + parser.output());
        // if config is defined; always check before parser.config()
        if (parser.hasConfig()) {
            System.out.println("Config: " + parser.config());
        }

        BarrierParser.Barrier.waitOnBarrier();        
        
        Thread process = new Thread(broadcaster);
        process.start();

        broadcaster.broadcast("Hey, i'm process number " + broadcaster.me().getId());
        //broadcaster.broadcast(broadcaster.me().getId() + ":Do you like foo?" + broadcaster.me().getId());

    }

    public static void writeOutput(String m){
        try {
            FileWriter myWriter = new FileWriter(parser.output(), true);
            myWriter.write(m + "\n");
            myWriter.close();
            //System.out.println("Successfully wrote to the file.");
          } catch (IOException e) {
            System.out.println("An error occurred writing to output file");
            e.printStackTrace();
          }
    }

    public static Host hostFromId(int id){
        for( Host h : parser.hosts()){
            if(h.getId() == id) return h;
        }
        return new Host();

        Coordinator coordinator = new Coordinator(parser.myId(), parser.barrierIp(), parser.barrierPort(), parser.signalIp(), parser.signalPort());

	System.out.println("Waiting for all processes for finish initialization");
        coordinator.waitOnBarrier();

	System.out.println("Broadcasting messages...");

	System.out.println("Signaling end of broadcasting messages");
        coordinator.finishedBroadcasting();

	while (true) {
	    // Sleep for 1 hour
	    Thread.sleep(60 * 60 * 1000);
	}
    }
}
