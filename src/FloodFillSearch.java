import java.util.*;

/**
 * Implements our flood fill algorithm to get a clear view of the board for
 * internal to see what we are able to reach and boundaries.
 * 
 * @author Yufan Zou
 * @see href="https://en.wikipedia.org/wiki/Flood_fill#Alternative_implementations" Wikipedia - Flood Fill 
 * Pseudocode
 */
public class FloodFillSearch {
  private Coordinate start, goal;
  private Map<Coordinate, Character> map;

  /**
   * FloodFill class constructor.
   * 
   * @param map
   *          current internal map of the board
   * @param start
   *          starting point
   * @param goal
   *          goal point
   */
  public FloodFillSearch(Map<Coordinate, Character> map, Coordinate start, Coordinate goal) {
    this.map = map;
    this.start = start;
    this.goal = goal;
  }

  /**
   * Begins our reachability test on the internal map from the given Map and start
   * and goal positions
   * 
   * @param hasKey
   *          we have a key and we are able to use it on doors to look beyond
   *          their points
   * @param hasAxe
   *          we have an axe and we are able to use it on doors to look beyond
   *          their points
   * @return boolean value of whether or not the end point is reachable
   */
  public boolean canReach(boolean hasKey, boolean hasAxe) {
    Queue<Coordinate> q = new ArrayDeque<>();
    Set<Coordinate> isConnected = new HashSet<>();

    q.add(start);

    while (!q.isEmpty()) {
      Coordinate first = q.remove();

      if (map.get(first) == null)
        continue;

      char tile = map.get(first);

      if (!isConnected.contains(first)) {
        // Pass tiles that we cannot access
        if (!State.isTilePassable(tile, hasKey, hasAxe)) {
          continue;
        }

        // Set as done
        isConnected.add(first);

        // add nodes from up, down, left, right
        int i = 0;
        while (i < 4) {
          int neighbourX = (int) first.getX();
          int neighbourY = (int) first.getY();

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
          if (!isConnected.contains(neighbour))
            q.add(neighbour);

          i++;
        }
      }
    }

    return isConnected.contains(goal);
  }
}
