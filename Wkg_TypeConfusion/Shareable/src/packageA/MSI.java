package packageA;

import javacard.framework.Shareable;

/**
 *
 * @author swatch
 */

/* Use this method declaration while compiling for ServerApplet Code */
/*public interface MSI extends Shareable {
    
    public short typeConfusion(short[] buffer); 
}*/

/* Use this method declaration while compiling for ClientApplet Code */
public interface MSI extends Shareable {

    public short typeConfusion(byte[] buffer); 
}