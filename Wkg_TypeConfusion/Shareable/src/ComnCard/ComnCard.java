package ComnCard;

import javax.smartcardio.ResponseAPDU;

/**
 *
 * @author swatch
 */
public class ComnCard {

    static CardMngr cardManager = new CardMngr();
    
    private final static byte CLA_SIMPLEAPPLET                            = (byte) 0xB0;
    
    private final static byte INS_SET_BUFFEROFFSET                        = (byte) 0x71;

    private final static byte INS_TYPE_CONFUSION                          = (byte) 0x72;
    
    private static final byte SELECT_SIMPLEAPPLET[] = {(byte) 0x00, (byte) 0xa4, (byte) 0x04, (byte) 0x00, (byte) 0x06,
                                                       (byte)0x01, (byte)0x02, (byte)0x03, (byte)0x04, (byte)0x02, (byte)0x01};
    
    public static void main(String[] args) {
        try {

            short additionalDataLen = 0;
            byte apdu[];
            apdu = new byte[CardMngr.HEADER_LENGTH + additionalDataLen];
            apdu[CardMngr.OFFSET_CLA] = CLA_SIMPLEAPPLET ;
            apdu[CardMngr.OFFSET_INS] = (byte) 0x71; 
            apdu[CardMngr.OFFSET_P1]  = (byte) 0x00;
            apdu[CardMngr.OFFSET_P2]  = (byte) 0x02;
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