/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package packageA;

import javacard.framework.Shareable;

/**
 *
 * @author swatch
 */
public interface MSI extends Shareable{
    
    public short getByteArrayAddress(byte[] bufferArray);
    
    public short[] castShortToShortArray(short addressValue);
}
