package simulation;

import geometry.Polygon;
import geometry.Vector;
import graphics.WorldMatrix;

/**
 * Classe representant un ensemble de murs avec lesquels le fluide peut entrer en collision.
 * 
 * @author Nicolas Ouellet-Payeur
 * @version 20 mars
 */
public class DistanceField {
	// dimensions du monde physique
	private WorldMatrix world;
	
	// liste de polygones representant les murs
	private Polygon[] walls;
	
	// rayon entre un mur et une particule pour qu'il y ait collision
	private final double PENETRATION_RADIUS = 10;

	/**
	 * Constructeur principal.
	 * 
	 * @param world Dimensions du monde physique.
	 */
	public DistanceField (WorldMatrix world, Polygon... walls) {
		this.world = world;
		this.walls = walls;
	}
	
	/**
	 * Retourne la force normale exercee sur une particule en un point (x, y).
	 * 
	 * @param x x du point.
	 * @param y y du point.
	 * @param softness Amortissement de la collision (facteur entre 0 et 1).
	 * @return La force normale que le monde applique sur cette particule.
	 */
	public Vector getNormalForce(double x, double y, double softness) {
		Vector normal = Vector.NIL;
		if (x < 0) {
			normal = normal.plus(new Vector(1, 0).times(x*softness));
		}
		if (y < 0) {
			normal = normal.plus(new Vector(0, 1).times(y*softness));
		}
		if (x > world.getMaximumX()) {
			normal = normal.plus(new Vector(-1, 0).times((world.getMaximumX()-x) * softness));
		}
		if (y > world.getMaximumY()) {
			normal = normal.plus(new Vector(0, -1).times((world.getMaximumY() - y) * softness));
		}
		int i;
		for (i = 0; i < walls.length; i++) {
			Polygon wall = walls[i];
			if (wall.distance(x, y) < PENETRATION_RADIUS) {
				double penetration = PENETRATION_RADIUS - wall.distance(x, y);
				if (wall.contains(x, y)) {
					penetration = wall.penetrationDepth(x, y) + PENETRATION_RADIUS;
				}
				normal = normal.plus(wall.normal(x,  y).times(penetration * softness));
			}
		}
		return normal;
	}

	/**
	 * Retourne le vecteur normal au mur le plus proche au point (x,y).
	 * 
	 * @param x Position en x du point pour la normale.
	 * @param y Position en y du point pour la normale.
	 * @return Un vecteur de norme 1 perpendiculaire au mur oriente vers l'exterieur.
	 */
	public Vector getNormalAt (double x, double y) {
		Vector normal = Vector.NIL;
		if (x < 0)
			normal = normal.plus(new Vector(1, 0));
		if (y < 0)
			normal = normal.plus(new Vector (0, 1));
		//if (x > world.getMaximumX())
			normal = normal.plus(new Vector (-1, 0));
		if (y > world.getMaximumY())
			normal = normal.plus(new Vector(0, -1));
		if (normal.getMagnitude()==0)
			return Vector.NIL;
		if (x > y) {
			normal = normal.plus(new Vector(-1, 1));
		}
		return normal.normalize(1);
	}
	
	/**
	 * Retourne le tableau des polygones qui representent les murs, ou obstacles.
	 * 
	 * @return Les murs qui interagissent avec les particules.
	 */
	public Polygon[] getWalls() {
		return walls;
	}
}
