package game;
// Classe respons�vel por encapsular as informa��es sobre os itens no campo
// No momento,itens podem ser SHIP ou FIREBALL

public class Item {
	
	public Item(int x, int y, short orientation, boolean type) {
		int[] p = {x, y};
		this.position = p;
		this.orientation = orientation;
		this.type = type;
	}
	// Posi��o no mapa {linha, coluna}
	public int[] position;
	
	// Orienta��o (UP, DOWN, LEFT, RIGHT)
	public short orientation;
	
	// Tipo (SHIP ou FB)
	public boolean type;
	
	// Identificador �nico do item
	public int id;
	
}
