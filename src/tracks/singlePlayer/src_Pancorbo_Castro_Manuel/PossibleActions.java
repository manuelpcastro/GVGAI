package tracks.singlePlayer.src_Pancorbo_Castro_Manuel;

import ontology.Types;

import java.util.ArrayList;
import java.util.Arrays;

public enum PossibleActions {
    UP(1),
    RIGHT(3),
    LEFT(5),
    DOWN(7);

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
        return Types.ACTIONS.ACTION_NIL;
    }

    public static PossibleActions getPossibleAction(int i) {
        ArrayList<PossibleActions> possibleActions = new ArrayList<>(Arrays.asList(UP, RIGHT, LEFT, DOWN));
        for (PossibleActions possibleAction : possibleActions) {
            if (possibleAction.getOrientation() == i)
                return possibleAction;
        }
        return null;
    }

    public static PossibleActions getPossibleAction(Types.ACTIONS action) {
        ArrayList<PossibleActions> possibleActions = new ArrayList<>(Arrays.asList(UP, RIGHT, LEFT, DOWN));
        for (PossibleActions possibleAction : possibleActions) {
            if (possibleAction.getAction() == action)
                return possibleAction;
        }
        return null;
    }
}
