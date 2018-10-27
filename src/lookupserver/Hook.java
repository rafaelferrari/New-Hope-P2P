package lookupserver;

public class Hook extends Thread {
    @Override
    public void run() {
        Server.serverSocket.close();
        System.out.println("[GAMESERVER] Servidor finalizado.");
    }
}
