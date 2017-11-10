/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package openemv;

import javacard.framework.*;

/**
 *
 * @author swatch
 */

public class MemoryMeasurement {

    public short persistentStart, persistentEnd;
    public short deselectStart, deselectEnd;
    public short resetStart, resetEnd;
    
    public MemoryMeasurement(){
        persistentStart = (short)0; persistentEnd = (short)0;
        deselectStart = (short)0;   deselectEnd = (short)0;
        resetStart = (short)0;      resetEnd = (short)0;
    }
    
    public void startMeasurement(){
        persistentStart = JCSystem.getAvailableMemory(JCSystem.MEMORY_TYPE_PERSISTENT);
        deselectStart = JCSystem.getAvailableMemory(JCSystem.MEMORY_TYPE_TRANSIENT_DESELECT);
        resetStart = JCSystem.getAvailableMemory(JCSystem.MEMORY_TYPE_TRANSIENT_RESET);
    }

    public void endMeasurement(){
        persistentEnd = JCSystem.getAvailableMemory(JCSystem.MEMORY_TYPE_PERSISTENT);
        deselectEnd = JCSystem.getAvailableMemory(JCSystem.MEMORY_TYPE_TRANSIENT_DESELECT);
        resetEnd = JCSystem.getAvailableMemory(JCSystem.MEMORY_TYPE_TRANSIENT_RESET);
    }
    
    public short getPersistentConsumption(){
        return (short)(persistentStart - persistentEnd);
    }
    
    public short getDeselectConsumption(){
        return (short)(deselectStart - deselectEnd);
    }
    
    public short getResetConsumption(){
        return (short)(resetStart - resetEnd);
    }
}
