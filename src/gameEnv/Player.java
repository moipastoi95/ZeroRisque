package gameEnv;

import java.util.ArrayList;
import java.util.Iterator;

public class Player {
	
	private String color;
	private ArrayList<Tile> tiles;
	private Game mGame;

	/**
	 * Main constructor
	 * @param color : String of the color
	 * @param game : ref to the game object
	 * */
	public Player(String color, Game game) {
		this.color = color;
		this.tiles = new ArrayList<>();
		this.mGame = game;
	}
	
	/**
	 * Add a tile to the list of the player's tiles
	 * @param tile : the new tile
	 * @author Vincent
	 */
	public void addTile(Tile tile) {
		tiles.add(tile);
	}
	
	/**
	 * Remove a tile from the list of the player's tiles
	 * @param tile : the tile to remove
	 */
	public void removeTile(Tile tile) {
		tiles.remove(tile);
	}
	
	/**
	 * Remove a tile from the list of the player's tiles
	 * @param idTile : id of the tile to remove
	 */
	public void removeTile(int idTile) {
		boolean found = false;
		Iterator<Tile> i = this.tiles.iterator();
		while (!found && i.hasNext()) {
			if (i.next().getId() == idTile) {
				i.remove();
			}
		}
	}
	
	/**
	 * Get a list of tiles owned by the player
	 * @return the tiles
	 */
	public ArrayList<Tile> getTiles() {
		return this.tiles;
	}
	
	/**
	 * Get the color of the player
	 * @return the color (String)
	 */
	public String getColor() {
		return this.color;
	}

	@Override
	public String toString() {
		return "Player " + color;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Tile) {
			Player p = (Player) obj;
			return p.getColor() == this.getColor() && p.getTiles().equals(this.getTiles());
		}
		return false;
	}
	
	// function choose action

	
}