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

    private short bufferOffset = 0;
    
    byte[] ServerAID = {(byte)0x01, (byte)0x02, (byte)0x03, (byte)0x04, (byte)0x01, (byte)0x01};
    
    byte[] ClientAID = {(byte)0x01, (byte)0x02, (byte)0x03, (byte)0x04, (byte)0x02, (byte)0x01};
    
    byte[] byteArray = null;
    
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
    
    /**
     * Method to return Shareable Interface Object after authenticating AID
     * @param clientAID passes the AID of the requesting Applet 
     * @param parameter passes the parameter type. Default is 0
     * 
     * @return 
     *  Shareable type to ClientApplet after authenticating ClientAID
     *  null otherwise
     */
    public Shareable getShareableInterfaceObject(AID clientAID, byte parameter) {
        if(clientAID.equals(ClientAID, (short)0, (byte) ClientAID.length))
            return this;
        else
            return null;
    }

    public void process(APDU apdu) throws ISOException{
        
        byte[] apduBuffer = apdu.getBuffer();

        if (selectingApplet())
            return;

        if(apduBuffer[ISO7816.OFFSET_CLA] == CLA_SIMPLEAPPLET){
            switch ( apduBuffer[ISO7816.OFFSET_INS] ){
                case INS_SET_BUFFEROFFSET: {
                    setBufferOffset(apdu);
                    break;
                }
                
                default :
                    ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED) ;
                break ;
            }
        }
        else ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
    }

    void setBufferOffset(APDU apdu) {
        
        byte[] apduBuffer = apdu.getBuffer();

        byte b1 = apduBuffer[ISO7816.OFFSET_P1];
        byte b2 = apduBuffer[ISO7816.OFFSET_P2];
        
        bufferOffset = Util.makeShort(b1, b2);   
    }

    /**
     * Method takes short[] as parameter to return short value at offset
     * @param buffer short[] of which value at offset is returned
     * @param offset at which value to be returned
     * @return value of short[] at offset
     * 
     * This method is to be used while compiling for ServerApplet code
     */
    /*public short typeConfusion(short[] buffer, short offset) {  
        short shortValue = buffer[offset];
        return shortValue;
    }*/
    
    /**
     * Method takes byte[] as parameter to return short value at offset
     * @param buffer byte[] of which value at offset is returned
     * @param offset at which value to be returned
     * @return value of byte[] at offset
     * 
     * This method is to be used while compiling for ClientApplet code
     */
    public short typeConfusion(byte[] buffer, short offset) {      
        short shortValue = buffer[offset];
        return shortValue;
    }

}
