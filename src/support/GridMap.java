package support;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.List;
import java.util.stream.Collectors;
import org.xguzm.pathfinding.grid.GridCell;
import org.xguzm.pathfinding.grid.NavigationGrid;
import org.xguzm.pathfinding.grid.NavigationGridGraph;
import org.xguzm.pathfinding.NavigationNode;
import org.xguzm.pathfinding.grid.finders.AStarGridFinder;
import org.xguzm.pathfinding.grid.finders.ThetaStarGridFinder;
import org.xguzm.pathfinding.grid.finders.GridFinderOptions;
import ws3dproxy.model.Thing;

/**
 *
 * @author danilo
 */
public class GridMap 
{    
    //private GridGraph mazeGraph;
    private NavigationGrid<GridCell> mazeGraph;
    private static int GRID_CELL_SIZE = 40;
    
    private // 0 means closed, 1 means open, 2 is marker for start, 3 is marker for goal
	int[][] navCells = new int[80][60];
    
    private int startX, startY, endX, endY;
    
    public GridMap(double startX, double startY, double endX, double endY) 
    {
        this(calcGridPosition(startX), calcGridPosition(startY), calcGridPosition(endX), calcGridPosition(endY));
    }
    
    private GridMap(int startX, int startY, int endX, int endY)  
    {
        for (int i = 0; i < navCells.length; i++) 
        {
            for (int k = 0; k < navCells[i].length; k++) 
            {
                navCells[i][k] = 1;
            }
        }
        
        navCells[startX][startY] = 2;
        navCells[endX][endY] = 3;
        
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;

        // FMT initialize new grid
        GridCell[][] cells = new GridCell[navCells[0].length][navCells.length];
	for (int y = navCells.length - 1; y >= 0; y--) 
   	    for (int x = 0; x < navCells[0].length; x++)
            {
		int invY = navCells.length - 1 - y;
		GridCell cell = new GridCell(x, invY, true);
		cells[x][invY] = cell;
	    }
        mazeGraph = new NavigationGrid<GridCell>(cells, false);
    }
    
    private NavigationGrid<GridCell> getGridCellMap(int[][] navCells) {
        GridCell[][] cells = new GridCell[navCells.length][navCells[0].length];

        for (int x = 0; x < navCells.length; x++) {
            for (int y = 0; y < navCells[0].length; y++) 
            {
                cells[x][y] = new GridCell(x, y, navCells[x][y] > 0);
            }
        }
        return new NavigationGrid<GridCell>(cells, false);

    }
    
    public void update(int x, int y, boolean isWalkable) 
    {
        navCells[x][y] = isWalkable? 1 : 0;
    }
    
    public void markStartPosition(double startX, double startY) {
        markStartPosition(calcGridPosition(startX), calcGridPosition(startY));
    }
    
    private void markStartPosition(int startX, int startY) {
        navCells[this.startX][this.startY] = 1;
        navCells[startX][startY] = 2;
        this.startX = startX;
        this.startY = startY;
    }
    
    public void markWall(Thing thing) {
        int horizontalCells = (int)(Math.ceil((thing.getX2() - thing.getX1()) / GRID_CELL_SIZE));
        int verticalCells = (int)(Math.ceil((thing.getY2() - thing.getY1()) / GRID_CELL_SIZE));
        int startingX = calcGridPosition(thing.getX1()); //Integer.valueOf(Math.ceil(thing.getX1() / GRID_CELL_SIZE) + "") - 1;
        int startingY = calcGridPosition(thing.getY1()); //Integer.valueOf(Math.ceil(thing.getY1() / GRID_CELL_SIZE) + "") - 1;

        for (int i = startingX; i <= startingX + horizontalCells; i++) 
        {
            for (int k = startingY; k <= startingY + verticalCells; k++) 
            {
                navCells[i][k] = 0;
                GridCell cell = new GridCell(i,k,false);
                mazeGraph.setCell(i,k,cell);
            }
        }
    }
    
    /*public List<Coordinate> convertCoordinates(int[][] path)
    {
       List<Coordinate> listCoord = new List<Coordinate>();
       listCoord.add(new Coordinate(path[0][0], path[0][1]));
       return(listCoord);
    }*/
    
    public synchronized List<Coordinates> findPath() 
    {
        //or create your own pathfinder options:
        GridFinderOptions opt = new GridFinderOptions();
        opt.allowDiagonal = false;
	
        System.out.println("findPath: (start) "+startX+" "+startY+" (end) "+endX+" "+endY);
        //AStarGridFinder<GridCell> pathAlg = new AStarGridFinder(GridCell.class, opt);
        //return(pathAlg.findPath(startX, startY, endX, endY, mazeGraph));
        AStarGridFinder<GridCell> finderStar = new AStarGridFinder(GridCell.class, opt);
        ThetaStarGridFinder<GridCell> finder = new ThetaStarGridFinder(GridCell.class, opt);
        return finder.findPath(startX, startY, endX, endY, getGridCellMap(navCells)).stream()
                .map(gridCell -> new Coordinates(
                        calcCoordinate(gridCell.getX()), calcCoordinate(gridCell.getY())
                )).collect(Collectors.toList()); 
        
        /*AStar shortestPathAlg;
        int [][] path;
        shortestPathAlg = new AStar(mazeGraph, startX, startY, endX, endY);
        shortestPathAlg.computePath();
        path = shortestPathAlg.getPath();
        return(convertCoordinates(path));*/
    }
    
    private double calcCoordinate(int pos) {
        return pos * GRID_CELL_SIZE + (GRID_CELL_SIZE / 2);
    }
    
    private static int calcGridPosition(double xy) {
        int result = ((int)(Math.ceil(xy / GRID_CELL_SIZE))) - 1;
        return result > 0 ? result : 0;
    }
}
