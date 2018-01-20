package solution;

import battleship.BattleShip;
import battleship.CellState;
import java.awt.Point;
import java.util.Random;

/**
 * Starting code for Comp10152 - Lab#6
 *
 * @author mark.yendt
 *
 * Lab 6: Christian Romar Paul Serad 000395777 A battleship game simulation to
 * test ai performance
 */
public class COMP10152_Lab6 {

    static final int NUMBEROFGAMES = 10000; // The number of games to be played

    /**
     * This method plays the battleship game automatically according to how many
     * games
     */
    public static void startingSolution() {
        int totalShots = 0; // Total of shots for all game
        System.out.println(BattleShip.version());

        // Plays the game
        for (int game = 0; game < NUMBEROFGAMES; game++) {

            BattleShip battleShip = new BattleShip();
            SampleBot sampleBot = new SampleBot(battleShip);

            // Call SampleBot Fire randomly - You need to make this better!
            while (!battleShip.allSunk()) {
                sampleBot.fireShot();
            }

            int gameShots = battleShip.totalShotsTaken();
            totalShots += gameShots;
        }

        // Display the average number of shots
        System.out.printf("SampleBot - The Average # of Shots required in %d games to sink all Ships = %.2f\n", NUMBEROFGAMES, (double) totalShots / NUMBEROFGAMES);

    }

    /**
     * Starts the simulation
     *
     * @param args - none
     */
    public static void main(String[] args) {
        startingSolution();   
    }

}
