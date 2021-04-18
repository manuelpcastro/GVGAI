package tracks.singlePlayer.src_Pancorbo_Castro_Manuel;

import ontology.Types;
import tools.Vector2d;
import tools.pathfinder.PathFinder;

import java.util.*;


/**
 *
 * @author Manuel Pancorbo
 */
public class Pathfinder {

    //POSSIBLE MOVES
    private static int POSSIBLE_ORIENTATIONS = 9;

    //STEPS TO CALCULATE
    private static int STEPS = Integer.MAX_VALUE;

    //MAP INFO
    private ArrayList<ArrayList<Integer>> map;
    private double mapHeight;
    private double mapWidth;
    ArrayList<ArrayList<Integer>> visitedAtMap;
    Integer nActionsExecuted;

    //PLAN
    ArrayList<Types.ACTIONS> plan;
    Set<Coordinates> generated;

    PathfinderOption initialPosition;
    PathfinderOption targetPosition;
    ArrayList<Coordinates> blockedPositions;
    ArrayList<Vector2d> resources;



    public Pathfinder(Vector2d dimensions, ArrayList<Vector2d> blockedPositions, ArrayList<Vector2d> resources) {
        this.mapWidth = dimensions.x;
        this.mapHeight = dimensions.y;
        this.blockedPositions = new ArrayList(Arrays.asList(blockedPositions.stream().map(vector -> new Coordinates(vector.x, vector.y)).toArray()));
        this.resources = resources;
        initializeVisitedAtMap();
        this.nActionsExecuted = 0;
    }

    public Pathfinder(Vector2d dimensions, ArrayList<Vector2d> blockedPositions) {
        this.mapWidth = dimensions.x;
        this.mapHeight = dimensions.y;
        this.blockedPositions = new ArrayList(Arrays.asList(blockedPositions.stream().map(vector -> new Coordinates(vector.x, vector.y)).toArray()));
        initializeVisitedAtMap();
        this.nActionsExecuted = 0;
    }

    private void initializeVisitedAtMap() {
        this.visitedAtMap = new ArrayList<>();
        for (int i = 0; i < this.mapWidth; i++) {
            ArrayList<Integer> row = new ArrayList<>();
            for (int j = 0; j < this.mapHeight; j++) {
                row.add(-1);
            }
            this.visitedAtMap.add(row);
        }
    }

    public ArrayList<Types.ACTIONS> getPlanForResources(ArrayList<Vector2d> resources, double orientationX, double orientationY, Vector2d initialPosition){

        this.resources = resources;

        Comparator<Vector2d> comparator = (Vector2d o1, Vector2d o2) -> {
            Coordinates resource1 = new Coordinates (o1.x, o1.y);
            Coordinates resource2 = new Coordinates (o2.x, o2.y);
            return Double.compare( resource1.calculateDistance(new Coordinates(initialPosition.x, initialPosition.y)), resource2.calculateDistance(new Coordinates(initialPosition.x, initialPosition.y)));
        };

        this.resources.sort(comparator);
        Vector2d targetPosition = resources.get(0);
        return pathFinding_a(orientationX, orientationY, initialPosition, targetPosition);
    }

    private int getOrientation(double x, double y) {
        if(x==1) return 5;
        if(x==-1) return 3;
        if(y==1) return 7;
        if(y==-1) return 1;
        return 0;
    }

    public ArrayList<Types.ACTIONS> pathFinding_a(double orientationX, double orientationY, Vector2d vInitialPosition, Vector2d vTargetPosition) {

        int originalOrientation = getOrientation(orientationX, orientationY);
        int step = 0;
        System.out.println("---Pathfinder: ON");

        this.initialPosition = new PathfinderOption(vInitialPosition.x, vInitialPosition.y, originalOrientation);
        this.targetPosition = new PathfinderOption(new Coordinates(vTargetPosition.x, vTargetPosition.y));

        plan = new ArrayList<>();
        generated = new HashSet<Coordinates>();

        Comparator<PathfinderOption> comparator = Comparator.comparingDouble(arg0 -> arg0.distance);
        PriorityQueue<PathfinderOption> queue = new PriorityQueue(comparator);

        PathfinderOption current = this.initialPosition;
       // current.setPath(new ArrayList<>(Arrays.asList(PossibleActions.getPossibleAction(originalOrientation).getAction())));
        current.setPath(new ArrayList<>());
        queue.add(current);

        //Mientras haya casillas que estudiar o no lleguemos al objetivo, seguimos evaluando
        while (step < STEPS && queue.size() != 0 && !current.checkCoordinates(this.targetPosition)) {

            System.out.println("Searching route from " + current.coordinates.x + ", " +  current.coordinates.y + " to " + vTargetPosition);
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
            System.out.println("br");
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
        if(current == null) {
            plan.add(Types.ACTIONS.ACTION_NIL);
            return plan;
        }

        System.out.println("Chosen: " + current + " --- is target: " + current.checkCoordinates(this.targetPosition));
        this.plan = current.getPath();

        return this.plan;
    }

    private ArrayList<PathfinderOption> generateChilds(PathfinderOption parent) {

        ArrayList<PathfinderOption> options = new ArrayList<>();
        for (int i = 1; i < Pathfinder.POSSIBLE_ORIENTATIONS; i+=2) {
            double xPosition = parent.coordinates.x - 1 + (i % 3);
            double yPosition = parent.coordinates.y - 1 + (i / 3);
            System.out.println(" ----- Studying position " + i + ": " + xPosition + ", " + yPosition);
            if (valid(xPosition, yPosition)) {
                PathfinderOption next = this.generateOption(parent, i, xPosition, yPosition);
                //this.visitedAtMap.get((int) xPosition).set(yPosition, this.nActionsExecuted);
                System.out.println(" ------------------Distance: " + next.distance);
                System.out.println(" ------------------Calculated Path: " + next.getPath() + "\n");
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
        return (x > 0 && x < this.mapWidth-1 && y > 0 && y < this.mapHeight-1);
    }

    PathfinderOption generateOption(PathfinderOption parent, int index, double xPosition, double yPosition) {
        //Creamos la opcion
        PathfinderOption option = new PathfinderOption(xPosition, yPosition, index);
        option.calculateDistance(this.targetPosition);
        option.setPath(parent.getPath());

        return option;
    }

    private ArrayList<PathfinderOption> generateChildsWithEnemyPresence(PathfinderOption parent, ArrayList<Vector2d> updatedEnemies) {

        ArrayList<PathfinderOption> options = new ArrayList<>();
        for (int i = 1; i < Pathfinder.POSSIBLE_ORIENTATIONS; i+=2) {
            double xPosition = parent.coordinates.x - 1 + (i % 3);
            double yPosition = parent.coordinates.y - 1 + (i / 3);
            System.out.println(" ----- Studying position " + i + ": " + xPosition + ", " + yPosition);
            if (valid(xPosition, yPosition)) {
                PathfinderOption next = this.generateEnemyOption(parent, i, xPosition, yPosition, updatedEnemies);
                //this.visitedAtMap.get((int) xPosition).set(yPosition, this.nActionsExecuted);
//                System.out.println(" ------------------Distance: " + next.distance);
//                System.out.println(" ------------------Calculated Path: " + next.getPath() + "\n");
                options.add(next);
            }
        }
        return options;
    }

    PathfinderOption generateEnemyOption(PathfinderOption parent, int index, double xPosition, double yPosition, ArrayList<Vector2d> updatedEnemies) {
        //Creamos la opcion
        PathfinderOption option = new PathfinderOption(xPosition, yPosition, index);
        option.calculateDistance(this.targetPosition);
        updatedEnemies.forEach(i -> option.addToDistance(option.coordinates.calculateDistance(new Coordinates(i.x, i.y))));
        System.out.println("This position has a risk of : " + option.distance );
        option.setPath(parent.getPath());

        return option;
    }

    public ArrayList<Types.ACTIONS> whereToRun(Types.ACTIONS action, Vector2d myPosition, ArrayList<Vector2d> updatedEnemies) {
        PossibleActions possibleAction = PossibleActions.getOppositeAction(action);
        Vector2d move = PossibleActions.move(possibleAction.getAction());
        Coordinates newPosition = new Coordinates(myPosition.x + move.x, myPosition.y + move.y);
        System.out.println("Next position is " + newPosition.x + ", " + newPosition.y);
/*

        Vector2d halfway = new Vector2d(myPosition.x,myPosition.y);
        final int[] boostCloserEnemies = {1};
        updatedEnemies.forEach(i -> { halfway.x+=(i.x/ boostCloserEnemies[0]); halfway.y+=(i.y/ boostCloserEnemies[0]); boostCloserEnemies[0] *=10; });
        halfway.x = halfway.x / updatedEnemies.size(); halfway.y = halfway.y / updatedEnemies.size();

        this.targetPosition = new PathfinderOption(new Coordinates(myPosition.x, myPosition.y));
        PathfinderOption current = new PathfinderOption(myPosition.x, myPosition.y, PossibleActions.getPossibleAction(action).getOrientation());
        current.setPath(new ArrayList<>(Arrays.asList(action)));
        ArrayList<PathfinderOption> child = generateChildsWithEnemyPresence(current, updatedEnemies);
        child.sort(Comparator.comparingDouble(arg0 -> arg0.distance));
        System.out.println("I will go for " +  child.get(0).coordinates );
        return child.get(0).getPath();*/
        System.out.println("X: Im in " + newPosition.x + " , whole map is " + mapWidth);
        System.out.println("Y: Im in " + newPosition.y + " , whole map is " + mapHeight);
        if (!insideMap(newPosition.x, newPosition.y)){

            System.out.println("I should change my axis direction...");
            double currentHeight = newPosition.y / mapHeight;
            double currentWidth = newPosition.x / mapWidth;

           possibleAction = PossibleActions.getOppositeAxisAction(currentHeight, currentWidth, possibleAction);
        }

        ArrayList<Types.ACTIONS> toRun = new ArrayList<>();
        toRun.add(possibleAction.getAction()); toRun.add(possibleAction.getAction());
        System.out.println("My Plan B is " + toRun.toString());
        return toRun;
    }

    public boolean shouldIContinue(Types.ACTIONS action, Vector2d myPosition, ArrayList<Vector2d> updatedEnemies) {
        System.out.println("acction " + action);
        if (action == null) return true; // Si todavia no hemos hecho ninguna accion, dejamos al personaje pensar en un path
        Vector2d move = PossibleActions.move(action);
        Coordinates currentPosition = new Coordinates(myPosition.x, myPosition.y);
        Coordinates nextPosition = new Coordinates(myPosition.x + move.x, myPosition.y + move.y);
        if(currentPosition.checkCoordinates(targetPosition.coordinates)){
            //Si estamos en la salida, queremos intentar esquivar siempre
            return false;
        }


        Comparator<Vector2d> comparator = (Vector2d o1, Vector2d o2) -> {
            Coordinates resource1 = new Coordinates (o1.x, o1.y);
            Coordinates resource2 = new Coordinates (o2.x, o2.y);
            return Double.compare( resource1.calculateDistance(new Coordinates(myPosition.x, myPosition.y)), resource2.calculateDistance(new Coordinates(myPosition.x, myPosition.y)));
        };
        updatedEnemies.sort(comparator);
        Vector2d closestEnemy = updatedEnemies.get(0);
        Coordinates closestEnemyCoord = new Coordinates (closestEnemy.x, closestEnemy.y);

        return nextPosition.calculateDistance(closestEnemyCoord) > currentPosition.calculateDistance(closestEnemyCoord);
    }
}