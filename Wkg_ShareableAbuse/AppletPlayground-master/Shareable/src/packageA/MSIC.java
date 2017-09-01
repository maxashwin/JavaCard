package packageA;

import javacard.framework.Shareable;

/**
 *
 * @author swatch
 */

public interface MSIC extends Shareable{
    public void grantCreditClientC(short amount);   
}