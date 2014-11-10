import geometry.Polygon;
import geometry.Rectangle;
import graphics.WorldMatrix;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.io.Serializable;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JApplet;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import simulation.Particle;
import simulation.Simulation;

import component.AngleView;
import component.OptionsView;
import component.SimulationView;

/**
 * Classe principale du projet, celle qui permet de creer la fenetre
 * de l'application et de lancer celle-ci.
 * 
 * @author Alexandre D'Amboise, Nicolas Ouellet-Payeur
 * 
 * @version 13 fevrier 2014 
 *  
 */
public class Run extends JApplet implements Serializable {
  public static final long serialVersionUID = 1L;

  // dimensions du monde
  private final double WORLD_WIDTH = 400;
  private final double WORLD_HEIGHT = 400;

  // nombre de particules utilisees dans la simulation
  private final int PARTICLE_COUNT = 600;

  // nombre de particules mises en evidence (dessinees en blanc)
  private final int HIGHLIGHTED_PARTICLE_COUNT = PARTICLE_COUNT / 30;

  private JPanel contentPane;

  private SimulationView stage;

  private WorldMatrix world;
  private Simulation simulation;

  private AngleView angleSelector;
  private OptionsView propertySelector;

  private boolean showParticles = false;

  /**
   * Lance l'application.
   */
  /*public static void main (String[] args) {
    // utiliser l'anticrénelage pour le texte
    System.setProperty("awt.useSystemAAFontSettings","on");
    System.setProperty("swing.aatext", "true");
    // demarrer l'application
    EventQueue.invokeLater (new Runnable () {
      public void run () {
        try {
          Run frame = new Run ();
          frame.setVisible (true);
        }
        catch (Exception e) {
          e.printStackTrace ();
        }
      }
    });
  }*/

  public void init () {
    // System.setProperty("awt.useSystemAAFontSettings","on");
    // System.setProperty("swing.aatext", "true");
    try {
      Run applet = new Run();
      // applet.setVisible(true);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Creer la fenetre de l'application.
   */
  public Run () {
    // setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
    // setResizable(false);
    // setTitle ("Fluid Simulator");
    contentPane = new JPanel ();
    contentPane.setBorder (new EmptyBorder (5, 5, 5, 5));
    contentPane.setLayout (new BorderLayout());
    setContentPane (contentPane);
    // creer la simulation
    createSimulation ();
    // composant principal
    stage = new SimulationView (world, simulation);
    stage.setPreferredSize(new Dimension((int) WORLD_WIDTH, (int) WORLD_HEIGHT));
    add (stage, BorderLayout.CENTER);
    // menu a droite
    JPanel panel = new JPanel();
    panel.setPreferredSize(new Dimension(300, 400));
    contentPane.add(panel, BorderLayout.EAST);
    panel.setLayout(null);
    // selectionneur d'angle pour la gravite
    angleSelector = new AngleView (simulation);
    angleSelector.setBounds(73, 29, 120, 120);
    panel.add(angleSelector);
    // selectionneur de proprietes variees (gravite, viscosite, densite...)
    propertySelector = new OptionsView (world, simulation);
    propertySelector.setBounds(30, 135, 605, 530);
    panel.add(propertySelector);
    // titre pour le selection d'angle pour la gravite
    JLabel label = new JLabel("Gravity Direction");
    label.setBounds(85, 0, 180, 30);
    panel.add(label);
    panel = new JPanel();
    panel.setLayout(new FlowLayout(FlowLayout.LEADING));
    contentPane.add(panel, BorderLayout.SOUTH);
    label = new JLabel("Reset: ");
    panel.add(label);
    JButton button = new JButton("Empty");
    button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        resetSimulation();
      }
    });
    panel.add(button);
    button = new JButton("Bowl");
    button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        resetSimulation(new Rectangle(100, 200, 200, 20),
            new Rectangle(100, 200, 20, 100),
            new Rectangle(280, 200, 20, 100));
      }
    }); 
    panel.add(button);
  }

  // reinitialise la simulation avec les murs donnes
  private void resetSimulation(Polygon... walls) {
    // continuer de visionner (ou ne pas visionner) les particules
    boolean small = simulation.isSmall();
    // creer une nouvelle simulation
    createSimulation(walls);
    simulation.setSmall(small);
    // affecter cette simulation aux autres composants
    stage.setSimulation(simulation);
    angleSelector.setSimulation(simulation);
    propertySelector.setSimulation(simulation);
  }

  /**
   * Initialise les dimensions du monde et l'instance de la simulation, si elles n'existent pas.
   * Cree "world" et "simulation" et ajoute PARTICLE_COUNT particules a des positions au hasard
   * dans le monde.
   */
  private void createSimulation (Polygon... walls) {
    // initialiser le monde
    world = WorldMatrix.getIdentity (WORLD_WIDTH, WORLD_HEIGHT);
    // initialiser la simulation
    simulation = new Simulation (world, walls);
    // creer PARTICLE_COUNT particules
    int i;
    for (i = 0; i < PARTICLE_COUNT; i++) {
      double x = Math.random () * WORLD_WIDTH;
      double y = Math.random() * (WORLD_HEIGHT - 200) + 200;
      boolean highlighted = i >= PARTICLE_COUNT - HIGHLIGHTED_PARTICLE_COUNT;
      Particle particle = new Particle (x, y, highlighted);
      simulation.addParticle (particle);
    }
    // marquer la particule 'principale'
    simulation.getParticleIterator().next().setImportant(true);
  }
}
