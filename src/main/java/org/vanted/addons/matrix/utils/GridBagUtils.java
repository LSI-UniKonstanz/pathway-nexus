package org.vanted.addons.matrix.utils;

import java.awt.GridBagConstraints;
import java.awt.Insets;

public class GridBagUtils {

	/**
    * returns an instance of GridBagConstraints with default values and x and y as position
    * @param x
    * @param y
    * @return
    */
   public static GridBagConstraints setPosition(int x, int y) {
   	return new GridBagConstraints(x, y, 1, 1, 0.5, 0.5, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0);
   }
   
   /**
    * returns an instance of GridBagConstraints with default values and x and y as position
    * and size constraints
    * @param x
    * @param y
    * @param width: number of columns
    * @param height: number of rows
    * @return
    */
   public static GridBagConstraints setPosition(int x, int y, int width, int height) {
   	return new GridBagConstraints(x, y, width, height, 0.5, 0.5, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0);
   }
}
