import java.util.*;

/**
 * AStar class aims to find the shortest distance between two points via the
 * Manhattan distance heuristic
 * 
 * @author Yufan Zou
 * @see "https://en.wikipedia.org/wiki/A*_search_algorithm#Pseudocode" Wikipedia A* Search Algorithm
 * Pseudocode
 */
public class AStarSearch {
  private Coordinate start, goal;
  private Map<Coordinate, Character> map;
  private Map<Coordinate, Coordinate> origin;
  private Map<Coordinate, Integer> gScore;
  private Map<Coordinate, Integer> fScore;

  private boolean searchFinished;

  private final int INFINITY_COST = 9999999;

  /**
   * AStar class constructor.
   * 
   * @param map
   *          internal view of the map
   * @param start
   *          starting coordinate
   * @param goal
   *          goal coordinate
   */
  public AStarSearch(Map<Coordinate, Character> map, Coordinate start, Coordinate goal) {
    this.map = map;

    this.start = start;
    this.goal = goal;
    this.origin = new HashMap<>();

    this.gScore = new HashMap<>();
    this.fScore = new HashMap<>();

    this.searchFinished = false;
  }

  /**
   * An internal sortViaFScore class that helps order points according to their
   * fScores. Lower cost scores will have higher priority
   */
  private class sortViaFScore implements Comparator<Coordinate> {
    @Override
    public int compare(Coordinate one, Coordinate two) {

      if (fScore.get(one) == fScore.get(two)) {
        return 0;
      } else if (fScore.get(one) > fScore.get(two)) {
        return 1;
      } else {
        return -1;
      }

    }
  }

  /**
   * We begin our A* search for the shorest path with this method.
   * 
   * @param hasKey
   *          allows player to pass through doors and look beyond if we have key
   * @param hasAxe
   *          allows player to pass through trees and look beyond if we have axe
   */
  public void search(boolean hasKey, boolean hasAxe) {
    sortViaFScore fss = new sortViaFScore();
    PriorityQueue<Coordinate> openSet = new PriorityQueue<>(10, fss);

    Set<Coordinate> closedSet = new HashSet<>();

    // initially set every grid to infinity cost
    int y = State.MAX_SIZE;
    while (y >= -State.MAX_SIZE) {
      int x = -State.MAX_SIZE;
      while (x <= State.MAX_SIZE) {
        gScore.put(new Coordinate(x, y), INFINITY_COST);
        fScore.put(new Coordinate(x, y), INFINITY_COST);
        ++x;
      }
      --y;
    }

    gScore.put(start, 0);

    fScore.put(start, getManhattanDistance(start, goal));
    openSet.add(start); // add start to priority queue

    while (!openSet.isEmpty()) {
      Coordinate currTile = openSet.remove();

      // Check if current tile is the goal tile
      if (currTile.equals(goal)) {
        searchFinished = true;
        return;
      }

      openSet.remove(currTile);
      closedSet.add(currTile);

      // For each adjacent tile of currTile (neighbours)
      for (int i = 0; i < 4; i++) {
        int neighbourX = (int) currTile.getX();
        int neighbourY = (int) currTile.getY();

        if (i == 0) {
          // right tile
          neighbourX++;
        } else if (i == 1) {
          // left tile
          neighbourX--;
        } else if (i == 2) {
          // up tile
          neighbourY++;
        } else if (i == 3) {
          // down tile
          neighbourY--;
        }

        Coordinate neighbour = new Coordinate(neighbourX, neighbourY);

        // Check if closedSet contains neighbor
        if (closedSet.contains(neighbour))
          continue;

        char tile = map.get(neighbour);

        if (!State.isTilePassable(tile, hasKey, hasAxe))
          continue; // can not get to this tile

        int tentative_gScore = gScore.get(currTile) + 1;

        // ignore if not a shorter path
        if (tentative_gScore >= gScore.get(neighbour)) {
          continue;
        }

        // is shorter path, save this path
        origin.put(neighbour, currTile);
        gScore.put(neighbour, tentative_gScore);
        fScore.put(neighbour, tentative_gScore + getManhattanDistance(neighbour, goal));

        if (!openSet.contains(neighbour))
          openSet.add(neighbour);
      }
    }

    // Failed search, no path
    searchFinished = true;
  }

  /**
   * Returns the Manhattan distance between the two points
   * 
   * @param start
   *          starting point
   * @param goal
   *          goal that we want to reach
   * @return
   */
  private int getManhattanDistance(Coordinate start, Coordinate goal) {
    int absX = Math.abs((int) start.getX() - (int) goal.getX());
    int absY = Math.abs((int) start.getY() - (int) goal.getY());
    int absDistance = absX + absY;
    return absDistance;
  }

  /**
   * Returns the found path that is the shortest distance we found between the two
   * points
   * 
   * @return returns found path as a linked list from one node to another
   */
  public LinkedList<Coordinate> returnPath() {
    LinkedList<Coordinate> sequence = new LinkedList<>();

    for (Coordinate u = goal; origin.get(u) != null; u = origin.get(u)) {
      sequence.add(u);
    }

    return sequence;
  }
}
