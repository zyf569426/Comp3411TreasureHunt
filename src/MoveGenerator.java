import java.util.*;

/**
 * MoveMaker class.
 *
 * Makes decisions about the next move (or moves) to make based on internal state (the map environment).
 * Functions by adding moves to a move queue which are carried out first before deciding what other moves to make.
 *
 */

/**
 * Main class determines what move we will make next based on our internal
 * state.
 * 
 * @author Yufan Zou
 */
public class MoveGenerator {
  private State state;
  private Queue<Character> currMoves;

  private boolean needKey;
  private boolean needAxe;
  private boolean needStone;
  private boolean needRaft;

  boolean canGetResource;
  boolean canGetKey;
  boolean canGetAxe;
  boolean canGetStone;
  boolean canReachArea;

  /**
   * MoveMaker class constructor
   */
  public MoveGenerator() {
    this.state = new State();
    this.currMoves = new LinkedList<>();

    this.needKey = false;
    this.needAxe = false;
    this.needStone = false;
    this.needRaft = false;

  }

  /**
   * Attempts to find the tool that we need to cross a point if we do not have it
   * currently.
   * 
   * @param toolCoordinates
   *          an ArrayList of where the tools are located
   * @param isToolAttainable
   *          if we can access the location of the tool
   */
  public void findTool(List<Coordinate> toolCoordinates, boolean isToolAttainable) {
    char TOOL = ' ';
    if (toolCoordinates.equals(state.getKeyCoordinates())) {
      canGetKey = false;
      TOOL = 'k';
    } else if (toolCoordinates.equals(state.getAxeCoordinates())) {
      canGetAxe = false;
      TOOL = 'a';
    } else if (toolCoordinates.equals(state.getSSCoordinates())) {
      canGetStone = false;
      TOOL = 'o';
    }

    for (Coordinate location : toolCoordinates) {
      // Sanity check
      if (state.getMap().get(location) == null || state.getMap().get(location) != TOOL) {
        continue;
      }

      FloodFillSearch newFloodFill = new FloodFillSearch(state.getMap(), state.getPlayerCoordinate(), location);
      if (newFloodFill.canReach(state.haveKey(), state.haveAxe())) {
        // Do A* traversal to location
        AStarToGoal(state.getPlayerCoordinate(), location, state.getDirection(), state.haveKey(), state.haveAxe());
        if (toolCoordinates.equals(state.getKeyCoordinates())) {
          canGetKey = true;
        } else if (toolCoordinates.equals(state.getAxeCoordinates())) {
          canGetAxe = true;
        } else if (toolCoordinates.equals(state.getSSCoordinates())) {
          canGetStone = true;
        } else if (toolCoordinates.equals(state.getSSCoordinates()))
          break;
      }
    }

  }

  /**
   * Reachability test on the tool that we need given the coordinates.
   * 
   * @param toolCoordinates
   *          list of locations in internal that tool is located
   */
  public void floodFillToGetTool(List<Coordinate> toolCoordinates) {
    for (Coordinate location : toolCoordinates) {
      FloodFillSearch newFloodFill = new FloodFillSearch(state.getMap(), state.getPlayerCoordinate(), location);

      if (newFloodFill.canReach(state.haveKey(), state.haveAxe())) {
        needKey = true;

        if (toolCoordinates.equals(state.getKeyCoordinates())) {
          needKey = true;
        } else if (toolCoordinates.equals(state.getAxeCoordinates())) {
          needAxe = true;
        } else if (toolCoordinates.equals(state.getSSCoordinates())) {
          needStone = true;
        }

        canGetResource = true;
        break;
      }
    }
  }

  /**
   * Attempt to use the stepping stone to jump over water locations
   * 
   * @param toolCoordinates
   *          list of locations in internal that tool is located
   */
  public void useSteppingStoneTo(List<Coordinate> toolCoordinates) {
    canReachArea = false;
    for (Coordinate location : toolCoordinates) {
      if (testWithStone(location)) {
        canReachArea = true;
        break;
      }
    }
  }

  /**
   * Key decision making class where it will return one move per loop to determine
   * what action to make
   * 
   * @param view
   *          view of the agent given by the agent class.
   * @return returns a char value of the chosen move
   */
  public char makeMove(char view[][]) {
    state.updateViaView(view);

    // if no pending moves, try to decide what to do next
    while (currMoves.isEmpty() == true) {

      // got treasure, go back to origin point
      // A* traversal to (0,0)
      if (state.haveTreasure()) {
        AStarToGoal(state.getPlayerCoordinate(), new Coordinate(0, 0), state.getDirection(), state.haveKey(),
            state.haveAxe());
        break;
      }

      // We don't have treasure but it can be seen
      if (state.treasureLocated() == true) {
        FloodFillSearch newFloodFill = new FloodFillSearch(state.getMap(), state.getPlayerCoordinate(),
            state.getTreasureCoordinate());

        // We can reach treasure
        if (newFloodFill.canReach(state.haveKey(), state.haveAxe())) {
          AStarToGoal(state.getPlayerCoordinate(), state.getTreasureCoordinate(), state.getDirection(), state.haveKey(),
              state.haveAxe());
          break;
        } else {
          if (!state.haveKey() && newFloodFill.canReach(true, state.haveAxe())) {
            needKey = true;
          }
          if (!state.haveAxe() && newFloodFill.canReach(state.haveKey(), true)) {
            needAxe = true;
          }
          if (!state.haveKey() && !state.haveAxe() && newFloodFill.canReach(true, true)) {
            needKey = true;
            needAxe = true;
          }
        }
      }
      
      // check if we need key and can get key
      if (needKey && !state.getKeyCoordinates().isEmpty()) {
        findTool(state.getKeyCoordinates(), canGetKey);
        if (canGetKey)
          break;
      }
      // check if we need axe and can get axe
      if (needAxe && !state.getAxeCoordinates().isEmpty()) {
        findTool(state.getAxeCoordinates(), canGetAxe);
        if (canGetAxe)
          break;
      }
      // check if we need stone and can get stone
      if (needStone && !state.getSSCoordinates().isEmpty()) {
        findTool(state.getSSCoordinates(), canGetStone);
        if (canGetStone)
          break;
      }
      
      // explore to get to new area
      SpiralSeek s = new SpiralSeek(state.getMap(), state.getPlayerCoordinate());
      Coordinate newHiddenTile = s.getTile(state.haveKey(), state.haveAxe());

      if (!newHiddenTile.equals(state.getPlayerCoordinate())) {
        AStarToGoal(state.getPlayerCoordinate(), newHiddenTile, state.getDirection(), state.haveKey(), state.haveAxe());
        break;
      }

      canGetResource = false;

      if (!needKey && !state.haveKey() && !state.getKeyCoordinates().isEmpty()) {
        // perform flood fill algorithm to get key
        floodFillToGetTool(state.getKeyCoordinates());
      }

      if (!needAxe && !state.haveAxe() && !state.getAxeCoordinates().isEmpty()) {
        // perform flood fill algorithm to get axe
        floodFillToGetTool(state.getAxeCoordinates());
      }

      if (!state.getSSCoordinates().isEmpty()) {
        // perform flood fill algorithm to get stepping stone
        floodFillToGetTool(state.getSSCoordinates());
      }

      if (!state.getTreeCoordinates().isEmpty()) {
        // perform flood fill algorithm to get to tree
        floodFillToGetTool(state.getTreeCoordinates());
      }

      if (canGetResource)
        continue;

      // Stage 7: Need to use our stepping stones to get to a new unreachable area
      // Note at this stage we have all resources that are reachable to us
      // So any tools we still see on the map are guaranteed to be unreachable
      // (without using stepping stones)

      // Try to get to the area near treasure
      if (state.treasureLocated()) {
        if (testWithStone(state.getTreasureCoordinate()))
          break;
      }

      // Try to get to the area near another stepping stone
      if (!state.getSSCoordinates().isEmpty()) {
        useSteppingStoneTo(state.getSSCoordinates());
        if (canReachArea) {
          break;
        }

      }

      // Try to get to the area near another key (don't prefer if we already have key)
      if (!state.getKeyCoordinates().isEmpty() && !state.haveKey()) {
        useSteppingStoneTo(state.getKeyCoordinates());
        if (canReachArea) {
          break;
        }
      }

      // Try to get to the area near another axe (don't prefer if we already have axe)
      if (!state.getAxeCoordinates().isEmpty() && !state.haveAxe()) {
        useSteppingStoneTo(state.getAxeCoordinates());
        if (canReachArea)
          break;
      }

      // Try to get to the area near another space
      if (!state.getSpaceCoordinates().isEmpty()) {
        boolean canReachArea = false;
        for (Coordinate location : state.getSpaceCoordinates()) {
          // Ensure this blank space is not reachable from our current player location
          FloodFillSearch newFloodFill = new FloodFillSearch(state.getMap(), state.getPlayerCoordinate(), location);

          if (!newFloodFill.canReach(state.haveKey(), state.haveAxe())) {
            if (testWithStone(location)) {
              canReachArea = true;
              break;
            }
          }
        }

        if (canReachArea) {
          break;
        }
      }

      AStarToGoal(state.getPlayerCoordinate(), new Coordinate(0, 0), state.getDirection(), state.haveKey(),
          state.haveAxe());

      break;
    }
    
    // try to get to another area
    if (!currMoves.isEmpty()) {
      char moveToMake = currMoves.remove();
      char nextTile = state.getMap().get(state.getFrontTile(state.getPlayerCoordinate()));

      if (moveToMake == 'F') {
        if (nextTile == 'a') {
          needAxe = false;
        } else if (nextTile == 'k') {
          needKey = false;
        }
      }

      state.updateViaMove(moveToMake);
      return moveToMake;
    }

    return 0;
  }

  /**
   * Performs A* movement to get from the current player position to the goal
   * coordinate in the shortest possible path.
   * 
   * @param start
   *          starting point
   * @param goal
   *          goal point
   * @param curDirection
   *          currently facing in what direction
   * @param hasKey
   *          boolean of if we have key
   * @param hasAxe
   *          boolean of if we have axe
   */
  private void AStarToGoal(Coordinate start, Coordinate goal, int curDirection, boolean hasKey, boolean hasAxe) {
    AStarSearch a = new AStarSearch(state.getMap(), start, goal);
    a.search(hasKey, hasAxe);

    // Get optimal path
    LinkedList<Coordinate> path = a.returnPath();
    path.addLast(start);

    // Reverse list
    for (int i = path.size() - 1; i >= 1; i--) {
      Coordinate curr = path.get(i);
      int directionHeaded = getAdjacentTileDirection(curr, path.get(i - 1));

      LinkedList<Character> alignMoves = getAlignmentMoves(curDirection, directionHeaded);

      currMoves.addAll(alignMoves);

      curDirection = directionHeaded;

      char nextTile = state.getMap().get(state.getFrontTile(curr, curDirection));
      if (nextTile == 'T') {
        currMoves.add('C');
      } else if (nextTile == '-') {
        currMoves.add('U');
      }

      currMoves.add('F');
    }
  }

  /**
   * If we cannot reach a point, we will attempt to reach it via using stepping
   * stones, until we have exhausted our supplies.
   * 
   * @param goal
   *          the goal point that we want to reach
   * @return boolean value of whether or not the point is reachable
   */
  private boolean testWithStone(Coordinate goal) {
    boolean moveMade = false;
    List<Coordinate[]> solutions = new ArrayList<>();

    for (int i = 1; i <= state.getNumSteppingStones() && !moveMade; ++i) {
      List<Coordinate[]> comboList = new ArrayList<>();
      Coordinate[] arr = state.getWaterCoordinates().toArray(new Coordinate[state.getWaterCoordinates().size()]);
      getAdjacentCombos(i, arr, comboList);

      for (Coordinate[] group : comboList) {
        // assume every water tile been placed stepping stone 
        for (Coordinate waterTile : group) {
          state.getMap().put(waterTile, State.COORDINATE_TEMPORARY_WATER);
        }
        // test if we can get to the goal
        FloodFillSearch newFloodFill = new FloodFillSearch(state.getMap(), state.getPlayerCoordinate(), goal);
        if (newFloodFill.canReach(state.haveKey(), state.haveAxe())) {
          // Add to solution 
          solutions.add(group);
          moveMade = true;
        }

        for (Coordinate waterTile : group) {
          state.getMap().put(waterTile, '~');
        }
      }
    }

    if (!solutions.isEmpty()) {
      int testPos = 0;

      List<Coordinate[]> treasureSolutions = new ArrayList<>();
      
      // check if can find a solution to get closer to treasure
      if (state.treasureLocated()) {
        int minCost = 999999;

        for (int i = 0; i < solutions.size(); ++i) {
          Coordinate[] group = solutions.get(i);
          int totalCost = 0;

          for (Coordinate solution : group) {
            int absX = Math.abs(solution.getX() - state.getTreasureCoordinate().getX());
            int absY = Math.abs(solution.getY() - state.getTreasureCoordinate().getY());
            totalCost = absX + absY; // Manhattan distance to treasure

            if (solution.getX() == state.getTreasureCoordinate().getX()
                || solution.getY() == state.getTreasureCoordinate().getY()) {
              if (!treasureSolutions.contains(group))
                treasureSolutions.add(group);
            }
          }

          if (totalCost < minCost) {
            minCost = totalCost;
            testPos = i;
          }
        }
      }

      boolean madePlan = false;
      
      // find a solution to get stepping stone
      if (treasureSolutions.isEmpty() && !state.getSSCoordinates().isEmpty()) {
        testPos = bestWaterStone(solutions, state.getSSCoordinates());
        madePlan = true;
      }
      
      if (!madePlan) {
        // find a solution to get key
        if (!state.getKeyCoordinates().isEmpty()) {
          testPos = bestWaterStone(solutions, state.getKeyCoordinates());
          madePlan = true;
        }
        // find a solution to get axe
        if (!state.getAxeCoordinates().isEmpty()) {
          testPos = bestWaterStone(solutions, state.getAxeCoordinates());
          madePlan = true;
        }

      }

      for (Coordinate waterTile : solutions.get(testPos)) {
        state.getMap().put(waterTile, State.COORDINATE_TEMPORARY_WATER);
      }
    }
    
    // A* traversal to the goal
    AStarToGoal(state.getPlayerCoordinate(), goal, state.getDirection(), state.haveKey(), state.haveAxe());

    return moveMade;
  }

  /**
   * Gets which direction you need to travel to get to the goal point
   * 
   * @param start
   *          starting position
   * @param goal
   *          goal position
   * @return returns direction in which we need to travel
   */
  private int getAdjacentTileDirection(Coordinate start, Coordinate goal) {
    int xDiff = goal.getX() - start.getX();
    int yDiff = goal.getY() - start.getY();
    int retDirection = -1;

    if (xDiff == 0 && yDiff == 0) {
      return retDirection;
    }

    if (xDiff < 0 && yDiff == 0) {
      retDirection = State.LEFT;
    } else if (xDiff > 0 && yDiff == 0) {
      retDirection = State.RIGHT;
    } else if (yDiff < 0 && xDiff == 0) {
      retDirection = State.DOWN;
    } else if (yDiff > 0 && xDiff == 0) {
      retDirection = State.UP;
    }

    return retDirection;
  }

  /**
   * We want to make sure that after movement the internal player is facing the
   * correct direction, returns a list of moves that need to be completed for this
   * to happen where startingDirection == goalDirection
   * 
   * @param startDirection
   *          initial starting direction
   * @param goalDirection
   *          final facing direction
   * @return returns list of moves that we need to take to make sure the direction
   *         is correct in the end
   */
  private LinkedList<Character> getAlignmentMoves(int startDirection, int goalDirection) {
    LinkedList<Character> l = new LinkedList<>();

    if (startDirection == goalDirection) {
      return l;
    }

    int numLeftMoves = 0;
    int numRightMoves = 0;

    if (startDirection <= goalDirection) {
      numRightMoves = goalDirection - startDirection;
      numLeftMoves = (4 - numRightMoves) % 4;
    } else if (startDirection > goalDirection) {
      numLeftMoves = (startDirection - goalDirection) % 4;
      numRightMoves = (4 - numLeftMoves) % 4;
    }

    if (numLeftMoves <= numRightMoves) {
      int count = 0;
      while (count < numLeftMoves) {
        l.add('L');
        count++;
      }

    } else {
      int count = 0;
      while (count < numRightMoves) {
        l.add('R');
        count++;
      }
    }

    return l;
  }

  /**
   * Helper method that will generate arrays from 1 to n lengths based on the
   * adjacent point groups and will then store and return them in the given list.
   * 
   * @param n
   *          max size of the subsets we want
   * @param arr
   *          contains the data we want to generate from
   * @param list
   *          to be filled with outputs and returned
   */
  private void getAdjacentCombos(int n, Coordinate[] arr, List<Coordinate[]> list) {
    long numArrays = binomial(arr.length, n);

    for (int i = 0; i < numArrays; i++) {
      Coordinate[] current = new Coordinate[n];
      for (int j = 0; j < n; j++) {
        int period = (int) Math.pow(arr.length, n - j - 1);
        int index = (i / period) % arr.length;
        current[j] = arr[index];
      }

      if (acceptedPoints(Arrays.asList(current))) {
        list.add(current);
      }

    }
  }

  /**
   * Tests to see if in a group of points, all points are reachable from every
   * other point in the group
   * 
   * @param group
   *          List of points that are to be compared
   * @return returns boolean value of whether or not everything is reachable
   */
  private static boolean acceptedPoints(List<Coordinate> group) {
    BitSet notVisited = new BitSet(group.size());
    notVisited.set(0, group.size());
    connected(group, notVisited, 0);

    if (notVisited.isEmpty()) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * Used alongside the acceptedPoints function to test if points are indeed
   * connected to each other
   * 
   * @param points
   *          group of points that we want to test
   * @param notVisited
   *          list of points that have yet to be visited stored as BitSet
   * @param visit
   *          the current point that we are testing reachability on
   */
  private static void connected(List<Coordinate> points, BitSet notVisited, int visit) {
    notVisited.set(visit, false);
    Coordinate curr = points.get(visit);

    for (int i = 0; i < points.size(); i++) {
      if (i != visit && notVisited.get(i)) {
        Coordinate other = points.get(i);
        boolean connected = false;
        if (curr.getX() == other.getX()) {

          if (curr.getY() - other.getY() >= -1 && curr.getY() - other.getY() <= 1) {
            connected = true;
          }
        } else if (curr.getY() == other.getY()) {
          if (curr.getX() - other.getX() >= -1 && curr.getX() - other.getX() <= 1) {
            connected = true;
          }
        }

        if (connected) {
          connected(points, notVisited, i);
        }
      }
    }
  }

  /**
   * Calculates the binomial coefficient of the given parameters
   * 
   * @param n
   *          number of possible outcomes
   * @param k
   *          unordered results
   * @return returns the binomial coefficient
   */
  private static long binomial(int n, int k) {
    if (k > n - k) {
      k = n - k;
    }

    long b = 1;
    int i = 1;
    int m = n;

    while (i <= k) {
      b = b * m / i;
      i++;
      m--;
    }

    return b;
  }

  /**
   * Takes in a list of possible points and calculates the optimal path to take
   * over water to reach our goal position. It attempts to find the shortest
   * distance to travel via the Manhattan distance by removing water tiles.
   * 
   * @param possiblePoints
   *          list of water tiles that are to be tested
   * @param acceptedPoints
   *          list of points that we need to take into consideration when we are
   *          calculating the movement
   * @return returns the index value of the optimal solution but if none are
   *         found, we just return the first index
   */
  private int bestWaterStone(List<Coordinate[]> possiblePoints, List<Coordinate> acceptedPoints) {
    int testPos = 0; // default solution is 0

    if (!acceptedPoints.isEmpty()) {
      int minCost = 999999;

      for (int i = 0; i < possiblePoints.size(); ++i) {
        Coordinate[] group = possiblePoints.get(i);
        int totalCost = 0;

        for (Coordinate a : group) {
          for (Coordinate b : acceptedPoints) {
            totalCost += (Math.abs((int) a.getX() - (int) b.getX()) + Math.abs((int) a.getY() - (int) b.getY()));
          }
        }

        // If cost is cheaper, replace it with new cost
        if (totalCost < minCost) {
          minCost = totalCost;
          testPos = i;
        }
      }
    }

    return testPos;
  }
}
