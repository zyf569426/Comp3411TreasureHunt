import java.util.*;

/**
 * SpiralSeek class uses the spiral seek algorithm from any given center point
 * to work itself outwards to traverse the map.
 * 
 * @author Yufan Zou
 */
public class SpiralSeek {
  private final Coordinate start;
  private final Map<Coordinate, Character> map;

  // These are the coordinates of the tiles surrounding the player in the
  // given view array
  private final List offsets = Arrays.asList(new Coordinate(0, -2), new Coordinate(0, -1), new Coordinate(0, 1),
      new Coordinate(0, 2), new Coordinate(1, -2), new Coordinate(1, -1), new Coordinate(1, 0), new Coordinate(1, 1),
      new Coordinate(1, 2), new Coordinate(2, -2), new Coordinate(2, -1), new Coordinate(2, 0), new Coordinate(2, 1),
      new Coordinate(2, 2), new Coordinate(-1, -2), new Coordinate(-1, -1), new Coordinate(-1, 0),
      new Coordinate(-1, 1), new Coordinate(-1, 2), new Coordinate(-2, -2), new Coordinate(-2, -1),
      new Coordinate(-2, 0), new Coordinate(-2, 1), new Coordinate(-2, 2));

  /**
   * SpiralSeek class constructor.
   * 
   * @param map
   *          internal view of the map.
   * @param start
   *          starting position of spiral.
   */
  public SpiralSeek(Map<Coordinate, Character> map, Coordinate start) {
    this.map = map;
    this.start = start;
  }

  /**
   * getTile will find a coordinate that will give us new insight into the game
   * board condition.
   * 
   * @param hasKey
   *          boolean value of if we current have key
   * @param hasAxe
   *          boolean value of if we current have axe
   * @return returns a coordinate of the point that is both reachable and will
   *         give new insight into the board
   */
  public Coordinate getTile(boolean hasKey, boolean hasAxe) {
    int x = 0;
    int y = 0;
    int dx = 0;
    int dy = -1;

    int maxX = State.MAX_SIZE + State.MAX_SIZE;
    int maxY = State.MAX_SIZE + State.MAX_SIZE;
    int maxB = State.MAX_SIZE * State.MAX_SIZE + 1;

    int counter = 0;

    while (counter < maxB) {
      if (x >= (-maxX / 2) && (x <= maxX / 2) && (y >= -maxY / 2) && (y <= maxY / 2)) {
        Coordinate newTile = new Coordinate(x + (int) start.getX(), y + (int) start.getY());

        // Ignore center
        if (!newTile.equals(start)) {
          if (map.get(newTile) != null) {
            char newTileType = map.get(newTile);

            // Can be passed
            if (State.isTilePassable(newTileType, hasKey, hasAxe)) {
              if (checkUnknown(newTile)) {
                FloodFillSearch newFloodFill = new FloodFillSearch(map, start, newTile);

                if (newFloodFill.canReach(hasKey, hasAxe)) {
                  // Tile acceptable
                  return newTile;
                }
              }
            }
          }
        }
      }

      // Update dx,dy if end of spirals straight line path
      if ((x == y) || ((x < 0) && (x == -y)) || ((x > 0) && (x == 1 - y))) {
        int tmp = dx;
        dx = -dy;
        dy = tmp;
      }

      x += dx;
      y += dy;

      ++counter;
    }

    return start;
  }

  /**
   * Helps determine if the coordinate that we are looking at will provide new
   * information to the internal board. This is determined by the existence of
   * unknown coordinates.
   * 
   * @param point
   *          the coordinate point that we want to inspect around
   * @return returns boolean value of if the position is valuable
   */
  private boolean checkUnknown(Coordinate point) {
    for (Object a : offsets) {
      Coordinate offset = (Coordinate) a;
      Coordinate neighbourCheck = new Coordinate(point.getX() + offset.getX(), point.getY() + offset.getY());

      if (map.get(neighbourCheck) != null) {
        char neighbourCheckType = map.get(neighbourCheck);

        if (neighbourCheckType == State.COORDINATE_UNKNOWN)
          return true;
      }
    }
    return false;
  }
}
