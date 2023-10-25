package org.vanted.addons.matrix;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.addons.AddonAdapter;
import org.graffiti.plugin.inspector.InspectorTab;
import org.vanted.addons.matrix.ui.SidebarTab;

import javax.swing.*;

/**
 * Main class used for initialising components/features of the addon.
 * Use {@link AddonAdapter} to indicate, that you are writing an addon.
 * If an exception occurs during instantiation, a proper error message
 * will be thrown and a standard addon icon will be used.
 */
public class MatrixAddon extends AddonAdapter {

    /**
     * This class will automatically start all implemented Algorithms, views and
     * other extensions written in your Add-on. A code formatting template
     * (save_action_format.xml) is available in the "make" project of the VANTED
     * CVS.
     */
    @Override
    protected void initializeAddon() {
        System.out.println("Loaded Pathway Nexus.");

        // register sidebar tabs
        this.tabs = new InspectorTab[]{new SidebarTab()};
/*
        // register a color map attribute
        this.attributes = new Class[1];
        this.attributes[0] = ColorMapAttribute.class;

        // register the color map attribute as a string attribute
        // A String attribute is simply and attribute that contains a string.
        StringAttribute.putAttributeType(ColorMapAttribute.name, ColorMapAttribute.class);

        // link the attribute and the attribute component
        attributeComponents.put(ColorMapAttribute.class, ColorMapAttributeComponent.class);
  */  }

    /**
     * Here you may specify your own logo, which will appear in menus and tabs.
     */
    @Override
    public ImageIcon getIcon() {
        return null;
    }
}
