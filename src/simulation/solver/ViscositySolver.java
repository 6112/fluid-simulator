package simulation.solver;

import geometry.Vector;

import java.util.Iterator;
import java.util.LinkedList;

import simulation.Particle;

/**
 * Classe permettant de faire les calculs lies a la viscosite du fluide.
 * 
 * @author Nicolas Ouellet-Payeur, Alexandre D'Amboise
 * @version 1 avril 2014
 */
public class ViscositySolver {

    /**
     * Applique la viscosite sur les particules en tenant compte des particules voisines
     * de celle-ci et de la distance les separant, ainsi que de la velocite.
     * 
     * @param particle Particule utilisee.
     * @param deltaT Intervalle de temps
     * @param neighbors LinkedList permettant d'ajouter les particules voisines
     * @param viscosity Parametre de viscosite 
     * @param interactionRadius Rayon d'interaction entre particules
     */
	public static void applyViscosity (Particle particle, double deltaT, LinkedList<Particle> neighbors, 
            double viscosity, double interactionRadius) {
        // vecteur position de cette particule
        Vector position = particle.getPositionVector ();
        // pour chaque voisin
        Iterator<Particle> neighborIterator = neighbors.iterator ();
        while (neighborIterator.hasNext ()) {
            // voisin et sa position
            Particle neighbor = neighborIterator.next ();
            Vector neighborPosition = neighbor.getPositionVector ();
            // distance vectorielle entre les deux voisins
            Vector displacement = position.minus (neighborPosition);
            // velocite interieure
            double inwardVelocity = particle.getVelocity ().minus (neighbor.getVelocity ())
                    .scalarProduct (displacement);
            // si la velocite interieure est positive
            if (inwardVelocity > 0) {
                // distance entre les particules
                double distance = displacement.getMagnitude ();
                inwardVelocity /= distance;
                // direction de la distance entre les particules
                Vector direction = displacement.times (1 / distance);
                double q = distance / interactionRadius;
                // difference de velocite a appliquer
                Vector I = direction.times (0.5 * deltaT * (1 - q) * 
                        (viscosity * inwardVelocity * inwardVelocity));
                particle.setViscosityForce(particle.getViscosityForce().minus(I.times(1/deltaT)));
                particle.setVelocity (particle.getVelocity ().minus (I));
            }
        }
    }
	
}
