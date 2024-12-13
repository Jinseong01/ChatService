package Client;

public class ClientApp {
    public static void main(String[] args) {
        // UI 인스턴스 생성
        ClientUI ui = new ClientUI();

        // Handler 인스턴스 생성 (UI 참조를 넘겨 UI 업데이트 가능하게 함)
        ClientHandler handler = new ClientHandler(ui);

        // UI에 Handler 연결
        ui.setClientHandler(handler);

        // 서버에 연결 시도
        handler.connectToServer("localhost", 12345);

        // UI 표시
        ui.showFrame();
    }
}
