package tracks.singlePlayer.src_Pancorbo_Castro_Manuel;
import ontology.Types;
import tracks.singlePlayer.src_Pancorbo_Castro_Manuel.Coordinates;

import java.util.ArrayList;

/**
 *
 * @author Manuel Pancorbo
 */
class PathfinderOption {
    Integer visitedAt;
    public Coordinates coordinates;
    public double distance;
    public int orientation;

    private ArrayList<Types.ACTIONS> path;

    public PathfinderOption(double x, double y, int orientation, int visited) {
        this.coordinates = new Coordinates(x,y);
        this.orientation = orientation;
        this.path = new ArrayList<>();
        this.visitedAt = visited;
    }

    public PathfinderOption(double x, double y, int orientation) {
        this.coordinates = new Coordinates(x,y);
        this.orientation = orientation;
        this.path = new ArrayList<>();
    }

    public PathfinderOption(Coordinates coordinates) {
        this.coordinates = coordinates;
        this.path = new ArrayList<>();
    }

    void calculateDistance(PathfinderOption target) {
        this.distance = this.coordinates.calculateDistance(target.coordinates);
    }

    void setPath(ArrayList<Types.ACTIONS> parentPath) {
        this.path = (ArrayList<Types.ACTIONS>) parentPath.clone();
        checkTurnArounds(path.get(path.size()-1));
    }

    ArrayList<Types.ACTIONS> getPath() {
        return this.path;
    }

    Boolean checkCoordinates(PathfinderOption target) {
        return this.coordinates.checkCoordinates(target.coordinates);
    }

    @Override
    public String toString() {
        return "PathfinderOption{" +
                "coordinates=" + coordinates +
                ", distance=" + distance +
                ", path=" + path +
                '}';
    }

    private void checkTurnArounds(Types.ACTIONS lastAction){
        PossibleActions lastOrientation = PossibleActions.getPossibleAction(lastAction);
        PossibleActions expected = lastOrientation;
        if(expected.getOrientation() != orientation)
            PossibleActions.howManyTurns(expected, PossibleActions.getPossibleAction(orientation)).forEach(i -> {this.path.add(i.getAction());});
        this.path.add(PossibleActions.getPossibleAction(orientation).getAction());
    }
}
