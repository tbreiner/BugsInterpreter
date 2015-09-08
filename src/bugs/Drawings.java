package bugs;

import java.awt.Color;

/**
 * Helper class to hold a line to be drawn on the display.
 * @author theresabreiner
 *
 */
public class Drawings {
	double x1, y1;
	double x2, y2;
	Color color;
	
	/**
	 * Constructor
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @param c Color of line
	 */
	public Drawings(double x1, double y1, double x2, double y2, Color c) {
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
		this.color = c;
	}
	
	@Override
	public String toString() {
		return "Line " + x1 + ", " + y1 + " to " + x2 + ", " + y2;
	}
}
