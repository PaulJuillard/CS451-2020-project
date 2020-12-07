package cs451.Parsers;

import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.Integer;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;

public class ConfigParser {

    private String path;
    private int nMessages;
    private Map<Integer, Set<Integer>> dependencies = new HashMap<>();

    public boolean populate(String path) {
        try{
        this.path=path;
        BufferedReader r = new BufferedReader(new FileReader(path));
        nMessages = Integer.parseInt(r.readLine());
        
        String line = null;
        while( (line = r.readLine()) != null ){
            String[] l = line.split("\\s+");
            List<Integer> dep = new ArrayList<Integer>();
            Arrays.asList(l).forEach(s -> dep.add(Integer.parseInt(s)));
            dependencies.put(dep.get(0), new HashSet<Integer>(dep.subList(1, dep.size())));
        }

        r.close();
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

    public Map<Integer, Set<Integer>> getDependencies(){
        return dependencies;
    }

}
