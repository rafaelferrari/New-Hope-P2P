// TODO: Explicar melhor parte de redes desta classe

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;

// Classe que atua como usuários online na representação local do jogo 
// Protocolo de Aplicação definido: Comandos A (addplayer), D (deadship) e W (who) são descritos na forma <comando>;
//									Comandos P (position) e F (fireball) são descritos como <comando>;<X>,<Y>:<orientacao>
//									Onde <comando> e <orientacao> têm 1 byte, <X> e <Y> podem ter quantidade de bytes variável 
									

public class Multiplayer {
	
	// Endereço IP e número de porta UDP do servidor de consulta (a serem passados como parâmetros)
	public static InetAddress gameserverIP;
	public static int gameserverUDP;
	
	// TODO: Analisar se estes sockets estão sendo bem utilizados - será que dá pra usar um só?
	// Sockets utilizados para transmissão de pacotes UDP
	// clientsock é o socket da porta que será monitorada para recepção de mensagens (port)
	// serversock é o socket utilizado para envio de dados (e recepção, no caso do servidor)
	public static DatagramSocket clientsock, serversock;
	public static final int port = 9876;
	public static final int sizepkg = 16;
	
	public static String mensagemRecebida;
	
	// Vetor com os endereços dos demais peers
	public static ArrayList<InetAddress> onlinePlayers;
	
	// Estrutura que mapeia cada endereço IP em uma Nave correspondente
	public static HashMap<InetAddress,Item> onlineShips;
	
	// Inicialização das variáveis do sistema
	public static void inicio(String servername, int serverport) throws UnknownHostException, SocketException {

		// Inicialização de IP/UDP do serrvidor de consulta
		gameserverIP = InetAddress.getByName(servername);
		gameserverUDP = serverport;

		// Inicialização dos sockets
		clientsock = new DatagramSocket(port);
		serversock = new DatagramSocket();
	}
	
	// Método para conexão com o Servidor fixo, onde é solicitada a lista de endereços online
	public static void connect2GameServer(int iniX, int iniY, short iniOR) throws IOException {
		byte[] sendData = new byte[2];
		byte[] receiveData = new byte[1024];
		InetAddress toSend;
		
		// Enviada uma mensagem WHO, que solicita ao servidor que envie os dados de sua tabela de endereços
		String cmd = "W";
		sendData = cmd.getBytes();
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, gameserverIP, gameserverUDP);
		System.out.println("[MULTIPLAYER] Enviada mensagem WHO ao servidor " + gameserverIP.toString() + "/" + gameserverUDP);
		serversock.send(sendPacket);
		
		// Aguarda a recepção de uma mensagem contendo os endereços IP de peers online
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		serversock.receive(receivePacket);
		String enderecos = new String(receivePacket.getData()), atual;
		System.out.println("[MULTIPLAYER] Recebido do servidor: " + enderecos);
		
		onlinePlayers = new ArrayList<InetAddress>();
		onlineShips = new HashMap<InetAddress,Item>();
		
		while (enderecos.contains(";")) {
			atual = enderecos.substring(0,enderecos.indexOf(';'));
			onlinePlayers.add(InetAddress.getByName(atual));
			
			// Envia mensagem para par pedindo que o inclua no seu jogo em sua posicao
			cmd = "A;"+iniX+","+iniY+":"+iniOR;
			sendData = cmd.getBytes();
			toSend = InetAddress.getByName(atual);
			sendPacket = new DatagramPacket(sendData, sendData.length, toSend, port);
			serversock.send(sendPacket);
			
			if (enderecos.indexOf(';')+1 >= enderecos.length()) {
				enderecos = "";
			} else {
				enderecos = enderecos.substring(enderecos.indexOf(';')+1);
			}	
			
			System.out.println("[MULTIPLAYER] Endereco adicionado: " + atual);
		}
		
		System.out.println("[MULTIPLAYER] A tabela de peers online foi adquirida do servidor " + gameserverIP.toString() + "/" + gameserverUDP + " com sucesso");
		
	}

	// Método envia mensagem para todos os peers online
	public static void enviarMensagem(String msg) {
		if (!onlinePlayers.isEmpty()) {
			byte[] sendData = new byte[sizepkg];
			sendData = msg.getBytes();
			DatagramPacket sendPacket;
			
			for (InetAddress i : onlinePlayers) {
				sendPacket = new DatagramPacket(sendData, sendData.length,i, port);
				try {
					serversock.send(sendPacket);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	// Reconhecimento das mensagens do protocolo definido recebidas pelo programa
	public static void reconhecerMensagens(DatagramPacket pacote) {		
		Thread t = new Thread(new Runnable() {           
            public void run() {
				String msg = new String(pacote.getData());
				char cmd;
				cmd = msg.charAt(0);
				
				int[] newpos = new int[2];
				short or;
				Item newitem;
				
				switch(cmd) {
				
				// Comando ADDPLAYER, atualiza posição de nova Nave e responde com a posição da Nave local
				case 'A':
					
					newpos[0] = Integer.parseInt(msg.substring(msg.indexOf(';')+1, msg.indexOf(',')));
					newpos[1] = Integer.parseInt(msg.substring(msg.indexOf(',')+1, msg.indexOf(':')));
					or = Short.parseShort(msg.substring(msg.indexOf(':')+1, msg.indexOf(':')+2));
					
					System.out.println("[MULTIPLAYER] Novo player em (" + newpos[0] + "," + newpos[1] + "): " + or);
					
					if (onlineShips.containsKey(pacote.getAddress())) {
						Engine.moveShip(onlineShips.get(pacote.getAddress()), newpos, or);
					} else {
						onlinePlayers.add(pacote.getAddress());
						newitem = new Item(newpos[0],newpos[1],or,Constants.ITEM_SHIP);
						onlineShips.put(pacote.getAddress(), newitem);
						Engine.addShip(newitem);				
					}
					
					enviarMensagem("P;"+MainAppFrame.getShipByID(MainAppFrame.myshipID).position[0]+","+MainAppFrame.getShipByID(MainAppFrame.myshipID).position[1]+":"+MainAppFrame.getShipByID(MainAppFrame.myshipID).orientation);
					
					break;
					
				// Comando POSITION, atualiza posição de uma Nave
				case 'P':
					newpos[0] = Integer.parseInt(msg.substring(msg.indexOf(';')+1, msg.indexOf(',')));
					newpos[1] = Integer.parseInt(msg.substring(msg.indexOf(',')+1, msg.indexOf(':')));
					or = Short.parseShort(msg.substring(msg.indexOf(':')+1, msg.indexOf(':')+2));
								
					if (onlineShips.containsKey(pacote.getAddress())) {
						System.out.println("[MULTIPLAYER] Solicitacao de movimento da Nave " + onlineShips.get(pacote.getAddress()).id + " no sentido " + or);
						Engine.moveShip(onlineShips.get(pacote.getAddress()), newpos, or);				
					} else {
						onlinePlayers.add(pacote.getAddress());
						newitem = new Item(newpos[0],newpos[1],or,Constants.ITEM_SHIP);
						onlineShips.put(pacote.getAddress(), newitem);
						Engine.addShip(newitem);
					}			
					break;
					
				// Comando FIREBALL, cria fireball na posição desejada
				case 'F':			
					newpos[0] = Integer.parseInt(msg.substring(msg.indexOf(';')+1, msg.indexOf(',')));
					newpos[1] = Integer.parseInt(msg.substring(msg.indexOf(',')+1, msg.indexOf(':')));
					or = Short.parseShort(msg.substring(msg.indexOf(':')+1, msg.indexOf(':')+2));
					System.out.println("[MULTIPLAYER] Solicitacao de fireball da nave " + onlineShips.get(pacote.getAddress()).id + " em (" + newpos[0] + "," + newpos[1] + ")");
					newitem = new Item(newpos[0],newpos[1],or,Constants.ITEM_FB);
					Engine.newFireball(newitem);
								
					break;
				
				// Comando DEADSHIP, avisa aos players que uma Nave foi removida do jogo
				case 'D':
					if (onlineShips.get(pacote.getAddress()) != null) {
						System.out.println("[MULTIPLAYER] Solicitada remocao da Nave " + onlineShips.get(pacote.getAddress()).id + " do jogo");
						onlinePlayers.remove(pacote.getAddress());
						
						Engine.deadShip(onlineShips.get(pacote.getAddress()));
						onlineShips.remove(pacote.getAddress());
					}			
					break;
					
				default:
					System.out.println("[MULTIPLAYER] Comando de protocolo nao reconhecido: " + cmd);
					break;
					
				}
            } 
        });
        t.start();
	}
	
	// Thread responsável por escutar, aguardando o recebimento de novas mensagens dos peers	
	public static void receberMensagens() {
		Thread t = new Thread(new Runnable() {           
            public void run() {
            	DatagramPacket pacote;
            	byte[] rcvData;
            	
            	while (true) {
            		rcvData = new byte[sizepkg];
            		pacote = new DatagramPacket(rcvData, rcvData.length);
            		try {
						clientsock.receive(pacote);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
            		
            		reconhecerMensagens(pacote);            		
            	}
            } 
        });
        t.start();
	}
}
