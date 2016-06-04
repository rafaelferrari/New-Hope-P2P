package lookupserver;
import java.net.*;
import java.util.ArrayList;

class Server {
	
	// Porta utilizada para conex�o com o Servidor
	public static final int port = 6666;
	
	// Vetor contendo os IP dos usu�rios atualmente conectados 
	public static ArrayList<String> connectedUsers;
	
	// Socket utilizado para leitura e escrita dos dados
	public static DatagramSocket serverSocket;
	
	// Classe para garantir o bom funcionamento do servidor ao desligar
	public void attachShutDownHook(){
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				serverSocket.close();
				System.out.println("[GAMESERVER] Servidor finalizado.");
			}
		});
	}
	
	// Execu��o sequencial do programa
	public static void main(String[] args) throws Exception {
		
		// Inicializa��o de vari�veis globais
		serverSocket = new DatagramSocket(port);
		connectedUsers = new ArrayList<String>();
		
		// Vetores contendo os dados em formato de bytes
		byte[] receiveData = new byte[2];
		byte[] sendData = new byte[1024];
		
		// Strings contendo mensagem recebida, IP do emissor e mensagem a ser enviada
		String sentence, sender, message;
		
		// Endere�o IP do emissor, em formato de rede
		InetAddress IPAddress;
		
		// Pacote UDP para envio
		DatagramPacket sendPacket;
		
		// Pacote UDP recebido
		DatagramPacket receivePacket;
		
		// Porta utilizada pelo emissor para envio
		int portaEmissor;
		
		while(true) {
			// Servidor inicializa um pacote UDP e aguarda tentativas de conex�o
			receivePacket = new DatagramPacket(receiveData, receiveData.length);
			System.out.println();
			System.out.println("[GAMESERVER] Peers online:");
			for (String i : connectedUsers) {
				System.out.println("             " + i);
			}			
			System.out.println("[GAMESERVER] Aguardando conexoes na porta " + port + "...");
			serverSocket.receive(receivePacket);	// Programa bloqueia aguardando o recebimento de dados na porta especificada
			
			// Uma vez recebido um pacote, recupera suas informa��es (dados, IP do remetente)
			sentence = new String( receivePacket.getData());
			IPAddress = receivePacket.getAddress();
			sender = new String(IPAddress.toString());
			sender = sender.substring(1);			

			// Caso a mensagem seja um W (WHO, mensagem solicitando a lista de n�s conectados)
			// Cria mensagem contendo os endere�os dos usu�rios conectados
			// Envia mensagem para o endere�o e porta do remetente do WHO
			if (sentence.charAt(0) == 'W') {
				System.out.println("[GAMESERVER] Recebida mensagem WHO de: " + sender);
				portaEmissor = receivePacket.getPort();
				message = "";
				for (String i : connectedUsers) {
					message = message.concat(i).concat(";");
				}			
				sendData = message.getBytes();
				sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, portaEmissor);
				serverSocket.send(sendPacket);
				System.out.println("[GAMESERVER] Enviado para " + sender + ": " + message);
				
				// Caso a lista de usu�rios conectados n�o contenha o usu�rio emissor, ele � adicionado a ela
				if (!connectedUsers.contains(sender)) {
					connectedUsers.add(sender);
				}
				
			// Caso a mensagem seja um D (DEAD, mensagem informando que o n� foi desconectado o jogador perdeu)
			// Remove o endere�o deste usu�rio da lista de usu�rios conectados
			} else if (sentence.charAt(0) == 'D') {
				
				System.out.println("[GAMESERVER] Recebida mensagem DEADSHIP de: " + sender);
				if (connectedUsers.contains(sender)) {
					connectedUsers.remove(sender);
					System.out.println("[GAMESERVER] Removido: " + sender);
				}
			}
			
		}
	}
}