package listeners;

import java.util.EventListener;

/**
 * Ecouteur d'evenements pour la Simulation.
 * 
 * @author Nicolas Ouellet-Payeur
 * @version 21 avril 2014
 */
public interface SimulationListener extends EventListener {
	/**
	 * Evenement lance quand une nouvelle etape de la simulation est amorcee.
	 */
	public void frameEntered();
}
