package tracks.singlePlayer.src_Pancorbo_Castro_Manuel;

import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;

import java.util.ArrayList;

public class Agent extends AbstractPlayer {

    Pathfinder pathfinder;
    int i;
    ArrayList<Types.ACTIONS> plan;
    public Agent (StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
        i=0;
        this.pathfinder = new Pathfinder(stateObs.getWorldDimension().width, stateObs.getWorldDimension().height);
        plan = this.pathfinder.pathFinding_a(stateObs.getAvatarOrientation().x, stateObs.getAvatarOrientation().y, stateObs.getAvatarPosition().x, stateObs.getAvatarPosition().y, stateObs.getPortalsPositions()[0].get(0).position.x,stateObs.getPortalsPositions()[0].get(0).position.y);
    }

    @Override
    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
        Types.ACTIONS next = Types.ACTIONS.ACTION_NIL;
        System.out.println(plan.toString());
        if (i < plan.size()) {
            next = plan.get(i);
        } else {
            i = 0;
            plan = this.pathfinder.pathFinding_a(stateObs.getAvatarOrientation().x, stateObs.getAvatarOrientation().y, stateObs.getAvatarPosition().x,stateObs.getAvatarPosition().y, stateObs.getPortalsPositions()[0].get(0).position.x,stateObs.getPortalsPositions()[0].get(0).position.y);
        }
        i++;
        System.out.println("MOVING: " + next);
        return next;
    }


//0.1 SUR
    //1.0 Right
    //-1.0 Left
    //0.-1 UP

    }
