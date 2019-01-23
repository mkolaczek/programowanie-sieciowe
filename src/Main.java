import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

class ListenThread implements Runnable {

    Thread t;
    private InetAddress group;
    private MulticastSocket socket;
    private String nick;
    private CountDownLatch countDownLatch;


    public ListenThread(InetAddress group, MulticastSocket socket, String nick, CountDownLatch countDownLatch) {
        this.group = group;
        this.socket = socket;
        this.nick = nick;
        this.countDownLatch = countDownLatch;
        t = new Thread(this);
        t.start();
    }

    @Override
    public void run() {

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


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
    private CountDownLatch countDownLatch;
    Thread t;

    public SendThread(InetAddress group, MulticastSocket socket,CountDownLatch countDownLatch) {
        this.group = group;
        this.socket = socket;
        this.countDownLatch = countDownLatch;
        t = new Thread(this);
        t.start();
    }

    @Override
    public void run() {

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

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

    public static void main(String[] args) throws IOException {

        CountDownLatch countDownLatch = new CountDownLatch(1);

        InetAddress group = null;
        try {
            group = InetAddress.getByName("228.5.6.7");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        MulticastSocket socket = new MulticastSocket(6789);
        socket.joinGroup(group);

        Scanner in = new Scanner(System.in);
        String nick;

        while (true) {
            System.out.println("podaj nick:");
            nick = in.nextLine();
            nick = "NICK " + nick;
            DatagramPacket dp = new DatagramPacket(nick.getBytes(), nick.length(),
                    group, 6789);

            try {
                socket.send(dp);
                System.out.println("wyslano nick");
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            byte[] buf = new byte[1024];
            DatagramPacket recv = new DatagramPacket(buf, buf.length);
            String received = "";
            try {
                socket.receive(recv);
                received = new String(recv.getData(), 0, recv.getLength());
                System.out.println("odebrano: " + received);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // sprawdz ten warunek jeszcze!
            if (!(received.equals(nick + " BUSY"))) {
                System.out.println("zostałeś zarejestrowany!");
                countDownLatch.countDown();
                break;
            }

            System.out.println(nick + " BUSY");

        }


        SendThread w1 = new SendThread(group, socket, countDownLatch);
        ListenThread w2 = new ListenThread(group, socket, nick, countDownLatch);

        try {
            w1.t.join();
            w2.t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        in.close();
    }
}

