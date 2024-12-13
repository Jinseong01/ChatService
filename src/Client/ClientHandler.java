package Client;

import java.io.*;
import java.net.*;

public class ClientHandler {
    private String serverAddress = "localhost";
    private int serverPort = 12345;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    private ClientUI ui;

    public ClientHandler(ClientUI ui) {
        this.ui = ui;
        connectToServer();
    }

    public void connectToServer() {
        try {
            socket = new Socket(serverAddress, serverPort);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            new Thread(new Listener()).start();
        } catch (IOException e) {
            ui.showErrorMessage("서버에 연결할 수 없습니다.");
        }
    }

    public void login(String id, String pw) {
        if (out != null) out.println("/login " + id + " " + pw);
    }

    public void register(String id, String pw, String userName, String birthday, String nickname, String information) {
        if (out != null) out.println("/signup " + id + " " + pw + " " + userName + " " + birthday + " " + nickname + " " + information);
    }

    public void switchToSignup() {
        ui.switchToSignup();
    }

    private class Listener implements Runnable {
        @Override
        public void run() {
            try {
                String msg;
                while ((msg = in.readLine()) != null) {
                    // 서버에서 받은 메시지를 UI로 전달
                    processMessage(msg);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void processMessage(String msg) {
            if (msg.startsWith("/login success")) {
                ui.showChatUI();
            } else if (msg.startsWith("/signup success")) {
                ui.showLoginUI();
            } else if (msg.startsWith("/error")) {
                ui.showErrorMessage(msg.substring(7));
            }
        }
    }
}
