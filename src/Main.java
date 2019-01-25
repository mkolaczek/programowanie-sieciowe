import java.io.IOException;
import java.net.*;
import java.util.Scanner;


public class Main {

    private static String getNick(InetAddress group, MulticastSocket socket) {

        Scanner in = new Scanner(System.in);
        System.out.println("podaj nick:");
        String nick = in.nextLine();
        nick = "NICK " + nick;
        DatagramPacket dp = new DatagramPacket(nick.getBytes(), nick.length(),
                group, 5000);

        try {
            socket.send(dp);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return nick;

    }

    private static String loop(InetAddress group, MulticastSocket socket) throws IOException {

        String nick = getNick(group, socket);

        socket.setSoTimeout(1000);

        int condition = 0;

        while (true) {


            byte[] buf = new byte[1024];
            DatagramPacket dp = new DatagramPacket(buf, buf.length);
            String received = "";

            try {
                socket.receive(dp);
                received = new String(dp.getData(), 0, dp.getLength());
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
            group = InetAddress.getByName("239.0.0.222");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        MulticastSocket socket = new MulticastSocket(5000);
        socket.joinGroup(group);

        Scanner in = new Scanner(System.in);
        String nick = loop(group, socket);

        System.out.println("zostałeś zarejestrowany!");

        System.out.println("podaj nazwe pokoju:");
//        String room = in.nextLine();
        Room roomClass = new Room(in.nextLine());

        WhoIsRoom whoIsRoom = new WhoIsRoom();

        SendThread w1 = new SendThread(nick, roomClass,  whoIsRoom);
        ListenThread w2 = new ListenThread(nick, roomClass,  whoIsRoom);

        try {
            w1.t.join();
            w2.t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        in.close();
    }
}

