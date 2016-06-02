import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Random;
public class Multiplayer {
	
	// Nesta classe, as conexões são apenas de recepção de dados.
	// O envio de dados ocorre depois que são reconhecidos os comandos do teclado.
	// TODO: Definir classe que envia os dados
	
	// http://gamedevelopment.tutsplus.com/tutorials/building-a-peer-to-peer-multiplayer-networked-game--gamedev-10074
	// https://docs.oracle.com/javase/tutorial/networking/datagrams/clientServer.html
	// https://systembash.com/a-simple-java-udp-server-and-udp-client/
	// https://docs.oracle.com/javase/8/docs/api/java/net/DatagramSocket.html
	// https://docs.oracle.com/javase/tutorial/networking/sockets/readingWriting.html
	
	//  TODO: Aumentar quantidade de rivais simultâneos
	public InetAddress rival,gameserverIP;
	public int gameserverUDP;
	public Item rivalShip;
	
	public DatagramSocket clientsock, serversock;
	
	public String mensagemRecebida;
	
	public static final int port = 9876;
	public static final int sizepkg = 16;
	
	public static ArrayList<String> onlinePlayers;
	
	public Multiplayer (String servername, int serverport) throws UnknownHostException, SocketException {
		// TODO: Colocar um server TCP para dar o endereço do rival
		this.gameserverIP = InetAddress.getByName(servername);
		this.gameserverUDP = serverport;
		// TODO: Alterar endereço para o do rival em questão, não hard-code p/ localhost
//		this.rival = InetAddress.getLocalHost();
//		this.rival = InetAddress.getByName("192.168.25.69");
		this.clientsock = new DatagramSocket(port);
		this.serversock = new DatagramSocket();
	}
	
	public void connect2GameServer(int iniX, int iniY, short iniOR) throws IOException {
		byte[] sendData = new byte[2];
		byte[] receiveData = new byte[1024];
		String cmd = "W";
		sendData = cmd.getBytes();
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, gameserverIP, gameserverUDP);
		serversock.send(sendPacket);
		InetAddress toSend;
		
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		serversock.receive(receivePacket);
//		receiveData = receivePacket.getData();
		String enderecos = new String(receivePacket.getData()), atual;
//		String enderecos = receiveData.toString(), atual;
		System.out.println("Recebido do servidor: " + enderecos);
		
		onlinePlayers = new ArrayList<String>();
		
		while (enderecos.contains(";")) {
			atual = enderecos.substring(0,enderecos.indexOf(';'));
			onlinePlayers.add(atual);
			
			// TODO: Tirar isso daqui, debug only, criar lista
			rival = InetAddress.getByName(atual);
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
			
			System.out.println("Endereco adicionado: " + atual);
		}
		
		System.out.println("Conectado com sucesso.");
		
	}
	
	
	
	public void receberMensagens() {
		Thread t = new Thread(new Runnable() {           
            public void run() {
            	DatagramPacket pacote;
            	byte[] rcvData;
            	
            	// TODO: Condição de parada ou break forçado
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
	
	// TODO: Modificar isso para incluir alem da msg o destino
	public void enviarMensagem(String msg) {
		if (rival != null) {
			byte[] sendData = new byte[sizepkg];
			sendData = msg.getBytes();
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,rival, port);
			try {
				serversock.send(sendPacket);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void reconhecerMensagens(DatagramPacket pacote) {
		// TODO: Colocar coisas aqui numa thread, de modo a não bloquear receberMensagens() em atividades não-rede
		String msg = new String(pacote.getData());
		char cmd;
		System.out.println("Msg recebida: " + msg);
		cmd = msg.charAt(0);
		
		int[] newpos = new int[2];
		short or;
		Item fb;
		Random rand = new Random();
		
		switch(cmd) {
		case 'A':
			// TODO: Atualiza posição de novo Player e responde
			
			newpos[0] = Integer.parseInt(msg.substring(msg.indexOf(';')+1, msg.indexOf(',')));
			newpos[1] = Integer.parseInt(msg.substring(msg.indexOf(',')+1, msg.indexOf(':')));
			or = Short.parseShort(msg.substring(msg.indexOf(':')+1, msg.indexOf(':')+2));
			System.out.println("Parsing (" + newpos[0] + "," + newpos[1] + ") orient " + or);
			
			if(rivalShip != null) {				
				Engine.moveShip(rivalShip, newpos, or);
			} else {
				rival = pacote.getAddress();
				rivalShip = new Item(newpos[0],newpos[1],or,Constants.ITEM_SHIP,4444);
				Engine.addShip(rivalShip);
			}
			
			enviarMensagem("P;"+MainAppFrame.getShipByID(MainAppFrame.myshipID).position[0]+","+MainAppFrame.getShipByID(MainAppFrame.myshipID).position[1]+":"+MainAppFrame.getShipByID(MainAppFrame.myshipID).orientation);
			
			break;
		case 'P':
			// TODO: Atualiza posição do Player
			
			newpos[0] = Integer.parseInt(msg.substring(msg.indexOf(';')+1, msg.indexOf(',')));
			newpos[1] = Integer.parseInt(msg.substring(msg.indexOf(',')+1, msg.indexOf(':')));
			or = Short.parseShort(msg.substring(msg.indexOf(':')+1, msg.indexOf(':')+2));
			System.out.println("Parsing (" + newpos[0] + "," + newpos[1] + ") orient " + or + "\n");
			
			if(rivalShip != null) {				
				Engine.moveShip(rivalShip, newpos, or);
			} else {
				rivalShip = new Item(newpos[0],newpos[1],or,Constants.ITEM_SHIP,4444);
				Engine.addShip(rivalShip);
			}
			
			break;
		case 'F':
			// TODO: Cria Fireball
			
			newpos[0] = Integer.parseInt(msg.substring(msg.indexOf(';')+1, msg.indexOf(',')));
			newpos[1] = Integer.parseInt(msg.substring(msg.indexOf(',')+1, msg.indexOf(':')));
			or = Short.parseShort(msg.substring(msg.indexOf(':')+1, msg.indexOf(':')+2));
			System.out.println("Parsing (" + newpos[0] + "," + newpos[1] + ") orient " + or + "\n");
			fb = new Item(newpos[0],newpos[1],or,Constants.ITEM_FB,rand.nextInt(300)+1);
			Engine.newFireball(fb);
						
			break;
		case 'D':
			// TODO: Avisa que Player morreu
			Engine.deadShip(rivalShip);
			rivalShip = null;
			break;
		default:
			System.out.println("Nao reconhecido: " + cmd);
			break;
			
		}
	}
	
}
