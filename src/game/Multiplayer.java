package game;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;

// Classe que atua como usu�rios online na representa��o local do jogo 
// Protocolo de Aplica��o definido: Comandos A (addplayer), D (deadship) e W (who) s�o descritos na forma <comando>;
//									Comandos P (position) e F (fireball) s�o descritos como <comando>;<X>,<Y>:<orientacao>
//									Onde <comando> e <orientacao> t�m 1 byte, <X> e <Y> podem ter quantidade de bytes vari�vel 
									

public class Multiplayer {
	
	// Endere�o IP e n�mero de porta UDP do servidor de consulta (a serem passados como par�metros)
	public static InetAddress gameserverIP;
	public static int gameserverUDP;
	
	// Sockets utilizados para transmiss�o de pacotes UDP
	// clientsock � o socket da porta que ser� monitorada para recep��o de mensagens (port)
	// serversock � o socket utilizado para envio de dados (e recep��o, no caso do servidor)
	public static DatagramSocket listensock, serversock;
	public static final int portPeers = 9876;
	public static final int sizepkg = 16;
	
	// Vari�vel de controle para as threads
	public static boolean running = true; 
	
	public static String mensagemRecebida;
	
	// Vetor com os endere�os dos demais peers
	public static ArrayList<InetAddress> onlinePlayers;
	
	// Estrutura que mapeia cada endere�o IP em uma Nave correspondente
	public static HashMap<InetAddress,Item> onlineShips;
	
	public static DatagramPacket pacote;
	
	// Inicializa��o das vari�veis do sistema
	public static void inicio(String servername, int serverport, int iniX, int iniY, short iniOR) throws IOException {

		// Inicializa��o de IP/UDP do serrvidor de consulta
		gameserverIP = InetAddress.getByName(servername);
		gameserverUDP = serverport;

		// Inicializa��o do socket para comunica��o entre pares
		listensock = new DatagramSocket(portPeers);
		
		// Inicializa��o do socket para comunica��o com o servidor de consulta
		serversock = new DatagramSocket();
		
		// Conex�o com o servidor de consulta fixo
		connect2GameServer(iniX, iniY, iniOR);
		
		// Inicia thread para receber mensagens
		receberMensagens();
	}
	
	// M�todo para desconectar-se e fechar sockets
	public static void sair() {
		running = false;
		
		try {
			Thread.sleep(50);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		listensock.close();
		serversock.close();
	}
	
	// M�todo para conex�o com o Servidor fixo, onde � solicitada a lista de endere�os online
	public static void connect2GameServer(int iniX, int iniY, short iniOR) throws IOException {
		byte[] sendData = new byte[2];
		byte[] receiveData = new byte[1024];
		InetAddress toSend;
		
		// Enviada uma mensagem WHO, que solicita ao servidor que envie os dados de sua tabela de endere�os
		String cmd = "W";
		sendData = cmd.getBytes();
		
		// Pacote � endere�ado ao servidor de consulta na porta padr�o que ele estar� recebendo, ambos pr�-definidos
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, gameserverIP, gameserverUDP);
		System.out.println("[MULTIPLAYER] Enviada mensagem WHO ao servidor " + gameserverIP.toString() + "/" + gameserverUDP);
		// Pacote  � enviado atrav�s de um socket definido pelo SO
		serversock.send(sendPacket);
		
		// Aguarda a recep��o de uma mensagem contendo os endere�os IP de peers online
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		// Pacote � recebido atrav�s do mesmo socket que usou para enviar a requisi�ao
		serversock.receive(receivePacket);
		
		String enderecos = new String(receivePacket.getData()), atual;
		System.out.println("[MULTIPLAYER] Recebido do servidor: " + enderecos);
		
		onlinePlayers = new ArrayList<InetAddress>();
		onlineShips = new HashMap<InetAddress,Item>();
		
		// Obt�m os v�rios endere�os poss�veis na mensagem do servidor
		while (enderecos.contains(";")) {
			atual = enderecos.substring(0,enderecos.indexOf(';'));
			onlinePlayers.add(InetAddress.getByName(atual));
			
			// Envia mensagem para par pedindo que o inclua no seu jogo em sua posicao
			cmd = "A;"+iniX+","+iniY+":"+iniOR;
			sendData = cmd.getBytes();
			toSend = InetAddress.getByName(atual);
			sendPacket = new DatagramPacket(sendData, sendData.length, toSend, portPeers);
			// Mensagem � enviada atrav�s do socket definido pelo SO para a porta UDP de comunica��o entre peers
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

	// M�todo envia mensagem para todos os peers online
	public static void enviarMensagem(String msg) {
		DatagramPacket sendPacket;

		byte[] sendData = new byte[sizepkg];
		sendData = msg.getBytes();
		
		// Caso a mensagem seja DEADSHIP, enviar c�pia para o servidor de consulta, para atualiza��o
		if (msg.startsWith("D")) {				
			// Envia uma mensagem D, que solicita ao servidor que envie os dados de sua tabela de endere�os
			sendPacket = new DatagramPacket(sendData, sendData.length, gameserverIP, gameserverUDP);
			System.out.println("[MULTIPLAYER] Enviada mensagem DEAD ao servidor " + gameserverIP.toString() + "/" + gameserverUDP);
			try {
				// Pacote enviado atrav�s do socket criado pelo SO para a porta UDP de comunica��o do servidor
				serversock.send(sendPacket);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		// Envia uma c�pia da mensagem para cada peer conectado
		if (!onlinePlayers.isEmpty()) {
			for (InetAddress i : onlinePlayers) {
				sendPacket = new DatagramPacket(sendData, sendData.length,i, portPeers);
				try {
					// Mensagens enviadas atrav�s do socket criado pelo SO para a porta UDP de comunica��o entre peers
					serversock.send(sendPacket);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	// Reconhecimento das mensagens do protocolo definido recebidas pelo programa
	public static void reconhecerMensagens(DatagramPacket packet) {	
		pacote = packet;
		Thread t = new Thread(new Runnable() {           
            public void run() {
				String msg = new String(pacote.getData());
				char cmd;
				cmd = msg.charAt(0);
				
				int[] newpos = new int[2];
				short or;
				Item newitem;
				
				switch(cmd) {
				
				// Comando ADDPLAYER, atualiza posi��o de nova Nave e responde com a posi��o da Nave local
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
					
					enviarMensagem("P;"+GUI.getShipByID(GUI.myshipID).position[0]+","+GUI.getShipByID(GUI.myshipID).position[1]+":"+GUI.getShipByID(GUI.myshipID).orientation);
					
					break;
					
				// Comando POSITION, atualiza posi��o de uma Nave
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
					
				// Comando FIREBALL, cria fireball na posi��o desejada
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
	
	// Thread respons�vel por escutar na porta UDP padr�o entre peers, aguardando o recebimento de novas mensagens	
	public static void receberMensagens() {
		Thread t = new Thread(new Runnable() {           
            public void run() {
            	DatagramPacket pacote;
            	byte[] rcvData;
            	
            	// Checa a cada 25ms se a thread ainda deve rodar
            	try {
					listensock.setSoTimeout(25);
				} catch (SocketException e1) {
					e1.printStackTrace();
				}
            	
            	while (running) {
            		rcvData = new byte[sizepkg];
            		pacote = new DatagramPacket(rcvData, rcvData.length);
            		
            		// Escuta o socket padr�o para mensagens enviadas pelos peers, e delega o recnhecimento das mensagens p/ outra thread
            		try {
						listensock.receive(pacote);
						reconhecerMensagens(pacote);
					} catch (IOException e) {
						if (!(e instanceof SocketTimeoutException)) {
							e.printStackTrace();
						}
					}            		
            	}
            } 
        });
        t.start();
	}
}
