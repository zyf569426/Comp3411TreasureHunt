# Comp3411TreasureHunt

Project Introduction: http://www.cse.unsw.edu.au/~cs3411/17s1/hw3raft/

Usage: java Step -i ../maps/s0.in

Treasure Hunt 是一个文本类冒险游戏。地图中有树、门、水、墙等障碍物以及斧子、钥匙、垫脚石、宝藏等道具，玩家在拾取道具后可以使用斧子砍树和砍下的树做木筏过水（一次性）、用钥匙开门、放垫脚石趟水（永久）。玩家在找到宝藏之后返回出发地点即可通关。





Briefly describe how your program works, including any algorithms and data structures employed, and explain any design decisions you made along the way.

 Firstly, as our program receives view arrays it will update the internal view of the board stored in the state class. 
 The view is automatically rotated according to our current direction and all tool and item locations are recorded for later use. 
 The board is stored internally as a HashMap with a Coordinate instance (that keeps track of a point's x and y coordinates) 
 as the key and a char as its value (where it is set as '?' until we are able to update it with the given views). 
 
 Secondly, in order to explore the extent of the board (and what can reach with the current state of our inventory) we have chosen to use the flood fill algorithm.  
 We have chosen to use flood fill because even in the worst case scenario (or best case, because that means we have access to all the squares, 
 and ultimately the treasure) where we have to check every single cell in our board the complexity is still relatively cheap-O(N^2)-given the small size of our boards. 
 
 In normal run time it allows us to quickly and efficiently find the boundaries of the current island the player is on. 
 The flood fill class take the current inventory of the player (axe and key) to see if it allows for travel to other areas of the map. 
 Once we are certain that we are able to access certain areas of the map we will then traverse via A* algorithm to get the most efficient path from the player's position to the goal position.
 
 And finally, we have implemented the spiral seek algorithm as a last resort step to when we need to find new unexplored areas of the map. 
 Beginning from the player's current position we travel outwards in a circular motion to check points that have not yet been explored and will provide us with new insight into the board. 
 In terms of the MoveGenerator class we have implemented a top down approach in terms of prioritizing what we need to do in order to find the treasure. 
 Ultimately our approach is (if one point on the list fails we will move down and try to do the next item on the list): 
 -1.If we have moves we want to execute, we will return them now to be done. 
 -2.If we possess gold we will A* back to our beginning position 
 -3.If we do not possess gold but we see it, we will test reachability via flood fill 
 	-a.If it can be reached, we will get it and return to original position 
 -4.If cannot be reached we will attempt to find the tools that will allow us to reach it 
 	-a.If we have coordinates of the tools, we will attempt to reach it via A*
 -5.If tools cannot be found we will run spiral seek to try to discover new parts of the map  
 -6.If no new parts can be discovered then we will go back and pick up any tool we have access to 
 -7.Once we cannot go any further in our current island, we will attempt to use rafts and stepping stones to see if we can reach a new area  
 -8.If all else fails we will return to our starting position
