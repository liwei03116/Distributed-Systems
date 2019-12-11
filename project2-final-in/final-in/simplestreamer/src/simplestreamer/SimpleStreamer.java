/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simplestreamer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.Map;
import org.json.simple.JSONValue;

/**
 *
 * @author weili
 */
public class SimpleStreamer {

    /**
     * @param args the command line arguments
     */
    static Thread server;
    static Thread client;
    
   public static void main(String[] args) {
        // TODO code application logic here
        int port = 6262;//default server port
        String[]remote = new String[0];
        int[] rport= null;
        int width = 420;//default width
        int height = 240;//default height
        int rate = 100;//default rate
        String ip = "";
        
        if(args.length== 2){
            Server s =  new Server(port);
            
            
            server = new Thread(s, "Server");
            server.start();
            
            try {
                    
                    server.join();     
            } catch (InterruptedException e) {
                    e.printStackTrace();
            }
        }
        else{
            for (int i = 0; i < args.length; i++) {
                String arg = args[i].toLowerCase();
                //if port is specified from command line
                if(arg.startsWith("-sport")){//port is specified
                    port = Integer.parseInt(args[i + 1]);
                }else if(arg.startsWith("-remote")){//remote is specified
                    remote = new String[args[i + 1].split(",").length];
                    if(args[i + 1].length() == 1){
                        remote[0] = args[i + 1];
                    }
                    else{
                        remote = args[i + 1].split(",");
                    }
                } else if (arg.startsWith("-rport")) { //rport is specified
                    
                    rport = new int[args[i + 1].split(",").length];
                    if(rport.length == 1){
                        rport[0] = Integer.parseInt(args[i + 1]);
                    }
                    else{
                        String[] rportstring = args[i + 1].split(",");
                        for(int j=0;j<rport.length;j++){
                            //System.out.println(rport.length);
                            rport[j] = Integer.parseInt(rportstring[i]);
                        }
                    }
                } else if (arg.startsWith("-width")) { //if width is specified
                    width = Integer.parseInt(args[i + 1]);
                } else if (arg.startsWith("-height")) { //if height is specified
                    height = Integer.parseInt(args[i + 1]);
                } else if (arg.startsWith("-rate")){//if rate is specified
                    rate = Integer.parseInt(args[i + 1]);
                }
            }
            
            
            try {
                Server s =  new Server(port);
                server = new Thread(s, "Server");
                server.start();
                for(int j=0;j<remote.length;j++){
                    Client c = new Client(remote[j], rport[j], width, height, rate);
                    client = new Thread(c, "Client");
                    client.start();
                    
                    client.join();
                }
                    
                    server.join();     
            } catch (InterruptedException e) {
                    e.printStackTrace();
            }

        }
    }
}
