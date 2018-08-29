package platform.camera.components;

import java.io.Serializable;

public class FloatRange implements Serializable {

	protected float min;
	protected float max;

	/**
	 * Ruft den Wert der min-Eigenschaft ab.
	 * 
	 */
	public float getMin() {
		return min;
	}

	/**
	 * Legt den Wert der min-Eigenschaft fest.
	 * 
	 */
	public void setMin(float value) {
		this.min = value;
	}

	/**
	 * Ruft den Wert der max-Eigenschaft ab.
	 * 
	 */
	public float getMax() {
		return max;
	}

	/**
	 * Legt den Wert der max-Eigenschaft fest.
	 * 
	 */
	public void setMax(float value) {
		this.max = value;
	}

}
