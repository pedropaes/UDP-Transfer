import java.io.*;
import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.List;


public class Responder extends Thread {


    private Connection connection;
    private DatagramPacket packet;

    public Responder(Connection c , DatagramPacket p){
        this.connection = c;
        this.packet = p;
    }




    public void print_list(String list){
        String[] files = list.split(";");
        System.out.println("=========================");
        for(String s : files)
            System.out.println(s+"\n");
        System.out.println("=========================");
    }

    public void sendFileList() {
        List<String> results = new ArrayList<>();
        try {
            File[] files = new File("/home/pedro/files").listFiles();
            for (File file : files) {
                if (file.isFile()) {
                    results.add(file.getName().concat(";"));
                }
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(baos);

            for (String element : results) {
                out.writeChars(element);
            }

            byte[] bytes = baos.toByteArray();
            PDU p = new PDU(6,0,0,0,0,0);
            p.setData(bytes);
            connection.sendPacket(p);

        }
        catch(Exception e){
            System.out.println("Erro...");
        }
    }

    public void sendFile(String filename){
        StringBuilder sb = new StringBuilder();
        sb.append("/home/pedro/files/");
        sb.append(filename);
        File f = new File(sb.toString());
        if(f.isFile() && f.canRead()) {
            try {
                FileInputStream file = new FileInputStream(f);
                    ArrayList<PDU> p = connection.File_to_PDUArray(file);
                    this.connection.sendPacketList(p);
            } catch (Exception e) {
                e.printStackTrace();
            }


        }
        else System.out.println("Ficheiro não existe\n");


    }

    public void download(String filename){
        StringBuilder sb = new StringBuilder();
        sb.append("download ");
        sb.append(filename);
        PDU p = new PDU(4,0,0,0,0,0);
        connection.setFilename(filename);
        p.setData(sb.toString().getBytes());
        connection.sendPacket(p);

    }

    public void processRequest(PDU p){
        String s = new String(p.getData());
        String[] comandos = s.split(" ");
        switch (comandos[0]){
            case "lista" :
                sendFileList();
                break;
            case "download":
                sendFile(comandos[1]);
                break;
            case "upload":
                download(comandos[1]);
                break;
            default:  System.out.println("Request Desconhecido");
                return;
        }
    }
    public void disconnect(){
        PDU p = new PDU(7,0,0,0,0,0);
        this.connection.sendPacket(p);
    }

    public ArrayList<PDU> pdu_ArrayList (PDU[] p) {
        ArrayList<PDU> array = new ArrayList<>();
        for (int i = 0; i < p.length; i++)
            array.add(p[i]);
        return array;
    }

    public void assemble(PDU p){
        if(p.getTotal() == 0){
            connection.setTransfer_complete(false);
            System.out.println("O filename é: " + connection.getFilename());
            connection.sendAck(p.getNumber());
            try (FileOutputStream stream = new FileOutputStream(connection.getFilename())) {
                stream.write(p.getData());
                System.out.println("Ficheiro: " + connection.getFilename() + " recebido com sucesso!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else{
            if(connection.getFicheiro() ==  null ) {connection.setFicheiroSize(p.getTotal()); connection.setTransfer_complete(false);}
            connection.addPDU(p);
            int ackNumber = connection.getFileAck();
            connection.sendAck(ackNumber);
            if (connection.ficheiroCompleto() && !connection.getTransfer_complete()) {
                connection.setTransfer_complete(true);
                PDU[] ficheiro = connection.getFicheiro();
                PDU pacote = ficheiro[ficheiro.length-1];
                connection.sendAck(pacote.getNumber());
                connection.array_to_File(pdu_ArrayList(connection.getFicheiro()));
                System.out.println("Ficheiro: " + connection.getFilename() + " recebido com sucesso!");
                connection.setFicheiroNull();
            }
        }
        //System.out.println("O ack actual da ligação é: "+ connection.getAck_number());


    }

    public synchronized void run(){
        PDU p = new PDU(this.packet.getData(),this.packet.getLength());

        if(connection.getAck_number()<p.getNumber()) connection.setAck_number(p.getNumber());

        System.out.println( p.get_TypeString() + " recebido" + " Number: " + p.getNumber() +" do host " + connection.getAddress().getHostAddress() );
        int type = p.getType();
        switch (type) {
            case 1 :
                      break;
            case 2 :  if(p.getWindow() != this.connection.getWindow()) this.connection.setWindow(p.getWindow());
                      break;
            case 3 :  break;
            case 4 :  processRequest(p);
                      break;
            case 5 :  if(connection.checkPDU(p)) assemble(p);
                      break;
            case 6 :  print_list(new String(p.getData()));
                      this.connection.sendAck(p.getNumber());
                      break;
            case 7 :  this.connection.sendAck(p.getNumber());
                      System.out.println("Cliente no ip: "+ connection.getAddress().getHostAddress() + " desligou a ligação");
                      this.connection.getSocket().disconnect();
                      return;
            default:  System.out.println("Invalid Type");
                      return;
        }
        return;
    }
}
