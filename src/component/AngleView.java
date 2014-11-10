package component;

import geometry.Vector;
import graphics.WorldMatrix;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;

import javax.swing.JPanel;

import simulation.Simulation;

/**Classe derivant de JPanel servant a creer un composant personnalise
 * pouvant etre utilise et ajoute a l'interface de l'application. Permet
 * la modification de l'orientation de la gravite.
 * 
 * @author Alexandre D'Amboise, Nicolas Ouellet-Payeur
 * @version 13 mars 2014
 */
public class AngleView extends JPanel {
    public static final long serialVersionUID = 1L;
	
    // taille du point au centre du cercle
    private final double POINT_SIZE = 6;
    
    //
    private final double SNAP = 0.15; 
    
	// rayon du cercle
	private final int RADIUS = 60;
	
	// angle du vecteur gravite
	private double angle = -Math.PI / 2;
	
	// simulation associee a ce composant
	private Simulation simulation;

	/**
	 * Constructeur principal.
	 * 
	 * @param simulation Simulation associee a ce composant.
	 */
	public AngleView(Simulation simulation) {
		super();
		//setBackground(Color.GRAY);
		setLayout(null);
		this.simulation = simulation;
		this.addMouseListener (new MouseAdapter(){
			public void mousePressed (MouseEvent event) {
				update (event);
			}
		});
		this.addMouseMotionListener (new MouseAdapter(){
			public void mouseDragged (MouseEvent event){
				update (event);
			}
		});
	}

	// met a jour l'angle d'apres la position de la souris
	private void update (MouseEvent event) {
		double x = event.getX () - RADIUS;
		double y = -event.getY () + RADIUS;
		angle = Math.atan2 (y, x);
		double theAngle = 0;
		for (theAngle = 0; theAngle < Math.PI * 2; theAngle += Math.PI/2) {
			if(Math.abs(Math.cos(angle)-Math.cos(theAngle)) < SNAP
			    && Math.abs(Math.sin(angle)-Math.sin(theAngle)) < SNAP) {
				angle = theAngle;
			}
		}
		double gravityMagnitude = simulation.getGravity ().getMagnitude ();
		Vector gravity = new Vector (Math.cos (angle) * gravityMagnitude, Math.sin (angle) * gravityMagnitude);
		simulation.setGravity (gravity);
		repaint ();
	}
	
	/**
	 * Utilise pour dessiner le composant.
	 */
	public void paintComponent(Graphics g){
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		
		//g2d.setBackground(Color.DARK_GRAY);
		g2d.clearRect(0, 0, getWidth(), getHeight());
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		// cercle du contour
		Ellipse2D.Double circle = new Ellipse2D.Double(0, 0, RADIUS * 2 - 1,RADIUS * 2 - 1);
		g2d.setColor(Color.BLACK);
		g2d.draw(circle);
		
		// vecteur qui montre l'angle
		g2d.translate(RADIUS, - RADIUS);
		Vector vecteurDirection = new Vector(RADIUS*Math.cos(angle), - RADIUS*Math.sin(angle));
		vecteurDirection.draw(g2d, new WorldMatrix(0, 0, RADIUS * 2, - RADIUS * 2, RADIUS * 2, RADIUS * 2));
		g2d.translate(- RADIUS, RADIUS);
		
		// point au centre
		Ellipse2D.Double point = new Ellipse2D.Double(RADIUS - POINT_SIZE / 2.0, 
				RADIUS - POINT_SIZE / 2.0,
				POINT_SIZE, POINT_SIZE);
		g2d.fill(point);
	}
	
	/**
	 * Attache ce composant a une nouvelle simulation. L'affichage est immediatement mis a jour pour
	 * correspondre a la nouvelle simulation.
	 * 
	 * @param simulation Nouvelle simulation a utiliser.
	 */
	public void setSimulation(Simulation simulation) {
		this.simulation = simulation;
		Vector gravity = simulation.getGravity();
		angle = Math.atan2(gravity.getY(), gravity.getX());
		repaint();
	}
}
