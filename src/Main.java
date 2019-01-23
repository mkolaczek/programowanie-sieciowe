import java.io.IOException;
import java.net.*;
import java.util.Scanner;
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

//        System.out.println("watek odbierajacy");

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
                            group, 6789);
                    socket.send(dp);
                } else if (received.equals(nick + " BUSY")) {
                    // nie rob nic
                } else if (matcher.find()){
                    // nie rob nic
                }
                else {
                    System.out.println(received);
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


            nick = nick.substring(4, nick.length());
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


public class Main {

    private static String getNick(InetAddress group, MulticastSocket socket) {

        Scanner in = new Scanner(System.in);
        System.out.println("podaj nick:");
        String nick = in.nextLine();
        nick = "NICK " + nick;
        DatagramPacket dp = new DatagramPacket(nick.getBytes(), nick.length(),
                group, 6789);

        try {
            socket.send(dp);
//            System.out.println("wyslano nick");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return nick;

    }

    private static String loop(InetAddress group, MulticastSocket socket) throws IOException {

        String nick = getNick(group, socket);

        socket.setSoTimeout(10000);

        int condition = 0;

        while (true) {


            byte[] buf = new byte[1024];
            DatagramPacket dp = new DatagramPacket(buf, buf.length);
            String received = "";

            try {
                socket.receive(dp);
                received = new String(dp.getData(), 0, dp.getLength());
//                System.out.println("odebrano: " + received);
            } catch (SocketTimeoutException e) {
                condition = 1;
            }

            if ((received.equals(nick + " BUSY"))) {
                System.out.println(nick + " BUSY");
                nick = loop(group, socket);
                break;
            }

            if (condition == 1)
                break;
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
        String nick = loop(group, socket);

        System.out.println("zostałeś zarejestrowany!");


        SendThread w1 = new SendThread(nick);
        ListenThread w2 = new ListenThread(nick);

        try {
            w1.t.join();
            w2.t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        in.close();
    }
}

