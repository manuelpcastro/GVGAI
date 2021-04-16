package tracks.singlePlayer.src_Pancorbo_Castro_Manuel;

import ontology.Types;
import tools.Vector2d;

import java.util.*;


/**
 *
 * @author Manuel Pancorbo
 */
public class Pathfinder {

    //POSSIBLE MOVES
    private static int POSSIBLE_ORIENTATIONS = 9;

    //STEPS TO CALCULATE
    private static int STEPS = 40543234;

    //MAP INFO
    private ArrayList<ArrayList<Integer>> map;
    private double mapHeight;
    private double mapWidth;

    //PLAN
    ArrayList<Types.ACTIONS> plan;
    Set<Coordinates> generated;

    PathfinderOption initialPosition;
    PathfinderOption targetPosition;
    ArrayList<Coordinates> blockedPositions;
    ArrayList<Vector2d> resources;

    public Pathfinder(double width, double height, ArrayList<Vector2d> blockedPositions, ArrayList<Vector2d> resources) {
        this.mapWidth = width;
        this.mapHeight = height;
        this.blockedPositions = new ArrayList(Arrays.asList(blockedPositions.stream().map(vector -> new Coordinates(vector.x, vector.y)).toArray()));
        this.resources = resources;
    }

    public ArrayList<Types.ACTIONS> getPlanForResources(int index, double orientationX, double orientationY, Vector2d initialPosition){
        Vector2d targetPosition = resources.get(index);
        return pathFinding_a(orientationX, orientationY, initialPosition, targetPosition);
    }

    private int getOrientation(double x, double y) {
        if(x==1) return 5;
        if(x==-1) return 3;
        if(y==1) return 7;
        if(y==-1) return 1;
        return 0;
    }

    public ArrayList<Types.ACTIONS> pathFinding_a(double orientationX, double orientationY, Vector2d initialPosition, Vector2d targetPosition) {

        int originalOrientation = getOrientation(orientationX, orientationY);
        int step = 0;
        System.out.println("---Pathfinder: ON");

        this.initialPosition = new PathfinderOption(initialPosition.x, initialPosition.y, originalOrientation);
        this.targetPosition = new PathfinderOption(new Coordinates(targetPosition.x, targetPosition.y));

        plan = new ArrayList<>();
        generated = new HashSet<Coordinates>();

        Comparator<PathfinderOption> comparator = Comparator.comparingDouble(arg0 -> arg0.distance);
        PriorityQueue<PathfinderOption> queue = new PriorityQueue(comparator);

        PathfinderOption current = this.initialPosition;
        current.setPath(new ArrayList<>(Arrays.asList(PossibleActions.getPossibleAction(originalOrientation).getAction())));
        queue.add(current);

        //Mientras haya casillas que estudiar o no lleguemos al objetivo, seguimos evaluando
        while (step < STEPS && queue.size() != 0 && !current.checkCoordinates(this.targetPosition)) {

            System.out.println("Searching route from " + current.coordinates.x + ", " +  current.coordinates.y + " to " + targetPosition);
            //Sacamos el primero de la cola
            queue.poll();

            //Si no lo hemos evaluado, lo metemos en evaluados
            Coordinates posicion = current.coordinates;
            if ((generated.stream().filter(i -> posicion.checkCoordinates(i)).toArray().length == 0)) {
                generated.add(posicion);
            }
            //System.out.println("GENERATED: " + generated.toString());

            ArrayList<PathfinderOption> child = generateChilds(current);

            //System.out.println("Child : " + child.toString());

            //Introducimos aquellas que no hemos visitado
            for (PathfinderOption option : child) {
                if (generated.stream().filter(i -> option.coordinates.checkCoordinates(i)).toArray().length == 0) {
                    generated.add(option.coordinates);
                    queue.add(option);
                }
            }

            //cogemos el siguiente
            step++;
            current = queue.peek();
        }

        //Hemos terminado, recuperamos el plan si hemos logrado llegar

        System.out.println("Chosen: " + current + " --- is target: " + current.checkCoordinates(this.targetPosition));
        this.plan = current.getPath();


        return this.plan;
    }

    private ArrayList<PathfinderOption> generateChilds(PathfinderOption parent) {

        ArrayList<PathfinderOption> options = new ArrayList<>();
        for (int i = 1; i < Pathfinder.POSSIBLE_ORIENTATIONS; i+=2) {
            double xPosition = parent.coordinates.x - 1 + (i % 3);
            double yPosition = parent.coordinates.y - 1 + (i / 3);
           // System.out.println(" ----- Studying position " + i + ": " + xPosition + ", " + yPosition);
            if (valid(xPosition, yPosition)) {
                PathfinderOption next = this.generateOption(parent, i, xPosition, yPosition);
//                System.out.println(" ------------------Distance: " + next.distance);
//                System.out.println(" ------------------Calculated Path: " + next.getPath() + "\n");
                options.add(next);
            }
        }
        return options;
    }

    private boolean valid(double x, double y) {
        if(!insideMap(x, y))
            return false;

        Coordinates coordinates = new Coordinates(x,y);
        for (Coordinates blocked : blockedPositions) {
           if (coordinates.checkCoordinates(blocked))
                return false;
        }

        return true;
    }

    private boolean insideMap(double x, double y) {
        return (x >= 0 && x < this.mapWidth && y >= 0 && y < this.mapHeight);
    }

    PathfinderOption generateOption(PathfinderOption parent, int index, double xPosition, double yPosition) {
        //Creamos la opcion
        PathfinderOption option = new PathfinderOption(xPosition, yPosition, index);
        option.calculateDistance(this.targetPosition);
        option.setPath(parent.getPath());

        return option;
    }


}