/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simplestreamer;

import com.github.sarxos.webcam.ds.buildin.natives.Device;
import com.github.sarxos.webcam.ds.buildin.natives.DeviceList;
import com.github.sarxos.webcam.ds.buildin.natives.OpenIMAJGrabber;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.JFrame;
import org.apache.commons.codec.binary.Base64;
import org.bridj.Pointer;
import org.json.simple.JSONValue;
import simplestream.Compressor;

/**
 *
 * @author weili
 */
public class Connection extends Thread {
	Socket socket;
	BufferedReader in;
	PrintWriter out;

	public Connection(Socket socket) {
		this.socket = socket;
		try {
			in = new BufferedReader(new InputStreamReader(
					this.socket.getInputStream()));
			out = new PrintWriter(this.socket.getOutputStream(), true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		String msg;
		try {   
                        boolean checkfirst = true;
                        msg = in.readLine();
                        //get width and height from sender
                        
                        System.out.println(msg);
                        String [] st11 =msg.split(":");
                        String tempst11 = st11[2];
                        String[] sttemp1 =tempst11.split("}");
                        String tempst111 = sttemp1[0];
                        String tempst121 = tempst111.replace("\"", "");
                        
                        int getw = 320;
                        int geth = 240;
                        if(tempst121.compareTo("raw,width") == 0){
                               //System.out.println(msg);
                                 String[] st = msg.split(",");
                                 String sttemp121 =st[2];
                                 String tempst1113 = st[3];
                                 String[] tempst11121w = sttemp121.split(":");
                                 String[] tempst11131h = tempst1113.split(":");
                                 String tempst111311 = tempst11131h[1].replace("}", "");
                                 getw = Integer.parseInt(tempst11121w[1]);
                                 geth = Integer.parseInt(tempst111311);
                               //out.println("OK");
                        }
                        
                        Viewer myViewer = new Viewer(getw, geth);
                        JFrame frame = new JFrame("Server Stream Viewer");
                        frame.setVisible(true);
                        frame.setSize(getw, geth);
                        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                        frame.add(myViewer);
                        
                        OpenIMAJGrabber grabber = new OpenIMAJGrabber();
                        
                        Device device = null;
                        Pointer<DeviceList> devices = grabber.getVideoDevices();

                        for (Device d : devices.get().asArrayList()) {
                            device = d;
                            break;
                        }

                        boolean started = grabber.startSession(getw, geth, 30, Pointer.pointerTo(device));
                        /*
                        if (!started) {
                            throw new RuntimeException("Not able to start native grabber!");
                        }
                        * 
                        */
                        
			while ((msg = in.readLine())!=null) {
				//msg = in.readLine();
				System.out.println(msg);
                                String [] st =msg.split(":");
                                if(msg.length() != 21){
                                    String tempst = st[2];
                                    String[] sttemp =tempst.split("}");
                                    String tempst1 = sttemp[0];
                                    String tempst12 = tempst1.replace("\"", "");
                                    
                                        byte[] buf = tempst12.getBytes();

                                        /**
                                        * This example show how to use native OpenIMAJ API to capture raw bytes
                                        * data as byte[] array. It also calculates current FPS.
                                        */

                                            /*
                                            * The image data can be sent to connected clients.
                                            */

                                            /*
                                            * Assume we received some image data.
                                            * Remove the text friendly encoding.
                                            */
                                            byte[] nobase64_image = Base64.decodeBase64(buf);
                                            /* Decompress the image */
                                            byte[] decompressed_image = Compressor.decompress(nobase64_image);
                                            /* Give the raw image bytes to the viewer. */
                                            myViewer.ViewerInput(decompressed_image, getw, geth);
                                            frame.repaint();

                                            //grabber.stopSession();
                                            /* Get a frame from the webcam. */
                                            grabber.nextFrame();
                                            /* Get the raw bytes of the frame. */
                                            byte[] raw_image=grabber.getImage().getBytes(getw * geth * 3);
                                            /* Apply a crude kind of image compression. */
                                            byte[] compressed_image = Compressor.compress(raw_image);
                                            /* Prepare the date to be sent in a text friendly format. */
                                            byte[] base64_image = Base64.encodeBase64(compressed_image);
                                            Map obj=new LinkedHashMap();
                                            obj.put("type", "image");
                                            obj.put("data", new String(base64_image));
                                            String stream = JSONValue.toJSONString(obj);

                                            System.err.println("Sending: "+ stream);
                                            out.println(stream);
                                      
                                }
                                else{
                                    String tempst13 = st[1].replace("\"", "");
                                    if(tempst13.compareTo("stopstream") == 1){
                                        out.println("stopstream");
                                    }
                                }
                                
                                
			}
                        frame.setVisible(false);
                        frame.dispose();
                        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                        //System.exit(0);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
