
import java.util.Scanner;


public class Menu implements Runnable {
    private Connection connection;

    public Menu(Connection c){
        this.connection = c;
    }


    public void lista(){
        String s = "lista";
        PDU p = new PDU(4,0,connection.ack_number.intValue(),0,0,0);
        p.setData(s.getBytes());
        connection.sendPacket(p);
    }

    public void disconnect(){
        PDU p = new PDU(7,0,0,0,0,0);
        this.connection.sendPacket(p);
        connection.getSocket().close();
        System.exit(0);
    }

    public void download(String filename){
        StringBuilder sb = new StringBuilder();
        sb.append("download ");
        sb.append(filename);
        PDU p = new PDU(4,0,0,0,0,0);
        p.setData(sb.toString().getBytes());
        connection.sendPacket(p);
    }

    public void upload(String filename){
        StringBuilder sb = new StringBuilder();
        sb.append("upload ");
        sb.append(filename);
        PDU p = new PDU(4,0,0,0,0,0);
        p.setData(sb.toString().getBytes());
        connection.sendPacket(p);
    }

    /*
    public void upload(String filename){
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
    }*/

    public void run() {
        String filename;
        while (true) {
            Scanner sc = new Scanner(System.in);
            System.out.println("******************************");
            System.out.println("*1) pedir lista ficheiros    *");
            System.out.println("*2) receber ficheiro         *");
            System.out.println("*3) enviar ficheiro          *");
            System.out.println("*4) desligar                 *");
            System.out.println("******************************");
            String option = sc.nextLine();
            switch (option) {
                case "1":
                    lista();
                    break;
                case "2":
                    System.out.println("Insira o nome do ficheiro:");
                    filename = sc.nextLine();
                    this.connection.setFilename(filename);
                    download(filename);
                    break;
                case "3":
                    System.out.println("Insira o nome do ficheiro:");
                    filename = sc.nextLine();
                    upload(filename);
                    break;
                case "4":
                    disconnect();
                    break;
                default:
                    System.out.println("Invalid Option");
                    break;
            }
        }
    }
}
