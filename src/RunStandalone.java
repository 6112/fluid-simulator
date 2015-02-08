import javax.swing.JFrame;
import javax.swing.JLabel;

import java.awt.Dimension;

import java.io.Serializable;

public class RunStandalone extends JFrame implements Serializable {
  public static final long serialVersionUID = 1L;

  public static void main(String[] args) {
    RunStandalone r = new RunStandalone();
  }

  public RunStandalone() {
    Runner r = new Runner(this);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    pack();
    setVisible(true);
  }
}
