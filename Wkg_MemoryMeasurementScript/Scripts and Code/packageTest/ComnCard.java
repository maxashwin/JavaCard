package packageTest;

import javax.smartcardio.ResponseAPDU;
import java.io.*;

/**
 *
 * @author swatch
 */
public class ComnCard {

    static CardMngr cardManager = new CardMngr();

    private static byte SELECT_SIMPLEAPPLET[] = {(byte) 0x00, (byte) 0xa4, (byte) 0x04, (byte) 0x00, (byte) 0x06,
        (byte)0x01, (byte)0x02, (byte)0x03, (byte)0x04, (byte)0x01, (byte)0x01};
    
    public static void main(String[] args) {
        try {

            short additionalDataLen = 0;
            byte apdu[];
            apdu = new byte[CardMngr.HEADER_LENGTH + additionalDataLen];
            apdu[CardMngr.OFFSET_CLA] = (byte) 0xB0;
            apdu[CardMngr.OFFSET_INS] = (byte) 0x80; 
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
            
            /***********************************************************************************/
            
            byte[] byteResponse = response.getBytes();

            System.out.println();
            System.out.println("*******************************************************************");
            System.out.println();
            
            if((byteResponse[(short)(byteResponse.length - 2)] == -112) &&  (byteResponse[(short)(byteResponse.length - 1)] == 0)){
                System.out.println("Success....!!!");
                
                System.out.println();
                
                short index = 0;
                short count = 0;
                byte[] memPersistent        = new byte[2];
                byte[] memTransientDeSelect = new byte[2];
                byte[] memTransientReset    = new byte[2];
                
                String fileName = "/home/swatch/Desktop/Thesis_Chennai/ScriptFile/constructorDetails.txt";
                
                BufferedReader reader = new BufferedReader(new FileReader(new File(fileName)));
                int fileSize = 0;
                String line = null;
                while ((line = reader.readLine()) != null){
                    fileSize++;
                }
                reader.close();

                reader = new BufferedReader(new FileReader(new File(fileName)));
                String[] constructorLine = new String[fileSize];
                int fileIndex = 0;
                while ((line = reader.readLine()) != null){
                    constructorLine[fileIndex] = line;
                    fileIndex++;
                }
                reader.close();    

                BufferedWriter writer = new BufferedWriter(new FileWriter(new File(fileName)));

                while (index < (byteResponse.length-2)){
                    memPersistent[0]        = byteResponse[index];
                    memPersistent[1]        = byteResponse[index+1];
                    
                    memTransientDeSelect[0] = byteResponse[index+2];
                    memTransientDeSelect[1] = byteResponse[index+3];
                    
                    memTransientReset[0]    = byteResponse[index+4];
                    memTransientReset[1]    = byteResponse[index+5];
                    
                    System.out.println(count+"  "+cardManager.byteToHex(memPersistent[0])+""+cardManager.byteToHex(memPersistent[1])+
                            "  "+cardManager.byteToHex(memTransientDeSelect[0])+""+cardManager.byteToHex(memTransientDeSelect[1])+
                            "  "+cardManager.byteToHex(memTransientReset[0])+""+cardManager.byteToHex(memTransientReset[1]));
                    
                    writer.write(constructorLine[count]+" "+cardManager.byteToHex(memPersistent[0])+""+cardManager.byteToHex(memPersistent[1])+
                            "  "+cardManager.byteToHex(memTransientDeSelect[0])+""+cardManager.byteToHex(memTransientDeSelect[1])+
                            "  "+cardManager.byteToHex(memTransientReset[0])+""+cardManager.byteToHex(memTransientReset[1]));
                    writer.newLine();
                       
                    index+=6; count++;                 
                }
                writer.close();
                System.out.println();
                System.out.println("Memory Measurements written into : "+fileName);
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