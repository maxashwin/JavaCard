package packageTest;

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
    
    private final static byte INS_FIND_BUFFER_ADDRESS               = (byte)0x70;
    private final static byte INS_RETURN_MEMORY_OFFSET              = (byte)0x71;
    private final static byte INS_RETURN_MEMORY_ARRAY               = (byte)0x72;
    private final static byte INS_RETURN_READABLE_MEMORY_ADDRESSES  = (byte)0x73;
    
    private final static short SW_SERVER_UNAVAILABLE                = (short) 0x7001;
    private final static short SW_MSI_UNAVAILABLE                   = (short) 0x7002;
    private final static short OFFSET_DATA_UNAVAILABLE              = (short) 0x7003 ;
    
    static CardMngr cardManager = new CardMngr();
    
    private static final byte SELECT_SIMPLEAPPLET[] = {(byte) 0x00, (byte) 0xa4, (byte) 0x04, (byte) 0x00, (byte) 0x06,
     (byte)0x01, (byte)0x02, (byte)0x03, (byte)0x04, (byte)0x02, (byte)0x01};

    public static void main(String[] args) {
        try {
            
            /**
             * Following example based on tests on JavaCOS A40 card
             * 
             * CASE-1: To find the memory address of APDU Buffer
             * 
             * apdu[CardMngr.OFFSET_CLA] = (byte) 0xB0;
             * apdu[CardMngr.OFFSET_INS] = (byte) 0x70; 
             * apdu[CardMngr.OFFSET_P1] = (byte) 0x00;
             * apdu[CardMngr.OFFSET_P2] = (byte) 0x00;
             * 
             * Returns 00 50 as memory reference of APDU Buffer
             * 
             * CASE-2: To find the content of memory address 0092 at offset 0014
             * 
             * MEMORY_ADDRESS[] = {(byte) 0x00, (byte) 0x92};
             * apdu[CardMngr.OFFSET_CLA] = (byte) 0xB0;
             * apdu[CardMngr.OFFSET_INS] = (byte) 0x71; 
             * apdu[CardMngr.OFFSET_P1] = (byte) 0x00;
             * apdu[CardMngr.OFFSET_P2] = (byte) 0x14;
             * 
             * Returns 00 92 00 b0 00 14 0e 00
             * where:
             * 0092 = address passed
             * 00b0 = length of short[]
             * 0014 = offset of short[] passed
             * 0e00 = content of short[] at offset returned from illegal memory read 
             * 
             * CASE-3: To find the content of memory address 008c as array starting from offset 0000
             * 
             * MEMORY_ADDRESS[] = {(byte) 0x00, (byte) 0x8c};
             * MAXLENGTH[]     = {(byte) 0x00, (byte) 0x20};
             * apdu[CardMngr.OFFSET_CLA] = (byte) 0xB0;
             * apdu[CardMngr.OFFSET_INS] = (byte) 0x72; 
             * apdu[CardMngr.OFFSET_P1] = (byte) 0x00;
             * apdu[CardMngr.OFFSET_P2] = (byte) 0x00;
             * 
             * Returns 01 00 00 8d 80 00 00 06 73 69 6d 70 6c 65 00 00 20 00 01 00 08 00 00 00 00 00 00 00 00 8c 00 8f 90 00  
             * where each short value is the content of short[] at the memory address passed
             * 
             * CASE-4: To find the readable memory addresses starting from 0050, Number of iterations are 0040.
             *         (Not all the memory addresses were readable)
             * 
             * MEMORY_ADDRESS[] = {(byte) 0x00, (byte) 0x50};
             * MAXLENGTH[]     = {(byte) 0x00, (byte) 0x40};
             * apdu[CardMngr.OFFSET_CLA] = (byte) 0xB0;
             * apdu[CardMngr.OFFSET_INS] = (byte) 0x73; 
             * apdu[CardMngr.OFFSET_P1] = (byte) 0x00;
             * apdu[CardMngr.OFFSET_P2] = (byte) 0x00;
             * 
             * Returns
             * 00 50 00 52 00 54 00 56 00 58 00 80 00 82 00 86 00 8c 00 92 00 b6 00 c2 00 c4 00 c6 00 ca 00 00 00 00 00 00 00 00 00 
             * 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 90 00 
             * 
             * where each short value is the memory addresses readable. If value is 0000, memory address is not accesssible 
             */
            
            byte MEMORY_ADDRESS[] = {(byte) 0x00, (byte) 0x92};
    
            byte MAXLENGTH[]     = {(byte) 0x00, (byte) 0x40};
    
            byte ADDL_DATA[] = new byte[MEMORY_ADDRESS.length + MAXLENGTH.length];
            
            System.arraycopy(MEMORY_ADDRESS, 0, ADDL_DATA, 0, MEMORY_ADDRESS.length);
            System.arraycopy(MAXLENGTH, 0, ADDL_DATA, MEMORY_ADDRESS.length, MAXLENGTH.length);
            
            short additionalDataLen = (short)(ADDL_DATA.length);

            byte apdu[];
            apdu = new byte[CardMngr.HEADER_LENGTH + additionalDataLen];
            apdu[CardMngr.OFFSET_CLA] = (byte) 0xB0;
            apdu[CardMngr.OFFSET_INS] = (byte) 0x71; 
            apdu[CardMngr.OFFSET_P1] = (byte) 0x00;
            apdu[CardMngr.OFFSET_P2] = (byte) 0x14;
            apdu[CardMngr.OFFSET_LC] = (byte) additionalDataLen;
            
            if (additionalDataLen != 0){
                System.arraycopy(ADDL_DATA, 0, apdu, CardMngr.OFFSET_DATA, additionalDataLen);
            }
   
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