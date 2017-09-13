
/*******************************
 * Package : 01 02 03 04 01
 * 
 * Applet  : 01 02 03 04 01 02 
 * 
 ******************************/

package packageB;

import javacard.framework.*;
import packageA.ServerApplet;
import packageA.TestClass;

/**
 *
 * @author swatch
 */
public class MemoryApplet extends Applet implements MultiSelectable{
    
    final static byte CLA_SIMPLEAPPLET               = (byte) 0xB0;
    final static byte INS_MEMORYMEASURE              = (byte) 0x71;
    
    byte[] AID_SERVER = {(byte)0x01, (byte)0x02, (byte)0x03, (byte)0x04, (byte)0x01, (byte)0x01};
    byte[] AID_MEMORY = {(byte)0x01, (byte)0x02, (byte)0x03, (byte)0x04, (byte)0x01, (byte)0x02};
    
    byte[] AID_CLIENT = {(byte)0x01, (byte)0x02, (byte)0x03, (byte)0x04, (byte)0x02, (byte)0x01};
    
    TestClass TC  = null;

    public static void install(byte[] bArray, short bOffset, byte bLength) throws ISOException{
        new MemoryApplet();
    }
    
    public MemoryApplet(){
  
        MemoryMeasure.startMeasurement();
        
        register();  
        
        TC = new TestClass();
   
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
                    memoryMeasure(apdu); 
                    break;
                default :
                    ISOException.throwIt( ISO7816.SW_INS_NOT_SUPPORTED ) ;
                break ;

            }
        }
        else ISOException.throwIt( ISO7816.SW_CLA_NOT_SUPPORTED);
    }
        
    public void memoryMeasure(APDU apdu){
                      
        byte[]    apdubuf = apdu.getBuffer();

        MemoryMeasure.endMeasurement();
        
        apdu.setOutgoing();

        apdu.setOutgoingLength((short) 18);

        Util.setShort(apdubuf,(short)(0), (MemoryMeasure.persistentEnd));
        Util.setShort(apdubuf,(short)(2), (MemoryMeasure.deselectEnd));
        Util.setShort(apdubuf,(short)(4), (MemoryMeasure.resetEnd));
        Util.setShort(apdubuf,(short)(6), (MemoryMeasure.persistentStart));
        Util.setShort(apdubuf,(short)(8), (MemoryMeasure.deselectStart));
        Util.setShort(apdubuf,(short)(10), (MemoryMeasure.resetStart));
        Util.setShort(apdubuf,(short)(12), (MemoryMeasure.getPersistentConsumption()));
        Util.setShort(apdubuf,(short)(14), (MemoryMeasure.getDeselectConsumption()));
        Util.setShort(apdubuf,(short)(16), (MemoryMeasure.getResetConsumption()));
        
        apdu.sendBytes((short)0 , (short) 18);
    }

}
