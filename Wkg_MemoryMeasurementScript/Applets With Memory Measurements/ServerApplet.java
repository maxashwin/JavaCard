
/*******************************
 * Package : 01 02 03 04 01
 * 
 * Applet  : 01 02 03 04 01 01 
 * 
 ******************************/

package packageA;

import javacard.framework.*;

/**
 *
 * @author swatch
 */
public class ServerApplet extends Applet{
    
    final static byte CLA_SIMPLEAPPLET               = (byte) 0xB0;
    final static byte INS_USERINPUT1                 = (byte) 0x80;
    
    byte[] AID_SERVER = {(byte)0x01, (byte)0x02, (byte)0x03, (byte)0x04, (byte)0x01, (byte)0x01};
    
    public static short miles;
    
    final static short ARRAY_LENGTH = (short) 0x0500;

    private byte m_ramArray_Deselect1[] = null;
    private byte m_ramArray_Reset1[] = null;
    private byte[] m_dataArray1 = null;

    private byte[] array1;
    private byte[] array2;
    private byte[] array3;
    private byte[] array4;
    private byte[] array5;
    private byte[] array6;
    
    public static void install() throws ISOException{
        new ServerApplet ();
    }
    
    public static void install(byte[] bArray, short bOffset, byte bLength) throws ISOException{
        new ServerApplet ();
    }
    
    public ServerApplet(){
        
        miles = (short) 0;
        

 /* Persistent : 12 bytes,  Transient(Deselect) : 0 bytes,  Transient(Reset) : 0 bytes */
        array1 = new byte[(byte)8]; 

 /* Persistent : 16 bytes,  Transient(Deselect) : 0 bytes,  Transient(Reset) : 0 bytes */
        array2 = new byte[(byte)12]; 

 /* Persistent : 28 bytes,  Transient(Deselect) : 0 bytes,  Transient(Reset) : 0 bytes */
        array3 = new byte[(byte)23]; 

 /* Persistent : 28 bytes,  Transient(Deselect) : 0 bytes,  Transient(Reset) : 0 bytes */
        array4 = new byte[(byte)24]; 

 /* Persistent : 32 bytes,  Transient(Deselect) : 0 bytes,  Transient(Reset) : 0 bytes */
        array5 = new byte[(byte)25]; 

 /* Persistent : 36 bytes,  Transient(Deselect) : 0 bytes,  Transient(Reset) : 0 bytes */
        array6 = new byte[(byte)32];
        
        register();
    } 
    
    public boolean select(){
      return true;
    }

    public void deselect(){
        return;
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
                case INS_USERINPUT1: 
                    foo(apdu); 
                    break;
                default :
                    ISOException.throwIt( ISO7816.SW_INS_NOT_SUPPORTED ) ;
                break ;

            }
        }
        else ISOException.throwIt( ISO7816.SW_CLA_NOT_SUPPORTED);
    }

    public void foo(APDU apdu){
                      
        byte[]    apdubuf = apdu.getBuffer();

        apdu.setOutgoing();

        apdu.setOutgoingLength((short) (6 * memoryMeasureLength));
        
        short index = (short)0;
        for(short i=0; i<memoryMeasureLength; i++){
            Util.setShort(apdubuf,(index), (mm[i].getPersistentConsumption()));
            Util.setShort(apdubuf,(short)(index+2), (mm[i].getDeselectConsumption()));
            Util.setShort(apdubuf,(short)(index+4), (mm[i].getResetConsumption()));
            index = (short)(index + 6);
        }
        apdu.sendBytes((short)0 , (short) (6 * memoryMeasureLength));
    }
    
}
