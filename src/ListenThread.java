import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ListenThread implements Runnable {

    Thread t;
    private String nick;
    private Room roomClass;
    private WhoIsRoom whoIsRoom;


    public ListenThread(String nick, Room roomClass, WhoIsRoom whoIsRoom) {
        this.nick = nick;
        this.roomClass = roomClass;
        this.whoIsRoom = whoIsRoom;
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

            String room = roomClass.getRoom();


            DatagramPacket recv = new DatagramPacket(buf, buf.length);
            try {
                socket.receive(recv);
                String received = new String(recv.getData(), 0, recv.getLength());

                Pattern patternNICK = Pattern.compile("NICK");
                Matcher matcherNICK = patternNICK.matcher(received);

                Pattern patternJOIN = Pattern.compile("JOIN");
                Matcher matcherJOIN = patternJOIN.matcher(received);

                Pattern patternROOM = Pattern.compile(room);
                Matcher matcherROOM = patternROOM.matcher(received);

                Pattern patternLEFT = Pattern.compile("LEFT");
                Matcher matcherLEFT = patternLEFT.matcher(received);

                Pattern patternWHOIS = Pattern.compile("WHOIS");
                Matcher matcherWHOIS = patternWHOIS.matcher(received);


                // w nick "NICK nick"
                if (received.equals(nick)) {
                    String nickBusy = nick + " BUSY";
                    DatagramPacket dp = new DatagramPacket(nickBusy.getBytes(), nickBusy.length(),
                            group, 5000);
                    socket.send(dp);
                } else if (received.equals(nick + " BUSY")) {
                    // nie rob nic
                } else if (matcherNICK.find()) {
                    // nie rob nic
                } else {
                    if (matcherJOIN.find()) {
                        if (matcherROOM.find()) {
                            if ((room + " ").equals(received.substring(5, 5 + room.length() + 1))) {
                                int endLength = received.length();
                                int startLength = 5 + room.length() + 1;
                                String receivedNick = received.substring(startLength, endLength);
                                if (!(receivedNick.equals(nick.substring(5, nick.length())))) {
                                    System.out.println(receivedNick + " przylaczyl sie do Twojego pokoju (" + room + ")");
                                }
                            }
                        }
                    }
                    if (matcherLEFT.find()) {
                        if (matcherROOM.find()) {
                            // MSG nick room: LEFT room nick
                            int nickLength = (received.length() - (13 + 2 * room.length())) / 2;
                            if ((room + ":").equals(received.substring(5 + nickLength, 5 + nickLength + room.length() + 1))) {
                                String receivedNick = received.substring(4, 4 + nickLength);
                                if (!(receivedNick.equals(nick.substring(5, nick.length())))) {
                                    System.out.println(receivedNick + " opuscil Twoj pokoj (" + room + ")");
                                }
                            }
                        }
                    }

                    if (matcherWHOIS.find()) {
                        if (matcherROOM.find()) {
                            String whoisMsg = room.toUpperCase() + " " + room + " " + nick.substring(5, nick.length());
//                            System.out.println(whoisMsg);
                            DatagramPacket dp = new DatagramPacket(whoisMsg.getBytes(), whoisMsg.length(),
                                    group, 5000);
                            socket.send(dp);
                        }
                    }

                    // MSG nick room: msg
                    if (matcherROOM.find()) {
                        try {
                            Pattern pattern = Pattern.compile("MSG (\\w+) " + room + ":.*");
                            Matcher matcher = pattern.matcher(received);
                            matcher.matches();
                            String receivedNick = matcher.group(1);
                            String msg = received.substring(7 + receivedNick.length() + room.length(),
                                    received.length());

                            Pattern patternLEFT2 = Pattern.compile("LEFT");
                            Matcher matcherLEFT2 = patternLEFT2.matcher(msg);

                            if (matcherLEFT2.find()) {
                                // nie rob nic
                            } else {
                                Matcher matcherWHOIS2 = patternWHOIS.matcher(msg);
                                if (matcherWHOIS2.find()) {
                                    // nie rob nic
                                } else
                                    System.out.println(receivedNick + ": " + msg);
                            }
                        } catch (IllegalStateException e) {
                            Pattern patternROOM2 = Pattern.compile(room.toUpperCase());
                            Matcher matcherROOM2 = patternROOM2.matcher(received);
                            if (matcherROOM2.find()) {
                                whoIsRoom.addToList(received.substring(room.length() * 2 + 2, received.length()));
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
