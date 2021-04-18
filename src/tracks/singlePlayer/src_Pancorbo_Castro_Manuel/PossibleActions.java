package tracks.singlePlayer.src_Pancorbo_Castro_Manuel;

import ontology.Types;
import tools.Vector2d;

import java.util.ArrayList;
import java.util.Arrays;

public enum PossibleActions {
    UP(1),
    RIGHT(5),
    LEFT(3),
    DOWN(7),
    STILL(4);

    private int orientation;
    private Types.ACTIONS action;


    PossibleActions(int i) {
        this.orientation = i;
        this.action = getAction(i);
    }

    public int getOrientation() {
        return orientation;
    }

    public Types.ACTIONS getAction() {
        return action;
    }

    private Types.ACTIONS getAction(int index) {
        switch(index) {
            case 1: return Types.ACTIONS.ACTION_UP;
            case 3: return Types.ACTIONS.ACTION_LEFT;
            case 5: return Types.ACTIONS.ACTION_RIGHT;
            case 7: return Types.ACTIONS.ACTION_DOWN;
        }
        return null;
    }

    public static PossibleActions getPossibleAction(int i) {
        ArrayList<PossibleActions> possibleActions = new ArrayList<>(Arrays.asList(UP, RIGHT, LEFT, DOWN, STILL));
        for (PossibleActions possibleAction : possibleActions) {
            if (possibleAction.getOrientation() == i)
                return possibleAction;
        }
        return null;
    }

    public static PossibleActions getPossibleAction(Types.ACTIONS action) {
        ArrayList<PossibleActions> possibleActions = new ArrayList<>(Arrays.asList(UP, RIGHT, LEFT, DOWN, STILL));
        for (PossibleActions possibleAction : possibleActions) {
            if (possibleAction.getAction() == action)
                return possibleAction;
        }
        return null;
    }

    public static Vector2d move (Types.ACTIONS action) {
        PossibleActions possibleAction = getPossibleAction(action);
        switch(possibleAction) {
            case UP: return new Vector2d(0,-1);
            case DOWN: return new Vector2d(0,1);
            case LEFT: return new Vector2d(-1,0);
            case RIGHT: return new Vector2d(1,0);
        }
        return new Vector2d(0,0);
    }

    public static int getOrientation(double x, double y) {
        if(x==1) return 5;
        if(x==-1) return 3;
        if(y==1) return 7;
        if(y==-1) return 1;
        return 0;
    }
}
