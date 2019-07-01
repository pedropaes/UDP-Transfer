import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.atomic.AtomicInteger;

public class Listener extends Thread {

    AtomicInteger number = new AtomicInteger();
    int port;
    int tries = 3;
    int rtt = 300; //round trip time

    private Connection connection;
    DatagramSocket s;
    DatagramPacket packet;
    InetAddress address;


    public void run() {
        System.out.println("Socket criado na porta: "+ port + " ligado ao ip: " +  address.getHostAddress());
        Thread m = new Thread(new Menu(connection));
        m.start();

        while(true){
            byte[] receiveData = new byte[1500];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            try {
                s.receive(receivePacket);
            } catch (IOException e) {
                System.out.println("Ligação encerrada\n");
            }
            Responder r = new Responder(connection, receivePacket);
            r.start();
        }

    }

    public Listener(Connection c, DatagramPacket packet){
        this.connection  = c;
        this.packet = packet;
        this.address = c.getAddress();
        this.port = c.getPort();
        this.s = c.getSocket();
    }


}
