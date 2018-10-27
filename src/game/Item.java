package game;
// Classe responsável por encapsular as informações sobre os itens no campo
// No momento,itens podem ser SHIP ou FIREBALL

class Item {
	
	Item(int x, int y, short orientation, boolean type) {
		this.position = new int[]{x, y};
		this.orientation = orientation;
		this.type = type;
	}
	// Posição no mapa {linha, coluna}
	int[] position;
	
	// Orientação (UP, DOWN, LEFT, RIGHT)
	short orientation;
	
	// Tipo (SHIP ou FB)
	boolean type;
	
	// Identificador único do item
	int id;
	
}
