/*************************************************************************************
 * The Exemplary Add-on is (c) 2008-2011 Plant Bioinformatics Group, IPK Gatersleben,
 * http://bioinformatics.ipk-gatersleben.de
 * The source code for this project, which is developed by our group, is
 * available under the GPL license v2.0 available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html. By using this
 * Add-on and VANTED you need to accept the terms and conditions of this
 * license, the below stated disclaimer of warranties and the licenses of
 * the used libraries. For further details see license.txt in the root
 * folder of this project.
 ************************************************************************************/
package org.vanted.addons.matrix;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.Main;

public class StartVantedWithMatrixAddon {

    /**
     * Startmethod for Vanted. The Add-on will be loaded, if you specify the
     * correct xml-file. Starting with more than one Add-on at the same time is
     * not supported yet, but should be sufficient for most cases.
     * <p>
     * In the Add-on-Example.xml (which may be renamed if you want to) you can specify the description, version, ... for your Add-on. This information is used
     * for the listing of your Add-on in the Add-on-Manager dialog. Additionally the Plugin-class is instantiated by entering the location
     * (<main>example_addon.MatrixAddon</main> means that Vanted tries to find the class "MatrixAddon" in the package "example_addon") and instantiates it.
     * <p>
     * For startup you should also read the <code>readme.txt</code> (as usual).
     * <p>
     * For creating a jar of your add-on just use ant to start the <code>updateJar.xml</code>. It will also track your progress by copying the actual jar into an
     * "old"-directory before overwriting, which can be used to start vanted with an older version of your add-on. But please don't forget to rename it again
     * ("Add-on-Example(09-02-14).jar" -> "Add-on-Example.jar"), because otherwise vanted won't load the plugin. Note that the jar also contains the sourcecode.
     */
    public static void main(String[] args) {
        System.out.println("Starting VANTED with Add-on " + getAddonName()
                + " for development...");
        Main.startVanted(args, getAddonName());

        // alternatively you may also start several Addons at the same time:
        // Main.startVantedExt(args, new String[]{"","",...});
    }

    public static String getAddonName() {
        return "Pathway-Nexus.xml";
    }
}