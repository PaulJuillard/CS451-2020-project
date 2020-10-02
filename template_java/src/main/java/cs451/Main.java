package cs451;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Main {

    private static void handleSignal() {
        //immediately stop network packet processing
        System.out.println("Immediately stopping network packet processing.");

        //write/flush output file if necessary
        System.out.println("Writing output.");
    }

    private static void initSignalHandlers() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                handleSignal();
            }
        });
    }

    public static void main(String[] args) {
        Parser parser = new Parser(args);
        parser.parse();

        initSignalHandlers();

        // example
        long pid = ProcessHandle.current().pid();
        System.out.println("My PID is " + pid + ".");
        System.out.println("Use 'kill -SIGINT " + pid + " ' or 'kill -SIGTERM " + pid + " ' to stop processing packets.");

        System.out.println("My id is " + parser.myId() + ".");
        System.out.println("List of hosts is:");

        int myPort;

        for (Host host: parser.hosts()) {
            System.out.println(host.getId() + ", " + host.getIp() + ", " + host.getPort());
            if(host.getId() == parser.myId()){
                myPort = host.getPort();
            }
        }

        System.out.println("Barrier: " + parser.barrierIp() + ":" + parser.barrierPort());
        System.out.println("Output: " + parser.output());
        // if config is defined; always check before parser.config()
        if (parser.hasConfig()) {
            System.out.println("Config: " + parser.config());
        }

        BarrierParser.Barrier.waitOnBarrier();

        // send 

        DatagramSocket socket = new DatagramSocket(myPort);

        String message = "Hi i'm host number" + pid;

        for(Host host : parser.hosts()){
            byte[] s_buf = new byte[256];

            InetAddress address = host.getIp();
            s_buf = message.getBytes();
            DatagramPacket packet = new DatagramPacket(s_buf, s_buf.length, address, host.getPort());
            socket.send(packet);
        }
        
        byte[] r_buf = new byte[256];
        DatagramPacket r_p = new DatagramPacket(r_buf, r_buf.length);

        socket.receive(r_p);
        String received = new String(r_buf, StandardCharsets.UTF_8);

        System.out.println(received);


    }
}
