package component;

import graphics.WorldMatrix;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.Iterator;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import simulation.Particle;
import simulation.Simulation;

/**
 * Classe derivant de JPanel permettant l'ajout des differents curseurs necessaires
 * a la modification des parametres pouvant etre regles par l'utilisateur sur 
 * l'interface de l'application. 
 * 
 * @author Alexandre D'Amboise, Nicolas Ouellet-Payeur
 * @version 18 mars 2014	
 */
public class OptionsView extends JPanel implements Serializable {
	public static final long serialVersionUID = 1L;

	// le facteur multiplicatif a appliquer a la viscosite pour la convertir de l'echelle entiere
	// a l'echelle reelle
	private final double VISCOSITY_FACTOR = 10000;

	// la viscosite minimale reelle
	private final double MINIMUM_VISCOSITY = 10 / VISCOSITY_FACTOR;

	private JSlider gravitySlider;
	private JSlider viscositySlider;
	private JSlider restDensSlider;
	private JCheckBox smallCheckbox;

	private Simulation simul;

	/**
	 * Constructeur principal.
	 */
	public OptionsView(WorldMatrix world, Simulation simulation) {
		//setBackground(Color.DARK_GRAY);
		setLayout(null);
		simul = simulation;
		//Ajout d'un curseur qui permet de modifier la valeur de la gravite
		gravitySlider = new JSlider();
		//gravitySlider.setBackground(Color.DARK_GRAY);
		//gravitySlider.setForeground(Color.LIGHT_GRAY);
		gravitySlider.setMaximum(300);
		gravitySlider.setMinimum(200);
		gravitySlider.setValue((int) simul.getGravity().getMagnitude());
		//Ecouteur permettant d'obtenir la valeur courante du curseur et l'applique au parametre
		//de gravite
		gravitySlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				simul.setGravity(simul.getGravity().normalize(gravitySlider.getValue()));
			}
		});
		gravitySlider.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), 
				"Gravity", TitledBorder.LEADING, TitledBorder.TOP, null, 
				Color.BLACK));
		gravitySlider.setBounds(10, 27, 180, 50);
		add(gravitySlider);
		//Ajout d'un curseur qui permet de modifier la valeur de la viscosite
		viscositySlider = new JSlider();
		//viscositySlider.setBackground(Color.DARK_GRAY);
		//viscositySlider.setForeground(Color.LIGHT_GRAY);
		viscositySlider.setMaximum(1000);
		viscositySlider.setMinimum(0);
		viscositySlider.setValue((int) ((simul.getViscosity() - MINIMUM_VISCOSITY) 
				* VISCOSITY_FACTOR));
		//Ecouteur permettant d'obtenir la valeur courante du curseur et l'applique au parametre
		//de viscosite
		viscositySlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				simul.setViscosity(viscositySlider.getValue() / VISCOSITY_FACTOR 
						+ MINIMUM_VISCOSITY);
			}
		});
		viscositySlider.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), 
				"Viscosity", TitledBorder.LEADING, TitledBorder.TOP, 
				null, Color.BLACK));
		viscositySlider.setBounds(10, 90, 180, 50);
		add(viscositySlider);
		//Ajout d'un curseur qui permet de modifier la valeur de la densite au repos
		restDensSlider = new JSlider();
		//restDensSlider.setBackground(Color.DARK_GRAY);
		//restDensSlider.setForeground(Color.LIGHT_GRAY);
		//restDensSlider.setMajorTickSpacing(2);
		//restDensSlider.setMinorTickSpacing(1);
		restDensSlider.setMinimum(10);
		restDensSlider.setMaximum(20);
		//restDensSlider.setPaintTicks(true);
		//restDensSlider.setPaintLabels(true);
		restDensSlider.setValue((int) simul.getRestDensity());
		//Ecouteur permettant d'obtenir la valeur courante du curseur et l'applique au parametre de
		//densite au repos
		restDensSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				simul.setRestDensity(restDensSlider.getValue());
			}
		});
		restDensSlider.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), 
				"Rest Density", TitledBorder.LEADING, TitledBorder.TOP, null, 
				Color.BLACK));
		restDensSlider.setBounds(10, 154, 180, 50);
		add(restDensSlider);
		smallCheckbox = new JCheckBox("Show particles");
		//smallCheckbox.setBackground(Color.DARK_GRAY);
		//smallCheckbox.setForeground(Color.LIGHT_GRAY);
		smallCheckbox.setSelected(false);
		smallCheckbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				simul.setSmall(smallCheckbox.isSelected());
				Iterator<Particle> iterator = simul.getParticleIterator();
				while(iterator.hasNext()) {
					Particle particle = iterator.next();
					particle.setSmall(smallCheckbox.isSelected());
				}
			}
		});
		smallCheckbox.setBounds(10,215,180,30);
		add(smallCheckbox);
	}
	
	/**
	 * Attache ce composant a une nouvelle simulation. Les valeurs des composants sont mises a jour
	 * immediatement pour refleter la nouvelle simulation. 
	 * 
	 * @param simulation Nouvelle simulation.
	 */
	public void setSimulation(Simulation simulation) {
		this.simul = simulation;
		restDensSlider.setValue((int) simul.getRestDensity());
		viscositySlider.setValue((int) ((simul.getViscosity() - MINIMUM_VISCOSITY) 
				* VISCOSITY_FACTOR));
		gravitySlider.setValue((int) simul.getGravity().getMagnitude());
	}
}
