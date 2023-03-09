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

		// for each player --> deploy, attack, fortify
	}
	public static void main(String[] args) {
		// this main function will just start the game, with parameters (list of player, list of tiles)
		// the Game object will make players play, and restrict time for turn. It will ends by giving the winner
	}
}
