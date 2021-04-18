package tracks.singlePlayer.src_Pancorbo_Castro_Manuel;

import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.Vector2d;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class Agent extends AbstractPlayer {

    Pathfinder pathfinder;
    Vector2d fescala;
    int i;
    ArrayList<Types.ACTIONS> plan;
    ArrayList<Types.ACTIONS> planB; //Por si vienen enemigos a por nosotros!
    int iPlanB;


    public Agent (StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
        fescala = new Vector2d(stateObs.getWorldDimension().width / stateObs.getObservationGrid().length, stateObs.getWorldDimension().height / stateObs.getObservationGrid()[0].length);
        i=0;
        iPlanB=0;
        ArrayList<Vector2d> inmovablePositions = new ArrayList(Arrays.asList(Arrays.stream(stateObs.getImmovablePositions()).flatMap(i -> i.stream().map(j -> applyScale(j.position))).toArray()));
        System.out.println("Blocked: " + inmovablePositions);
        if (stateObs.getResourcesPositions() != null) {
            ArrayList<Vector2d> resources = new ArrayList(Arrays.asList(Arrays.stream(stateObs.getResourcesPositions()).flatMap(i -> i.stream().map(j -> applyScale(j.position))).toArray()));
            this.pathfinder = new Pathfinder(applyScale(new Vector2d(stateObs.getWorldDimension().width,stateObs.getWorldDimension().height)), inmovablePositions, resources);
        } else
            this.pathfinder = new Pathfinder(applyScale(new Vector2d(stateObs.getWorldDimension().width,stateObs.getWorldDimension().height)), inmovablePositions);
        this.plan = new ArrayList<>();
        this.planB = new ArrayList<>();
    }

    @Override
    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
        Types.ACTIONS next = Types.ACTIONS.ACTION_NIL;
        Vector2d myPosition = applyScale(stateObs.getAvatarPosition());
        if (stateObs.getNPCPositions() != null) {
            System.out.println ("Evaluating all enemies");
            ArrayList<Vector2d> updatedEnemies = new ArrayList(Arrays.asList(Arrays.stream(stateObs.getNPCPositions()).flatMap(i -> i.stream().map(j -> applyScale(j.position))).toArray()));

            if (areEnemiesNearby(myPosition, updatedEnemies)) {
                System.out.println ("Evaluating all enemies");
                Types.ACTIONS action;
                if (plan.size() == 0)
                    action = null;
                else
                    action = i >= plan.size() ? plan.get(i - 1) : plan.get(i);
                if (!this.pathfinder.shouldIContinue(action, myPosition, updatedEnemies)) {

                    planB = this.pathfinder.whereToRun(action, myPosition, updatedEnemies);
                    iPlanB = 0;

                    next = planB.get(iPlanB);
                    iPlanB++;
                    return next;
                }
            }
        }

        if (i < plan.size()) {
            next = plan.get(i);
        } else {
            i = 0; //Volvemos a empezar a seguir el plan

            if (stateObs.getResourcesPositions() != null) {
                if (stateObs.getResourcesPositions()[0].size() > 0) { //Si no hemos conseguido todos, vamos a seguimos buscando
                    ArrayList<Vector2d> updatedResources = new ArrayList(Arrays.asList(Arrays.stream(stateObs.getResourcesPositions()).flatMap(i -> i.stream().map(j -> applyScale(j.position))).toArray()));
                    plan = this.pathfinder.getPlanForResources(updatedResources, stateObs.getAvatarOrientation().x, stateObs.getAvatarOrientation().y, myPosition);
                }
            } else {
                                                                                                                                                                    //new Vector2d(5.0,5.0)
                plan = this.pathfinder.pathFinding_a(stateObs.getAvatarOrientation().x, stateObs.getAvatarOrientation().y, applyScale(stateObs.getAvatarPosition()), applyScale(stateObs.getPortalsPositions()[0].get(0).position) );
            }
        }
        i++;
        System.out.println("MOVING: " + next);

        return next;
    }

    boolean areEnemiesNearby(Vector2d myPosition, ArrayList<Vector2d> updatedEnemies) {
         for(Vector2d v : updatedEnemies) {
            System.out.println("Enemy position: " + v + " --- distance from me: " + new Coordinates(v.x, v.y).calculateDistance(new Coordinates(myPosition.x, myPosition.y)));
            if (new Coordinates(v.x, v.y).calculateDistance(new Coordinates(myPosition.x, myPosition.y)) < 12.0)
                return true;
        }
       return false;
    }

    private Vector2d applyScale(Vector2d vector) {
        return new Vector2d(vector.x / fescala.x, vector.y / fescala.y);
    }

//0.1 SUR
    //1.0 Right
    //-1.0 Left
    //0.-1 UP

}
