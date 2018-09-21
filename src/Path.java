//Search algorithm to find the path through arena
//Floyd Warshall algorithm is used to perform figure out shortest distance between any two cells
//Those shortest paths are then used to determine the order of visiting patient's cells and a total path

public class Path {

  int[][] matrix;                    //Matrix used by Floyd Warshall algorithm
  int[][] dist;                     //Array storing shortest distance from each vertex to another
  int[][] prefix;                  //Array storing predecessir of each vertes on shortest path
  int width;                      //Grid width
  int length;                    //Grid length
  int[] obstacles;              //Obstacle coordinates array
  int numOfObstacles;          //Number of obstacles
  int[] patients;             //Patients coordinates Array
  int startX;                //Path starting point x coordinate
  int startY;               //Path starting point y coordinate
  String[] traversalPath;  //Total path through the arena
  int next;               //Next cell on the computed path

  //Constructor
  public Path (int w, int l, int[] obst, int numOfObst, int[] pat, int sX, int sY) {
    width = w;
    length = l;
    obstacles = obst;
    numOfObstacles = numOfObst;
    patients = pat;
    startX = sX;
    startY = sY;
  }

  //Method to set up a matrix used by Floyd Warshall algorithm representing the grid
  public void setUpMatrix () {

    //Matrix size
    int size = this.width * this.length;
    //Matrix
    this.matrix =  new int[size][size];
    //Defining unreachable cell
    int unreachable = 100;

    //Initially all unreachable
    for (int i = 0; i < size; i++){
      for (int j = 0; j < size; j++){
        //Temporal value only - to indicate that the cell has not yet been explored
        this.matrix[i][j] = 1000;
      }
    }

    //Obstacle cells are unreachable
    for (int k = 0; k < 2 * this.numOfObstacles; k = k + 2) {
      int a = this.obstacles[k];
      int b = this.obstacles[k+1];
      //Computing cell number from coordinates (cells numberd 0,1,2,...starting from top left corner, going right then down)
      int cellNum = (this.width * b) + a;
      for (int l = 0; l < size; l++) {
        this.matrix[cellNum][l] = unreachable;
        this.matrix[l][cellNum] = unreachable;
      }
    }

    //Path from each cell to itself has a length 0
    for (int i = 0; i < size; i++) {
      this.matrix[i][i] = 0;
    }

    //Path form each cell to adjacent cell has a length 1 (if that cell is not an obstacle)
    for( int i = 0; i < size; i++) {
      if (i - 1 >= 0) {
        if (this.matrix[i][i - 1] != 100) {
          int div1 = (i - 1) / this.width;
          int div2 = i / this.width;
          if (div1 == div2) {
            this.matrix[i][i - 1] = 1;
          }
        }
      }
	  if (i + 1 < size) {
        if (this.matrix[i][i + 1] != 100) {
          int div1 = (i + 1) / this.width;
          int div2 = i / this.width;
          if (div1 == div2) {
            this.matrix[i][i + 1] = 1;
          }
        }
      }
	  if (i + this.width < size) {
        if (this.matrix[i][i + this.width] != 100) {
          this.matrix[i][i + this.width] = 1;
        }
      }
	  if (i - this.width >= 0) {
        if (this.matrix[i][i - this.width] != 100) {
          this.matrix[i][i - this.width] = 1;
        }
      }
    }

    //Finally signing unreachable cells
    for (int i = 0; i < size; i++) {
      for (int j = 0; j < size; j++) {
        if (this.matrix[i][j] == 1000) {
          this.matrix[i][j] = 100;
        }
      }
    }
  }

  //Method to find shortest path between ant two vertices - Floyd Warshall algorithm
  public void floydWarshall () {

    int size = this.width * this.length;
 	this.dist = new int[size][size];
    this.prefix = new int[size][size];

 	for (int i = 0; i < size; i++){
 		for (int j = 0; j < size; j++){
 			this.dist[i][j] = this.matrix[i][j];
			if (this.matrix[i][j] == 1){
				this.prefix[i][j] = i;
			} else {
				this.prefix[i][j] = 100;
			}
 		}
 	}

 	for (int k = 0; k < size; k++){
 		for (int i = 0; i < size; i++){
 			for( int j = 0; j < size; j++){
 				if (this.dist[i][k] + this.dist[k][j] < this.dist[i][j]){
 					this.dist[i][j] = this.dist[i][k] + this.dist[k][j];
					this.prefix[i][j] = this.prefix[k][j];
 				}
 			}
 		}
     }
  }
	

  //Method to get path between two vertices uding the prefix array
  public int[] getPath (int start, int end, int path[], int length){
	path[length - 1] = end;
	for(int i = length - 2; i >= 0; i--){
		path[i] = prefix[start][end];
		end = path[i];
	}
    return path;
  } 

  //Method to get the shortest traversal path visiting every patient locations
  public void shortestPath () {
	  
    //Compute starting cell number from coordinates
    int startCell = (this.width * this.startY) + this.startX;
    //Compute patients' cells numbers from coordinates
    int[] patientsCell = new int[5];
    patientsCell[0] = (this.width * this.patients[1]) + this.patients[0];
    patientsCell[1] = (this.width * this.patients[3]) + this.patients[2];
    patientsCell[2] = (this.width * this.patients[5]) + this.patients[4];
    patientsCell[3] = (this.width * this.patients[7]) + this.patients[6];
    patientsCell[4] = (this.width * this.patients[9]) + this.patients[8];
	
    //Initial path length - start point -> patient1 -> patient2 -> patient3 -> patient4 -> patient5
    int pathLength = this.dist[startCell][patientsCell[0]] + this.dist[patientsCell[0]][patientsCell[1]] + this.dist[patientsCell[1]][patientsCell[2]] + this.dist[patientsCell[2]][patientsCell[3]] + this.dist[patientsCell[3]][patientsCell[4]];
    int first = patientsCell[0];
    int second = patientsCell[1];
    int third = patientsCell[2];
    int fourth = patientsCell[3];
    int fifth = patientsCell[4];

    //Computing lengths of all possible paths (different orders of visiting patients and choosing the shortest one)
    for (int i = 0; i < 5; i++) {
      int firstDist = this.dist[startCell][patientsCell[i]];
      for (int j = 0; j < 5; j++) {
        if (j != i) {
          int secondDist = this.dist[patientsCell[i]][patientsCell[j]];
          for (int k = 0; k < 5; k++) {
            if (k != i && k != j) {
              int thirdDist = this.dist[patientsCell[j]][patientsCell[k]];
              for (int l = 0; l < 5; l++) {
                if (l != i && l != j && l != k) {
                  int fourthDist = this.dist[patientsCell[k]][patientsCell[l]];
                  for (int m = 0; m < 5; m++) {
                    if (m != i && m != j && m != k && m != l) {
                      int fifthDist = this.dist[patientsCell[l]][patientsCell[m]];
                      int tempPathLength = firstDist + secondDist + thirdDist + fourthDist + fifthDist;
                      if (tempPathLength < pathLength) {
                        pathLength = tempPathLength;
                        first = patientsCell[i];
                        second = patientsCell[j];
                        third = patientsCell[k];
                        fourth = patientsCell[l];
                        fifth = patientsCell[m];
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
    
    this.next = 0;
	
	
    //Get the actual path (cells' numbers)
	int firstLength = this.dist[startCell][first];
	int secondLength = this.dist[first][second];
	int thirdLength = this.dist[second][third];
	int fourthLength = this.dist[third][fourth];
	int fifthLength = this.dist[fourth][fifth];
	int[] path = new int[firstLength+secondLength+thirdLength+fourthLength+fifthLength];
	int num = 0;
	
	if(first != startCell) {
		int[] path1 = new int[firstLength];
		path1 = getPath (startCell, first, path1, firstLength);
		for(int i = 0 ; i < firstLength ; i++){
		path[num] = path1[i];
		num++;
		}
	}
    
	
	int[] path2 = new int[secondLength];
    path2 = getPath (first, second, path2, secondLength);
		for(int i = 0 ; i < secondLength ; i++){
		path[num] = path2[i];
		num++;
	}
	
	int[] path3 = new int[thirdLength];
    path3 = getPath (second, third, path3, thirdLength);
		for(int i = 0 ; i < thirdLength ; i++){
		path[num] = path3[i];
		num++;
	}
	
	int[] path4 = new int[fourthLength];
    path4 = getPath (third, fourth, path4, fourthLength);
		for(int i = 0 ; i < fourthLength ; i++){
		path[num] = path4[i];
		num++;
	}
	
	int[] path5 = new int[fifthLength];
    path5 = getPath (fourth, fifth, path5, fifthLength);
		for(int i = 0 ; i < fifthLength ; i++){
		path[num] = path5[i];
		num++;
	}

    //Compute directions for path
    this.traversalPath = new String[pathLength];
    int fCell = startCell;
    for (int i = 0; i < pathLength; i++) {
      if (path[i] == fCell - 1) {
        traversalPath[i] = "left";
      }
      if (path[i] == fCell + 1) {
        traversalPath[i] = "right";
      }
      if (path[i] == fCell - this.width) {
        traversalPath[i] = "up";
      }
      if (path[i] == fCell + this.width) {
        traversalPath[i] = "down";
      }
      fCell = path[i];
    }
  }
}
