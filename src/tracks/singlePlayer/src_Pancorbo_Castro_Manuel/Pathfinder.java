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
    private static int STEPS = Integer.MAX_VALUE;

    //MAP INFO
    private double mapHeight;
    private double mapWidth;
    private ArrayList<Coordinates> blockedPositions;
    private ArrayList<Vector2d> resources;

    //PLAN
    private ArrayList<Types.ACTIONS> plan;
    private Set<Coordinates> generated;

    private PathfinderOption initialPosition;
    private PathfinderOption targetPosition;

    public Pathfinder(Vector2d dimensions, ArrayList<Vector2d> blockedPositions, ArrayList<Vector2d> resources) {
        this.mapWidth = dimensions.x;
        this.mapHeight = dimensions.y;
        this.blockedPositions = new ArrayList(Arrays.asList(blockedPositions.stream().map(vector -> new Coordinates(vector.x, vector.y)).toArray()));
        this.resources = resources;
    }

    public Pathfinder(Vector2d dimensions, ArrayList<Vector2d> blockedPositions) {
        this.mapWidth = dimensions.x;
        this.mapHeight = dimensions.y;
        this.blockedPositions = new ArrayList(Arrays.asList(blockedPositions.stream().map(vector -> new Coordinates(vector.x, vector.y)).toArray()));
    }

    public ArrayList<Types.ACTIONS> getPlanForResources(ArrayList<Vector2d> resources, double orientationX, double orientationY, Vector2d initialPosition){
        this.resources = resources;

        Comparator<Vector2d> comparator = (Vector2d o1, Vector2d o2) -> {
            Coordinates resource1 = new Coordinates (o1.x, o1.y);
            Coordinates resource2 = new Coordinates (o2.x, o2.y);
            return Double.compare( resource1.calculateDistance(new Coordinates(initialPosition.x, initialPosition.y)), resource2.calculateDistance(new Coordinates(initialPosition.x, initialPosition.y)));
        };

        this.resources.sort(comparator);
        // Queremos coger la gema mas proxima
        Vector2d targetPosition = resources.get(0);
        return pathFinding_a(orientationX, orientationY, initialPosition, targetPosition);
    }



    public ArrayList<Types.ACTIONS> pathFinding_a(double orientationX, double orientationY, Vector2d vInitialPosition, Vector2d vTargetPosition) {
        int originalOrientation = PossibleActions.getOrientation(orientationX, orientationY);
        int step = 0;

        this.initialPosition = new PathfinderOption(vInitialPosition.x, vInitialPosition.y, originalOrientation);
        this.targetPosition = new PathfinderOption(new Coordinates(vTargetPosition.x, vTargetPosition.y));

        plan = new ArrayList<>();
        generated = new HashSet<Coordinates>();

        Comparator<PathfinderOption> comparator = Comparator.comparingDouble(arg0 -> arg0.distance);
        PriorityQueue<PathfinderOption> queue = new PriorityQueue(comparator);

        PathfinderOption current = this.initialPosition;
        current.setPath(new ArrayList<>());
        queue.add(current);

        //Mientras haya casillas que estudiar o no lleguemos al objetivo, seguimos evaluando
        while (step < STEPS && queue.size() != 0 && !current.checkCoordinates(this.targetPosition)) {
            //Sacamos el primero de la cola (la mejor opcion)
            queue.poll();

            //Si no lo hemos evaluado, lo metemos en evaluados
            Coordinates posicion = current.coordinates;
            if ((generated.stream().filter(i -> posicion.checkCoordinates(i)).toArray().length == 0)) {
                generated.add(posicion);
            }

            //Generamos aquellas posibles posiciones VALIDAS a las que podemos acceder
            ArrayList<PathfinderOption> child = generateChild(current);

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

        //Hemos terminado, recuperamos el plan si hemos logrado llegar. En caso contrario, devolvemos NIL;
        if(current == null) {
            plan.add(Types.ACTIONS.ACTION_NIL);
        } else {
            this.plan = current.getPath();
        }

        return this.plan;
    }

    private ArrayList<PathfinderOption> generateChild(PathfinderOption parent) {
        ArrayList<PathfinderOption> options = new ArrayList<>();
        // Generamos las posiciones a la que podemos movernos (ARRIBA, DERECHA, IZQUIERDA, ABAJO)
        // ya que a las esquinas no podemos acceder con nuestras acciones
        for (int i = 1; i < Pathfinder.POSSIBLE_ORIENTATIONS; i+=2) {
            double xPosition = parent.coordinates.x - 1 + (i % 3);
            double yPosition = parent.coordinates.y - 1 + (i / 3);
            if (valid(xPosition, yPosition)) { //Evaluamos que sea valida: dentro del mapa y no bloqueada
                PathfinderOption next = this.generateOption(parent, i, xPosition, yPosition);
                options.add(next);
            }
        }
        return options;
    }

    // Evaluamos que unas coordenadas sean accesibles
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
        //Cuidado con estas comprobaciones
        return (x >= 0 && x < this.mapWidth && y >= 0 && y < this.mapHeight);
    }

    PathfinderOption generateOption(PathfinderOption parent, int index, double xPosition, double yPosition) {
        //Creamos la opcion
        PathfinderOption option = new PathfinderOption(xPosition, yPosition, index);
        option.calculateDistance(this.targetPosition);
        option.setPath(parent.getPath());

        return option;
    }

    private ArrayList<PathfinderOption> generateChildWithEnemyPresence(PathfinderOption parent, ArrayList<Vector2d> updatedEnemies) {
        // Hacemos el mismo procedimiento que de forma normal, pero en este caso teniendo en cuenta a los enemigos
        ArrayList<PathfinderOption> options = new ArrayList<>();
        for (int i = 1; i < Pathfinder.POSSIBLE_ORIENTATIONS; i+=2) {
            double xPosition = parent.coordinates.x - 1 + (i % 3);
            double yPosition = parent.coordinates.y - 1 + (i / 3);
            if (valid(xPosition, yPosition)) {
                PathfinderOption next = this.generateEnemyOption(parent, i, xPosition, yPosition, updatedEnemies);
                options.add(next);
            }
        }
        return options;
    }

    PathfinderOption generateEnemyOption(PathfinderOption parent, int index, double xPosition, double yPosition, ArrayList<Vector2d> updatedEnemies) {
        //Creamos la opcion
        PathfinderOption option = new PathfinderOption(xPosition, yPosition, index);
        option.calculateDistance(this.targetPosition);
        /* Al fin y al cabo nuestra medida de la distancia es el coste con el que evaluamos si una casilla es mejor que otra.
         * Podemos aprovechar esto para "sobrecargar" el coste cuando haya enemigos cerca, para quitarle preferencia a una casilla
         * que se encuentre mas proxima a los enemigos
         *
         * Sumamos 1 a la distancia por el caso en el que un enemigo este en la casilla objetivo, para evitar una posible excepcion
         * (aunque no he llegado a tener ninguna, puede haber conflictos segun la version del jdk)
         */
        updatedEnemies.forEach(i -> option.addToDistance(Double.MAX_VALUE / (option.coordinates.calculateDistance(new Coordinates(i.x, i.y))+1.0)));
        option.setPath(parent.getPath());

        return option;
    }

    public ArrayList<Types.ACTIONS> whereToRun(Types.ACTIONS action, Vector2d myPosition, ArrayList<Vector2d> updatedEnemies) {
        PathfinderOption current = new PathfinderOption(myPosition.x, myPosition.y, PossibleActions.getPossibleAction(action).getOrientation());

        ArrayList<PathfinderOption> child = generateChildWithEnemyPresence(current, updatedEnemies);
        child.sort(Comparator.comparingDouble(arg0 -> arg0.distance));
        return child.get(0).getPath(); //Solo nos interesa la primera accion, ya que tendremos que volver a calcular la proxima vez que se mueva el enemigo
    }

    public boolean shouldIContinue(Types.ACTIONS action, Vector2d myPosition, ArrayList<Vector2d> updatedEnemies) {
        if (action == null) {
            return true; // Si todavia no tenemos plan, dejamos al personaje pensar en un posible plan
        }
        Vector2d move = PossibleActions.move(action);
        Coordinates currentPosition = new Coordinates(myPosition.x, myPosition.y);
        Coordinates nextPosition = new Coordinates(myPosition.x + move.x, myPosition.y + move.y);

        if(currentPosition.checkCoordinates(targetPosition.coordinates)){
            //Si estamos en la salida parados, queremos empezar a esquivar siempre
            return false;
        }

        Comparator<Vector2d> comparator = (Vector2d o1, Vector2d o2) -> {
            Coordinates resource1 = new Coordinates (o1.x, o1.y);
            Coordinates resource2 = new Coordinates (o2.x, o2.y);
            return Double.compare( resource1.calculateDistance(new Coordinates(myPosition.x, myPosition.y)), resource2.calculateDistance(new Coordinates(myPosition.x, myPosition.y)));
        };
        updatedEnemies.sort(comparator);
        Vector2d closestEnemy = updatedEnemies.get(0);
        Coordinates closestEnemyCoord = new Coordinates(closestEnemy.x, closestEnemy.y);

        return nextPosition.calculateDistance(closestEnemyCoord) >= currentPosition.calculateDistance(closestEnemyCoord);
    }
}