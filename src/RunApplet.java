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
public class RunApplet extends JApplet implements Serializable {
  public static final long serialVersionUID = 1L;

  public void init() {
  }

  /**
   * Creer la fenetre de l'application.
   */
  public RunApplet () {
    Runner r = new Runner(this);
  }
}
