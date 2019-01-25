import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Scanner;

class SendThread implements Runnable {

    private String nick;
    private Room roomClass;
    private WhoIsRoom whoIsRoom;
    Thread t;

    public SendThread(String nick, Room room, WhoIsRoom whoIsRoom) {
        this.nick = nick;
        this.roomClass = room;
        this.whoIsRoom = whoIsRoom;
        t = new Thread(this);
        t.start();
    }

    private void newRoom() {
        Scanner in = new Scanner(System.in);

        this.roomClass.setRoom("");
        System.out.println("podaj nowy pokoj:");
        this.roomClass.setRoom(in.nextLine());
    }

    private void sendJoinRoomNick(String room, InetAddress group, MulticastSocket socket) {
        String joinRoomNick = "JOIN " + room + " " + nick;
        DatagramPacket joinRN = new DatagramPacket(joinRoomNick.getBytes(), joinRoomNick.length(),
                group, 5000);
        try {
            socket.send(joinRN);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {

        nick = nick.substring(5, nick.length());
        //teraz w nick "nazwa"

        String room = roomClass.getRoom();

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

        sendJoinRoomNick(room, group, socket);


        Scanner in = new Scanner(System.in);

        while (true) {

            String msg = in.nextLine();
            room = roomClass.getRoom();

            boolean condition = false;
            if (msg.equals("WHOIS " + room)) {
                whoIsRoom.resetList();
                condition = true;
            }


            msg = "MSG " + nick + " " + room + ": " + msg;

            // MSG nick room: LEFT room nick
            if (msg.equals("MSG " + nick + " " + room + ": LEFT " + room + " " + nick)) {
                newRoom();
                sendJoinRoomNick(this.roomClass.getRoom(), group, socket);
            }

            DatagramPacket dp = new DatagramPacket(msg.getBytes(), msg.length(),
                    group, 5000);


            try {
                socket.send(dp);

            } catch (IOException e) {
                e.printStackTrace();
            }

            if (condition) {
                try {
                    Thread.sleep(3000);
                    List<String> list = whoIsRoom.getNicksInRoom();
                    System.out.print("w pokoju sa: ");
                    for (int i = 0; i < list.size() - 1; i++) {
                        System.out.print(list.get(i) + ", ");
                    }
                    System.out.print(list.get(list.size() - 1) + ".");
                    System.out.println();

                    whoIsRoom.resetList();

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }
}
