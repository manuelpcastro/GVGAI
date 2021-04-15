package tracks.singlePlayer.src_Pancorbo_Castro_Manuel;

/**
 * Clase Coordinates, que contiene unicamente las coordenadas de una casilla
 * @author Manuel Pancorbo
 */
class Coordinates {

    public double x;
    public double y;

    /**
     * Constructor
     *
     * @param x posicionX de la coordenada
     * @param y posicionY de la coordenada
     * @author Manuel Pancorbo
     */
    public Coordinates(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Comprueba silas coordenadas coinciden con las del target que se le pasa
     *
     * @param target Coordinates
     * @return booleano indicando si el target está en esas coordenadas
     * @author Manuel Pancorbo
     */
    public Boolean checkCoordinates(Coordinates target) {
        return this.x == target.x && this.y == target.y;
    }

    /**
     * Calcula la distancia de la coordenada a la que se le pasa
     * @param target Coordinates
     * @author Manuel Pancorbo
     * @return distancia a la coordenada pasada
     *
     */
    public double calculateDistance(Coordinates target){
        return Math.abs(this.x - target.x) + Math.abs(this.y - target.y);
    }

    @Override
    public String toString() {
        return "Coordinates{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}