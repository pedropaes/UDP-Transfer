import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class PDU {

    private int packet_type; // 1 -> SYN 2 -> ACK 3 -> SYN/ACK 4 -> REQ 5 -> DATA 6 -> LIST 7 -> FIN
    private int packet_seq;
    private int packet_number;
    private int packet_window;
    private int total_segments;
    private int checksum;
    private byte[] data;

    public PDU () {
        this.packet_type = 0;
        this.packet_seq = 0;
        this.packet_number = 0;
        this.packet_window = 1;
        this.total_segments = 0;
        this.data = new byte[0];
    }

    public PDU (int type, int seq, int number, int window, int total, int c) {
        this.packet_type = type;
        this.packet_seq = seq;
        this.packet_number = number;
        this.packet_window = window;
        this.total_segments = total;
        this.checksum = c;
        this.data = new byte[0];
    }

    public String get_TypeString () {
        String t = "";
        switch (packet_type) {
            case 1: t = "SYN";
                break;
            case 2: t = "ACK";
                break;
            case 3: t = "SYN/ACK";
                break;
            case 4: t = "REQ";
                break;
            case 5: t = "DATA";
                break;
            case 6: t = "LIST";
                break;
            case 7: t = "FIN";
                break;
            default:
                break;
        }
        return t;
    }


    public PDU (byte[] data, int length) {
        setType(ByteBuffer.wrap(Arrays.copyOfRange(data, 0, 4)).getInt());
        setSeq(ByteBuffer.wrap(Arrays.copyOfRange(data, 4, 8)).getInt());
        setNumber(ByteBuffer.wrap(Arrays.copyOfRange(data, 8, 12)).getInt());
        setWindow(ByteBuffer.wrap(Arrays.copyOfRange(data, 12, 16)).getInt());
        setTotal(ByteBuffer.wrap(Arrays.copyOfRange(data, 16, 20)).getInt());
        setCheck(ByteBuffer.wrap(Arrays.copyOfRange(data, 20, 24)).getInt());
        setData(ByteBuffer.wrap(Arrays.copyOfRange(data, 24, length)).array());
    }

    public byte[] PDU_to_data () {
        ByteBuffer buff = ByteBuffer.allocate(24 + this.getData().length);
        buff.put(ByteBuffer.allocate(4).putInt(this.getType()).array());
        buff.put(ByteBuffer.allocate(4).putInt(this.getSeq()).array());
        buff.put(ByteBuffer.allocate(4).putInt(this.getNumber()).array());
        buff.put(ByteBuffer.allocate(4).putInt(this.getWindow()).array());
        buff.put(ByteBuffer.allocate(4).putInt(this.getTotal()).array());
        buff.put(ByteBuffer.allocate(4).putInt(this.getCheck()).array());
        buff.put(ByteBuffer.allocate(this.getData().length).put(this.getData()).array());

        return buff.array();
    }

    public int getType() {
        return this.packet_type;
    }

    public int getSize() {
        return 24 + this.data.length;
    }

    public int getNumber() {
        return this.packet_number;
    }

    public int getWindow() {
        return this.packet_window;
    }

    public int getSeq() {
        return this.packet_seq;
    }

    public int getTotal() {
        return this.total_segments;
    }

    public int getCheck() {
        return this.checksum;
    }


    public byte[] getData() {
        byte[] clone = new byte[this.data.length];
        clone = Arrays.copyOfRange(this.data, 0, this.data.length);
        return clone;
    }

    public void setType(int type) {
        this.packet_type = type;
    }


    public void setSeq(int seq) {
        this.packet_seq = seq;
    }



    public void setNumber(int number) {
        this.packet_number = number;
    }

    public void setWindow(int window) {
        this.packet_window = window;
    }

    public void setTotal(int total) {this.total_segments = total;}

    public void setCheck(int check) {
        this.checksum = check;
    }

    public void setData(byte[] dt) {
        this.data = new byte[dt.length];
        this.data = Arrays.copyOfRange(dt, 0, dt.length);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Type: " + this.get_TypeString() + "\n");
        sb.append("Num Seq: " + this.getSeq() + "\n");
        sb.append("Number: " + this.getNumber() + "\n");
        sb.append("Checksum: " + this.getCheck() + "\n");
        sb.append("Window: " + this.getWindow() + "\n");
        sb.append("Total Segments: " + this.getTotal() + "\n");
        sb.append("Size: " + this.getSize() + "\n");
        /*
        if(this.data.length > 0) {
            String s = new String(this.getData());
            sb.append("Data: " + this.data.length + "\n");
            sb.append(s);
        }
        */
        return sb.toString();
    }

}