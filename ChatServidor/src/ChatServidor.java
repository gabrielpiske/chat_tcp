
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ChatServidor {

    private static final int PORT = 12345;
    private static ServerSocket serverSocket;
    private static Map<String, PrintWriter> clients = new HashMap<>();

    public static void main(String[] args) throws Exception {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Servidor aguardando conexões...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Cliente conectado: " + clientSocket.getInetAddress());

                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Classe que gerencia a comunicação com o cliente
    private static class ClientHandler implements Runnable {

        private BufferedReader in;
        private PrintWriter out;
        private String clienteName;

        public ClientHandler(Socket socket) {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void run() {
            try {
                out.println("Digite seu nome:");
                clienteName = in.readLine();

                synchronized (clients) {
                    clients.put(clienteName, out);
                }
                System.out.println(clienteName + " entrou no Chat.");

                // Atualiza lista de usuários
                broadcastUserList();

                String message;
                while ((message = in.readLine()) != null) {
                    if (message.startsWith("/send")) {
                        String[] parts = message.split(" ", 3);
                        if (parts.length == 3) {
                            String target = parts[1];
                            String msg = parts[2];
                            sendMessageToClient(target, msg);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (clienteName != null) {
                    synchronized (clients) {
                        clients.remove(clienteName);
                        System.out.println("Usuário: " + clienteName + " saiu do servidor");
                    }
                    broadcastUserList(); // Atualiza a lista quando o usuário sai
                }
            }
        }

        private void sendMessageToClient(String target, String message) {
            PrintWriter targetOut = clients.get(target);
            if (targetOut != null) {
                targetOut.println(clienteName + " diz: " + message);
            } else {
                out.println("Usuário " + target + " não encontrado");
            }
        }

        private void broadcastUserList() {
            synchronized (clients) {
                StringBuilder userList = new StringBuilder("/users ");
                for (String user : clients.keySet()) {
                    userList.append(user).append(" ");
                }
                String userListMessage = userList.toString();

                for (PrintWriter writer : clients.values()) {
                    writer.println(userListMessage);
                }
            }
        }
    }
}
