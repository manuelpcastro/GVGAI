package tracks.singlePlayer.src_Pancorbo_Castro_Manuel;

import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.Vector2d;

import java.util.ArrayList;
import java.util.Arrays;

public class Agent extends AbstractPlayer {

    Pathfinder pathfinder;
    Vector2d fescala;
    int i;
    ArrayList<Types.ACTIONS> plan;
    public Agent (StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
        fescala = new Vector2d(stateObs.getWorldDimension().width / stateObs.getObservationGrid().length,
                stateObs.getWorldDimension().height / stateObs.getObservationGrid()[0].length);
        i=0;
        ArrayList<Vector2d> inmovablePositions = new ArrayList(Arrays.asList(Arrays.stream(stateObs.getImmovablePositions()).flatMap(i -> i.stream().map(j -> applyScale(j.position))).toArray()));
        System.out.println("Blocked: " + inmovablePositions);
        this.pathfinder = new Pathfinder(stateObs.getWorldDimension().width, stateObs.getWorldDimension().height, inmovablePositions);
        plan = this.pathfinder.pathFinding_a(stateObs.getAvatarOrientation().x, stateObs.getAvatarOrientation().y, applyScale(stateObs.getAvatarPosition()), applyScale(new Vector2d(stateObs.getPortalsPositions()[0].get(0).position.x,stateObs.getPortalsPositions()[0].get(0).position.y)));
    }

    @Override
    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
        Types.ACTIONS next = Types.ACTIONS.ACTION_NIL;
        if (i < plan.size()) {
            next = plan.get(i);
        } else {
            i = 0;
            plan = this.pathfinder.pathFinding_a(stateObs.getAvatarOrientation().x, stateObs.getAvatarOrientation().y, applyScale(stateObs.getAvatarPosition()), applyScale(new Vector2d(stateObs.getPortalsPositions()[0].get(0).position.x,stateObs.getPortalsPositions()[0].get(0).position.y)));
        }
        i++;
        System.out.println("MOVING: " + next);
        return next;
    }

    private Vector2d applyScale(Vector2d vector) {
        return new Vector2d(vector.x / fescala.x, vector.y / fescala.y);
    }

//0.1 SUR
    //1.0 Right
    //-1.0 Left
    //0.-1 UP

}
