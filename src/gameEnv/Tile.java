package gameEnv;

import java.util.ArrayList;

public class Tile {

	private int id;
	private String name;
	private Player occupier;
	private int numTroops;
	private ArrayList<Tile> neighbors;

	/**
	 * Main constructor. Create an empty tile.
	 * @param id : id of the Tile
	 * @param name : the name of the territory
	 */
	public Tile(int id, String name) {
		this.id = id;
		this.name = name;
		this.occupier = null;
		this.numTroops = 0;
		this.neighbors = new ArrayList<>();
	}
	
	/**
	 * Add a tile to the list of neighbors
	 * @param tile : the tile to add
	 */
	public void addNeighbor(Tile tile) {
		if (!neighbors.contains(tile)) {
			neighbors.add(tile);
		}
	}
	
	/**
	 * Put a player on a tile. Moreover, give the new player a ref to the tile and remove the ref from the other player.
	 * @param p : the player
	 * @param numTroops : the number of troops to put on it. Has to be greater or equal to 1
	 */
	public void setOccupier(Player p, int numTroops) {
		if (numTroops < 1) {
			p.addTile(this);
			if (this.occupier != null) {
				this.occupier.removeTile(this);
			}
			
			this.occupier = p;
			this.numTroops = numTroops;
		}
	}
	
	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Player getOccupier() {
		return occupier;
	}

	public int getNumTroops() {
		return numTroops;
	}

	public void setNumTroops(int numTroops) {
		if (numTroops >= 1) {
			this.numTroops = numTroops;
		}
	}

	public ArrayList<Tile> getNeighbors() {
		return neighbors;
	}

	public void setNeighbors(ArrayList<Tile> neighbors) {
		this.neighbors = neighbors;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Tile) {
			Tile t = (Tile) obj;
			return t.id == this.id && t.name == this.name && t.neighbors.equals(this.neighbors);
		}
		return false;
	}

	@Override
	public String toString() {
		return "The tile " + this.name + " is occupied by " + this.occupier + " with " + this.numTroops + " troops.";
	}
}
