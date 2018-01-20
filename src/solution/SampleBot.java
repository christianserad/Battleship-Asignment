package solution;

import battleship.BattleShip;
import battleship.CellState;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Stack;

/**
 * A Sample random shooter - Takes no precaution on double shooting and has no
 * strategy once a ship is hit.
 *
 * @author mark.yendt 
 * Modified by: Christian Romar Paul Serad
 */
public class SampleBot {

    private int gameSize;                    // The size of the game
    private BattleShip battleShip;           // The current battleship game
    private CellState[][] map;               // Used to determine if the position not yet shot
    private Stack<Point> potentialHitStack;  // Used for sinking a ship when hit
    private int numberOfShipsSunk;           // Determines the number of ship sunk
    private int sinkModeShotCount;           // Counts the number of shot when sinking a ship
    int lastX = -1;                          // The last hit x coordinate
    int lastY = -1;                          // The last hit y coordinate
    int numberHitsToSunk = -1;               // The number of hits when a single ship sunk
    int shipsSunked = 0;                     // It only counts number of ships sunk while in sink mode (neglects record when in hunt mode)
    private ArrayList<Point> prevShotList = new ArrayList<Point>();  // Recording the prevoius hit 
    private ArrayList<Integer> currentShips = new ArrayList<Integer>(); // Determines the current ship that has not sunk yet

    /**
     * Constructor keeps a copy of the BattleShip instance and instantiate
     * instance variables
     *
     * @param b previously created battleship instance - should be a new game
     */
    public SampleBot(BattleShip b) {
        battleShip = b;
        gameSize = b.BOARDSIZE;
        numberOfShipsSunk = b.numberOfShipsSunk();
        potentialHitStack = new Stack<>();
        sinkModeShotCount = 0;

        // Sets every position into empty or not shot yet
        map = new CellState[gameSize][gameSize];
        for (int x = 0; x < gameSize; x++) {
            for (int y = 0; y < gameSize; y++) {
                map[x][y] = CellState.Empty;
            }
        }

        // Getting or record all ships
        currentShips.add(2);
        currentShips.add(3);
        currentShips.add(3);
        currentShips.add(4);
        currentShips.add(5);
    }

    /**
     * Create a random shot and calls the battleship shoot method
     *
     * @return true if a Ship is hit, false otherwise
     */
    public boolean fireShot() {
        Point shot = null;

        if (potentialHitStack.isEmpty()) {
            // HUNT MODE

            // Removes the ship matched (previously recorded) when sink mode is done
            if (numberHitsToSunk == sinkModeShotCount) {
                currentShips.remove(new Integer(numberHitsToSunk));
                numberHitsToSunk = -1;
            }

            // Default values when on hunt mode
            shipsSunked = 0;
            prevShotList.clear();
            lastX = -1;
            lastY = -1;
            sinkModeShotCount = 0;

            int greatestPoints = 0; // Biggest probability hit throughout cell search

            // Locating the highest probability hit
            for (int yRow = 0; yRow < gameSize; yRow++) {
                for (int xCol = 0; xCol < gameSize; xCol++) {
                    if (map[xCol][yRow] == CellState.Empty) {
                        // Calculating probability on current cell
                        int points = probabilityShot(xCol, yRow);

                        // Updates if current cell has the highest point so far
                        if (points >= greatestPoints) {
                            greatestPoints = points;
                            shot = new Point(xCol, yRow);
                        }
                    }
                }
            }

        } else {
            // SINK MODE
            shot = potentialHitStack.pop();
        }

        boolean hit = battleShip.shoot(shot);  // Shoots the target

        if (hit) {
            prevShotList.add(shot);  // Recording the hit shot

            map[shot.x][shot.y] = CellState.Hit;  // Marks the target as hit

            sinkModeShotCount++;  // Counting shot hits in sink mode

            if (numberOfShipsSunk != battleShip.numberOfShipsSunk()) {
                // Minimizing shots when a ship has sunk (Usually clearing the stack)
                // Checks what ship has sunk

                shipsSunked++; // Counts or record number of ships sunk while in sink mode
                int minimumShip = Collections.min(currentShips);

                if (sinkModeShotCount == 2) {
                    // When recorded 2 shots when a ship sunked (clear all possible shots and switch to hunt mode)
                    potentialHitStack.clear();
                    sinkModeShotCount = 0;
                    currentShips.remove(new Integer(2));
                } else if (sinkModeShotCount == 3) {
                    if (sinkModeShotCount == minimumShip) {
                        // Clear all possible shots and switch to hunt mode when recorded 3 shots and smallest ship left is 3
                        potentialHitStack.clear();
                        sinkModeShotCount = 0;
                        currentShips.remove(new Integer(3));
                    } else {
                        // Shoot around first hit
                        potentialHitStack.clear();
                        targetAround(prevShotList.get(0));
                        numberHitsToSunk = 3;
                    }
                } else if (sinkModeShotCount == 4) {
                    if (sinkModeShotCount == minimumShip) {
                        // Clear all possible shots and switch to hunt mode when recorded 4 shots and smallest ship left is 4
                        potentialHitStack.clear();
                        sinkModeShotCount = 0;
                        currentShips.remove(new Integer(4));
                    } else if (minimumShip == 3) {
                        // Shoot around first hit
                        potentialHitStack.clear();
                        targetAround(prevShotList.get(0));
                        numberHitsToSunk = 4;
                    } else if (minimumShip == 2) {
                        // Shoot around first 2 hits 
                        potentialHitStack.clear();
                        targetAround(prevShotList.get(1));
                        targetAround(prevShotList.get(0));
                        numberHitsToSunk = 4;
                    }
                } else if (sinkModeShotCount == 5 && shipsSunked == 1) {
                    if (sinkModeShotCount == minimumShip) {
                        // Clear all possible shots and switch to hunt mode when recorded 5 shots and smallest ship left is 5
                        potentialHitStack.clear();
                        sinkModeShotCount = 0;
                        currentShips.remove(new Integer(5));
                    } else if (minimumShip == 4) {
                        // Shoot around first hit
                        potentialHitStack.clear();
                        targetAround(prevShotList.get(0));
                        numberHitsToSunk = 5;
                    } else if (minimumShip == 3) {
                        // Shoot around first 2 hit
                        potentialHitStack.clear();
                        targetAround(prevShotList.get(1));
                        targetAround(prevShotList.get(0));
                        numberHitsToSunk = 5;
                    } else if (minimumShip == 2) {
                        // Shoot around first 3 hit
                        potentialHitStack.clear();
                        targetAround(prevShotList.get(2));
                        targetAround(prevShotList.get(1));
                        targetAround(prevShotList.get(0));
                        numberHitsToSunk = 5;
                    }
                } else if (sinkModeShotCount == 5 && shipsSunked == 2) {
                    // Clear all possible shots and switch to hunt mode when recorded 5 shots and 2 sunk ships (which sunk 2 and 3 ship)
                    potentialHitStack.clear();
                    sinkModeShotCount = 0;
                    currentShips.remove(new Integer(2));
                    currentShips.remove(new Integer(3));
                } else if (sinkModeShotCount == 7 && shipsSunked == 2) {
                    if (minimumShip == 3) {
                        // Clear all possible shots and switch to hunt mode when recorded 7 shots and 2 sunk ships when smalles ship is 3(which sunk 3 and 4 ship)
                        potentialHitStack.clear();
                        sinkModeShotCount = 0;
                        currentShips.remove(new Integer(3));
                        currentShips.remove(new Integer(4));
                    }
                }

                numberOfShipsSunk = battleShip.numberOfShipsSunk();

            } else {
                if (lastX >= 0) {
                    // This will allow straight shooting ()
                    // Effective when sunk a ship while shooting straight otherwise it will shoot the remaining stack

                    if (lastX - shot.x == -1 && lastY - shot.y == 0) {
                        // Shoots to the right
                        if (shot.x - 1 >= 0 && map[shot.x - 1][shot.y] == CellState.Empty) {
                            potentialHitStack.push(new Point(shot.x - 1, shot.y));
                        }
                        if (shot.y - 1 >= 0 && map[shot.x][shot.y - 1] == CellState.Empty) {
                            potentialHitStack.push(new Point(shot.x, shot.y - 1));
                        }
                        if (shot.y + 1 < gameSize && map[shot.x][shot.y + 1] == CellState.Empty) {
                            potentialHitStack.push(new Point(shot.x, shot.y + 1));
                        }
                        if (shot.x + 1 < gameSize && map[shot.x + 1][shot.y] == CellState.Empty) {
                            potentialHitStack.push(new Point(shot.x + 1, shot.y));
                        }
                    } else if (lastX - shot.x == 1 && lastY - shot.y == 0) {
                        // Shoots to the left
                        if (shot.x + 1 < gameSize && map[shot.x + 1][shot.y] == CellState.Empty) {
                            potentialHitStack.push(new Point(shot.x + 1, shot.y));
                        }
                        if (shot.y - 1 >= 0 && map[shot.x][shot.y - 1] == CellState.Empty) {
                            potentialHitStack.push(new Point(shot.x, shot.y - 1));
                        }
                        if (shot.y + 1 < gameSize && map[shot.x][shot.y + 1] == CellState.Empty) {
                            potentialHitStack.push(new Point(shot.x, shot.y + 1));
                        }
                        if (shot.x - 1 >= 0 && map[shot.x - 1][shot.y] == CellState.Empty) {
                            potentialHitStack.push(new Point(shot.x - 1, shot.y));
                        }
                    } else if (lastY - shot.y == -1 && lastX - shot.x == 0) {
                        // Shoots to the bottom
                        if (shot.x - 1 >= 0 && map[shot.x - 1][shot.y] == CellState.Empty) {
                            potentialHitStack.push(new Point(shot.x - 1, shot.y));
                        }
                        if (shot.x + 1 < gameSize && map[shot.x + 1][shot.y] == CellState.Empty) {
                            potentialHitStack.push(new Point(shot.x + 1, shot.y));
                        }
                        if (shot.y - 1 >= 0 && map[shot.x][shot.y - 1] == CellState.Empty) {
                            potentialHitStack.push(new Point(shot.x, shot.y - 1));
                        }
                        if (shot.y + 1 < gameSize && map[shot.x][shot.y + 1] == CellState.Empty) {
                            potentialHitStack.push(new Point(shot.x, shot.y + 1));
                        }

                    } else if (lastY - shot.y == 1 && lastY - shot.y == 0) {
                        // Shoots to the top
                        if (shot.x - 1 >= 0 && map[shot.x - 1][shot.y] == CellState.Empty) {
                            potentialHitStack.push(new Point(shot.x - 1, shot.y));
                        }
                        if (shot.x + 1 < gameSize && map[shot.x + 1][shot.y] == CellState.Empty) {
                            potentialHitStack.push(new Point(shot.x + 1, shot.y));
                        }
                        if (shot.y + 1 < gameSize && map[shot.x][shot.y + 1] == CellState.Empty) {
                            potentialHitStack.push(new Point(shot.x, shot.y + 1));
                        }
                        if (shot.y - 1 >= 0 && map[shot.x][shot.y - 1] == CellState.Empty) {
                            potentialHitStack.push(new Point(shot.x, shot.y - 1));
                        }
                    } else {
                        // Happens when next hit didn't happen beside the last hit
                        targetAround(shot);
                    }

                } else {
                    // Surround shot traget on hit (happens when got a hit for the first time after hunt mode)
                    targetAround(shot);
                }

                // Recording last coordinate hist for STRAIGHT SHOOTING
                lastX = shot.x;
                lastY = shot.y;
            }
        } else {
            map[shot.x][shot.y] = CellState.Miss;  // Marks the target as miss
        }

        return hit;
    }

    /**
     * This method will compute probability points on specified coordinates
     *
     * @param x the x position
     * @param y the y position
     * @return the probability points
     */
    public int probabilityShot(int x, int y) {
        int xUPPoints = probabilityShotCheckX(x, y, 1, 0);
        int xDownPoints = probabilityShotCheckX(x, y, -1, 0);
        int yUPPoints = probabilityShotCheckY(x, y, 1, 0);
        int yDownPoints = probabilityShotCheckY(x, y, -1, 0);

        return xUPPoints + xDownPoints + yUPPoints + yDownPoints;
    }

    /**
     * This method counts probability on the x direction coordinate
     *
     * @param xCoordinate the x position
     * @param yCoordinate the y position
     * @param direction the x direction (-1 = left or 1 = right)
     * @param points the probability points
     * @return the probability count for x direction
     */
    public int probabilityShotCheckX(int xCoordinate, int yCoordinate, int direction, int points) {
        xCoordinate += direction;

        if ((xCoordinate >= 0 && xCoordinate < gameSize) && map[xCoordinate][yCoordinate] == CellState.Empty && points < 4) {
            points++;
            points = probabilityShotCheckX(xCoordinate, yCoordinate, direction, points);
        }

        return points;

    }

    /**
     * This method counts probability on the y direction coordinate
     *
     * @param xCoordinate the x position
     * @param yCoordinate the y position
     * @param direction the y direction (-1 = up or 1 = down)
     * @param points the probability points
     * @return the probability count for y direction
     */
    public int probabilityShotCheckY(int xCoordinate, int yCoordinate, int direction, int points) {
        yCoordinate += direction;

        if ((yCoordinate >= 0 && yCoordinate < gameSize) && map[xCoordinate][yCoordinate] == CellState.Empty && points < 4) {
            points++;
            points = probabilityShotCheckY(xCoordinate, yCoordinate, direction, points);
        }
        return points;
    }

    /**
     * The surround target or a specified position
     *
     * @param shot the position shot
     */
    public void targetAround(Point shot) {
        if (shot.x - 1 >= 0 && map[shot.x - 1][shot.y] == CellState.Empty) {
            potentialHitStack.push(new Point(shot.x - 1, shot.y));
        }
        if (shot.x + 1 < gameSize && map[shot.x + 1][shot.y] == CellState.Empty) {
            potentialHitStack.push(new Point(shot.x + 1, shot.y));
        }
        if (shot.y - 1 >= 0 && map[shot.x][shot.y - 1] == CellState.Empty) {
            potentialHitStack.push(new Point(shot.x, shot.y - 1));
        }
        if (shot.y + 1 < gameSize && map[shot.x][shot.y + 1] == CellState.Empty) {
            potentialHitStack.push(new Point(shot.x, shot.y + 1));
        }
    }

}
