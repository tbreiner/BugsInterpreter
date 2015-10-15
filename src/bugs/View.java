package bugs;

import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * View to display the animation of the Bugs.
 * @author theresabreiner
 *
 */
public class View extends JPanel {
	private static final long serialVersionUID = 1L;
	Interpreter interpreter;
	double xScale, yScale;
	Timer timer;
	
	/**
	 * Constructor that links this View object to the given Interpreter.
	 * Repaints the View to show updates from Interpreter
	 * @param i
	 */
	public View(Interpreter i) {
		interpreter = i;
		timer = new Timer(40, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				repaint();
			}
		});
		timer.start();
	}
	
	/**
	 * Method to adjust the x scale of the View
	 * @param s New scale to be used
	 */
	public void setXScale(double s) {
		xScale = s;
	}
	
	/**
	 * Method to adjust the y scale of the View
	 * @param s New scale to be used
	 */
	public void setYScale(double s) {
		yScale = s;
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.JComponent#paint(java.awt.Graphics)
	 */
	public void paint(Graphics g) {
		super.paint(g);
		@SuppressWarnings("unchecked")
		ArrayList<Bug> bugsCopy = (ArrayList<Bug>) interpreter.bugs.clone();
		@SuppressWarnings("unchecked")
		ArrayList<Drawings> toDrawCopy = (ArrayList<Drawings>) interpreter.toDraw.clone();
		for (Bug b : bugsCopy) {
			if (b.getColor() == null) continue;
			drawBug(g, b);
			
		}
		for (Drawings d : toDrawCopy) {
			//System.out.println("Gonna draw a line: " + d);
			g.setColor(d.color);
			g.drawLine((int)(d.x1 * xScale), (int)(d.y1 * yScale), (int)(d.x2 * xScale), (int)(d.y2 * yScale));
		}
	}
	
	/**
	 * Computes how much to move to add to this Bug's x-coordinate,
	 * in order to displace the Bug by "distance" pixels in 
	 * direction "degrees".
	 * 
	 * @param distance The distance to move.
	 * @param degrees The direction in which to move.
	 * @return The amount to be added to the x-coordinate.
	 */
	private static double computeDeltaX(int distance, int degrees) {
	    double radians = Math.toRadians(degrees);
	    return distance * Math.cos(radians);
	}
	
	/**
	 * Computes how much to move to add to this Bug's y-coordinate,
	 * in order to displace the Bug by "distance" pixels in 
	 * direction "degrees.
	 * 
	 * @param distance The distance to move.
	 * @param degrees The direction in which to move.
	 * @return The amount to be added to the y-coordinate.
	 */
	private static double computeDeltaY(int distance, int degrees) {
	    double radians = Math.toRadians(degrees);
	    return distance * Math.sin(-radians);
	}
	
	/**
	 * Helper method to draw the given bug on the display at the appropriate coordinates.
	 * @param g Graphics object being used for display
	 * @param b Bug object to be drawn
	 */
	private void drawBug(Graphics g, Bug b) {
		g.setColor(b.getColor());
		double x = b.getX();
		double y = b.getY();
		double angle = b.getAngle();
		
		int x1 = (int) (xScale * x + computeDeltaX(12, (int)angle));
	    int x2 = (int) (xScale * x + computeDeltaX(6, (int)angle - 135));
	    int x3 = (int) (xScale * x + computeDeltaX(6, (int)angle + 135));
	    
	    int y1 = (int) (yScale * y + computeDeltaY(12, (int)angle));
	    int y2 = (int) (yScale * y + computeDeltaY(6, (int)angle - 135));
	    int y3 = (int) (yScale * y + computeDeltaY(6, (int)angle + 135));
	    g.fillPolygon(new int[] { x1, x2, x3 }, new int[] { y1, y2, y3 }, 3);

	}
	
	
}
