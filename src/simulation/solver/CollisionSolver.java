package simulation.solver;

import geometry.Vector;
import simulation.DistanceField;
import simulation.Particle;

/**
 * Classe encapsulant les calculs lies a la collision entre le monde et une particule.
 * 
 * @author Nicolas Ouellet-Payeur, Alexandre D'Amboise
 * @version 1 avril 2014
 */
public class CollisionSolver {
	
    /**
     * Permet de resoudre les collisions se produisant sur les 4 "murs" de la fenetre et
     * ainsi de calculer le deplacement resultant des particules dans l'espace.
     * 
     * @param particle Particule utilisee.
     * @param bounds Limites du monde.
     * @param deltaT Intervalle de temps.
     * @param collisionSoftness Facteur influencant la douceur des rebondissements
     */
    public static void resolveCollisions (Particle particle, DistanceField bounds, 
    		double collisionSoftness, double deltaT) {
    	Vector normal = bounds.getNormalForce(particle.getX(), particle.getY(), collisionSoftness);
    	particle.setNormalForce(normal.times(1 / deltaT));
    	particle.setPositionVector(particle.getPositionVector().minus(normal));
    }
    
	
}
