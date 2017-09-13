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

    final static byte INS_TYPE_CONFUSION                          = (byte) 0x72;

    private final static short SW_SERVER_UNAVAILABLE              = (short) 0x7001;
    private final static short SW_MSI_UNAVAILABLE                 = (short) 0x7002;

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
                case INS_TYPE_CONFUSION :
                    executeTypeConfusion( apdu ) ;
                    break ;

                default :
                    ISOException.throwIt( ISO7816.SW_INS_NOT_SUPPORTED ) ;
                break ;

            }
        }
        else ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
    }

    public void executeTypeConfusion( APDU apdu ){
        
        AID aid = JCSystem.lookupAID(ServerAID, (short) 0, (byte) ServerAID.length);
        
        if (aid == null) 
            ISOException.throwIt(SW_SERVER_UNAVAILABLE);

        MSI ServerObject = (MSI)(JCSystem.getAppletShareableInterfaceObject(aid, (byte) 0));
        
        if (ServerObject == null) 
            ISOException.throwIt(SW_MSI_UNAVAILABLE);

        byte[]    apduBuffer = apdu.getBuffer();

        short tmp = ServerObject.typeConfusion(apduBuffer);
        
        Util.setShort(apduBuffer, (short)0, tmp);
        apdu.setOutgoingAndSend((short)0, (short) 2);
  
    }
}