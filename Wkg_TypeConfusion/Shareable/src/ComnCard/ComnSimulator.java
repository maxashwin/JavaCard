package ComnCard;

//import applets.SimpleApplet;
import packageB.ClientApplet;
import java.util.Arrays;
import javacard.framework.Util;
import javax.smartcardio.ResponseAPDU;

/**
 *
 * @author swatch
 */
public class ComnSimulator {

    static CardMngr cardManager = new CardMngr();
    
    private final static byte CLA_TYPE_ATTACK                     = (byte)0xB0 ;

    private final static byte INS_START_ATTACK_METHOD             = (byte)0x40 ;
    
    private static byte SELECT_SIMPLEAPPLET[] = {(byte) 0x00, (byte) 0xa4, (byte) 0x04, (byte) 0x00, (byte) 0x06,
                                                (byte)0x01, (byte)0x02, (byte)0x03, (byte)0x04, (byte)0x02, (byte)0x01};
    
    public static void main(String[] args) {
        try {
            
            /*********************** Measure the Free Memory Available *******************/

            byte[] installData = new byte[10]; // no special install data passed now - can be used to pass initial keys etc.
            //Can be used to set some initial Secure data such as default PIN etc...
            cardManager.prepareLocalSimulatorApplet(SELECT_SIMPLEAPPLET, installData, ClientApplet.class);   
            
            short additionalDataLen = 0;
            byte apdu[];
            apdu = new byte[CardMngr.HEADER_LENGTH + additionalDataLen];
            apdu[CardMngr.OFFSET_CLA] = (byte)0xB0 ;
            apdu[CardMngr.OFFSET_INS] = (byte)0x71; 
            apdu[CardMngr.OFFSET_P1] = (byte) 0x04;
            apdu[CardMngr.OFFSET_P2] = (byte) 0x06;
            apdu[CardMngr.OFFSET_LC] = (byte) additionalDataLen;
        
            /*************************************************************************/
            
            //byte[] byteResponse = response.getBytes();

            byte[] response = cardManager.sendAPDUSimulator(apdu); 
            
            System.out.println();
            System.out.println("*******************************************************************");
            System.out.println();
            System.out.println(cardManager.bytesToHex(response));
            
            if((cardManager.byteToHex(response[0]).equals("90") &&  cardManager.byteToHex(response[1]).equals("00"))){
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