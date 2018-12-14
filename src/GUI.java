import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.MessageFormat;
import java.util.Random;

class GUI extends JFrame {
    private final Color background = new Color(238, 238, 238);

    private JButton send;
    private Client client;
    private JTextArea log;
    private JButton connect;
    private JButton refresh;
    private JTextField input;
    private JTextArea status0;
    private JTextArea status1;
    private JScrollPane clients;
    private JList<String> stringJList;
    private DefaultListModel<String> listModel;

    GUI(Client client) {
        super(client.getName());
        this.client = client;
        createGUI();
    }

    JTextArea getLog() {
        return log;
    }

    DefaultListModel<String> getListModel() {
        return listModel;
    }

    JList<String> getStringJList() {
        return stringJList;
    }

    JButton getConnect() {
        return connect;
    }

    JButton getSend() {
        return send;
    }

    JButton getRefresh() {
        return refresh;
    }

    JScrollPane getClients() {
        return clients;
    }

    JTextArea getStatus0() {
        return status0;
    }

    JTextArea getStatus1() {
        return status1;
    }

    private void createGUI() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        JPanel centerPanel = new JPanel();
        centerPanel.setBorder(BorderFactory.createTitledBorder(Main.LogName + ":"));
        centerPanel.setLayout(new BorderLayout());

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BorderLayout());

        JPanel southPanel = new JPanel();
        GridBagLayout gridBagLayout = new GridBagLayout();
        southPanel.setLayout(gridBagLayout);
        southPanel.setBorder(BorderFactory.createEtchedBorder());

        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.gridheight = GridBagConstraints.RELATIVE;
        gridBagConstraints.gridwidth = GridBagConstraints.RELATIVE;
        gridBagConstraints.gridx = GridBagConstraints.RELATIVE;
        gridBagConstraints.gridy = GridBagConstraints.RELATIVE;
        gridBagConstraints.insets = new Insets(1, 3, 0, 0);
        gridBagConstraints.ipadx = 0;
        gridBagConstraints.ipady = 0;
        gridBagConstraints.weightx = 100.;
        gridBagConstraints.weighty = 1.;

        input = new JTextField();
        input.setEditable(true);
        gridBagLayout.setConstraints(input, gridBagConstraints);
        southPanel.add(input);

        gridBagConstraints.weightx = 1.;
        gridBagConstraints.weighty = 1.;
        send = new JButton(Main.SendName);
        gridBagLayout.setConstraints(send, gridBagConstraints);
        send.setEnabled(false);
        southPanel.add(send);

        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createTitledBorder(Main.StatusName + ":"));
        leftPanel.add(BorderLayout.NORTH, statusPanel);

        status0 = new JTextArea();
        status1 = new JTextArea();
        status0.setBackground(background);
        status1.setBackground(background);
        status0.setEditable(false);
        status1.setEditable(false);
        status0.setLineWrap(false);
        status1.setLineWrap(false);
        statusPanel.add(BorderLayout.WEST, status0);
        statusPanel.add(BorderLayout.CENTER, status1);

        listModel = new DefaultListModel<>();
        stringJList = new JList<>(listModel);
        listModel.add(0, Main.ClientsNo);
        clients = new JScrollPane(stringJList);
        clients.getViewport().getView().setEnabled(false);
        clients.getViewport().getView().setBackground(background);
        clients.setBorder(BorderFactory.createTitledBorder(Main.ClientsName + ":"));
        leftPanel.add(BorderLayout.CENTER, clients);

        JPanel buttonLeftSouthPanel = new JPanel();
        leftPanel.add(BorderLayout.SOUTH, buttonLeftSouthPanel);

        log = new JTextArea();
        log.setEditable(false);
        log.setLineWrap(true);
        log.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(log);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        centerPanel.add(scrollPane);

        refresh = new JButton(Main.RefreshName);
        buttonLeftSouthPanel.add(BorderLayout.SOUTH, refresh);
        connect = new JButton(Main.ConnectName);
        buttonLeftSouthPanel.add(BorderLayout.SOUTH, connect);

        mainPanel.add(BorderLayout.SOUTH, southPanel);
        mainPanel.add(BorderLayout.CENTER, centerPanel);
        mainPanel.add(BorderLayout.WEST, leftPanel);
        setContentPane(mainPanel);

        setBounds(new Random().nextInt(Toolkit.getDefaultToolkit().getScreenSize().width * 2 / 3),new Random().nextInt(Toolkit.getDefaultToolkit().getScreenSize().height /2),
                Toolkit.getDefaultToolkit().getScreenSize().width / 2, Toolkit.getDefaultToolkit().getScreenSize().height / 2);
        listeners();
        pack();
        setVisible(true);
        setResizable(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setMinimumSize(getSize());
        connect.setMinimumSize(connect.getSize());
    }

    private void send(String text) {
        log.append(MessageFormat.format("{0}:\n{1}\n", client.getName(), text));
        input.setText("");
    }

    private void listeners() {
        input.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == 10) {
                    if (client.send(input.getText())) {
                        send(input.getText());
                    }
                }
            }
        });

        send.addActionListener(e -> {
            if (client.send(input.getText())) {
                send(input.getText());
            }
        });

        stringJList.addListSelectionListener(e -> {
            Object selected = stringJList.getSelectedValue();
            if (selected != null) {
                client.setChoosedName(selected.toString());
                client.setChoosedPort(client.getPeers().get1Arg(selected.toString()));
            }
        });

        connect.addActionListener(e -> {
            if (client.IsDisconnected()) {
                if (client.getChoosedName() != null) {
                    if (client.connect()) {
                        client.setIsConnected(true);
                        connect.setText(Main.DisconnectName);
                        refresh.setEnabled(false);
                        clients.getViewport().getView().setEnabled(false);
                        send.setEnabled(true);
                        client.status();
                    }
                } else {
                    JOptionPane.showMessageDialog(GUI.this,
                            Main.NeedChooseUser,
                            Main.Error,
                            JOptionPane.ERROR_MESSAGE);
                }
            } else {
                if (client.disconnect()) {
                    client.setIsConnected(false);
                    connect.setText(Main.ConnectName);
                    refresh.setEnabled(true);
                    clients.getViewport().getView().setEnabled(true);
                    send.setEnabled(false);
                    input.setText("");
                    client.status();
                }
            }
        });

        refresh.addActionListener(e -> client.refresh());
    }
}
