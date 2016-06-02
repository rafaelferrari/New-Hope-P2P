// TODO: Verificar as variáveis globais necessárias
// TODO: Implementar controle de acesso às variáveis (mutex?)
// TODO: Implementar reconhecimento de teclas
// TODO: Calcular colisões
// TODO: Ajustar todas as threads necessárias para o programa
// TODO: Resolver como funcionará a parte de rede
// TODO: Implementar GUI decente
// TODO: Refatorar código para retirar variáveis globais estáticas
// TODO: Bug conhecido: tentar lancar uma fireball e andar no mesmo sentido ao mesmo tempo
// TODO: Causa #1 do bug acima: crash quando o player 1 morre
// TODO: Causa #2 do bug acima: se ele anda e atira, o tiro pega nele próprio

import java.util.ArrayList;

import javax.swing.ImageIcon;

public class Engine {
		
	public Engine() {
		Engine.presentItems = new ArrayList<Item>();
	}
	
	public static ArrayList<Item> presentItems;
	public static Object mutex_presentItems = new Object();
	public static Object mutex_tablechanged = new Object();
	public static boolean tablechanged;

	public static Item lastcolision;
	
	// Returns an ImageIcon, or null if the path was invalid.
	public ImageIcon createImageIcon(String path) {
	    java.net.URL imgURL = getClass().getResource(path);
	    if (imgURL != null) {
	        return new ImageIcon(imgURL);
	    } else {
	        System.err.println("Couldn't find file: " + path);
	        return null;
	    }
	}
	
	// Função que cria as naves
	public static void addShip(Item ship) {
		synchronized (Engine.mutex_presentItems) {
			presentItems.add(ship);
			synchronized (Engine.mutex_tablechanged) {
				Engine.tablechanged = true;
			}
		}
	}
	
	// Função que move as naves
	public static void moveShip(Item target, int[] newpos, short neworient) {
		synchronized (Engine.mutex_presentItems) {
			int targetIndex = Engine.presentItems.indexOf(target);
			target.position = newpos;
			target.orientation = neworient;
			Engine.presentItems.set(targetIndex, target);
			synchronized (Engine.mutex_tablechanged) {
				Engine.tablechanged = true;
			}
		}
	}
	
	// Função que cria as fireballs
	public static void newFireball(Item origin) {

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
		
		// Checa se local é válido
		if ((newpos[0] >= 0) && (newpos[0] < Constants.SIZE_V) && (newpos[1] >= 0) && (newpos[1] < Constants.SIZE_H)) {
			// Cria fireball
			// TODO: Integrar parametro Player3
			fb = new Item(newpos[0], newpos[1], shipOrientation ,Constants.ITEM_FB,0);
			
			// Adiciona fireball em presentItems
			synchronized (Engine.mutex_presentItems) {
				Engine.presentItems.add(fb);
				synchronized (Engine.mutex_tablechanged) {
					Engine.tablechanged = true;
				}
			}
			System.out.println("Fireball criada.");
		} else {
			System.out.println("Fireball nao criada.");
		}
	}
	
	// Função que remove ships do jogo
	public static void deadShip(Item shipToRemove) {
		synchronized (mutex_presentItems) {
			presentItems.remove(shipToRemove);
			synchronized (mutex_tablechanged) {
				tablechanged = true;
			}
		}
	}
	
	// Thread usada para calcular colisões
	public void calcColision() {
		Thread t = new Thread(new Runnable() {
            public void run() { 
            	Item[][] table;
            	int[] posicao; 
            	Item col1, col2;
            	
//            	ArrayList<Item> toremove = new ArrayList<Item>();
            	while(!presentItems.isEmpty()) {
//            		toremove.clear();
            		table = new Item[Constants.SIZE_V][Constants.SIZE_H];
    				synchronized (mutex_presentItems) {
    					for(Item it: presentItems) {
    						col1 = null;
    						col2 = null;
    						posicao = it.position;
    						
    						if (table[posicao[0]][posicao[1]] != null) {
    							// Colisão!!!
    							
    							col1 = table[posicao[0]][posicao[1]];
    							col2 = it;    							
    						}    						
    						table[posicao[0]][posicao[1]] = it;
    						
    						if (col1 != null) {
    							// Duas fireballs colidindo
    							if ((col1.type == Constants.ITEM_SHIP) && (col2.type == Constants.ITEM_FB)) {
//									toremove.add(col2);
//									toremove.add(col1);
									lastcolision = col1;
//									System.out.println("Colisao 1");
    							} else if ((col1.type == Constants.ITEM_FB) && (col2.type == Constants.ITEM_SHIP)) {
//    								toremove.add(col1);
//    								toremove.add(col2);
    								lastcolision = col2;
//    								System.out.println("Colisao 2");
    							}
    						}
    						
    					}
//    					presentItems.removeAll(toremove);
//        				if (!toremove.isEmpty()) {
//        					synchronized (mutex_tablechanged) {
//            					tablechanged = true;
//    						}
//        				}
    				}
            	}
    			// Verificar colisões
            	// Subtrair pontos de vida do jogador
            	// Caso cabivel, remover jogador do tabuleiro
            	// Remover fireball do tabuleiro
            }
		});
		t.start();
	}

	// Thread usada para mover as fireballs
	public void moveFireballs() {
		Thread t = new Thread(new Runnable() {           
            public void run() { 
            	int[] posicao, newposicao;
            	ArrayList<Item> toremove = new ArrayList<Item>();
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
        						
        			// Espera até ler novos estados
        			try {
        				// Velocidade da fireball: 10 cell/s
        				Thread.sleep(100);
        			} catch (InterruptedException e) {
        				e.printStackTrace();
        				return;
        			}
        		}
            } 
        });
        t.start();
	}
	

}
