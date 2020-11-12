package cs451;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.io.Serializable;
import java.util.Objects;

public class Host implements Serializable{

    private static final String IP_START_REGEX = "/";

    private int id;
    private String ip;
    private int port = -1;

    public Host(){} // Dummy intialization

    public boolean populate(String idString, String ipString, String portString) {
        try {
            id = Integer.parseInt(idString);

            String ipTest = InetAddress.getByName(ipString).toString();
            if (ipTest.startsWith(IP_START_REGEX)) {
                ip = ipTest.substring(1);
            } else {
                ip = InetAddress.getByName(ipTest.split(IP_START_REGEX)[0]).getHostAddress();
            }

            port = Integer.parseInt(portString);
            if (port <= 0) {
                System.err.println("Port in the hosts file must be a positive number!");
                return false;
            }
        } catch (NumberFormatException e) {
            if (port == -1) {
                System.err.println("Id in the hosts file must be a number!");
            } else {
                System.err.println("Port in the hosts file must be a number!");
            }
            return false;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        return true;
    }

    public int getId() {
        return id;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    @Override
    public boolean equals(Object o){
        if(o == null || o.getClass() != this.getClass()){
            return false;
       }
       else{
           Host h2 = (Host) o;
       return (
           this.id == h2.getId() &&
           this.ip == h2.getIp() &&
           this.port == h2.getPort()
           );
       }
    }

    @Override
    public int hashCode(){
        return Objects.hash(id, ip, port);
    }
}
