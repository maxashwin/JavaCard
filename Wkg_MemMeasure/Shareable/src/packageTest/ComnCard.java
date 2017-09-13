package packageTest;

import javax.smartcardio.ResponseAPDU;

/**
 *
 * @author swatch
 */
public class ComnCard {

    static CardMngr cardManager = new CardMngr();

    private static byte SELECT_SIMPLEAPPLET[] = {(byte) 0x00, (byte) 0xa4, (byte) 0x04, (byte) 0x00, (byte) 0x06,
     (byte)0x01, (byte)0x02, (byte)0x03, (byte)0x04, (byte)0x01, (byte)0x02};
    
    public static void main(String[] args) {
        try {

            short additionalDataLen = 0;
            byte apdu[];
            apdu = new byte[CardMngr.HEADER_LENGTH + additionalDataLen];
            apdu[CardMngr.OFFSET_CLA] = (byte) 0xB0;
            apdu[CardMngr.OFFSET_INS] = (byte) 0x71; 
            apdu[CardMngr.OFFSET_P1] = (byte) 0x03;
            apdu[CardMngr.OFFSET_P2] = (byte) 0x04;
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
                
                System.out.println();
                
                byte[] memPersistent = new byte[2];
                memPersistent[0] = byteResponse[12];
                memPersistent[1] = byteResponse[13];
            
                byte[] memTransientDeSelect = new byte[2];
                memTransientDeSelect[0] = byteResponse[14];
                memTransientDeSelect[1] = byteResponse[15];
            
                byte[] memTransientReset = new byte[2];
                memTransientReset[0] = byteResponse[16];
                memTransientReset[1] = byteResponse[17];
                           
                if((memPersistent[0]== 0) & (memPersistent[1]== 0)){
                
                    System.out.println("Either the Persistent Memory Consumption is Zero or");
                
                    //If the available Persistent Memory is greater than 7FFF then the function returns 7FFF only
                    System.out.println("Persistent Memory Consumption cannot be measured");
                }
                else{
                    System.out.println("Persistent Memory Consumption is : " + cardManager.byteToHex(memPersistent[0]) 
                            + cardManager.byteToHex(memPersistent[1]) + " bytes");
                }

                System.out.println();

                if((memTransientDeSelect[0]== 0) & (memTransientDeSelect[1]== 0)){
                    System.out.println("Transient (Deselect) Memory Consumption is Zero");
                }
                else{
                    System.out.println("Transient (Deselect) Memory Consumption is : " + cardManager.byteToHex(memTransientDeSelect[0]) 
                            + cardManager.byteToHex(memTransientDeSelect[1]) + " bytes");
                }
                
                System.out.println();

                if((memTransientReset[0]== 0) & (memTransientReset[1]== 0)){
                    System.out.println("Transient (Reset) Memory Consumption is Zero");
                }
                else{
                    System.out.println("Transient (Reset) Memory Consumption is : " + cardManager.byteToHex(memTransientReset[0]) 
                            + cardManager.byteToHex(memTransientReset[1]) + " bytes");
                }
                
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