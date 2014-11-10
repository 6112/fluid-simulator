package simulation.solver;

import geometry.Vector;

import java.util.Iterator;
import java.util.LinkedList;

import simulation.Particle;

/**
 * Classe encapsulant les calculs lies a la conversation du volume.
 * 
 * @author Nicolas Ouellet-Payeur, Alexandre D'Amboise
 * @version 1 avril 2014
 */
public class DensitySolver {

	/**
     * Permet de calculer la densite et la densite proche pour chacune des particules voisines
     * a une certaine particule, en fonction de la distance entre elles et la pression et la pression
     * proche entre les particules.
     * 
     * @param particle Particule utilisee.
     * @param deltaT Intervalle de temps
     * @param neighbors LinkedList contenant les particules voisines
     * @param stiffness Rigidite, assure la conservation du volume
     * @param nearbyStiffness Evite que des mini-boules de fluide se forment
     * @param restDensity Densite que le fluide cherche a atteindre
     * @param interactionRadius Rayon d'interaction des particules
     */
    public static void doubleDensityRelaxation (Particle particle, double deltaT, LinkedList<Particle> neighbors,
            double stiffness, double nearbyStiffness, double restDensity, 
            double interactionRadius) {
        Vector position = particle.getPositionVector ();
        double density = 0;
        double nearbyDensity = 0;
        // calculer la densite et la densite proche
        // pour chaque particule voisine
        Iterator<Particle> neighborIterator = neighbors.iterator ();
        while (neighborIterator.hasNext ()) {
            // particule voisine et sa position
            Particle neighbor = neighborIterator.next ();
            Vector neighborPosition = neighbor.getPositionVector ();
            // distance entre les deux particules
            double distance = position.minus (neighborPosition).getMagnitude ();
            double q = 1.0 - distance / interactionRadius;
            // ajouter ce facteur aux densites
            density = density + q * q;
            nearbyDensity = nearbyDensity + q * q * q;
        }
        // calculer la pression et la pression proche a l'aide de la densite et de la densite 
        // proche
        double pressure = stiffness * (density - restDensity);
        particle.setPressure (pressure);
        double nearbyPressure = nearbyStiffness * nearbyDensity;
        // pour chaque particule voisine
        neighborIterator = neighbors.iterator ();
        while (neighborIterator.hasNext ()) {
            // particule voisine et sa position
            Particle neighbor = neighborIterator.next ();
            Vector neighborPosition = neighbor.getPositionVector ();
            // distance entre les particules
            double distance = position.minus (neighborPosition).getMagnitude ();
            double q = 1.0 - distance / interactionRadius;
            // vecteur direction entre les deux particules, donc de norme 1
            Vector direction = neighborPosition.minus (position);
            direction = direction.times (1 / distance);
            // vecteur direction ajuste selon la pression
            Vector displacement = direction
                    .times (0.5 * deltaT * deltaT * (pressure * q + nearbyPressure * q * q));
            // deplacer la particule voisine selon la pression
            neighbor.setStiffnessForce(neighbor.getStiffnessForce().plus(displacement.times(1 / deltaT)));
            neighbor.setPositionVector (neighborPosition.plus (displacement));
        }
    }
	
}
