import javax.swing.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;

class Client {
    private final DoubleSideMap<Integer, String> peers = new DoubleSideMap<>();
    private RSA rsa;
    private GUI gui;
    private int port;
    private Socket link;
    private String name;
    private Server server;
    private int choosedPort;
    private String choosedName;
    private DataInputStream inLink;
    private DataOutputStream outLink;
    boolean interrupt = false;
    private boolean isConnected = false;

    Client(String name, int port) {
        this.name = name;
        this.port = port;
        gui = new GUI(this);
        status();

        server = new Server(this);
        Thread serverThread = new Thread(server, "Server" + name);
        serverThread.setDaemon(true);
        serverThread.start();
    }

    RSA getRsa() {
        return rsa;
    }

    void setRsa(RSA rsa) {
        this.rsa = rsa;
    }

    boolean IsDisconnected() {
        return !isConnected;
    }

    void setIsConnected(boolean isConnected) {
        this.isConnected = isConnected;
    }

    void setLink(Socket link) {
        this.link = link;
    }

    void setInLink(DataInputStream inLink) {
        this.inLink = inLink;
    }

    void setOutLink(DataOutputStream outLink) {
        this.outLink = outLink;
    }

    GUI getGui() {
        return gui;
    }

    void setChoosedPort(int choosedPort) {
        this.choosedPort = choosedPort;
    }

    String getChoosedName() {
        return choosedName;
    }

    void setChoosedName(String choosedName) {
        this.choosedName = choosedName;
    }

    DoubleSideMap<Integer, String> getPeers() {
        return peers;
    }

    String getName() {
        return name;
    }

    int getPort() {
        return port;
    }

    void refresh() {
        CountDownLatch latch = new CountDownLatch(Main.EndPortInterval - Main.BeginPortInterval);
        List<String> authed = new ArrayList<>();
        gui.getListModel().clear();
        peers.clear();

        for (int i = Main.BeginPortInterval; i <= Main.EndPortInterval; i++) {
            if (i != port) {
                int finalI = i;

                Thread thread = new Thread(() -> {
                    try {
                        Socket socket = new Socket("127.0.0.1", finalI);
                        socket.setSoTimeout(500);
                        DataInputStream in = new DataInputStream(socket.getInputStream());
                        DataOutputStream out = new DataOutputStream(socket.getOutputStream());

                        out.writeUTF("auth");
                        out.flush();

                        if (!in.readUTF().equals("authed"))
                            return;

                        String name = in.readUTF();
                        synchronized (authed) {
                            authed.add(name);
                        }
                        synchronized (peers) {
                            peers.put(finalI, name);
                        }

                        out.writeUTF(getName());
                        out.flush();

                        out.writeUTF(String.valueOf(port));
                        out.flush();

                        out.writeUTF("stop");
                        out.flush();
                    } catch (IOException ignored) {
                    }
                    latch.countDown();
                });
                thread.setDaemon(true);
                thread.start();
            }
        }
        if (!authed.isEmpty()) {
            if (authed.size() > 1) {
                authed.sort(Comparator.naturalOrder());
            }
            for (String i : authed) {
                gui.getListModel().addElement(i);
            }
            gui.getClients().getViewport().getView().setEnabled(true);
        } else {
            gui.getListModel().addElement(Main.ClientsNo);
        }
    }

    boolean connect() {
        try {
            link = new Socket("127.0.0.1", Main.EnvironmentPort);
            inLink = new DataInputStream(link.getInputStream());
            outLink = new DataOutputStream(link.getOutputStream());

            server.setNoNeedAccept();

            server.setSocket(link);
            server.setIn(inLink);
            server.setOut(outLink);
            server.setConnectedName(choosedName);

            outLink.writeUTF(String.valueOf(choosedPort));
            outLink.flush();

            outLink.writeUTF("connect");
            outLink.flush();

            if (!inLink.readUTF().equals("free")) {
                JOptionPane.showMessageDialog(gui,
                        new String[]{Main.BusyServerConnection},
                        Main.Error,
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }

            if (connectRSA()) return true;
        } catch (
                IOException e2) {
            return false;
        }
        return false;
    }

    private boolean connectRSA() {
        rsa = new RSA();
        try {
            outLink.writeUTF("e:" + String.valueOf(rsa.getE()));
            outLink.flush();
            outLink.writeUTF("n:" + String.valueOf(rsa.getN()));
            outLink.flush();

            rsa.setRecievedE(Long.parseLong(inLink.readUTF().split(":")[1]));
            rsa.setRecievedN(Long.parseLong(inLink.readUTF().split(":")[1]));
        } catch (IOException io) {
            return false;
        }
        return true;
    }

    boolean disconnect() {
        try {
            if (!disconnectRSA()) return false;

            outLink.writeUTF("disconnect");
            outLink.flush();

            interrupt = true;
            outLink.writeUTF("stop");
            outLink.flush();

            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private boolean disconnectRSA() {
        rsa = null;
        return true;
    }

    boolean send(String message) {
        try {
            outLink.writeUTF("message");
            outLink.flush();

            String string;
            string = Coder.codeRSA(rsa.getRecievedE(), rsa.getRecievedN(), message);

            outLink.writeUTF(string);
            outLink.flush();
            return true;
        } catch (IOException io) {
            return false;
        }
    }

    void status() {
        String[] status = new String[2];
        status[0] = Main.rsaStatusNames;
        if (rsa != null) {
            status[1] = MessageFormat.format("{0}\n{1}\n{2}\n{3}\n{4}\n{5}\n{6}\n{7}",
                    rsa.getP(),
                    rsa.getQ(),
                    rsa.getN(),
                    rsa.getF(),
                    rsa.getE(),
                    rsa.getD(),
                    rsa.getRecievedE(),
                    rsa.getRecievedN());
        } else {
            status[1] = "0\n0\n0\n0\n0\n0\n0\n0";
        }

        getGui().getStatus0().setText(status[0]);
        getGui().getStatus1().setText(status[1]);
    }
}

class Server implements Runnable {
    private Client client;
    private ServerSocket serverSocket;
    private String connectedName;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private boolean needAccept = true;

    Server(Client client) {
        this.client = client;
        try {
            serverSocket = new ServerSocket(client.getPort());
        } catch (IOException e) {
            System.err.println("Error in Constructor of Server");
        }
    }

    void setConnectedName(String connectedName) {
        this.connectedName = connectedName;
    }

    void setNoNeedAccept() {
        this.needAccept = false;
    }

    void setSocket(Socket socket) {
        this.socket = socket;
    }

    void setIn(DataInputStream in) {
        this.in = in;
    }

    void setOut(DataOutputStream out) {
        this.out = out;
    }

    private void serverSocketAccept() {
        while (needAccept) {
            try {
                serverSocket.setSoTimeout(100);
                socket = serverSocket.accept();
                needAccept = false;
                in = new DataInputStream(socket.getInputStream());
                out = new DataOutputStream(socket.getOutputStream());
            } catch (IOException ignored) {
            }
        }
    }

    @Override
    public void run() {
        try {
            serverSocketAccept();

            client.setLink(socket);
            client.setInLink(in);
            client.setOutLink(out);

            while (true) {
                switch (in.readUTF()) {
                    case "auth":
                        out.writeUTF("authed");
                        out.flush();

                        out.writeUTF(client.getName());
                        out.flush();

                        connectedName = in.readUTF();

                        client.getPeers().put(Integer.parseInt(in.readUTF()), connectedName);
                        if (!client.getGui().getListModel().contains(connectedName)) {
                            if (!client.getGui().getClients().getViewport().getView().isEnabled()) {
                                client.getGui().getListModel().clear();
                            }
                            client.getGui().getListModel().addElement(connectedName);
                        }
                        break;
                    case "connect":
                        if (connect()) {
                            client.getGui().getConnect().setText(Main.DisconnectName);
                            client.getGui().getStringJList().setSelectedValue(connectedName, true);
                            client.getGui().getClients().getViewport().getView().setEnabled(false);
                            client.getGui().getSend().setEnabled(true);
                            client.getGui().getRefresh().setEnabled(false);
                            client.setIsConnected(true);
                            client.status();
                        }
                        break;
                    case "disconnect":
                        if (disconnect()) {
                            client.getGui().getConnect().setText(Main.ConnectName);
                            client.getGui().getClients().getViewport().getView().setEnabled(true);
                            client.getGui().getSend().setEnabled(false);
                            client.getGui().getRefresh().setEnabled(true);
                            client.setIsConnected(false);
                            client.status();
                        }
                        break;
                    case "message":
                        String message = in.readUTF();
                        client.getGui().getLog().append(MessageFormat.format("{0}:\n{1}\n", connectedName, msgDecode(message)));
                        break;
                    case "stop":
                        if (socket.isConnected() && !client.interrupt) {
                            out.writeUTF("stop");
                            out.flush();
                        } else client.interrupt = false;
                        needAccept = true;
                        socket.close();
                        return;
                }
            }
        } catch (IOException e) {
            System.err.println("Error in run() of Server");
        } finally {
            run();
        }
    }

    private boolean connect() {
        try {
            if (client.IsDisconnected()) {
                out.writeUTF("free");
                out.flush();

                if (connectRSA()) return true;
            } else {
                out.writeUTF("busy");
                out.flush();
            }
        } catch (IOException e) {
            System.err.println("Error in connect() of Server");
        }
        return false;
    }

    private boolean connectRSA() {
        client.setRsa(new RSA());
        try {
            client.getRsa().setRecievedE(Long.parseLong(in.readUTF().split(":")[1]));
            client.getRsa().setRecievedN(Long.parseLong(in.readUTF().split(":")[1]));

            out.writeUTF("e:" + String.valueOf(client.getRsa().getE()));
            out.flush();
            out.writeUTF("n:" + String.valueOf(client.getRsa().getN()));
            out.flush();
        } catch (IOException io) {
            io.printStackTrace();
            return false;
        }
        return true;
    }

    private boolean disconnect() {
        return disconnectRSA();
    }

    private boolean disconnectRSA() {
        client.setRsa(null);
        return true;
    }

    private String msgDecode(String message) {
        return Coder.decodeRSA(client.getRsa().getD(), client.getRsa().getN(), message);
    }
}