import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.Scanner;

class SendThread implements Runnable {

    private MulticastSocket socket;
    private InetAddress group;
    private String nick, room;
    Thread t;

    public SendThread(String nick, String room) {
        this.nick = nick;
        this.room = room;
        t = new Thread(this);
        t.start();
    }

    @Override
    public void run() {

        nick = nick.substring(5, nick.length());
        //teraz w nick "nazwa"

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

        String joinRoomNick = "JOIN " + room + " " + nick;
        DatagramPacket joinRN = new DatagramPacket(joinRoomNick.getBytes(), joinRoomNick.length(),
                group, 5000);
        try {
            socket.send(joinRN);

        } catch (IOException e) {
            e.printStackTrace();
        }


        Scanner in = new Scanner(System.in);

        while (true) {

            String msg = in.nextLine();
            msg = "MSG " + nick + " " + room + ": " + msg;
            DatagramPacket dp = new DatagramPacket(msg.getBytes(), msg.length(),
                    group, 5000);

            try {
                socket.send(dp);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
