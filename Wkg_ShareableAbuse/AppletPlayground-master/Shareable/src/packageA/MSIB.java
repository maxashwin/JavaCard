
package packageA;

import javacard.framework.Shareable;

/**
 *
 * @author swatch
 */

public interface MSIB extends Shareable{
    public void grantCreditClientB(short amount);   
}
