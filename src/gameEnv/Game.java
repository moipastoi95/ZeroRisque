package gameEnv;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class Game {

	public Game() {
		// x players of less
		ArrayList<Player> players = new ArrayList<>();
		players.add(new Player("Blue", this));
		players.add(new Player("Red", this));

		// list of tiles
		ArrayList<Tile> tiles = new ArrayList<>();

		ArrayList<String> countries = new ArrayList<>();
		countries.add("Alaksa");

		HashMap<String, ArrayList<String>> neig = new HashMap<>();

		HashMap<String, Tile> countriesDict = new HashMap<>();
		int i = 1;
		for(String terr : countries) {
			countriesDict.put(terr, new Tile(i, terr));
			i++;
		}



		int i = 1;
		// North America
		Tile alaska = new Tile(i++, "Alaska");
		Tile alberta = new Tile(i++, "Alberta");
		Tile centralAmerica = new Tile(i++, "Central America");
		Tile easternUs = new Tile(i++, "Eastern US");
		Tile greenland = new Tile(i++, "Greenland");
		Tile northwestTerritory = new Tile(i++, "Northwest Territory");
		Tile ontario = new Tile(i++, "Ontario");
		Tile quebec = new Tile(i++, "Quebec");

		// South America
		Tile argentina = new Tile(i++, "Argentina");
		Tile brazil = new Tile(i++, "Brazil");
		Tile venezuela = new Tile(i++, "Venezuela");
		Tile peru = new Tile(i++, "Peru");

		// Europe


		// Africa

		// Asia

		// Australia

		// Link between continents


		// for each player --> deploy, attack, fortify
	}
	public static void main(String[] args) {
		// this main function will just start the game, with parameters (list of player, list of tiles)
		// the Game object will make players play, and restrict time for turn. It will ends by giving the winner
	}
}
