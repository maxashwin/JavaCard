/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package packageA;

import javacard.framework.APDU; 
import javacard.framework.Shareable;

/**
 *
 * @author swatch
 */
public interface MSI extends Shareable{
    
    //This function is available to other methods 
    public static short miles = (short) 0;
    public void grantcredit(short amount);
    
    
}
