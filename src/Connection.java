import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Connection {
    private static final int rtt = 100;
    private static final int tries = 3;

    private int window;
    private String filename;

    AtomicInteger ack_number = new AtomicInteger();

    PDU[] ficheiro = null;


    private boolean active = false;
    private int port;
    private InetAddress address;
    private DatagramSocket socket;


    AtomicBoolean transfer_complete = new AtomicBoolean();

    public int getPort() {
        return port;
    }

    public PDU[] getFicheiro(){
        return this.ficheiro;
    }

    public int addPDU(PDU p){
        if(ficheiro[p.getSeq()] == null) {ficheiro[p.getSeq()] = p; return 1;}
        else{ return 0;}
    }

    public boolean ficheiroCompleto(){
        boolean complete = true;
        if (!transfer_complete.get()) {
            for (int i = 0; i < ficheiro.length && complete; i++) {
                if (ficheiro[i] == null) complete = false;
            }
        }
        return complete;
    }

    public int getFileAck(){
        int res = 0;
        for(int i = 0; i < ficheiro.length; i++){
            if(ficheiro[i] == null) break;
            else res = ficheiro[i].getNumber();
        }
        //System.out.println("ACK ENVIADO >>>>>>>>>>>>>>  "+ res + " e a transferencia esta  concluida: " + transfer_complete.get());
        return res;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setFicheiroSize(int i){
        this.ficheiro = new PDU[i];
    }

    public void setPort(int port) {
        this.port = port;
    }

    public InetAddress getAddress() {
        return address;
    }

    public void setAddress(InetAddress address) {
        this.address = address;
    }

    public DatagramSocket getSocket() {
        return socket;
    }

    public void setSocket(DatagramSocket socket) {
        this.socket = socket;
    }

    public void setWindow(int window) {
        this.window = window;
    }

    public synchronized void setAck_number(int n) {this.ack_number.set(n);}

    public void setFilename(String s) {this.filename = s;}

    public String getFilename() {return this.filename;}

    public synchronized int getAck_number () {
        return this.ack_number.intValue();
    }

    public int getWindow () {
        return this.window;
    }

    public void setTransfer_complete(boolean v){
        this.transfer_complete.set(v);
    }

    public synchronized boolean getTransfer_complete(){
        return this.transfer_complete.get();
    }

    public synchronized void setFicheiroNull(){
        this.ficheiro = null;
    }

    public Connection(int port, InetAddress address, DatagramSocket s){
        this.port = port;
        this.address = address;
        this.socket = s;
        this.ack_number.set(0);
        this.window = 10;
        this.transfer_complete.set(false);
    }

    public int checksum (byte[] buf) {
        int crc = 0xFFFF;
        for (int pos = 0; pos < buf.length; pos++) {
            crc ^= (int)buf[pos] & 0xFF;
            for (int i = 8; i != 0; i--) {
                if ((crc & 0x0001) != 0) {
                    crc >>= 1;
                    crc ^= 0xA001;
                }
                else crc >>= 1;
            }
        }
        return crc;
    }

    public boolean checkPDU(PDU p){
        return p.getCheck() == checksum(p.getData());
    }

    public void sendPacket(PDU p){
        p.setNumber(this.ack_number.intValue()+1);
        DatagramPacket d = new DatagramPacket(p.PDU_to_data(), p.getSize(), address, port);
        for(int i = 0; i < tries; i++){
            try {
                socket.send(d);
                Thread.currentThread().sleep(rtt);
                if(p.getNumber() <= this.ack_number.intValue()){
                    return;
                }
            } catch (Exception e) {
               System.out.println("Error Sending!!");
            }

        }

    }

    public void sendPacketList(ArrayList<PDU> list) throws Exception{
        int inicio = this.ack_number.intValue();
        int recebidos = this.ack_number.intValue() - inicio;
        DatagramPacket sendPacket;
        while(recebidos < list.size()) {

            for (int i = 0; i < this.window + recebidos && i < list.size()-recebidos ; i++) {
                PDU p = list.get(i+recebidos);
                System.out.println("A enviar pacote nº "+ p.getNumber() +" pacotes recbidos "+ recebidos);
                sendPacket = new DatagramPacket(p.PDU_to_data(), p.getSize(), address, port);
                socket.send(sendPacket);
            }

            Thread.currentThread().sleep(this.rtt);
            recebidos = this.ack_number.intValue() - inicio;

        }
        System.out.println("Ficheiro Enviado com sucesso!\n");

    }

    public  ArrayList<PDU> File_to_PDUArray (FileInputStream file) throws IOException {
        ArrayList<PDU> array = new ArrayList<>();
        int numSeq = 0;
        int sourceSize = (int)file.getChannel().size();
        int partSize = 1400;
        int numSplits = (int) Math.ceil((double)sourceSize / partSize);
        System.out.println("NUUUMMERO DE SPLITS>>>>>>>>>>>>>>>>>>>>>>>>"+numSplits);
        int buffSize = partSize;
        if (sourceSize < partSize) {
            PDU pdu = new PDU();
            byte[] b = new byte[sourceSize];
            file.read(b, 0, sourceSize);
            pdu.setSeq(numSeq);
            pdu.setType(5);
            pdu.setNumber(this.ack_number.intValue() + numSeq);
            pdu.setTotal(numSplits);
            pdu.setWindow(this.getWindow());
            pdu.setData(b);
            pdu.setCheck(checksum(pdu.getData()));
            array.add(pdu);
        } else {
            while (numSeq < numSplits) {
                PDU pdu = new PDU();
                if ((numSeq + 1) == numSplits) buffSize = sourceSize - (numSeq * partSize);
                byte[] b = new byte[buffSize];
                file.read(b, 0, buffSize);
                pdu.setSeq(numSeq);
                pdu.setType(5);
                pdu.setNumber(this.ack_number.intValue() + numSeq);
                pdu.setTotal(numSplits);
                pdu.setWindow(this.getWindow());
                pdu.setData(b);
                pdu.setCheck(checksum(pdu.getData()));
                array.add(pdu);
                numSeq++;
            }
        }
        return array;
    }

    public  void array_to_File (ArrayList<PDU> array)  {
        String filename = this.filename;
        try {
            OutputStream os = new FileOutputStream(filename);
            for (PDU pdu : array) {
                os.write(pdu.getData());
            }
            os.close();
        } catch (Exception e) {
            System.out.println("Exception: " + e);
        }
    }

    public void sendAck(int number){
        PDU p = new PDU(2,0,number+1,300,0,0);
        DatagramPacket ack = new DatagramPacket(p.PDU_to_data(),p.getSize(),address,port);
        try {
            socket.send(ack);
        } catch (IOException e) {
            System.out.println("Erro ao enviar!");
        }
    }


}
