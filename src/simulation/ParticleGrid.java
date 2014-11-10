package simulation;

import geometry.Vector;
import graphics.WorldMatrix;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Permet d'acceder rapidement aux particules qui sont voisines d'une autre particule.
 * 
 * @author Alexandre D'Amboise
 * @version 13 fevrier 2014
 */
public class ParticleGrid {
	// collection des particules que contient cette grille
	private ArrayList<Particle> particles;

	// grille contenant les listes des particules dans chaque partie de la grille
	private LinkedList<Particle>[][] grid;

	private double particleRadius;

	// taille d'un element de la grille, en unites physiques
	private double cellSize;
	
	// nombre de cellules a l'horizontale et a la verticale
	private int verticalCellCount;
	private int horizontalCellCount;

	/**
	 * Constructeur principal.
	 * 
	 * @param world Dimensions du monde a utiliser.
	 * @param particleRadius Rayon maximal qu'une particule peut avoir.
	 */ 
	public ParticleGrid (WorldMatrix world, double particleRadius) {
		this.particles = new ArrayList<Particle> ();
		this.particleRadius = particleRadius;
		cellSize = particleRadius;
		verticalCellCount = (int) Math.ceil (world.getHeight () / cellSize)+2;
		horizontalCellCount = (int) Math.ceil (world.getWidth () / cellSize)+2;
		// le warning ci-dessous ne peut pas etre contourne, donc il est la pour rester
		this.grid = (LinkedList<Particle>[][]) (new LinkedList[verticalCellCount][horizontalCellCount]);
		int x;
		int y;
		for (x = 0; x < horizontalCellCount; x++)
			for (y = 0; y < verticalCellCount; y++)
				grid[y][x] = new LinkedList<Particle> ();
	}
	
	/**
	 * Ajoute une particule a la liste des particules dans la grille.
	 * 
	 * @param particle Particule a ajouter.
	 */
	public void addParticle (Particle particle) {
	    particles.add (particle);
	}

	/**
	 * Met a jour la position de chacune des particules dans la grille.
	 */
	public void update () {
		int x;
		int y;
		for (x=0; x<horizontalCellCount; x++) {
			for (y=0; y<verticalCellCount; y++) {
				grid[y][x].clear();
			}
		}
		Iterator<Particle> iter = particles.iterator ();
		while (iter.hasNext()) {
			Particle particle = iter.next ();
			int particleX = getParticleIndexX (particle);
			int particleY = getParticleIndexY (particle);
			if (outOfBounds (particleX, particleY))	continue;
			grid [particleY][particleX].add (particle);
		}
	}

	/**
	 * Retourne la liste des particules qui sont voisines d'une particule. Deux particules sont
	 * voisines si elles se touchent, sont en collision.
	 * 
	 * @param particle Particule pour laquelle on veut connaitre les voisins.
	 * @return Liste des particules voisines de la particule.
	 */
	public LinkedList<Particle> neighborsOf (Particle particle) {
		LinkedList<Particle> neighbors = new LinkedList<Particle> ();
		int dx;
		int dy;
		int y = getParticleIndexY(particle);
		int x = getParticleIndexX(particle);
		for (dx=-1; dx<=1; dx++){
			for (dy=-1; dy<=1; dy++){
				if (outOfBounds(x+dx,y+dy)) continue;
				else {
					LinkedList<Particle> cell = grid[y+dy][x+dx];
					Iterator<Particle> cells = cell.iterator();
					while(cells.hasNext()){
						Particle next = cells.next ();
						Vector d = particle.getPositionVector().minus(next.getPositionVector());
						double distance = d.getMagnitude ();
						if (distance <= particleRadius && particle != next){
							neighbors.add (next);
						}
					}
				}
			}
		}
		return neighbors;
	}
	
	/**
	 * Permet de determiner si les particules sont "out of bounds" (hors limites)
	 * en fonction de leur position et du nombre de cellules de la grille.
	 * 
	 * @param x Position en x de la particule
	 * @param y Position en y de la particule
	 * @return vrai ou faux
	 */
	private boolean outOfBounds(int x, int y){
		if (x < 0 || y < 0 || x >= horizontalCellCount || y >= verticalCellCount) {
			return true;
		} else {
			return false;
		}

	}
	
	/**
	 * Retourne la position en x de la particule en fonction de la taille des cellules.
	 * 
	 * @param particle Une particule
	 * @return La position en x de particle
	 */
	private int getParticleIndexX (Particle particle) {

		return (int) (particle.getX()/cellSize)+1;
	}
	
	/**
	 * Retourne la position en y de la particule en fonction de la taille des cellules.
	 * 
	 * @param particle Une particule
	 * @return La position en y de particle
	 */
	private int getParticleIndexY (Particle particle) {

		return (int) (particle.getY()/cellSize)+1;
	}
}
