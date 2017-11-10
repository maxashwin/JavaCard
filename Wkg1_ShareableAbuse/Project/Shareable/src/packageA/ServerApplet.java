
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
public class ServerApplet extends javacard.framework.Applet implements MSIB, MSIC{
    
    private final static byte CLA_TYPE_ATTACK           = (byte)0xB0 ;

    private final static byte INS_RETURN_MILES          = (byte)0x71 ;

    protected short milesB;
    protected short milesC;
    
    private static byte AID_SERVER[] = {(byte)0x01, (byte)0x02, (byte)0x03, (byte)0x04, (byte)0x01, (byte)0x01};
    
    private static byte AID_CLIENT_B[] = {(byte)0x01, (byte)0x02, (byte)0x03, (byte)0x04, (byte)0x02, (byte)0x01};
    
    private static byte AID_CLIENT_C[] = {(byte)0x01, (byte)0x02, (byte)0x03, (byte)0x04, (byte)0x03, (byte)0x01};

    protected ServerApplet(){
          register();
    }
    
    protected ServerApplet(byte[] buffer, short offset, byte length){
        milesB = (short)0;
        milesC = (short)0;
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
     * Method to return Shareable Interface Object depending on requesting Applet
     * @param clientAID passes the AID of the requesting Applet 
     * @param parameter passes the parameter type. Default is 0
     * 
     * @return 
     *  MSIB type to ClientAppletB after authenticating AID_CLIENT_B
     *  MSIC type to ClientAppletC after authenticating AID_CLIENT_C
     *  null if neither ClientAppletB or ClientAppletC
     */
    public Shareable getShareableInterfaceObject(AID clientAID, byte parameter) {
        
        /* Return MSIB type to ClientApplet having AID of AID_CLIENT_B */
        if(clientAID.equals(AID_CLIENT_B, (short)0, (byte) AID_CLIENT_B.length))
            return (MSIB)this;
        
        /* Return MSIC type to ClientApplet having AID of AID_CLIENT_C */
        else if(clientAID.equals(AID_CLIENT_C, (short)0, (byte) AID_CLIENT_C.length))
            return (MSIC)this;
        
        /* Return null if not AID_CLIENT_B nor AID_CLIENT_C */
        else
            return null;
    }
    
    public void process(APDU apdu) throws ISOException
    {
        byte[] apduBuffer = apdu.getBuffer();

        if (selectingApplet())
            return;

        if(apduBuffer[ISO7816.OFFSET_CLA] == CLA_TYPE_ATTACK){
          switch ( apduBuffer[ISO7816.OFFSET_INS] )
          {
              case INS_RETURN_MILES: {
                returnCreditedMiles(apdu);
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
     * Method returns the miles credited by Applets through Shareable Interface Objects
     * @param apdu 
     * 
     * @return Sends back values of milesB and milesC
     *  milesB represents miles credited through MSIB Shareable Interface Object
     *  milesC represents miles credited through MSIC Shareable Interface Object
     */
    void returnCreditedMiles(APDU apdu) {
        
        byte[]    apdubuf = apdu.getBuffer();

        apdu.setOutgoing();

        apdu.setOutgoingLength( (short) 4 );

        Util.setShort(apdubuf,(short)(0), milesB);
        Util.setShort(apdubuf,(short)(2), milesC);
        apdu.sendBytes((short)0 , (short) 4);
    }

    /**
     * Method to credit the amount passed by ClientAppletB. 
     * Access to this method by ClientAppletC should be prevented.
     * 
     * @param amount value added to the existing milesB
     */
    public void grantCreditClientB(short amount){     
        milesB = (short)(milesB + amount);
    }
    
    /**
     * Method to credit the amount passed by ClientAppletC. 
     * Access to this method by ClientAppletB should be prevented.
     * 
     * @param amount value added to the existing milesC
     */
    public void grantCreditClientC(short amount){     
        milesC = (short)(milesC + amount);
    }
    
}
