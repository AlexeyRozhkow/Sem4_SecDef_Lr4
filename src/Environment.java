import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

class Environment {
    private GUI gui;

    Environment(int port) {
        gui = new GUI();
        Thread clientToServer = new Thread(new ClientToServer(port, this));
        clientToServer.setDaemon(true);
        clientToServer.start();
    }

    class GUI extends JFrame {
        private JTextArea log;

        GUI() {
            super("Внешняя среда");
            createGUI();
        }

        JTextArea getLog() {
            return log;
        }

        private void createGUI() {
            JPanel mainPanel = new JPanel();
            mainPanel.setLayout(new BorderLayout());

            log = new JTextArea("");
            log.setEditable(false);
            log.setLineWrap(true);
            log.setWrapStyleWord(true);
            log.setBorder(new EtchedBorder());
            JScrollPane scrollPane = new JScrollPane(log);
            scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

            mainPanel.add(scrollPane);

            DefaultCaret caret = (DefaultCaret)log.getCaret();
            caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

            setContentPane(mainPanel);
            setPreferredSize(new Dimension(Toolkit.getDefaultToolkit().getScreenSize().width / 3, Toolkit.getDefaultToolkit().getScreenSize().height / 3));
            pack();
            setVisible(true);
            setResizable(false);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            setMinimumSize(getSize());
        }
    }

    synchronized void print(String s, boolean toServer) {
        if (toServer) {
            if (Main.LogEnv) System.out.println(s);
            gui.getLog().append("toServer: " + s + '\n');
        } else {
            if (Main.LogEnv) System.err.println(s);
            gui.getLog().append("toClient:  " + s + '\n');
            if(s.equals("stop"))gui.getLog().append("\n");;
        }
    }
}

class ClientToServer implements Runnable {
    private ServerSocket serverSocket;
    private Environment e;

    ClientToServer(int port, Environment e) {
        this.e = e;
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException ex) {
            System.err.println("Error in Constructor of DownloadClient");
        }
    }

    @Override
    public void run() {
        try {
            Socket socketIn = serverSocket.accept();
            DataInputStream fromClient = new DataInputStream(socketIn.getInputStream());
            DataOutputStream toClient = new DataOutputStream(socketIn.getOutputStream());

            Socket socketOut = new Socket("127.0.0.1", Integer.parseInt(fromClient.readUTF()));
            DataInputStream fromServer = new DataInputStream(socketOut.getInputStream());
            DataOutputStream toServer = new DataOutputStream(socketOut.getOutputStream());

            Thread thread = new Thread(new ServerToClient(fromServer, toClient, e));
            thread.setDaemon(true);
            thread.start();

            do {
                String string = fromClient.readUTF();
                toServer.writeUTF(string);
                toServer.flush();
                e.print(string, true);
            } while (true);
        } catch (EOFException ignored) {
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error in run() of ClientToServer");
        }
        run();
    }

}

class ServerToClient implements Runnable {
    private DataInputStream fromServer;
    private DataOutputStream toClient;
    private Environment e;

    ServerToClient(DataInputStream fromServer, DataOutputStream toClient, Environment e) {
        this.e = e;
        this.fromServer = fromServer;
        this.toClient = toClient;
    }

    @Override
    public void run() {
        try {
            do {
                String string = fromServer.readUTF();
                toClient.writeUTF(string);
                toClient.flush();
                e.print(string, false);
            } while (true);
        } catch (EOFException ignored) {
        } catch (IOException ex) {
            ex.printStackTrace();
            System.err.println("Error in run() of ServerToClient");

        }
    }
}
