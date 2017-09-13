/******************************
 * Package : 01 02 03 04 01
 * 
 * Applet  : 01 02 03 04 01 01 
 * 
 *****************************/

package packageA;

import javacard.framework.*;

/**
 *
 * @author swatch
 */

public class ServerApplet extends Applet implements MSI{
    
    private final static byte CLA_SIMPLEAPPLET          = (byte)0xB0;

    private final static byte INS_SET_BUFFEROFFSET      = (byte)0x71;
    private final static byte INS_GET_BUFFEROFFSET      = (byte)0x72;

    private short bufferOffset = 0;
    
    byte[] ServerAID = {(byte)0x01, (byte)0x02, (byte)0x03, (byte)0x04, (byte)0x01, (byte)0x01};
    
    byte[] ClientAID = {(byte)0x01, (byte)0x02, (byte)0x03, (byte)0x04, (byte)0x02, (byte)0x01};
    
    protected ServerApplet(){
        register();
    }
    
    protected ServerApplet(byte[] buffer, short offset, byte length){
        register();
    }

    public static void install(byte[] bArray, short bOffset, byte bLength) throws ISOException{
        new ServerApplet(bArray, bOffset, bLength );
    }

    public boolean select(){
        return true;
    }

    public void deselect(){
        return;
    }
    
    public Shareable getShareableInterfaceObject(AID aid, byte parameter) {
      return this;
    }

    public void process(APDU apdu) throws ISOException{
        
        byte[] apduBuffer = apdu.getBuffer();

        if (selectingApplet())
            return;

        if(apduBuffer[ISO7816.OFFSET_CLA] == CLA_SIMPLEAPPLET){
            switch ( apduBuffer[ISO7816.OFFSET_INS] ){
                case INS_SET_BUFFEROFFSET: {
                    SetBufferOffset(apdu);
                    break;
                }
                
                case INS_GET_BUFFEROFFSET: {
                    GetBufferOffset(apdu);
                    break;
                }
              
                default :
                    ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED) ;
                break ;
            }
        }
        else ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
    }

    void SetBufferOffset(APDU apdu) {
        
        byte[] apduBuffer = apdu.getBuffer();
        short apduBufferLength = apdu.setIncomingAndReceive();

        byte b1 = apduBuffer[ISO7816.OFFSET_P1];
        byte b2 = apduBuffer[ISO7816.OFFSET_P2];
        
        bufferOffset = Util.makeShort(b1, b2);   
    }
    
    void GetBufferOffset(APDU apdu) {
        
        byte[] apduBuffer = apdu.getBuffer();
        short apduBufferLength = apdu.setIncomingAndReceive();

        apdu.setOutgoing();
        apdu.setOutgoingLength( (short) (2) );
        
        Util.setShort(apduBuffer, (short)0, bufferOffset);
        
        apdu.sendBytes((short)0, (short)2);
    }
    
    /* Use this method while Compiling for ServerApplet Code */
    /*public short typeConfusion(short[] buffer) {  
        short shortValue = buffer[bufferOffset];
        return shortValue;
    }*/
    
    /* Use this method while Compiling for ClientApplet Code */
    public short typeConfusion(byte[] buffer) {      
        short shortValue = buffer[bufferOffset];
        return shortValue;
    }

}
