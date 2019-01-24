import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ListenThread implements Runnable {

    Thread t;
    private InetAddress group;
    private MulticastSocket socket;
    private String nick;


    public ListenThread(String nick) {
        this.nick = nick;
        t = new Thread(this);
        t.start();
    }

    @Override
    public void run() {

        InetAddress group = null;
        try {
            group = InetAddress.getByName("239.0.0.222");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        MulticastSocket socket = null;
        try {
            socket = new MulticastSocket(5000);
            socket.joinGroup(group);
        } catch (IOException e) {
            e.printStackTrace();
        }

        byte[] buf = new byte[1024];

        while (true) {


            DatagramPacket recv = new DatagramPacket(buf, buf.length);
            try {
                socket.receive(recv);
                String received = new String(recv.getData(), 0, recv.getLength());

                Pattern compiledPattern = Pattern.compile("NICK");
                Matcher matcher = compiledPattern.matcher(received);

                if (received.equals(nick)) {
                    String nickBusy = nick + " BUSY";
                    DatagramPacket dp = new DatagramPacket(nickBusy.getBytes(), nickBusy.length(),
                            group, 5000);
                    socket.send(dp);
                } else if (received.equals(nick + " BUSY")) {
                    // nie rob nic
                } else if (matcher.find()) {
                    // nie rob nic
                } else {
                    System.out.println(received);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
