package tracks.singlePlayer.src_Pancorbo_Castro_Manuel;

import ontology.Types;
import tools.Vector2d;

import java.util.ArrayList;
import java.util.Arrays;

public enum PossibleActions {
    UP(1),
    RIGHT(5),
    LEFT(3),
    DOWN(7);

    private int orientation;
    private Types.ACTIONS action;


    PossibleActions(int i) {
        this.orientation = i;
        this.action = getAction(i);
    }

    public static PossibleActions getOppositeAxisAction(double currentHeight, double currentWidth, PossibleActions possibleAction) {
        System.out.println("---Curent % height: " + currentHeight + " ----- currentWidth % :" + currentWidth);
        if (possibleAction == UP || possibleAction == DOWN) {
            return currentWidth > 0.5 ? LEFT : RIGHT;
        }

        return currentHeight > 0.5 ? UP : DOWN;
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

    public static ArrayList<PossibleActions> howManyTurns (PossibleActions action, PossibleActions wantedAction) {
        ArrayList<PossibleActions> actions = new ArrayList<>();
        if (wantedAction == UP && action == DOWN ||
            wantedAction == LEFT && action == RIGHT ||
            wantedAction == RIGHT && action == LEFT ||
            wantedAction == DOWN && action == UP) {
            actions.add(LEFT);
        }

        actions.add(action);
        return actions;
    }

    public static Vector2d move (Types.ACTIONS action) {
        PossibleActions possibleAction = getPossibleAction(action);
        switch(possibleAction) {
            case UP: return new Vector2d(0,-1);
            case DOWN: return new Vector2d(0,1);
            case LEFT: return new Vector2d(-1,0);
            case RIGHT: return new Vector2d(1,0);
        }
        return null;
    }

    public static PossibleActions getOppositeAction (Types.ACTIONS action) {
        PossibleActions possibleAction = getPossibleAction(action);
        switch(possibleAction) {
            case UP: return DOWN;
            case DOWN: return UP;
            case LEFT: return RIGHT;
            case RIGHT: return LEFT;
        }
        return null;
    }
}
