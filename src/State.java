import java.util.*;
import java.awt.geom.Point2D;

/**
 * State class maintains an internal view of the game board via a Map and keeps
 * track of all the resources that we can find.
 * 
 * @author Yufan Zou
 * 
 */
public class State {
  // Direction that we are facing
  final static int UP = 0;
  final static int RIGHT = 1;
  final static int DOWN = 2;
  final static int LEFT = 3;

  // Direction characters for board representation
  final static char MAP_UP = '^';
  final static char MAP_DOWN = 'v';
  final static char MAP_LEFT = '<';
  final static char MAP_RIGHT = '>';

  final static char COORDINATE_UNKNOWN = '?';
  final static char COORDINATE_BOUNDARY = '.';

  final static char COORDINATE_TEMPORARY_WATER = '#'; // stepping stone will be placed here

  // Max size of the map
  final static int MAX_SIZE = 80;

  // The map itself
  private Map<Coordinate, Character> map;

  // Tools we currently have
  private boolean haveAxe;
  private boolean haveKey;
  private boolean haveTreasure;
  private int num_stones;
  private static int num_rafts;

  private int curX;
  private int curY;
  private int direction;
  private int totalNumMoves;

  private boolean treasureLocated;
  private Coordinate treasureCoordinate;
  private ArrayList<Coordinate> axeCoordinates;
  private ArrayList<Coordinate> keyCoordinates;
  private ArrayList<Coordinate> stoneCoordinates;
  private ArrayList<Coordinate> waterCoordinates;
  private ArrayList<Coordinate> spaceCoordinates;
  private ArrayList<Coordinate> treeCoordinates;

  /**
   * State class constructor.
   */
  public State() {
    this.haveAxe = false;
    this.haveKey = false;
    this.haveTreasure = false;
    this.num_stones = 0;
    this.num_rafts = 0;
    this.totalNumMoves = 0;

    // (0, 0) is origin point
    this.curX = 0;
    this.curY = 0;

    // Load map with unknowns first
    this.map = new HashMap<>();
    int x = -MAX_SIZE;
    while (x <= MAX_SIZE) {
      int y = -MAX_SIZE;
      while (y <= MAX_SIZE) {
        this.map.put(new Coordinate(x, y), COORDINATE_UNKNOWN);
        ++y;
      }
      ++x;
    }

    this.direction = UP;
    this.map.put(new Coordinate(0, 0), MAP_UP);

    this.treasureLocated = false;
    this.axeCoordinates = new ArrayList<>();
    this.keyCoordinates = new ArrayList<>();
    this.stoneCoordinates = new ArrayList<>();
    this.waterCoordinates = new ArrayList<>();
    this.spaceCoordinates = new ArrayList<>();
    this.treeCoordinates = new ArrayList<>();
  }

  /**
   * Updates the internal view of the board from the 5*5 view we are given as we
   * traverse the game board. It will delegate the board to another method if we
   * are not facing upwards to be rotated and then it will begin recording the
   * current state of the board and the items we have collected.
   * 
   * @param view
   *          the 5*5 view that we are given
   */
  public void updateViaView(char view[][]) {
    int numTimesToRotate = direction;

    for (int i = 0; i < numTimesToRotate; ++i) {
      view = rotateBoard(view);
    }

    for (int i = 0; i < 5; ++i) {
      for (int j = 0; j < 5; ++j) {
        char curTile = view[i][j];
        // If this is the players tile, show the correct directional character
        if (i == 2 && j == 2) {
          if (direction == UP) {
            curTile = MAP_UP;
          } else if (direction == DOWN) {
            curTile = MAP_DOWN;
          } else if (direction == RIGHT) {
            curTile = MAP_RIGHT;
          } else if (direction == LEFT) {
            curTile = MAP_LEFT;
          }
        }

        Coordinate newTile = new Coordinate(curX + (j - 2), curY + (2 - i));
        checkToolUsage(curTile, newTile);

        if (map.get(newTile) != null && map.get(newTile) == COORDINATE_TEMPORARY_WATER)
          continue;

        // Update map
        map.put(newTile, curTile);
      }
    }
  }

  /**
   * Updates the internal state of the game board via the move that is made.
   * 
   * @param move
   *          the move that is made represented by a char
   */
  public void updateViaMove(char move) {
    ++totalNumMoves;

    move = Character.toUpperCase(move);
    char nextTile;
    // turn left
    if (move == 'L') {
      if (direction == UP) {
        direction = LEFT;
      } else if (direction == DOWN) {
        direction = RIGHT;
      } else if (direction == LEFT) {
        direction = DOWN;
      } else if (direction == RIGHT) {
        direction = UP;
      }
      // turn right
    } else if (move == 'R') {
      if (direction == UP) {
        direction = RIGHT;
      } else if (direction == DOWN) {
        direction = LEFT;
      } else if (direction == LEFT) {
        direction = UP;
      } else if (direction == RIGHT) {
        direction = DOWN;
      }
      // move forward
    } else if (move == 'F') {
      Coordinate nextTilePoint = getFrontTile(new Coordinate(curX, curY), direction);
      nextTile = map.get(nextTilePoint);
      // next tile is tree and have axe
      if ((nextTile == 'T') && (haveAxe == true)) {
        if (treeCoordinates.contains(nextTilePoint))
          treeCoordinates.remove(nextTilePoint);

        ++num_rafts;
        // System.exit(0);
        // System.err.println("get raft" + num_rafts);
      }
      // next tile is water and placed stepping stone
      if (nextTile == '~' || nextTile == COORDINATE_TEMPORARY_WATER) {
        if (num_stones > 0) {
          --num_stones; // we will place a stone on the water
        }

        if (nextTile == COORDINATE_TEMPORARY_WATER) {
          map.put(nextTilePoint, 'O');
        }

        waterCoordinates.remove(nextTilePoint); // no longer water
      }
      // next tile is stepping stone
      if (nextTile == 'o') {
        if (stoneCoordinates.contains(nextTilePoint)) {
          stoneCoordinates.remove(nextTilePoint);
        }

        ++num_stones;
      } else if (nextTile == 'a') {
        // next tile is axe
        haveAxe = true;
      } else if (nextTile == 'k') {
        // next tile is key
        haveKey = true;
      } else if (nextTile == '$') {
        // next tile is treasure
        haveTreasure = true;
      }

      if (direction == UP) {
        curY++;
      } else if (direction == DOWN) {
        curY--;
      } else if (direction == LEFT) {
        curX--;
      } else if (direction == RIGHT) {
        curX++;
      }

    } else if (move == 'C') {
      // Update rafts count when chop down a tree
      Coordinate frontC = getFrontTile(new Coordinate(curX, curY));
      nextTile = map.get(frontC);
      if ((nextTile == 'T') && (haveAxe == true)) {
        if (treeCoordinates.contains(frontC)) {
          treeCoordinates.remove(frontC);
        }
        ++num_rafts; // update number of rafts we have
      }
    } else if (move == 'U') {
      // Unlock door
      Coordinate potentialDoor = getFrontTile(new Coordinate(curX, curY));
      if (haveKey && keyCoordinates.contains(potentialDoor)) {
        keyCoordinates.remove(potentialDoor);
      }
    }
  }

  /**
   * Checks the tile that we are currently looking at, and if it is of use/we need
   * to keep track of it, we will store it in their respective arrayList for later
   * use.
   * 
   * @param curTile
   *          the char representation of the current tile
   * @param newTile
   *          the coordinate of the tile that we are looking at
   */
  private void checkToolUsage(char curTile, Coordinate newTile) {
    if (curTile == '$' && !treasureLocated) {
      treasureCoordinate = newTile;
      treasureLocated = true;
    } else if (curTile == 'a' && !axeCoordinates.contains(newTile)) {
      axeCoordinates.add(newTile);
    } else if (curTile == 'k' && !keyCoordinates.contains(newTile)) {
      keyCoordinates.add(newTile);
    } else if (curTile == 'o' && !stoneCoordinates.contains(newTile)) {
      stoneCoordinates.add(newTile);
    } else if (curTile == '~' && !waterCoordinates.contains(newTile)) {
      waterCoordinates.add(newTile);
    } else if (curTile == ' ' && !spaceCoordinates.contains(newTile)) {
      spaceCoordinates.add(newTile);
    } else if (curTile == 'T' && !treeCoordinates.contains(newTile)) {
      treeCoordinates.add(newTile);
    }

  }

  /**
   * Internal method that rotates the view we are given depending on the direction
   * we are facing.
   * 
   * @param numRotate
   *          the number of rotations we have to make
   * @return returns a rotated board that is the right way up
   */
  private static char[][] rotateBoard(char[][] numRotate) {
    char[][] ret = new char[numRotate[0].length][numRotate.length];
    for (int r = 0; r < numRotate.length; r++) {
      for (int c = 0; c < numRotate[0].length; c++) {
        ret[c][numRotate.length - 1 - r] = numRotate[r][c];
      }
    }
    return ret;
  }

  /**
   * Will return a boolean value indicating if the tile can be crossed given the
   * tools that we have and the condition of the board.
   * 
   * @param tile
   *          char representation of the tile that we are inspecting
   * @param hasKey
   *          that we have a key if it is a door
   * @param hasAxe
   *          that we have an axe if it is a tree
   * @return boolean value to indicate if it can be crossed
   */
  public static boolean isTilePassable(char tile, boolean hasKey, boolean hasAxe) {
    if (tile == '-' && !hasKey) {
      return false;
    } else if (tile == 'T' && !hasAxe) {
      return false;
    } else if (tile == '~') {
      return false;
    } else if (tile == '*') {
      return false;
    } else if (tile == State.COORDINATE_UNKNOWN) {
      return false;
    } else if (tile == State.COORDINATE_BOUNDARY) {
      return false;
    } else {
      return true;
    }

  }

  /**
   * Will return the tile that is directly in front of our given position given
   * our coordinate and facing direction.
   * 
   * @param tile
   *          tile that we are currently standing on.
   * @return returns coordinate tile of the tile in front of us.
   */
  public Coordinate getFrontTile(Coordinate tile) {
    return getFrontTile(tile, direction);
  }

  /**
   * Will return the tile that is directly in front of our given position given
   * our coordinate and facing direction.
   * 
   * @param curDirection
   *          the direction that we are currently facing.
   * @param tile
   *          tile that we are currently standing on.
   * @return returns coordinate tile of the tile in front of us.
   */
  public Coordinate getFrontTile(Coordinate tile, int curDirection) {
    int nextX = (int) tile.getX();
    int nextY = (int) tile.getY();

    if (curDirection == UP) {
      nextY++;
    } else if (curDirection == DOWN) {
      nextY--;
    } else if (curDirection == LEFT) {
      nextX--;
    } else if (curDirection == RIGHT) {
      nextX++;
    }

    return new Coordinate(nextX, nextY);
  }

  /**
   * Indicates if we have the treasure
   * 
   * @return boolean value of if we have treasure
   */
  public boolean haveTreasure() {
    return haveTreasure;
  }

  /**
   * Indicates if we have the treasure on the internal view
   * 
   * @return boolean value of if we have treasure on the internal board
   */
  public boolean treasureLocated() {
    return treasureLocated;
  }

  /**
   * Returns the current location of the player
   * 
   * @return returns the player location as a coordinate
   */
  public Coordinate getPlayerCoordinate() {
    return new Coordinate(curX, curY);
  }

  /**
   * Gets the direction of the player
   * 
   * @return returns the direction the player is currently facing
   */
  public int getDirection() {
    return direction;
  }

  /**
   * Indicates if we have a key
   * 
   * @return boolean value of if we have a key
   */
  public boolean haveKey() {
    return haveKey;
  }

  /**
   * Indicates if we have an axe
   * 
   * @return boolean value of if we an axe
   */
  public boolean haveAxe() {
    return haveAxe;
  }

  /**
   * Returns the internal map view of the board
   * 
   * @return returns the internal state of the map as a Map
   */
  public Map<Coordinate, Character> getMap() {
    return map;
  }

  /**
   * Gets the coordinate location of the goal if it is avaliable
   * 
   * @return returns the location of the treasure as a Coordinate
   */
  public Coordinate getTreasureCoordinate() {
    return treasureCoordinate;
  }

  /**
   * Gets the locations of the keys that we can see on the internal
   * 
   * @return returns the location of the keys as a List
   */
  public List<Coordinate> getKeyCoordinates() {
    return keyCoordinates;
  }

  /**
   * Gets the locations of the axes that we can see on the internal
   * 
   * @return returns the location of the axes as a List
   */
  public List<Coordinate> getAxeCoordinates() {
    return axeCoordinates;
  }

  /**
   * Gets the locations of the stepping stones that we can see on the internal
   * 
   * @return returns the location of the stepping stones as a List
   */
  public List<Coordinate> getSSCoordinates() {
    return stoneCoordinates;
  }

  /**
   * Gets the locations of all the water tiles that we can see on the internal
   * 
   * @return returns the location of the water tiles as a List
   */
  public List<Coordinate> getWaterCoordinates() {
    return waterCoordinates;
  }

  /**
   * Gets the locations of all the empty space tiles that we can see on the
   * internal
   * 
   * @return returns the location of the empty space tiles as a List
   */
  public List<Coordinate> getSpaceCoordinates() {
    return spaceCoordinates;
  }

  /**
   * Returns the number of stepping stones that we have as an int
   * 
   * @return int of number of stepping stones we have
   */
  public int getNumSteppingStones() {
    return num_stones;
  }

  /**
   * Gets the locations of all the tree tiles that we can see on the internal
   * 
   * @return returns the location of the tree tiles as a List
   */
  public List<Coordinate> getTreeCoordinates() {
    return treeCoordinates;
  }

  /**
   * Returns the number of rafts that we have as an int
   * 
   * @return int of number of rafts we have
   */
  public static int getNumRafts() {
    return num_rafts;
  }

}
