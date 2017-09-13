
/*******************************
 * Package : 01 02 03 04 01
 * 
 * Applet  : 01 02 03 04 01 02 
 * 
 ******************************/

package packageA;

import javacard.framework.*;

/**
 *
 * @author swatch
 */
public class MemoryMeasurementApplet extends Applet implements MultiSelectable{
    
    final static byte CLA_SIMPLEAPPLET               = (byte) 0xB0;
    final static byte INS_MEMORYMEASURE              = (byte) 0x71;
    
    byte[] AID_SERVER = {(byte)0x01, (byte)0x02, (byte)0x03, (byte)0x04, (byte)0x01, (byte)0x01};
    byte[] AID_MEMORY = {(byte)0x01, (byte)0x02, (byte)0x03, (byte)0x04, (byte)0x01, (byte)0x02};
    
    byte[] AID_CLIENT = {(byte)0x01, (byte)0x02, (byte)0x03, (byte)0x04, (byte)0x02, (byte)0x01};
    
    MemoryMeasurement mm = new MemoryMeasurement();

    public static void install(byte[] bArray, short bOffset, byte bLength) throws ISOException{
        new MemoryMeasurementApplet();
    }
    
    public MemoryMeasurementApplet(){
  
        /* Measurement by invoking static method of MemoryMeasurement */
        //MemoryMeasurement.startMeasurementStatic();
        
        /* Measurement by invoking non-static method of MemoryMeasurement Object */
        mm.startMeasurement();
        
        register();  
        
        ServerApplet.install();
        
    } 
    
    public boolean select(){
      return true;
    }

    public void deselect(){
        return;
    }
    
    public boolean select(boolean bln) {
        return true;
    }

    public void deselect(boolean bln) {
        
    }
    
    public void init() {
    }
    
    public void process(APDU apdu) throws ISOException {
        byte[] apduBuffer = apdu.getBuffer();

        if (selectingApplet())
            return;

        if (apduBuffer[ISO7816.OFFSET_CLA] == CLA_SIMPLEAPPLET) {
            switch (apduBuffer[ISO7816.OFFSET_INS] )
            {
                case INS_MEMORYMEASURE: 
                    getMemoryConsumption(apdu); 
                    break;
                default :
                    ISOException.throwIt( ISO7816.SW_INS_NOT_SUPPORTED ) ;
                break ;

            }
        }
        else ISOException.throwIt( ISO7816.SW_CLA_NOT_SUPPORTED);
    }
        
    public void getMemoryConsumption(APDU apdu){
                      
        byte[]    apdubuf = apdu.getBuffer();

        //MemoryMeasurement.endMeasurementStatic();
        mm.endMeasurement();
        
        apdu.setOutgoing();

        apdu.setOutgoingLength((short) 18);

        /* Enable this block to return Static type measurements */
        /*
        Util.setShort(apdubuf,(short)(0), (MemoryMeasurement.persistentEndStatic));
        Util.setShort(apdubuf,(short)(2), (MemoryMeasurement.deselectEndStatic));
        Util.setShort(apdubuf,(short)(4), (MemoryMeasurement.resetEndStatic));
        Util.setShort(apdubuf,(short)(6), (MemoryMeasurement.persistentStartStatic));
        Util.setShort(apdubuf,(short)(8), (MemoryMeasurement.deselectStartStatic));
        Util.setShort(apdubuf,(short)(10), (MemoryMeasurement.resetStartStatic));
        Util.setShort(apdubuf,(short)(12), (MemoryMeasurement.getPersistentConsumptionStatic()));
        Util.setShort(apdubuf,(short)(14), (MemoryMeasurement.getDeselectConsumptionStatic()));
        Util.setShort(apdubuf,(short)(16), (MemoryMeasurement.getResetConsumptionStatic()));*/
        
        /* Enable this block to return Non-Static type measurements */
        Util.setShort(apdubuf,(short)(0), (mm.persistentEnd));
        Util.setShort(apdubuf,(short)(2), (mm.deselectEnd));
        Util.setShort(apdubuf,(short)(4), (mm.resetEnd));
        Util.setShort(apdubuf,(short)(6), (mm.persistentStart));
        Util.setShort(apdubuf,(short)(8), (mm.deselectStart));
        Util.setShort(apdubuf,(short)(10), (mm.resetStart));
        Util.setShort(apdubuf,(short)(12), (mm.getPersistentConsumption()));
        Util.setShort(apdubuf,(short)(14), (mm.getDeselectConsumption()));
        Util.setShort(apdubuf,(short)(16), (mm.getResetConsumption()));
        
        apdu.sendBytes((short)0 , (short) 18);
    }

}
