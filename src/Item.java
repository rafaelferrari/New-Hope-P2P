
public class Item {
	
	public Item(int x, int y, short orientation, boolean type, int id) {
		int[] p = {x, y};
		this.position = p;
		this.orientation = orientation;
		this.type = type;
		this.id = id;
	}
	
	public int[] position;
	public short orientation;
	public boolean type;
	public int id;
	
}
