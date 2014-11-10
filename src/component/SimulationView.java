package component;

import geometry.Polygon;
import geometry.Vector;
import graphics.WorldMatrix;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.Iterator;

import javax.swing.JPanel;

import simulation.Particle;
import simulation.Simulation;

/**
 * Permet la creation des particules qui seront utilisees pour effectuer une
 * animation, qui est realisee dans cette meme classe. C'est elle qui fait en
 * sorte de dessiner les particules dans le JPanel.
 * 
 * @author Nicolas Ouellet-Payeur
 * @version 13 fevrier 2014
 */
public class SimulationView extends JPanel implements Runnable, Serializable {
	public static final long serialVersionUID = 1L;
	// delai, en millisecondes, entre deux etapes de la simulation
	private final int DELAY = 30;
	
	// les pixels avec une valeur alpha plus grande que ceci seront dessines
	private final int ALPHA_THRESHOLD = 50;
	
	// valeur minimale du vecteur vitesse a dessiner
	private final double MINIMUM_VELOCITY = 20;
	
	// processus utilise pour la simulation et l'animation
	private Thread thread;
	
	// position de la souris pendant qu'elle est enfoncee
	private Point2D.Double mousePosition = null;
	private Point2D.Double previousMousePosition = null;
	
	// objet representant la simulation et gerant les particules
	private Simulation simulation = null;
	
	// objet representant le monde et ses dimensions
	private WorldMatrix world = null;
	
	/**
	 * Constructeur principal.
	 */
	public SimulationView (WorldMatrix world, Simulation simulation) {
		super ();
		setBackground (Color.BLACK);
		this.world = world;
		this.simulation = simulation;
		// debuter l'animation
		thread = new Thread (this);
		thread.start ();
		// ajouter le support pour les evenements de la souris
		this.addMouseListener (new StageMouseListener ());
		this.addMouseMotionListener (new StageMouseListener ());
	}
	/**
	 * Dessine les particules a l'ecran.
	 */
	public void paintComponent (Graphics g) {
		// dessiner l'arriere-plan
		super.paintComponent (g);
		BufferedImage offscreenImage = null;
		Graphics2D graphics;
		// creer une image en dehors de l'ecran si necessaire
		if (!simulation.isSmall()) {
			offscreenImage = new BufferedImage (getWidth (), getHeight (),
					BufferedImage.TYPE_INT_ARGB);
			graphics = (Graphics2D) offscreenImage.getGraphics ();
		}
		else{
			graphics = (Graphics2D) g;
			graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
		}
		// pour chaque particule
		Iterator<Particle> iterator = simulation.getParticleIterator ();
		while (iterator.hasNext ()) {
			// dessiner cette particule
			Particle particle = iterator.next ();
			particle.draw (graphics, world);
		}
		// pour chaque pixel de l'image
		int x, y;
		if(!simulation.isSmall()) {
			for (y = 0; y < getHeight (); y++) {
				for (x = 0; x < getWidth (); x++) {
					// calculer la valeur alpha du pixel
					int alpha = new Color (offscreenImage.getRGB (x, y), true).getAlpha ();
					// tronquer a 0 ou a 255 si la valeur d'alpha depasse ALPHA_THRESHOLD
					alpha = alpha >= ALPHA_THRESHOLD ? 255 : 0;
					// creer la couleur du pixel
					Color fullAlpha = new Color (offscreenImage.getRGB (x, y));
					Color pixel = new Color (fullAlpha.getRed (), fullAlpha.getGreen (),
							fullAlpha.getBlue (), alpha);
					// affecter la couleur du pixel
					offscreenImage.setRGB (x, y, pixel.getRGB ());
				}
			}
		}
    graphics.dispose();
		// effectuer le rendu sur l'ecran
		graphics = (Graphics2D) g;
		graphics.drawImage (offscreenImage, 0, 0, null);
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		// dessiner le contenant et les murs
		graphics.setColor(Color.WHITE);
		Polygon[] walls = simulation.getWalls();
		int i;
		for(i = 0; i < walls.length; i++) {
			walls[i].draw(graphics, world);
		}
		// dessiner le vecteur vitesse si necessaire
		if(simulation.isSmall()) {
			Particle particle = simulation.getParticleIterator().next();
			graphics.translate(particle.getX(), - particle.getY());
			Vector velocity = particle.getVelocity();
			velocity = velocity.normalize(Math.max(velocity.getMagnitude(), MINIMUM_VELOCITY));
			graphics.setStroke(new BasicStroke(2));
			graphics.setColor(Color.RED);
			// velocity.draw(graphics, world);
			graphics.translate(- particle.getX(), particle.getY());
		}
		// dessiner une bordure noire de 1 pixel
		graphics.setColor (Color.BLACK);
		graphics.drawRect (0, 0, getWidth () - 1, getHeight () - 1);
	}
	
	/**
	 * Lance la procedure d'animation.
	 */
	public void run () {
		// boucle infinie
		while (true) {
			// attendre DELAY millisecondes
			try {
				Thread.sleep (DELAY);
			}
			catch (InterruptedException error) {
				return;
			}
			// mettre a jour la simulation
			simulation.update (DELAY / 1000.0, mousePosition, previousMousePosition);
			// mettre a jour la derniere position de la souris
			previousMousePosition = mousePosition;
			// mettre a jour la zone de dessin
			repaint ();
		}
	}
	
	/**
	 * Change la simulation attachee a ce composant. L'animation continuera a rouler, mais pour
	 * la nouvelle simulation.
	 * 
	 * @param simulation Nouvelle simulation a utiliser.
	 */
	public void setSimulation(Simulation simulation) {
		this.simulation = simulation;
		repaint();
	}
	
	/*
	 * Ecouteur de souris pour la zone de dessin principale.
	 */
	private class StageMouseListener extends MouseAdapter {
		/**
		 * Evenement lance quand la souris est enfoncee. Met a jour mousePosition si le bouton 
		 * gauche est le bouton utilise.
		 */
		public void mousePressed (MouseEvent event) {
			// si bouton gauche
			if (event.getButton () == 1) {
				mousePosition = new Point2D.Double ();
				mousePosition.setLocation (event.getPoint ());
				mousePosition = world.detransform (mousePosition);
			}
		}
		/**
		 * Evenement lance quand la souris est deplacee et enfoncee. Met a jour mousePosition si le
		 * bouton gauche est le bouton utilise.
		 */
		public void mouseDragged (MouseEvent event) {
			// si bouton gauche
			if (mousePosition != null) {
				mousePosition = new Point2D.Double ();
				mousePosition.setLocation (event.getPoint ());
				mousePosition = world.detransform (mousePosition);
			}
		}
		/**
		 * Evenement lance quand la souris est relachee. Affecte mousePosition a null pour que la
		 * souris n'interagisse plus. Si le bouton droit est relache, change le sens de la gravite
		 * dans la simulation.
		 */
		public void mouseReleased (MouseEvent event) {
			mousePosition = null;
		}
	}
}
