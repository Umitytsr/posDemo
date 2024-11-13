package test.demo.activity;

public class ApduSend {
    public byte[] Command = null;
    public short  Lc;
    public byte[] DataIn = null;
    public short  Le;

    public ApduSend(byte[] Command, short  Lc, byte[] DataIn, short  Le){
        this.Command = new byte[Command.length];
        this.DataIn = new byte[DataIn.length];
        this.Command = Command;
        this.Lc = Lc;
        this.DataIn = DataIn;
        this.Le = Le;
    }

    public byte[] getBytes(){
        int index = 0;
        byte[] buf = new byte[520];
        System.arraycopy(Command, 0, buf, 0, Command.length);
        index = Command.length;
        buf[index++] = (byte) (Lc / 256);
        buf[index++] = (byte) (Lc % 256);
        System.arraycopy(DataIn, 0, buf, 6, DataIn.length);
        //index +=DataIn.length;
        //buf[index++] = (byte) (Le / 256);
        //buf[index] = (byte) (Le % 256);
        return buf;
    }
}
