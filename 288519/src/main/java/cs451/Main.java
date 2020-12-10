package cs451;

import cs451.Messages.Message;
import cs451.Parsers.*;
import cs451.Broadcasters.*;
import java.io.FileWriter;
import java.lang.StringBuilder;
import java.util.*;

public class Main {

    public static Parser parser;

    private static StringBuilder output = new StringBuilder();

    public static int nMessages;

    public static List<Host> hosts;
    public static Map<Integer, Host> hostByID = new HashMap<>();

    public static Map<Integer, Set<Integer>> dependencies = new HashMap<>();

    public static Integer me;
    public static Host hme;

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

        // SETUP

        parser = new Parser(args);
        parser.parse();

        // if config is defined; always check before parser.config()
        nMessages = 1;
        if (parser.hasConfig()) {
            nMessages = parser.nMessages();
            dependencies = parser.dependencies();
            System.out.println(nMessages);
            System.out.println(dependencies);
        }

        initSignalHandlers();

        hosts = parser.hosts();
        hosts.forEach( h -> hostByID.put(h.getId(), h));

        me = parser.myId();
        hme = hostByID.get(me);

        // START
        Coordinator coordinator = new Coordinator(parser.myId(), parser.barrierIp(), parser.barrierPort(), parser.signalIp(), parser.signalPort());
        coordinator.waitOnBarrier();
        
        Broadcaster broadcaster = new LocalCausalBroadcaster();

        // BROADCAST
        for(int m = 1; m <= nMessages; m++){
            broadcaster.broadcast( new Message("d " + me + " " + m, me, m));
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


    /*
        TEST CONFIGURATION FOR PROFILING
        me = parser.myId();


        List<Thread> thds = new ArrayList<>();
        for(Host host : hosts){
            me = host.getId();
            Thread T = new Thread(new Runnable() {
                Integer me_ = host.getId();
                @Override
                public void run(){
                    Broadcaster b = new LocalCausalBroadcaster(hostFromId(me_));
                    Coordinator coordinator = new Coordinator(me_, parser.barrierIp(), parser.barrierPort(), parser.signalIp(), parser.signalPort());
                    coordinator.waitOnBarrier();
                    for(int m = 1; m <= nMessages; m++){
                        b.broadcast(new Message("d " + me_ + " " + m, me_, m));
                        writeOutput("b " + m);
                    }
                    coordinator.finishedBroadcasting();

                    try {
                        Thread.sleep(60 * 60 * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
            T.start();
            thds.add(T);
        }

        while (true) {
            // Sleep for 1 hour
            Thread.sleep(60 * 60 * 1000);
        }
    */

}
