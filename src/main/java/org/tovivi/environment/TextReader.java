package org.tovivi.environment;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

public class TextReader {

    final private String sep = ":";

    /**
     * Read the file that give information about continents to create the Hashmap of continents
     * The line format is "continent:bonus" where ":" is the separator
     * @param url : the url of the file in the project repository
     * @return The HasMap that stores the continents with their names as keys
     */
    public HashMap<String, Continent> readContinents(URL url) throws IOException, URISyntaxException {

        HashMap<String, Continent> res = new HashMap<>();
        File file = new File(url.toURI());

        // Creating an object of BufferedReader class
        BufferedReader br = new BufferedReader(new FileReader(file));

        // Declaring a string variable
        String str;
        // Condition holds true until there is character in a string
        while ((str = br.readLine()) != null){

            //Getting the two values separated by the "sep"
            String continent = str.substring(0,str.indexOf(sep));
            String bonus = str.substring(str.indexOf(sep)+1);

            //Creating the continent, with 0 tile and no occupier
            Continent c = new Continent(continent, (Integer.parseInt(bonus)),0);
            res.put(continent,c);
        }
        return res;
    }

    public LinkedList<LinkedList<Double>> readProba(URL url) throws IOException, URISyntaxException {

        LinkedList<LinkedList<Double>> res = new LinkedList<>();
        File file = new File(url.toURI());

        // Creating an object of BufferedReader class
        BufferedReader br = new BufferedReader(new FileReader(file));

        // Declaring a string variable
        String str;

        for(int i = 0; i<50; i++){
            res.add(new LinkedList<>());
            for(int j = 0; j<50; j++) {
                str = br.readLine();
                res.get(i).add(Double.valueOf(str));
            }
        }
        return res;
    }

    /**
     * Read the file that give information about countries to create the Hashmap of countries, and also update the nbTiles of continents
     * The line format is "continent:countries" where ":" is the separator
     * @param g : the game which stores the continents that will be updated and added to tiles
     * @param url : the url of the file in the project repository
     * @return The HasMap that stores the countries with their names as keys
     */
    public HashMap<String, Tile> readCountries(Game g, URL url) throws IOException, URISyntaxException {
        HashMap<String, Tile> res = new HashMap<>();
        File file = new File(url.toURI());

        // Creating an object of BufferedReader class
        BufferedReader br = new BufferedReader(new FileReader(file));

        // Declaring a string variable
        String str;
        // Condition holds true until there is character in a string
        while ((str = br.readLine()) != null){

            //Getting the two values separated by the "sep"
            String continent = str.substring(0,str.indexOf(sep));
            String country = str.substring(str.indexOf(sep)+1);

            //Finding the continent in the hashmap
            Continent c = g.getContinents().get(continent);
            if (c==null) {
                System.out.println("There is no continent named " + continent);
            }
            else {
                //Updating the nbTiles of the continent
                c.setNbTiles(c.getNbTiles()+1);
                //Creating the tile, with the right name and the continent
                Tile t = new Tile(country, c) ;
                res.put(t.getName(), t);
            }
        }
        return res;
    }

    /**
     * Read the file that give information about countries to create the Hashmap of countries, and also update the nbTiles of continents
     * The line format is "continent:countries" where ":" is the separator
     * @param g : the game which stores the continents that will be updated and added to tiles
     * @param url : the url of the file in the project repository
     * @return The HasMap that stores the countries with their names as keys
     */
    public Stack<Card> readCards(Game g, URL url) throws IOException, URISyntaxException {
        Stack<Card> res = new Stack<>();
        File file = new File(url.toURI());

        // Creating an object of BufferedReader class
        BufferedReader br = new BufferedReader(new FileReader(file));

        // Declaring a string variable
        String str;
        // Condition holds true until there is character in a string
        while ((str = br.readLine()) != null) {

            //Getting the two values separated by the "sep"
            String country = str.substring(0, str.indexOf(sep));
            String cardType = str.substring(str.indexOf(sep) + 1);

            //Finding the continent in the hashmap
            Tile t = g.getTiles().get(country);
            if (t == null) {
                System.out.println("There is no country named " + country);
            } else {
                // Creating the Card
                Card c = new Card(CardType.valueOf(cardType), t);
                res.add(c);
            }
        }
        res.add(new Card(CardType.JOKER, null)); res.add(new Card(CardType.JOKER, null));
        Collections.shuffle(res);
        return res;
    }

    /**
     * Read the file that give information about countries to update the neighbors of each tiles
     * The line format is "continent:countries" where ":" is the separator
     * @param g : the game which  will be updated according to the edges
     * @param url: the url of the file in the project repository
     */
    public void readEdges(Game g, URL url) throws IOException, URISyntaxException {
        File file = new File(url.toURI());

        // Creating an object of BufferedReader class
        BufferedReader br = new BufferedReader(new FileReader(file));

        // Declaring a string variable
        String str;
        // Condition holds true until there is character in a string
        while ((str = br.readLine()) != null){

            //Getting the two values separated by the "sep"
            String country1 = str.substring(0,str.indexOf(sep));
            String country2 = str.substring(str.indexOf(sep)+1);

            //Finding the countries in the hashmap
            Tile t1 = g.getTiles().get(country1);
            Tile t2 = g.getTiles().get(country2);
            if (t1==null) {
                System.out.println("There is no country named " + country1);
            }
            if (t2==null) {
                System.out.println("There is no country named " + country2);
            }
            else {
                t1.addNeighbor(t2);
                t2.addNeighbor(t1);
            }
        }
    }

    /**
     * Read the file that give information about countries to update the neighbors of each tiles
     * The line format is "continent:countries" where ":" is the separator
     * @param g : the game which  will be updated according to the edges
     * @param env_data : the String array of text files that are used to create the game
     */
    public void readAll(Game g, String[] env_data) {
        try {
            for (String s : env_data) {
                if (s.compareTo("continent-bonus")==0) {
                    g.setContinents(readContinents(TextReader.class.getResource(s))) ;
                }
                if (s.compareTo("continent-country")==0) {
                    g.setTiles(readCountries(g,TextReader.class.getResource(s))) ;
                }
                if (s.compareTo("country-neighbor")==0) {
                    readEdges(g,TextReader.class.getResource(s)) ;
                }
                if (s.compareTo("country-card")==0) {
                    g.setTheStack(readCards(g,TextReader.class.getResource(s)));
                }
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
