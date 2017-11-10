/*****************************
 * Package : 01 02 03 04 02
 * 
 * Applet  : 01 02 03 04 02 01
 * 
 ****************************/

package packageB;

import javacard.framework.*;
import packageA.MSI;

/**
 *
 * @author swatch
 */

public class ClientApplet extends Applet{
    
    final static byte CLA_SIMPLEAPPLET                            = (byte) 0xB0;

    final static byte INS_TYPE_CONFUSION                          = (byte) 0x71;

    private final static short SW_SERVER_UNAVAILABLE              = (short) 0x7001;
    private final static short SW_MSI_UNAVAILABLE                 = (short) 0x7002;
    private final static short SW_SHORT_VALUE_UNAVAILABLE         = (short) 0x7003;

    byte[] ServerAID = {(byte)0x01, (byte)0x02, (byte)0x03, (byte)0x04, (byte)0x01, (byte)0x01};
    
    byte[] ClientAID = {(byte)0x01, (byte)0x02, (byte)0x03, (byte)0x04, (byte)0x02, (byte)0x01};

    AID aid;
    MSI ServerObject;

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
                case INS_TYPE_CONFUSION :
                    executeTypeConfusion(apdu);
                    break;

                default :
                    ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED) ;
                    break;

            }
        }
        else ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
    }

    public void getServerObject(){
        
        /* Look up for the AID of the ServerApplet */ 
        aid = JCSystem.lookupAID(ServerAID, (short) 0, (byte) ServerAID.length);
        
        if (aid == null) 
            ISOException.throwIt(SW_SERVER_UNAVAILABLE);

        /* Get the Shareable Interface Object*/
        ServerObject = (MSI)(JCSystem.getAppletShareableInterfaceObject(aid, (byte) 0));
        
        if (ServerObject == null) 
            ISOException.throwIt(SW_MSI_UNAVAILABLE);
    }
    
    /**
     * The method passes a byte[] as parameter which is interpreted as short[] by ServerApplet
     * @param apdu 
     * 
     * The method returns double the bytes than requested for
     */
    public void executeTypeConfusion(APDU apdu){
        
        byte[] apduBuffer = apdu.getBuffer();
        
        getServerObject();

        /* Get the offset from OFFSET_P1 and OFFSET_P2 */
        byte b1 = apduBuffer[ISO7816.OFFSET_P1];
        byte b2 = apduBuffer[ISO7816.OFFSET_P2];
        
        short offset = Util.makeShort(b1, b2);
        
        short shortValue;
        
        /* Invoke the typeConfusion method of the ServerObject */
        try{
            shortValue = ServerObject.typeConfusion(apduBuffer, offset);
        }catch(Exception e){
            shortValue = (short)0;
            ISOException.throwIt(SW_SHORT_VALUE_UNAVAILABLE);
        }

        Util.setShort(apduBuffer, (short)0, shortValue);
        apdu.setOutgoingAndSend((short)0, (short) 2);
  
    }
}
