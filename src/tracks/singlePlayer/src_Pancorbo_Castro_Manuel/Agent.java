package tracks.singlePlayer.src_Pancorbo_Castro_Manuel;

import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.Vector2d;

import java.util.ArrayList;
import java.util.Arrays;

public class Agent extends AbstractPlayer {

    private double DISTANCE_TO_ENEMIES = 8.0;
    private Pathfinder pathfinder;
    private Vector2d fescala;
    private int i;
    private ArrayList<Types.ACTIONS> plan;
    private ArrayList<Types.ACTIONS> planB; //Por si vienen enemigos a por nosotros!

    boolean enemiesInGame;
    boolean resourcesInGame;

    public Agent (StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
        fescala = new Vector2d(stateObs.getWorldDimension().width / stateObs.getObservationGrid().length, stateObs.getWorldDimension().height / stateObs.getObservationGrid()[0].length);
        i=0;
        ArrayList<Vector2d> immovablePositions = new ArrayList(Arrays.asList(Arrays.stream(stateObs.getImmovablePositions()).flatMap(i -> i.stream().map(j -> applyScale(j.position))).toArray()));
        if (stateObs.getResourcesPositions() != null) {
            ArrayList<Vector2d> resources = new ArrayList(Arrays.asList(Arrays.stream(stateObs.getResourcesPositions()).flatMap(i -> i.stream().map(j -> applyScale(j.position))).toArray()));
            this.pathfinder = new Pathfinder(applyScale(new Vector2d(stateObs.getWorldDimension().width,stateObs.getWorldDimension().height)), immovablePositions, resources);
        } else
            this.pathfinder = new Pathfinder(applyScale(new Vector2d(stateObs.getWorldDimension().width,stateObs.getWorldDimension().height)), immovablePositions);
        this.plan = new ArrayList<>();
        this.planB = new ArrayList<>();

        this.enemiesInGame = stateObs.getNPCPositions() != null;
        if (enemiesInGame && stateObs.getNPCPositions()[0].size() > 1) {
        // Ajustamos la distancia segun el numero de enemigos
        // si hay mas enemigos, queremos empezar a planear la estrategia B con mayor antelacion
           DISTANCE_TO_ENEMIES = DISTANCE_TO_ENEMIES *(stateObs.getNPCPositions()[0].size() - 1)*1.5;
        }

        this.resourcesInGame = stateObs.getResourcesPositions() != null;
    }

    @Override
    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
        Types.ACTIONS next = Types.ACTIONS.ACTION_NIL;
        Vector2d myPosition = applyScale(stateObs.getAvatarPosition());

        if (enemiesInGame) {
            // Comprobamos si debemos seguir nuestro plan para llegar al objetivo o debemos tener cuidado con enemigos
            next = checkEnemies(stateObs, myPosition);
            /* Si la accion devuelta es null
                 1. bien no hay enemigos cerca
                 2. hay enemigos cerca, pero siguiendo el plan nos alejamos de ellos
             */
            if (next != null)
                return next;
        }

        // Todavia tenemos un plan que seguir
        boolean followingPlan = i < plan.size();
        if (followingPlan) {
            next = plan.get(i);
            i++;
            return next;
        }

        //Volvemos a empezar con un plan nuevo
        i = 0;

        // Si hay gemas por coger, primero debemos recogerlas
        // Esta comprobacion se debe actualizar, ya que cuando terminemos de recoger
        // nos devolvera null porque no queda ninguna en la partida
        this.resourcesInGame = stateObs.getResourcesPositions() != null;
        if (resourcesInGame) {
            //Si no hemos conseguido todas, vamos a seguimos buscando
            ArrayList<Vector2d> updatedResources = new ArrayList(Arrays.asList(Arrays.stream(stateObs.getResourcesPositions()).flatMap(i -> i.stream().map(j -> applyScale(j.position))).toArray()));
            plan = this.pathfinder.getPlanForResources(updatedResources, stateObs.getAvatarOrientation().x, stateObs.getAvatarOrientation().y, myPosition);
        } else {
            // Hacemos plan para llegar a la puerta de salida
            plan = this.pathfinder.pathFinding_a(stateObs.getAvatarOrientation().x, stateObs.getAvatarOrientation().y, applyScale(stateObs.getAvatarPosition()), applyScale(stateObs.getPortalsPositions()[0].get(0).position) );
        }

        i++;
        return next;
    }


    private Types.ACTIONS checkEnemies (StateObservation stateObs, Vector2d myPosition) {
        ArrayList<Vector2d> updatedEnemies = new ArrayList(Arrays.asList(Arrays.stream(stateObs.getNPCPositions()).flatMap(i -> i.stream().map(j -> applyScale(j.position))).toArray()));

        if (areEnemiesNearby(myPosition, updatedEnemies)) {
            Types.ACTIONS action;
            if (plan.size() == 0)
                //No hemos empezado un plan
                action = null;
            else {
                //Nuestra ultima accion. La comprobacion es por simple gestion para evitar excepciones out of index
                action = i >= plan.size() ? plan.get(i - 1) : plan.get(i);
            }
            // Comprueba si el siguiente paso acerca al personaje a los enemigos o no.
            // Si lo acerca, generara un PlanB. Si no, podremos seguir con el plan principal.
            if (!this.pathfinder.shouldIContinue(action, myPosition, updatedEnemies)) {
                // Generamos el planB
                planB = this.pathfinder.whereToRun(action, myPosition, updatedEnemies);
                return planB.get(0);
            }
        }

        return null;
    }

    /*
     * Este metodo puede presentar problemas con caminos/espacios muy cerrados, pues incluso aunque un enemigo no se encuentre
     * al alcance del personaje puede hacer que este bloquee cuando no deberia (por ejemplo: un enemigo esta dos bloques mas
     * arriba que el personaje y hay una barra horizontal lo suficientemente larga que no permite al enemigo pasar a por nosotros)
     *
     * Una alternativa podria ser intentar calcular cuantos pasos tomaria al enemigo llegar hasta nuestra posicion, y evaluar esa cantidad.
     * Pero tras hacer pruebas con los mapas proporcionados para la practica y aquellos de años anteriores creo que es suficiente. Aparte de
     * que eso requeria mas tiempo de procesamiento, que podria hacer que tardaramos demasiado en devolver una accion.
     */
    private boolean areEnemiesNearby(Vector2d myPosition, ArrayList<Vector2d> updatedEnemies) {
         for(Vector2d v : updatedEnemies) {
             if (new Coordinates(v.x, v.y).calculateDistance(new Coordinates(myPosition.x, myPosition.y)) < DISTANCE_TO_ENEMIES)
                return true;
        }
       return false;
    }

    //Este metodo es esencial para poder aplicar correctamente el algoritmo y calculos sobre entidades y el mapa
    private Vector2d applyScale(Vector2d vector) {
        return new Vector2d(vector.x / fescala.x, vector.y / fescala.y);
    }


}
