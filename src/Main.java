import java.io.IOException;
import java.net.*;
import java.util.Scanner;

class ListenThread implements Runnable {

    Thread t;
    private InetAddress group;
    private MulticastSocket socket;
    private String nick;


    public ListenThread(InetAddress group, MulticastSocket socket, String nick) {
        this.group = group;
        this.socket = socket;
        this.nick = nick;
        t = new Thread(this);
        t.start();
    }

    @Override
    public void run() {

        System.out.println("watek odbierajacy");


        byte[] buf = new byte[1024];

        while (true) {


            DatagramPacket recv = new DatagramPacket(buf, buf.length);
            try {
                socket.receive(recv);
                String received = new String(recv.getData(), 0, recv.getLength());
                System.out.println("odebrano: " + received);
                if (received.equals(nick)) {
                    String nickBusy = nick + " BUSY";
                    DatagramPacket dp = new DatagramPacket(nickBusy.getBytes(), nickBusy.length(),
                            group, 6789);
                    socket.send(dp);
                    System.out.println("wyslano nick busy");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}

class SendThread implements Runnable {

    private MulticastSocket socket;
    private InetAddress group;
    Thread t;

    public SendThread(InetAddress group, MulticastSocket socket) {
        this.group = group;
        this.socket = socket;
        t = new Thread(this);
        t.start();
    }

    @Override
    public void run() {

        System.out.println("watek wysylajacy");

        Scanner in = new Scanner(System.in);

        while (true) {

            String msg = in.nextLine();
            DatagramPacket dp = new DatagramPacket(msg.getBytes(), msg.length(),
                    group, 6789);

            try {
                socket.send(dp);
                System.out.println("wyslano");


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}


public class Main {

    public static String getNick(InetAddress group, MulticastSocket socket) {

        Scanner in = new Scanner(System.in);
        System.out.println("podaj nick:");
        String nick = in.nextLine();
        nick = "NICK " + nick;
        DatagramPacket dp = new DatagramPacket(nick.getBytes(), nick.length(),
                group, 6789);

        try {
            socket.send(dp);
            System.out.println("wyslano nick");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return nick;

    }

    public static void main(String[] args) throws IOException {

        InetAddress group = null;
        try {
            group = InetAddress.getByName("228.5.6.7");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        MulticastSocket socket = new MulticastSocket(6789);
        socket.joinGroup(group);

        Scanner in = new Scanner(System.in);
        String nick = getNick(group, socket);


        while (true) {


            byte[] buf = new byte[1024];
            DatagramPacket dp = new DatagramPacket(buf, buf.length);
            String received = "";

                socket.receive(dp);
                received = new String(dp.getData(), 0, dp.getLength());
                System.out.println("odebrano: " + received);

            if ((received.equals(nick + " BUSY"))) {
                System.out.println(nick + " BUSY");
//                nick = getNick(group, socket);
                break;
            }
        }


        SendThread w1 = new SendThread(group, socket);
        ListenThread w2 = new ListenThread(group, socket, nick);

        try {
            w1.t.join();
            w2.t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        in.close();
    }
}

