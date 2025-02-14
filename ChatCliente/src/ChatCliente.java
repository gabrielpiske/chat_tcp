
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import javax.swing.*;

public class ChatCliente extends JFrame {

    private JTextArea taChat;
    private JTextField jtfMessage;
    private JTextField jtfRecipient;
    private JButton btnSend;
    private JList<String> userList;  // Lista de usuários conectados
    private DefaultListModel<String> userModel; // Modelo da lista
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    //private String serverAddress = "127.0.0.1"; // IP do servidor
    private String serverAddress = "localhost"; // IP do servidor
    private int port = 12345;

    public ChatCliente() {
        setTitle("Chat Cliente");
        setSize(500, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Área de Chat
        taChat = new JTextArea();
        taChat.setEditable(false);
        add(new JScrollPane(taChat), BorderLayout.CENTER);

        // Painel inferior
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        JPanel recipientPanel = new JPanel();
        recipientPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel lblRecipient = new JLabel("Destinatário:");
        recipientPanel.add(lblRecipient);
        jtfRecipient = new JTextField(15);
        recipientPanel.add(jtfRecipient);
        panel.add(recipientPanel, BorderLayout.NORTH);

        JPanel messagePanel = new JPanel();
        messagePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel lblMessage = new JLabel("Mensagem:");
        messagePanel.add(lblMessage);
        jtfMessage = new JTextField(20);
        messagePanel.add(jtfMessage);
        panel.add(messagePanel, BorderLayout.CENTER);

        btnSend = new JButton("Enviar");
        panel.add(btnSend, BorderLayout.EAST);
        add(panel, BorderLayout.SOUTH);

        // Lista de usuários conectados
        userModel = new DefaultListModel<>();
        userList = new JList<>(userModel);
        JScrollPane userScrollPane = new JScrollPane(userList);
        userScrollPane.setPreferredSize(new Dimension(120, 0));
        add(userScrollPane, BorderLayout.EAST);

        btnSend.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        connectToServer();
        new Thread(new MessageReceiver()).start();
    }

    private void connectToServer() {
        try {
            String ipHost = JOptionPane.showInputDialog("Digite o IP do servidor:");
            serverAddress = ipHost;
            socket = new Socket(serverAddress, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String name = JOptionPane.showInputDialog("Digite seu Nome:");
            setTitle("Chat Cliente - " + name);
            out.println(name);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendMessage() {
        String recipient = jtfRecipient.getText();
        String message = jtfMessage.getText();
        if (!message.isEmpty() && !recipient.isEmpty()) {
            out.println("/send " + recipient + " " + message);
            taChat.append("Você (para " + recipient + "): " + message + "\n");
            jtfMessage.setText("");
            jtfRecipient.setText("");
        }
    }

    private class MessageReceiver implements Runnable {

        public void run() {
            try {
                String message;
                while ((message = in.readLine()) != null) {
                    if (message.startsWith("/users")) {
                        updateUserList(message);
                    } else {
                        taChat.append(message + "\n");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void updateUserList(String message) {
        SwingUtilities.invokeLater(() -> {
            userModel.clear();
            String[] users = message.split(" ");
            for (int i = 1; i < users.length; i++) {
                userModel.addElement(users[i]);
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ChatCliente().setVisible(true));
    }
}
