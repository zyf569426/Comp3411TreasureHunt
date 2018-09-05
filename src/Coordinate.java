/**
 * Coordinate class keeps track of the internal tile locations.
 * 
 * @author Yufan Zou
 */
public class Coordinate {
  private int x;
  private int y;

  /**
   * Coordinate class constructor.
   * 
   * @param x
   *          x coordinate of point
   * @param y
   *          y coordinate of point
   */
  public Coordinate(int x, int y) {
    this.x = x;
    this.y = y;
  }

  /**
   * Gets the x value of cur coordinate.
   * 
   * @return x value of coordinate as an int.
   */
  public int getX() {
    return x;
  }

  /**
   * Sets the x value of cur coordinate.
   * 
   * @param x
   *          new x coordinate of point
   */
  public void setX(int x) {
    this.x = x;
  }

  /**
   * Gets the y value of cur coordinate.
   * 
   * @return y value of coordinate as an int.
   */
  public int getY() {
    return y;
  }

  /**
   * Sets the y value of cur coordinate.
   * 
   * @param y
   *          new x coordinate of point
   */
  public void setY(int y) {
    this.y = y;
  }

  /**
   * Tests if one point is equal to another.
   * 
   * @param o
   *          other point to be tested against
   * @return returns boolean value of whether or not they're equal
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o.getClass() != this.getClass()) {
      return false;
    }

    Coordinate other = (Coordinate) o;

    if (other.getX() == this.x && other.getY() == this.y) {
      return true;
    } else {
      return false;
    }

  }

  /**
   * Returns HashCode for equality testing
   * 
   * @return returns HashCode as an int value
   */
  @Override
  public int hashCode() {
    return x * 10001 ^ y * 22;
  }

}
