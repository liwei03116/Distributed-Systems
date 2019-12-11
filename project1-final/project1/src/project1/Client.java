/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package project1;

import filesync.CopyBlockInstruction;
import filesync.Instruction;
import filesync.NewBlockInstruction;
import filesync.SynchronisedFile;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.LinkedHashMap;
import java.util.Map;
import org.json.simple.JSONValue;

/**
 *
 * @author weili
 */
public class Client {
    
    public Client(){
         
    }
    public void Client(String sourcefilename, String hostname, int port){
        
        SynchronisedFile fromFile = null;
        DatagramSocket socket = null;
        Instruction inst;
        //InstructionFactory instFact=new InstructionFactory(); 
        while(true){
            int counter = 0;
            try{
                socket = new DatagramSocket();
                InetAddress host = InetAddress.getByName(hostname);
                //int port = 4144;//default server port
                fromFile = new SynchronisedFile(sourcefilename);
                //To check from file has changed.
                fromFile.CheckFileState();
                boolean statement = true;
                // The Client reads instructions to send to the Server
                while(statement){

                    if((inst = fromFile.NextInstruction())!=null){
                        //To check each time of client read instruction file whether or not changed.
                        /*
                        * 
                        */
                        /*
                        * Pretend the Client sends the msg to the Server.
                        */

                        // network delay

                        String msgs=inst.ToJSON();//.replace("{", "");
                        //msgs = msgs.replace("}", "");
                        //System.out.println(msgs);
                        Map obj=new LinkedHashMap();
                        obj.put("type", "inst");
                        obj.put("inst", msgs);
                        obj.put("counter", ++counter);
                        String msg = JSONValue.toJSONString(obj);
                        //msg = msg.replace("\\", "");
                        String printmsg = msg.replace("\\", "");
                        System.err.println("Sending: "+ printmsg);

                        //msgs = msgs + " " + Integer.toString(counter);

                        //System.out.println(counter);
                        byte[] buf = msg.getBytes();
                        DatagramPacket request = new DatagramPacket(buf, buf.length, host, port);
                        socket.send(request);

                        byte[] rbuf = new byte[2048];
                        DatagramPacket reply = new DatagramPacket(rbuf, rbuf.length);
                        socket.receive(reply);

                        //System.out.println((new String(reply.getData())).trim());
                        String compare = (new String(reply.getData())).trim();
                        //System.out.println(compare.compareTo("OK"));
                        if(compare.compareTo("OK") != 0){
                            if(compare.compareTo("NOblock") == 0){
                                /*
                                * Client upgrades the CopyBlock to a NewBlock instruction and sends it.
                                */

                                Instruction upgraded=new NewBlockInstruction((CopyBlockInstruction)inst);
                                String msg2 = upgraded.ToJSON();
                                msg2 = msg2.trim();
                                Map objblock=new LinkedHashMap();
                                objblock.put("type", "inst");
                                objblock.put("inst", msg2);
                                objblock.put("counter", ++counter);
                                String msg21 = JSONValue.toJSONString(objblock);
                                String printmsg21 = msg21.replace("\\", "");
                                System.err.println("Sending2: "+printmsg21);

                                byte[] buf1 = new byte[2048];
                                buf1 = msg21.getBytes();
                                DatagramPacket request1 = new DatagramPacket(buf1, buf1.length, host, port);
                                socket.send(request1);
                            }


                        }
                        try {
                                Thread.sleep(1000);
                        } catch (InterruptedException e) {
                                e.printStackTrace();
                                System.exit(-1);
                        }
                        String [] checkstate = msgs.split(":");
                        String tempcheckstate = checkstate[1];
                        String [] tempcheckstatelist = tempcheckstate.split("}");
                        String tempcheckstate1 = tempcheckstatelist[0];
                        String tempcheckstatefinal = tempcheckstate1.replace("\"", "");
                        if(tempcheckstatefinal.compareTo("EndUpdate") == 0){
                                    statement = false;
                        }
                        //System.out.println(statement);
                    }

                }
                //System.out.println("dcdcedce");

            }catch (IOException e){
                e.printStackTrace();
                System.exit(-1);
            }catch (InterruptedException e){
                e.printStackTrace();
                System.exit(-1);
            }finally {
                if(socket != null){
                    socket.close();
                }
            }
        }
        
    }
}
