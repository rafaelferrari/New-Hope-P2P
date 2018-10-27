package game;
// Classe respons�vel por encapsular as informa��es sobre os itens no campo
// No momento,itens podem ser SHIP ou FIREBALL

class Item {
	
	Item(int x, int y, short orientation, boolean type) {
		this.position = new int[]{x, y};
		this.orientation = orientation;
		this.type = type;
	}
	// Posi��o no mapa {linha, coluna}
	int[] position;
	
	// Orienta��o (UP, DOWN, LEFT, RIGHT)
	short orientation;
	
	// Tipo (SHIP ou FB)
	boolean type;
	
	// Identificador �nico do item
	int id;
	
}
