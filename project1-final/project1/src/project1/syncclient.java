/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package project1;

import filesync.*;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import java.util.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author weili
 */
public class syncclient {
    
    public static void main (String [] args){
        
        String filename = "";
        String hostname = "";
        int serverPort = 4144;//default server port
        int blockSize = 0;
        String direction = "";
        
        for (int i = 0; i < args.length; i++) {
            String arg = args[i].toLowerCase();
            //if port is specified from command line
            if(arg.startsWith("-file")){
                filename = args[i + 1];
            }else if(arg.startsWith("-host")){
                hostname = args[i + 1];
            } else if (arg.startsWith("-p")) { 
                serverPort = Integer.parseInt(args[i + 1]);
            } else if (arg.startsWith("-b")) { //if blocksize is specified
                blockSize = Integer.parseInt(args[i + 1]);
            } else if (arg.startsWith("-d")) { //if receiving
                direction = args[i + 1];
            }
        }
 
        
        Map objadd1=new LinkedHashMap();
        objadd1.put("type", "negotiation");
        objadd1.put("blocksize", blockSize);
        objadd1.put("direction", direction);
        
        String neg = JSONValue.toJSONString(objadd1);

        
        System.err.println("Sending: "+ neg);
        int port = serverPort + 1;
        
        try{
            DatagramSocket socket = null;
            socket = new DatagramSocket();
            InetAddress host = InetAddress.getByName(hostname);
            port = serverPort + 1;

            byte[] bufneg1 = neg.getBytes();
            DatagramPacket requestneg1 = new DatagramPacket(bufneg1, bufneg1.length, host, port);
            socket.send(requestneg1);
   
            
        }catch (SocketException e) {
                e.printStackTrace();
	} catch (IOException e) {
		e.printStackTrace();
	} 
        
        //check negotiation of direction is "push"
        if(direction.compareTo("push") == 0){
                try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        System.exit(-1);
                    }
                 Client cl = new Client();
                 cl.Client(filename, hostname, serverPort);
     
         }//

         //check negotiation of direction is "pull"
         if(direction.compareTo("pull") == 0){
                System.out.println("client is running...");
                syncserver ser = new syncserver();
                ser.sethostname(hostname);
                Server se = new Server();
                se.Server(filename, serverPort);
         }
        
       
        
    }//
}
