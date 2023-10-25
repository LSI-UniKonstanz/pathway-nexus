package org.vanted.addons.matrix.datavis;

import java.awt.Color;
import java.util.LinkedHashMap;

import java.util.ArrayList;
import java.util.Collection;

public class ColorCode {
//fields
	private final Color[] colorCodeSmall = {Color.decode("#990000"), Color.decode("#FF0000"), Color.decode("#FF6666"), Color.decode("#FF9999"), Color.decode("#FFCCCC")};
	private final LinkedHashMap<Double, Color> colorMap = new LinkedHashMap<>();

//constructors
	/**
	 * returns an instance of ColorCode for values between min and max
	 * centered around a "base" value with an amount of steps reflecting granularity
	 * if the calculated distinct values fall below 0 or exceed max they are cut away
	 * thus it can happen that the resulting ColorCode has less distinct ranges than the value of "granularity"
	 */
	public ColorCode(double min, double max, double base, int granularity){
		Color[] colorCodeBig = {Color.decode("#DCE510"), Color.decode("#BACA10"), Color.decode("#97B010"), Color.decode("#759510"), Color.decode("#527B10")};
		Color colorCodeBase = Color.LIGHT_GRAY;	//Color.decode("#FFFFFF");


		double step = (max - min)/(granularity);
		float colorStep = (float) 10/(granularity);		// the step size with which the color arrays will be iterated over
		
		//create array of values smaller than base, with value of "step" appart
		ArrayList<Double> smallValues = new ArrayList<>();
		for(int a = 0; a < granularity/2; a++) {
			double newValue = base-(step/2) - step*a;
			if(min < 0) {
				smallValues.add(newValue);
			} else {
				if(newValue > 0) {
					smallValues.add(newValue);
				}
			}
		}

		//map them to distinct colors
		float i = 0;
		for(int b = smallValues.size()-1; b >= 0; b--) {
			int index = Math.round(i);
			colorMap.put(smallValues.get(b), colorCodeSmall[index]);
			i += colorStep;
		}
		
		//create array of values bigger than base with value of "step" appart
		ArrayList<Double> bigValues = new ArrayList<>();
		for(int c = 0; c < granularity/2; c++) {
			double newValue = base+(step/2) + step*c;
			if(newValue < max) {
				bigValues.add(newValue);
			}
		}
			
		//map them to distinct colors
		float j = 0;
		for(int d = 0; d < bigValues.size()-1; d++) {
			int index = Math.round(j);
			colorMap.put(bigValues.get(d), colorCodeBig[index]);
			j += colorStep;
		}
		colorMap.replace(bigValues.get(0), colorCodeBase);
		colorMap.put(max, colorCodeBig[4]);	//maximum is always dark green
	}
	
//methods	
	/**returns a ArrayList containing the currently used colors
	 */
	public ArrayList<Color> mapping(){
		Collection<Color> col = colorMap.values();
		return new ArrayList<>(col);
	}

	/**
	 * returns the color corresponding to the given value 
	 * according to the current mapping
	 */
	public Color getColorFor(Double value) {
		Color color = colorCodeSmall[0];
		Double[] keys = colorMap.keySet().toArray(new Double[0]);
		
		for(int d = 0; d < keys.length-1; d++) {
			if(value > keys[d]) {
				color = colorMap.get(keys[d+1]);
			} else {
				return color;
			}
		}
		return color;
	}

	/**
	 * returns the current value to color mapping
	 */
	public LinkedHashMap<Double, Color> getMap() {
		return this.colorMap;
	}
}
	
