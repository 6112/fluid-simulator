package simulation;

import geometry.Polygon;
import geometry.Vector;
import graphics.WorldMatrix;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.TreeMap;

import javax.swing.event.EventListenerList;

import listeners.SimulationListener;
import simulation.solver.CollisionSolver;
import simulation.solver.DensitySolver;
import simulation.solver.ViscositySolver;

/**
 * Classe implementant les methodes relatives a la simulation. Permet d'appliquer
 * les forces et les parametres predefinis a une ou plusieurs particules.
 * 
 * @author Nicolas Ouellet-Payeur / Le codage des methodes est inspire
 *  en grande partie de "http://www.diva-portal.org/smash/get/diva2:676516/FULLTEXT01.pdf".  
 * @version 13 fevrier 2014
 */
public class Simulation {
    // distance a partir de laquelle la souris applique une force
    private final double MOUSE_PULL_DISTANCE = 80;
    
    // facteur de la force de la souris
    private final double MOUSE_PULL_FACTOR = 0.25;

    // facteur applique aux collisions avec le monde. La valeur devrait etre entre 0 et 1. Une
    // valeur basse fait que les collisions sont plus "douces", avec un certain rebondissement
    private final double COLLISION_SOFTNESS = 0.3;

    // direction par defautde la gravite au debut de la simulation
    private static final Vector DEFAULT_GRAVITY_DIRECTION = new Vector (0, -1);

    // norme par defaut du vecteur gravite
    private static final double DEFAULT_GRAVITY_MAGNITUDE = 250;

    // gravite en ce moment
    private Vector gravity;
    
    // rayon maximal des interactions entre deux particules
    private double radius;
    private static final double DEFAULT_RADIUS = 45;
    
    // facteur qui assure la conservation du volume; une haute valeur montre une conservation plus
    // aggressive du volume. Ce facteur s'appelle "rigidite" en francais
    private double stiffness;
    private static final double DEFAULT_STIFFNESS = 80;

    // si la rigidite est trop elevee, utiliser la rigidite proche plutot; elle empeche que les 
    // particules forment des minis "boules" de fluide separees
    private double nearbyStiffness;
    private static final double DEFAULT_NEARBY_STIFFNESS = 300;
    
    // densite que le fluide cherche a atteindre. La densite de chaque particule devrait tendre vers
    // cette valeur. Une haute densite diminue le volume pour un meme nombre de particules.
    private double restDensity;
    private static final double DEFAULT_REST_DENSITY = 15;
    
    // dependance quadratique de la viscosite par rapport a la vitesse
    private double viscosity;
    private static final double DEFAULT_VISCOSITY = 0.02;
    
    private boolean small = false;
    
    // liste des particules
    private ArrayList<Particle> particles;
    
    // une liste des particules voisines pour chaque particule
    private TreeMap<Particle,LinkedList<Particle>> neighborLists;
    
    // dimensions du monde
    private WorldMatrix world;
    
    // murs dans le monde
    private DistanceField bounds;
    
    // grille qui permet de connaitre les voisins de chaque particule
    private ParticleGrid grid;  
    
    // liste des ecouteurs d'evenements
	private final EventListenerList listeners = new EventListenerList();
	
    /**
     * Constructeur principal.
     * 
     * @param boundaries Dimensions du monde physique.
     * @param radius Rayon des interactions entre deux particules.
     * @param stiffness Rigidite.
     * @param nearbyStiffness Rigidite proche.
     * @param restDensity Densite au repos.
     * @param viscosity Viscosite (quadratique).
     * @param gravity Vecteur gravite.
     * @param walls Murs de la simulation.
     */
    public Simulation (WorldMatrix boundaries, double radius, double stiffness, 
            double nearbyStiffness, double restDensity, double viscosity, Vector gravity,
            Polygon... walls) {
        this.radius = radius;
        this.stiffness = stiffness;
        this.nearbyStiffness = nearbyStiffness;
        this.restDensity = restDensity;
        this.viscosity = viscosity;
        this.gravity = gravity;
        this.particles = new ArrayList<Particle> ();
        this.neighborLists = new TreeMap<Particle,LinkedList<Particle>> ();
        this.world = boundaries;
        this.grid = new ParticleGrid (world, this.radius);
        this.bounds = new DistanceField(world, walls);
    }
    
    /**
     * Constructeur alternatif. Utilise des valeurs par defaut pour la plupart des parametres.
     * 
     * @param boundaries Dimensions du monde physique.
     * @param walls Murs contenus par la simulation.
     */
    public Simulation (WorldMatrix boundaries, Polygon... walls) {
        this (boundaries, DEFAULT_RADIUS, DEFAULT_STIFFNESS, DEFAULT_NEARBY_STIFFNESS,
                DEFAULT_REST_DENSITY, DEFAULT_VISCOSITY, 
                DEFAULT_GRAVITY_DIRECTION.times (DEFAULT_GRAVITY_MAGNITUDE),
                walls);
    }
    
    /**
     * Ajoute une particule a la simulation.
     * 
     * @param particle Particule a ajouter.
     */
    public void addParticle (Particle particle) {
        particles.add (particle);
        grid.addParticle (particle);
        neighborLists.put (particle, new LinkedList<Particle> ());
    }

    /**
     * Avance la simulation d'un intervalle de temps deltaT, en utilisant mousePosition si non-nul
     * pour appliquer des forces exterieures.
     * 
     * @param deltaT Intervalle de temps.
     * @param mousePosition Position de la souris si elle doit interagir avec la simulation, ou null
     * @param previousMousePosition Position de la souris a la derniere iteration, ou null
     * si elle n'interagit pas.
     */
    public void update (double deltaT, Point2D.Double mousePosition, Point2D.Double previousMousePosition) {
        // appliquer les forces externes (gravite, forces de la souris)
        applyExternalForces (deltaT, mousePosition, previousMousePosition);
        // appliquer la viscosite
        applyViscosity (deltaT);
        // deplacer selon la vitesse
        advanceParticles (deltaT);
        // mettre a jour les voisins
        updateNeighbors ();
        // ajuster selon la densite
        doubleDensityRelaxation (deltaT);
        // resoudre les collisions
        resolveCollisions (deltaT);
        // lancer l'evenement pour chaque etape de la simulation
    	startEventFrameEntered();
        // mettre a jour la velocite
        updateVelocity (deltaT);
    }
    
    /**
     * Applique les forces externes si approprie.
     * 
     * @param deltaT Intervalle de temps.
     * @param mousePosition Position de la souris, ou null si elle n'interagit pas avec la
     * simulation.
     */
    private void applyExternalForces (double deltaT, Point2D.Double mousePosition, Point2D.Double previousMousePosition) {
        // pour chaque particule
        ListIterator<Particle> iterator = particles.listIterator ();
        while (iterator.hasNext ()) {
            // particule courante
            Particle particle = iterator.next ();
            // appliquer la gravite
            particle.setVelocity (particle.getVelocity ().plus (gravity.times (deltaT)));
            // si l'utilisateur est en train de cliquer
            if (mousePosition != null) {            	
                // appliquer la force de la souris
                Vector mouseForce = getMouseForce (particle, mousePosition, previousMousePosition, deltaT);
                particle.setVelocity (particle.getVelocity ().plus (mouseForce));
            }
        }
    }
    
    /**
     * Retourne le vecteur de la force que la souris applique sur une particule.
     * 
     * @param particle Particule pour laquelle on veut le vecteur force.
     * @param mousePosition Position de la souris, non-nulle.
     * @return Force appliquee sur particle.
     */
    private Vector getMouseForce (Particle particle, Point2D.Double mousePosition, Point2D.Double previousMousePosition, double deltaT) {
        Vector m = new Vector (mousePosition);
        Vector m2 = previousMousePosition == null ? m : new Vector (previousMousePosition);
        Vector d = m.minus (particle.getPositionVector ());
        Vector vm = m.minus (m2);
        if (d.getMagnitude () < MOUSE_PULL_DISTANCE) {
            return d.times(MOUSE_PULL_FACTOR).plus (vm.times (1)).minus(gravity.times(deltaT));
        }
        return Vector.NIL;
    }
    
    /**
     * Applique la viscosite sur toutes les particules pour que les particules s'attirent un peu 
     * entre elles. Donne une certaine consistence au liquide.
     * 
     * @param deltaT Intervalle de temps.
     */
    private void applyViscosity (double deltaT) {
        // pour chaque particule
        ListIterator<Particle> iterator = particles.listIterator ();
        while (iterator.hasNext ()) {
            // particule courante, son indice et sa position
            Particle particle = iterator.next ();
            // trouver les voisins de cette particule
            LinkedList<Particle> neighborList = neighborLists.get (particle);
            // appliquer la viscosite
            particle.setViscosityForce(Vector.NIL);
            ViscositySolver.applyViscosity (particle, deltaT, neighborList, viscosity, 
                    radius);
        }
    }
    
    /**
     * Deplace les particules d'apres leur velocite.
     * 
     * @param deltaT Intervalle de temps.
     */
    private void advanceParticles (double deltaT) {
        // pour chaque particule
        ListIterator<Particle> iterator = particles.listIterator ();
        while (iterator.hasNext ()) {
            // particule courante
            Particle particle = iterator.next ();
            // avancer la particule
            particle.advance (deltaT);
        }
    }
    
    /**
     * Met a jour les listes des voisins pour chaque particule.
     */
    private void updateNeighbors () {
        // mettre a jour la position de chaque particule dans la grille
    	grid.update ();
    	// pour chaque particule
    	ListIterator<Particle> iterator = particles.listIterator ();
    	while (iterator.hasNext ()) {
    	    // particule et son indice
    	    Particle particle = iterator.next ();
    	    // mettre a jour la liste des voisins
            LinkedList<Particle> neighbors = grid.neighborsOf (particle);
            neighborLists.put (particle, neighbors);
    	}
    }
    
    /**
     * Ajuste la position des particules selon les particules voisines, pour que la densite tende
     * vers une valeur precise.
     * 
     * @param deltaT Intervalle de temps.
     */
    private void doubleDensityRelaxation (double deltaT) {
        // pour chaque particule
        ListIterator<Particle> iterator = particles.listIterator ();
        while(iterator.hasNext()) {
        	Particle particle = iterator.next();
        	particle.setStiffnessForce(Vector.NIL);
        }
        iterator = particles.listIterator();
        while (iterator.hasNext ()) {
            // position, indice et particule courante
            Particle particle = iterator.next ();
            LinkedList<Particle> neighborList = neighborLists.get (particle);
            // appliquer l'ajustement de la densite
            DensitySolver.doubleDensityRelaxation (particle, deltaT, neighborList, stiffness, nearbyStiffness,
                    restDensity, radius);
        }
    }
    
    /**
     * Resout les collisions entre les murs et les particules.
     */
    private void resolveCollisions (double deltaT) {
        // pour chauqe particule
        ListIterator<Particle> iterator = particles.listIterator ();
        while (iterator.hasNext ()) {
            // particule courante
            Particle particle = iterator.next ();
            // resoudre les collisions
            CollisionSolver.resolveCollisions (particle, bounds, COLLISION_SOFTNESS, deltaT);
        }
    }
    
    /**
     * Met a jour la velocite des particules.
     * 
     * @param deltaT Intervalle de temps.
     */
    private void updateVelocity (double deltaT) {
        // pour chaque particule
        ListIterator<Particle> iterator = particles.listIterator ();
        while (iterator.hasNext ()) {
            // particule courante
            Particle particle = iterator.next ();
            // affecter comme velocite le deplacement divise par l'intervalle de temps
            Vector velocity = particle.getPositionVector ()
                    .minus (particle.getPreviousPositionVector ())
                    .times (1 / deltaT);
            particle.setVelocity (velocity);
        }
    }
    
    /**
     * Retourne un iterateur pour la liste des particules de la simulation.
     * 
     * @return Iterateur pour les particules.
     */
    public Iterator<Particle> getParticleIterator () {
        return particles.iterator ();
    }
    
    public Polygon[] getWalls() {
    	return bounds.getWalls();
    }

    /**
     * Retourne le vecteur gravite.
     * 
     * @return Vecteur gravite.
     */
    public Vector getGravity () {
        return gravity;
    }

    /**
     * Affecte la valeur de la gravite.
     * 
     * @param gravity Nouvelle gravite.
     */
    public void setGravity (Vector gravity) {
        this.gravity = gravity;
    }

    /**
     * Retourne le rayon des interactions entre les particules.
     * 
     * @return Rayon des interactions entre les particules.
     */
    public double getRadius () {
        return radius;
    }

    /**
     * Retourne la rigidite du fluide. La rigidite permet la conservation du volume dans le fluide.
     * 
     * @return Rigidite du fluide.
     */
    public double getStiffness () {
        return stiffness;
    }

    /**
     * Affecte la valeur de la rigidite du fluide.
     * 
     * @param stiffness Nouvelle rigidite.
     */
    public void setStiffness (double stiffness) {
        this.stiffness = stiffness;
    }

    /**
     * Retourne la rigidite proche. Empeche que les particules du fluide se collent
     * les unes aux autres et creent de minis "globules".
     * 
     * @return Rigidite proche.
     */
    public double getNearbyStiffness () {
        return nearbyStiffness;
    }

    /**
     * Affecte la valeur de la rigidite proche du fluide.
     * 
     * @param nearbyStiffness Nouvelle rigidite proche.
     */
    public void setNearbyStiffness (double nearbyStiffness) {
        this.nearbyStiffness = nearbyStiffness;
    }

    /**
     * Retourne la densite cible du fluide.
     * 
     * @return Densite cible.
     */
    public double getRestDensity () {
        return restDensity;
    }

    /**
     * Affecte la valeur de la densite cible du fluide.
     * 
     * @param restDensity Nouvelle densite cible.
     */
    public void setRestDensity (double restDensity) {
        this.restDensity = restDensity;
    }

    /**
     * Retourne la viscosite du fluide.
     * 
     * @return Viscosite.
     */
    public double getViscosity () {
        return viscosity;
    }

    /**
     * Affecte la valeur de la viscosite du fluide.
     * 
     * @param viscosity Nouvelle viscosite.
     */
    public void setViscosity (double viscosity) {
        this.viscosity = viscosity;
    }

    /**
     * Retourne vrai ssi les particules sont dessinees en petit (pour mieux les visualiser).
     * 
     * @return Vrai si les particules sont dessinees en petit.
     */
	public boolean isSmall() {
		return small;
	}

	/**
	 * Choisit si les particules doivent etres dessinees en petit (pour mieux les visualiser).
	 * 
	 * @param small Vrai ssi les particules doivent etre dessinees en petit.
	 */
	public void setSmall(boolean small) {
		this.small = small;
		Iterator<Particle> iterator = getParticleIterator();
		while(iterator.hasNext()) {
			Particle particle = iterator.next();
			particle.setSmall(small);
		}
	}
	
	/**
	 * Ajoute un ecouteur de type SimulationListener a cette simulation.
	 * 
	 * @param listener Ecouteur a ajouter.
	 */
	public void addSimulationListener(SimulationListener listener) {
		listeners.add(SimulationListener.class, listener);
	}
	
	// lance l'evenement pour chaque nouvelle etape de la simulation
	private void startEventFrameEntered() {
		for (SimulationListener listener : listeners.getListeners(SimulationListener.class)) {
			listener.frameEntered();
		}
	}
}
