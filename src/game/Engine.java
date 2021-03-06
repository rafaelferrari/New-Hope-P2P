package game;

import java.util.ArrayList;
import java.util.Random;

// Classe respons�vel pelo funcionamento do jogo, sendo interfaceada tanto pelo usu�rio (entradas no teclado)
// quanto pela rede (mensagens recebidas pelos outros jogadores)

class Engine {
	
	// Var�avel indicando se efeitos sonoros est�o habilitados
	private static final boolean soundeffects = true;
	
	// Vetor com os itens presentes no mapa em um dado momento e um mutex para acesso a ele
	static ArrayList<Item> presentItems;
	static final Object mutex_presentItems = new Object();
	
	// Flag que indica se houve altera��o n�o resolvida no mapa e mutex para acesso a ela
	static boolean tablechanged;
	static final Object mutex_tablechanged = new Object();
	
	// Vetor com os IDs utilizados para os itens
	private static ArrayList<Integer> presentIDs;
	
	// Vari�vel contendo a �ltima nave a participar de uma colis�o
	static Item lastcolision;
	
	// Efeitos sonoros
	private static Sound soundBG = new Sound("/resources/sounds/background.wav");
	private static Sound soundSHOOT = new Sound("/resources/sounds/shoot.wav");
	private static Sound soundEXPLODE = new Sound("/resources/sounds/explosion.wav");
	private static Sound soundNEWSHIP = new Sound("/resources/sounds/newship.wav");
	
	// Vari�vel geradora de n�meros aleat�rios
	private static Random rand;
	
	// M�todo de inicializa��o
	static void iniciar() {
		Engine.presentItems = new ArrayList<>();
		Engine.presentIDs = new ArrayList<>();
		rand = new Random();		
		if (soundeffects) soundBG.loop();
	}
	
	// M�todo de execu��o da Engine
	static void executar() {
		moveFireballs();
		calcColision();
	}
	
	// Encontra um ID �nico para um item
	private static int findUniqueID() {
		int id = rand.nextInt(1000);
		while (presentIDs.contains(id)) {
			id = rand.nextInt(1000);
		}
		return id;
	}
	
	// Fun��o que inclui as naves no vetor (parte da interface)
	static Item addShip(Item ship) {
		synchronized (Engine.mutex_presentItems) {
			ship.id = findUniqueID();
			presentIDs.add(ship.id);
			presentItems.add(ship);
			synchronized (Engine.mutex_tablechanged) {
				Engine.tablechanged = true;
			}
		}
		soundNEWSHIP.play();
		System.out.println("[ENGINE] Nave "+ ship.id +" criada em (" + ship.position[0] + "," + ship.position[1] + "): " + ship.orientation);
		return ship;
	}
	
	// Fun��o que altera a posi��o de uma nave (parte da interface)
	static void moveShip(Item target, int[] newpos, short neworient) {
		synchronized (Engine.mutex_presentItems) {
			int targetIndex = Engine.presentItems.indexOf(target);
			target.position = newpos;
			target.orientation = neworient;
			Engine.presentItems.set(targetIndex, target);
			synchronized (Engine.mutex_tablechanged) {
				Engine.tablechanged = true;
			}
		}
		System.out.println("[ENGINE] Nave " + target.id + " moveu-se para (" + newpos[0] + "," + newpos[1] + "): " + target.orientation);
	}
	
	// Fun��o que cria a fireball de uma nave (parte da interface)
	static void newFireball(Item origin) {

		Item fb;
    	int[] newpos = {0,0};
    	
		short shipOrientation= origin.orientation;
		
		switch(shipOrientation) {
		case Constants.LEFT:	newpos[0] = origin.position[0];
					newpos[1] = origin.position[1]-1;
					break;
		case Constants.RIGHT:	newpos[0] = origin.position[0];
					newpos[1] = origin.position[1]+1;
					break;
		case Constants.UP:	newpos[0] = origin.position[0]-1;
					newpos[1] = origin.position[1];
					break;
		case Constants.DOWN:	newpos[0] = origin.position[0]+1;
					newpos[1] = origin.position[1];
					break;			
		}
		
		// Checa se local � v�lido
		if ((newpos[0] >= 0) && (newpos[0] < Constants.SIZE_V) && (newpos[1] >= 0) && (newpos[1] < Constants.SIZE_H)) {
			
			// Cria fireball
			fb = new Item(newpos[0], newpos[1], shipOrientation ,Constants.ITEM_FB);
			if (soundeffects) soundSHOOT.play();
			
			// Adiciona fireball em presentItems
			synchronized (Engine.mutex_presentItems) {
				fb.id = findUniqueID();
				Engine.presentItems.add(fb);	
				presentIDs.add(fb.id);
				synchronized (Engine.mutex_tablechanged) {
					Engine.tablechanged = true;
				}
			}
			System.out.println("[ENGINE] Fireball " + fb.id + " criada em (" + newpos[0] + "," + newpos[1] + "): " + shipOrientation);
		} else {
			System.out.println("[ENGINE] Fireball NAO criada em (" + newpos[0] + "," + newpos[1] + "): " + shipOrientation);
		}
	}
	
	// Fun��o que remove naves do jogo (parte da interface)
	static void deadShip(Item shipToRemove) {
		synchronized (mutex_presentItems) {
			presentItems.remove(shipToRemove);
			presentIDs.remove(Integer.valueOf(shipToRemove.id));
			synchronized (mutex_tablechanged) {
				tablechanged = true;
			}
		}
		if (soundeffects) soundEXPLODE.play();
		System.out.println("[ENGINE] Nave " + shipToRemove.id + " removida do jogo");
	}
	
	// Thread usada para calcular colis�es (usada internamente pela Engine)
	// Vari�vel global lastcolision cont�m ultima nave a colidir
	private static void calcColision() {
		Thread t = new Thread(() -> {
			Item[][] table;
			int[] posicao;
			Item col1, col2;

			while(!presentItems.isEmpty()) {
				table = new Item[Constants.SIZE_V][Constants.SIZE_H];
				synchronized (mutex_presentItems) {
					for(Item it: presentItems) {
						col1 = null;
						col2 = null;
						posicao = it.position;

						if (table[posicao[0]][posicao[1]] != null) {
							// Colis�o!!!

							col1 = table[posicao[0]][posicao[1]];
							col2 = it;
						}
						table[posicao[0]][posicao[1]] = it;

						if (col1 != null) {
							if ((col1.type == Constants.ITEM_SHIP) && (col2.type == Constants.ITEM_FB)) {
								if (lastcolision == null) {
									System.out.println("[ENGINE] Fireball " + col2.id + " colidiu com a Nave " + col1.id);
								} else if (lastcolision.id != col1.id) {
									System.out.println("[ENGINE] Fireball " + col2.id + " colidiu com a Nave " + col1.id);
								}
								lastcolision = col1;
							} else if ((col1.type == Constants.ITEM_FB) && (col2.type == Constants.ITEM_SHIP)) {
								if (lastcolision == null) {
									System.out.println("[ENGINE] Fireball "+ col1.id + " colidiu com a Nave " + col2.id);
								} else if (lastcolision.id != col2.id) {
									System.out.println("[ENGINE] Fireball " + col1.id + " colidiu com a Nave " + col2.id);
								}
								lastcolision = col2;
							}

						}
					}
				}
			}
		});
		t.start();
	}

	// Thread usada para mover as fireballs
	private static void moveFireballs() {
		Thread t = new Thread(() -> {
			int[] posicao, newposicao;
			ArrayList<Item> toremove = new ArrayList<>();
			short orientacao;

			while (!presentItems.isEmpty()) {
				toremove.clear();
				synchronized(mutex_presentItems) {
					for(Item i: presentItems) {
						if (i.type == Constants.ITEM_FB) {
							newposicao = i.position;
							posicao = i.position;
							orientacao = i.orientation;

							switch(orientacao) {
							case Constants.LEFT:	newposicao[1] = posicao[1] - 1;
													break;
							case Constants.RIGHT:	newposicao[1] = posicao[1] + 1;
													break;
							case Constants.UP:	newposicao[0] = posicao[0] - 1;
													break;
							case Constants.DOWN:	newposicao[0] = posicao[0] + 1;
													break;
							}

							// Fireball atingiu o fim do tabuleiro
							if ((newposicao[0] < 0) || (newposicao[0] == Constants.SIZE_V) || (newposicao[1] < 0) || (newposicao[1] == Constants.SIZE_H)) {
								toremove.add(i);
							} else {
								i.position = newposicao;
							}
							synchronized (mutex_tablechanged) {
								tablechanged = true;
							}
						}
					}

					presentItems.removeAll(toremove);

					if (!toremove.isEmpty()) {
						synchronized (mutex_tablechanged) {
							tablechanged = true;
						}
					}

				}

				// Espera at� ler novos estados
				try {
					// Velocidade da fireball: 10 cell/s
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
					return;
				}
			}
		});
        t.start();
	}
	

}
