/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package packageA;

import javacard.framework.*;

/**
 *
 * @author swatch
 */

public class MemoryMeasurement {
    
    public static short persistentStartStatic, persistentEndStatic;
    public static short deselectStartStatic, deselectEndStatic;
    public static short resetStartStatic, resetEndStatic;
    
    public short persistentStart, persistentEnd;
    public short deselectStart, deselectEnd;
    public short resetStart, resetEnd;
    
    public MemoryMeasurement(){
        persistentStartStatic = (short)0; persistentEndStatic = (short)0;
        deselectStartStatic = (short)0;   deselectEndStatic = (short)0;
        resetStartStatic = (short)0;      resetEndStatic = (short)0;
        
        persistentStart = (short)0; persistentEnd = (short)0;
        deselectStart = (short)0;   deselectEnd = (short)0;
        resetStart = (short)0;      resetEnd = (short)0;
    }
    
    public static void startMeasurementStatic(){
        persistentStartStatic = JCSystem.getAvailableMemory(JCSystem.MEMORY_TYPE_PERSISTENT);
        deselectStartStatic = JCSystem.getAvailableMemory(JCSystem.MEMORY_TYPE_TRANSIENT_DESELECT);
        resetStartStatic = JCSystem.getAvailableMemory(JCSystem.MEMORY_TYPE_TRANSIENT_RESET);
    }
    
    public void startMeasurement(){
        persistentStart = JCSystem.getAvailableMemory(JCSystem.MEMORY_TYPE_PERSISTENT);
        deselectStart = JCSystem.getAvailableMemory(JCSystem.MEMORY_TYPE_TRANSIENT_DESELECT);
        resetStart = JCSystem.getAvailableMemory(JCSystem.MEMORY_TYPE_TRANSIENT_RESET);
    }
    
    public static void endMeasurementStatic(){
        persistentEndStatic = JCSystem.getAvailableMemory(JCSystem.MEMORY_TYPE_PERSISTENT);
        deselectEndStatic = JCSystem.getAvailableMemory(JCSystem.MEMORY_TYPE_TRANSIENT_DESELECT);
        resetEndStatic = JCSystem.getAvailableMemory(JCSystem.MEMORY_TYPE_TRANSIENT_RESET);
    }
    
    public void endMeasurement(){
        persistentEnd = JCSystem.getAvailableMemory(JCSystem.MEMORY_TYPE_PERSISTENT);
        deselectEnd = JCSystem.getAvailableMemory(JCSystem.MEMORY_TYPE_TRANSIENT_DESELECT);
        resetEnd = JCSystem.getAvailableMemory(JCSystem.MEMORY_TYPE_TRANSIENT_RESET);
    }
    
    public static short getPersistentConsumptionStatic(){
        return (short)(persistentStartStatic - persistentEndStatic);
    }
    
    public short getPersistentConsumption(){
        return (short)(persistentStart - persistentEnd);
    }
    
    public static short getDeselectConsumptionStatic(){
        return (short)(deselectStartStatic - deselectEndStatic);
    }
    
    public short getDeselectConsumption(){
        return (short)(deselectStart - deselectEnd);
    }
    
    public static short getResetConsumptionStatic(){
        return (short)(resetStartStatic - resetEndStatic);
    }
    
    public short getResetConsumption(){
        return (short)(resetStart - resetEnd);
    }
}
