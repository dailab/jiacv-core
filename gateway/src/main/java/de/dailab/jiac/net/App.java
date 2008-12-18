package de.dailab.jiac.net;

import java.net.ServerSocket;

/**
 * Hello world!
 *
 */
public class App {
    public static void main(String[] args) throws Exception {
//        InetAddress.get
        
        ServerSocket blaSocket= new ServerSocket(0, 5000);
        
        System.out.println(blaSocket.getLocalSocketAddress());
        blaSocket.close();
//        
//        InetAddress[] addresses= InetAddress.getAllByName("10.0.4.76");
//        
//        for(int i= 0; i < addresses.length; ++i) {
//            System.out.println(addresses[i].getHostAddress() + " :: " + addresses[i].getHostName());
//        }
//        
//        for(Enumeration<NetworkInterface> interfaces= NetworkInterface.getNetworkInterfaces(); interfaces.hasMoreElements();) {
//            NetworkInterface intf= interfaces.nextElement();
//            for(Enumeration<InetAddress> e= intf.getInetAddresses(); e.hasMoreElements(); ) {
//                InetAddress a= e.nextElement();
//                System.out.println(a.getHostAddress());
//            }
//        }
    }
}
