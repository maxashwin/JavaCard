
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

    final static byte INS_FOO                                     = (byte)0x71;

    private final static short SW_SERVER_NOT_EXISTS               = (short) 0x7001;
    
    private final static short SW_FAILED_TO_OBTAIN_MSI            = (short) 0x7002;

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
                
                case INS_FOO:
                    foo( apdu ) ;
                    break ;

                default :
                    ISOException.throwIt( ISO7816.SW_INS_NOT_SUPPORTED ) ;
                break ;

            }
        }
        else ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
    }
    
    public void foo( APDU apdu ){
        
        byte[]    apdubuf = apdu.getBuffer();
        
        byte b1 = apdubuf[ISO7816.OFFSET_P1];
        byte b2 = apdubuf[ISO7816.OFFSET_P2];
        
        short amount = (short)(b1 + b2);
        
        grantCredit(amount);
        
        apdu.setOutgoing();

        apdu.setOutgoingLength( (short) 2 );

        Util.setShort(apdubuf, (short)0, amount);
        apdu.sendBytes((short)0, (short)2);
    }
    
    public void grantCredit(short amount){
        
        serverAID = JCSystem.lookupAID(AID_SERVER, (short) 0, (byte) AID_SERVER.length);
        
        if (serverAID == null) 
            ISOException.throwIt(SW_SERVER_NOT_EXISTS);
        
        MSIB_SO = (MSIB)(JCSystem.getAppletShareableInterfaceObject(serverAID, (byte) 0));
        
        if (MSIB_SO == null) 
            ISOException.throwIt(SW_FAILED_TO_OBTAIN_MSI);
        
        MSIB_SO.grantCreditClientB(amount);
        
        /* ClientAppletB should be given access to only MSIB type. Access to MSIC type should be prevented */  
        MSIC_SO = (MSIC) JCSystem.getAppletShareableInterfaceObject(serverAID, (byte)0);
        
        if (MSIC_SO == null) 
            ISOException.throwIt(SW_FAILED_TO_OBTAIN_MSI);
        
        MSIC_SO.grantCreditClientC(amount);
               
    }

}
