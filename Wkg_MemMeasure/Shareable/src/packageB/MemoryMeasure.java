/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package packageB;

import javacard.framework.*;

/**
 *
 * @author swatch
 */

public class MemoryMeasure {
    
    public static short persistentStart, persistentEnd;
    public static short deselectStart, deselectEnd;
    public static short resetStart, resetEnd;
    
    public MemoryMeasure(){
        persistentStart = (short)0; persistentEnd = (short)0;
        deselectStart = (short)0;   deselectEnd = (short)0;
        resetStart = (short)0;      resetEnd = (short)0;
    }
    
    public static void startMeasurement(){
        persistentStart = JCSystem.getAvailableMemory(JCSystem.MEMORY_TYPE_PERSISTENT);
        deselectStart = JCSystem.getAvailableMemory(JCSystem.MEMORY_TYPE_TRANSIENT_DESELECT);
        resetStart = JCSystem.getAvailableMemory(JCSystem.MEMORY_TYPE_TRANSIENT_RESET);
    }
    
    public static void endMeasurement(){
        persistentEnd = JCSystem.getAvailableMemory(JCSystem.MEMORY_TYPE_PERSISTENT);
        deselectEnd = JCSystem.getAvailableMemory(JCSystem.MEMORY_TYPE_TRANSIENT_DESELECT);
        resetEnd = JCSystem.getAvailableMemory(JCSystem.MEMORY_TYPE_TRANSIENT_RESET);
    }
    
    public static short getPersistentConsumption(){
        return (short)(persistentStart - persistentEnd);
    }
    
    public static short getDeselectConsumption(){
        return (short)(deselectStart - deselectEnd);
    }
    
    public static short getResetConsumption(){
        return (short)(resetStart - resetEnd);
    }
    
}
