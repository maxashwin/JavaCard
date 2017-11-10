package ComnCard;

import javax.smartcardio.ResponseAPDU;

/**
 *
 * @author swatch
 */
public class ComnCard {
    
    /**
     * AID
     * 
     * ServerApplet  : 01 02 03 04 01 01
     * 
     * ClientApplet  : 01 02 03 04 02 01
     */

    static CardMngr cardManager = new CardMngr();
    
    private final static byte CLA_SIMPLEAPPLET                            = (byte) 0xB0;
    
    private final static byte INS_SET_BUFFEROFFSET                        = (byte) 0x71;
    private final static byte INS_TYPE_CONFUSION                          = (byte) 0x72;
    
    private static final byte SELECT_SIMPLEAPPLET[] = {(byte) 0x00, (byte) 0xa4, (byte) 0x04, (byte) 0x00, (byte) 0x06,
                                                       (byte)0x01, (byte)0x02, (byte)0x03, (byte)0x04, (byte)0x02, (byte)0x01};
    
    public static void main(String[] args) {
        try {
            
            /**
             * Following example based on Javacos A40 Java Card
             * 
             * All communications are with ClientApplet with 
             * 
             * SELECT_SIMPLEAPPLET[] = {(byte) 0x00, (byte) 0xa4, (byte) 0x04, (byte) 0x00, (byte) 0x06,
                                                       (byte)0x01, (byte)0x02, (byte)0x03, (byte)0x04, (byte)0x02, (byte)0x01};
             * 
             * CASE-1: Offset set as 00 00
             * 
             * apdu[CardMngr.OFFSET_CLA] = (byte) 0xB0;
             * apdu[CardMngr.OFFSET_INS] = (byte) 0x71; 
             * apdu[CardMngr.OFFSET_P1]  = (byte) 0x00;
             * apdu[CardMngr.OFFSET_P2]  = (byte) 0x00;
             * 
             * Returns b0 71 90 00. Here b0 71 are the first two bytes sent through APDU
             * 
             * CASE-2: Offset set as 00 ae
             * 
             * apdu[CardMngr.OFFSET_CLA] = (byte) 0xB0;
             * apdu[CardMngr.OFFSET_INS] = (byte) 0x71; 
             * apdu[CardMngr.OFFSET_P1]  = (byte) 0x00;
             * apdu[CardMngr.OFFSET_P2]  = (byte) 0xae;
             * 
             * Returns ca 75 90 00 
             * 
             * CASE-3: Offset set as 01 04
             * 
             * apdu[CardMngr.OFFSET_CLA] = (byte) 0xB0;
             * apdu[CardMngr.OFFSET_INS] = (byte) 0x71; 
             * apdu[CardMngr.OFFSET_P1]  = (byte) 0x01;
             * apdu[CardMngr.OFFSET_P2]  = (byte) 0x04;
             * 
             * Returns 59 f9 90 00 
             * 
             */

            short additionalDataLen = (short)0;
            byte apdu[];
            apdu = new byte[CardMngr.HEADER_LENGTH + additionalDataLen];
            apdu[CardMngr.OFFSET_CLA] = (byte) 0xB0;
            apdu[CardMngr.OFFSET_INS] = (byte) 0x71; 
            apdu[CardMngr.OFFSET_P1]  = (byte) 0x01;
            apdu[CardMngr.OFFSET_P2]  = (byte) 0x04;
            apdu[CardMngr.OFFSET_LC]  = (byte) additionalDataLen;
                  
            /***************************** Comn with Card ***************************************/
            
            ResponseAPDU response = null;
            
            if (cardManager.ConnectToCard()) {

                cardManager.sendAPDU(SELECT_SIMPLEAPPLET);
                
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