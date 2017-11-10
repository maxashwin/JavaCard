/***************************
 * Package : 01 02 03 04 02
 * 
 * Applet  : 01 02 03 04 02 01
 * 
 **************************/

package packageA;

import javacard.framework.*;

/**
 *
 * @author swatch
 */

public class ServerApplet extends Applet implements MSI{

    private final static byte CLA_SIMPLEAPPLET                      = (byte)0xB0 ;

    private final static byte INS_FIND_BUFFER_ADDRESS               = (byte)0x70;

    short byteArrayAddress;
    short APDUBufferAddress;
    
    byte[] ServerAID = {(byte)0x01, (byte)0x02, (byte)0x03, (byte)0x04, (byte)0x01, (byte)0x01};
    
    byte[] ClientAID = {(byte)0x01, (byte)0x02, (byte)0x03, (byte)0x04, (byte)0x02, (byte)0x01};    
    
    public static void install( byte[] bArray, short bOffset, byte bLength ) {
        new ServerApplet(bArray, bOffset, bLength);
    }

    protected ServerApplet(byte[] bArray, short bOffset, byte bLength) {
        register();
    }
    
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
                
                case INS_FIND_BUFFER_ADDRESS: {
                    findBufferAddress(apdu);
                    break;
                }

                default :
                    ISOException.throwIt( ISO7816.SW_INS_NOT_SUPPORTED ) ;
                break ;
            }
        }
        else ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
    }
    
    /**
     * Method to find the memory address of the parameter passed
     * 
     * @param apdu, whose memory reference is found illegally
     * 
     * @return Sends the memory address of parameter 
     */
    void findBufferAddress(APDU apdu){

        byte[] apduBuffer = apdu.getBuffer();

        APDUBufferAddress = getByteArrayAddress(apduBuffer);

        short length = (short)2;
                
        apdu.setOutgoing();
        apdu.setOutgoingLength(length);
        
        Util.setShort(apduBuffer, (short)0, APDUBufferAddress);
        apdu.sendBytes((short)0 , length);
    }
        
    /** Method to illegally return the memory reference of the byte[] passed as parameter.
     *
     * @param bufferArray, whose memory reference to be returned
     * 
     * @return byteArrayAddress of type short. 
     * Edit the CAP file to return address of bufferArray  
     *
     */
    public short getByteArrayAddress(byte[] bufferArray){

        /* Manipulate the jca/cap file to return short type of the byte array parameter */
        return byteArrayAddress;
    }
    
    /**
     * Method to perform illegal casting of a short value to a short[] reference 
     *
     * @param addressValue, whose array value at address to be returned
     * 
     * @return returns null. Edit the CAP file to return short[] at the addressValue
     *
     */
    public short[] castShortToShortArray(short addressValue) { 

        /* Manipulate the jca/cap file to cast short to short[] */
 	return null;
    }
    

}
