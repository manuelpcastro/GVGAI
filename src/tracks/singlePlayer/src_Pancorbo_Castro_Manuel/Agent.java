package tracks.singlePlayer.src_Pancorbo_Castro_Manuel;

import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;

public class Agent extends AbstractPlayer {

    public Agent (StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
    }
    @Override
    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
        return Types.ACTIONS.ACTION_NIL;
    }
}
