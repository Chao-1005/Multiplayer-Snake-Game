import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private TextArea ta;
    private TextField tf;

    private ServerSocket serverSocket;
    private List<ClientHandler> clientHandlers;
    private ExecutorService executor;
    private int clientCounter;

    public static void main(String[] args) {
        Server server = new Server();
        server.createUI();
        server.start();
    }

    public void createUI() {
        Frame f = new Frame("Server");
        ta = new TextArea();
        tf = new TextField();
        Button send = new Button("Send");

        Panel p = new Panel();
        p.setLayout(new BorderLayout());
        p.add(tf, BorderLayout.CENTER);
        p.add(send, BorderLayout.EAST);

        f.add(ta, BorderLayout.CENTER);
        f.add(p, BorderLayout.SOUTH);

        send.addActionListener(e -> {
            String message = tf.getText();
            broadcastMessage("Host: " + message);
            tf.setText("");
        });

        tf.addActionListener(e -> {
            String message = tf.getText();
            broadcastMessage("Host: " + message);
            tf.setText("");
        });

        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                stop();
                System.exit(0);
            }
        });

        f.setSize(400, 400);
        f.setVisible(true);
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(1234);
            clientHandlers = new ArrayList<>();
            executor = Executors.newCachedThreadPool();
            ta.append("Server started\n");
            clientCounter = 0;

            while (clientCounter!=4) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket, clientCounter);
                clientHandlers.add(clientHandler);
                executor.execute(clientHandler);
                ta.append("New client connected: User " + clientCounter + " [" + clientSocket.getInetAddress().getHostAddress() + "]\n");
                broadcastMessage("Current number of clients: " + clientHandlers.size());

                clientCounter++;
                if (clientCounter==4){broadcastMessage("game start");}
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        try {
            executor.shutdown();
            for (ClientHandler clientHandler : clientHandlers) {
                clientHandler.stopClient();
            }
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void broadcastMessage(String message) {
        ta.append(message + "\n");
        for (ClientHandler clientHandler : clientHandlers) {
            clientHandler.sendMessage(message);
        }
    }

    private class ClientHandler implements Runnable {
        private Socket clientSocket;
        private DataInputStream dis;
        private DataOutputStream dos;
        private int clientNumber;

        public ClientHandler(Socket clientSocket, int clientNumber) {
            this.clientSocket = clientSocket;
            this.clientNumber = clientNumber;
        }

        public void run() {
            try {
                dis = new DataInputStream(clientSocket.getInputStream());
                dos = new DataOutputStream(clientSocket.getOutputStream());

                while (true) {
                    String message = dis.readUTF();
                    broadcastMessage("User " + clientNumber + ": " + message);
                    if (message.equals("bye")) {
                        stopClient();
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void sendMessage(String message) {
            try {
                dos.writeUTF(message);
                dos.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void stopClient() {
            try {
                dis.close();
                dos.close();
                clientSocket.close();
                clientHandlers.remove(this);
                ta.append("Client " + clientNumber + " disconnected\n");
                broadcastMessage("Current number of clients: " + clientHandlers.size());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
