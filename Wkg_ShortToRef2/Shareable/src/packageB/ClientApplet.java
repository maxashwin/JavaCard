/***************************
 * Package : 01 02 03 04 02
 * 
 * Applet  : 01 02 03 04 02 01
 * 
 **************************/

package packageB;

import javacard.framework.*;
import packageA.MSI;

/**
 *
 * @author swatch
 */

public class ClientApplet extends Applet{
    
    final static byte CLA_SIMPLEAPPLET                            = (byte) 0xB0;

    final static byte INS_READ_MEMORY                             = (byte) 0x71 ;

    private final static short SW_SERVER_UNAVAILABLE              = (short) 0x7001 ;
    private final static short SW_MSI_UNAVAILABLE                 = (short) 0x7002 ;

    byte[] ServerAID = {(byte)0x01, (byte)0x02, (byte)0x03, (byte)0x04, (byte)0x01, (byte)0x01};
    
    byte[] ClientAID = {(byte)0x01, (byte)0x02, (byte)0x03, (byte)0x04, (byte)0x02, (byte)0x01};

    protected ClientApplet(){
          register();
    }

    
    protected ClientApplet(byte[] buffer, short offset, byte length){
          register();
    }

    public static void install(byte[] bArray, short bOffset, byte bLength) throws ISOException{
        new ClientApplet(bArray, bOffset, bLength );
    }

    public boolean select(){
        return true;
    }

    public void deselect(){
        return;
    }

    public void process(APDU apdu) throws ISOException
    {
        byte[] apduBuffer = apdu.getBuffer();

        if (selectingApplet())
            return;
        
        if (apduBuffer[ISO7816.OFFSET_CLA] == CLA_SIMPLEAPPLET) {
            switch (apduBuffer[ISO7816.OFFSET_INS] )
            {
                case INS_READ_MEMORY :
                    executeGetByteArrayAddress(apdu) ;
                    break ;

                default :
                    ISOException.throwIt( ISO7816.SW_INS_NOT_SUPPORTED ) ;
                break ;

            }
        }
        else ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
    }

    public void executeGetByteArrayAddress( APDU apdu ){
        
        byte[]    apduBuffer = apdu.getBuffer();

        byte b1 = apduBuffer[ISO7816.OFFSET_P1];
        byte b2 = apduBuffer[ISO7816.OFFSET_P2];
        
        /* Get the offset for the buffer array at which value to be returned */
        short offset = Util.makeShort(b1, b2);
        
        /* Get the AID of the ServerApplet */
        AID aid = JCSystem.lookupAID(ServerAID, (short) 0, (byte) ServerAID.length);
        
        if (aid == null) 
            ISOException.throwIt(SW_SERVER_UNAVAILABLE);

        /* Get the Shareable object from ServerApplet */
        MSI ServerObject = (MSI)(JCSystem.getAppletShareableInterfaceObject(aid, (byte) 0));
        
        if (ServerObject == null) 
            ISOException.throwIt(SW_MSI_UNAVAILABLE);

        /* Invoke the method to get the reference of the byte array passed */
        short byteArrayAddress = ServerObject.getByteArrayAddress(apduBuffer);
        
        /* Invoke the method to get the byte array from the short value passed */
        byte[] temp = ServerObject.castShortToByteArray(byteArrayAddress);
                
        apdu.setOutgoing();
        apdu.setOutgoingLength( (short) (2) );
        
        /* Take the offset from P1 and P2 and set it as offset to temp array */
        Util.arrayCopy(temp, offset, apduBuffer, (short)0, (short)2);

        apdu.sendBytes((short)0 , (short) (2));
    }
}