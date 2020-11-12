package cs451.Parsers;

import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.Integer;

public class ConfigParser {

    private String path;
    private int nMessages;

    public boolean populate(String path) {
        try{
        this.path=path;
        BufferedReader r = new BufferedReader(new FileReader(path));
        nMessages = Integer.parseInt(r.readLine());
        } catch (IOException e) {
            System.err.println("Problem in config file");
        }
        return true;
    }

    public String getPath() {
        return path;
    }

    public int getN(){
        return nMessages;
    }

}
