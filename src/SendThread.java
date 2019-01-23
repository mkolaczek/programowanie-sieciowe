import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.Scanner;

class SendThread implements Runnable {

    private MulticastSocket socket;
    private InetAddress group;
    private String nick;
    Thread t;

    public SendThread(String nick) {
        this.nick = nick;
        t = new Thread(this);
        t.start();
    }

    @Override
    public void run() {

//        System.out.println("watek wysylajacy");

        nick = nick.substring(4, nick.length());

        InetAddress group = null;
        try {
            group = InetAddress.getByName("228.5.6.7");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        MulticastSocket socket = null;
        try {
            socket = new MulticastSocket(6789);
            socket.joinGroup(group);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Scanner in = new Scanner(System.in);

        while (true) {

            String msg = in.nextLine();
            msg = "MSG " + nick + ": " + msg;
            DatagramPacket dp = new DatagramPacket(msg.getBytes(), msg.length(),
                    group, 6789);

            try {
                socket.send(dp);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
