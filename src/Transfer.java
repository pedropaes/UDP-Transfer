import javax.xml.crypto.Data;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Scanner;

public class Transfer {




    public static void printPacket(DatagramPacket dp){
        PDU p = new PDU(dp.getData(),dp.getLength());
        System.out.println(p.toString());
    }

    public static DatagramPacket synPacket(InetAddress a){
        PDU p = new PDU();
        p.setType(1);
        DatagramPacket syn = new DatagramPacket(p.PDU_to_data(),p.getSize(),a,7777);
        return syn;
    }


    public static DatagramPacket synackPacket(InetAddress a, int port){
        PDU p = new PDU();
        String s = Integer.toString(port);
        byte[] array = s.getBytes();
        p.setData(array);
        p.setType(3);
        DatagramPacket syn = new DatagramPacket(p.PDU_to_data(),p.getSize(),a,7777);

        return syn;
    }


    public static void main(String[] args) throws Exception  {
        DatagramSocket ss;
        DatagramPacket receivePacket;
        InetAddress address = InetAddress.getByName(args[0]);
        Scanner sc = new Scanner(System.in);
        int port = 7777;
        int number = 10000;
        System.out.println("À espera de ligação a ...\n" + args[0]);
        byte[] receiveData = new byte[1500];



        ss = new DatagramSocket(port);

        DatagramPacket sendPacket = synPacket(address);
        ss.send(sendPacket);
        while(true){
            receivePacket = new DatagramPacket(receiveData, receiveData.length);
            ss.receive(receivePacket);
            PDU p = new PDU(receivePacket.getData(),receivePacket.getLength());
            if(p.getType() == 1) {
                System.out.println("Deseja aceitar a ligação de : " + receivePacket.getAddress().getHostAddress() + " ?");
                String answer = sc.nextLine();
                if (answer.equals("s")) {
                    port++;
                    sendPacket = synackPacket(receivePacket.getAddress(),port);
                    ss.send(sendPacket);
                    Connection c = new Connection(port, receivePacket.getAddress(), new DatagramSocket(port));
                    Listener t = new Listener(c, receivePacket);
                    t.start();
                }
            }
            if(p.getType() == 3){
                int porta = Integer.parseInt(new String(p.getData()));
                Connection c = new Connection(porta, receivePacket.getAddress(), new DatagramSocket(porta));
                c.setActive(true);
                Listener t = new Listener(c, receivePacket);
                t.start();
            }


        }


    }
}
