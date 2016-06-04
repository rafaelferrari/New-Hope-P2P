package game;
// Classe responsável por encapsular as informações sobre os itens no campo
// No momento,itens podem ser SHIP ou FIREBALL

public class Item {
	
	public Item(int x, int y, short orientation, boolean type) {
		int[] p = {x, y};
		this.position = p;
		this.orientation = orientation;
		this.type = type;
	}
	// Posição no mapa {linha, coluna}
	public int[] position;
	
	// Orientação (UP, DOWN, LEFT, RIGHT)
	public short orientation;
	
	// Tipo (SHIP ou FB)
	public boolean type;
	
	// Identificador único do item
	public int id;
	
}
