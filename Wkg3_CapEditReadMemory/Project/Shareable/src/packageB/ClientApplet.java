/***************************
 * Package : 01 02 03 04 02
 * 
 * Applet  : 01 02 03 04 02 01
 * 
 **************************/

package packageB;

import javacard.framework.*;
import packageA.MSI;

/**
 *
 * @author swatch
 */

public class ClientApplet extends Applet{
    
    final static byte CLA_SIMPLEAPPLET                              = (byte) 0xB0;

    private final static byte INS_FIND_BUFFER_ADDRESS               = (byte)0x70;
    private final static byte INS_RETURN_MEMORY_OFFSET              = (byte)0x71;
    private final static byte INS_RETURN_MEMORY_ARRAY               = (byte)0x72;
    private final static byte INS_RETURN_READABLE_MEMORY_ADDRESSES  = (byte)0x73;
    
    private final static short SW_SERVER_UNAVAILABLE                = (short) 0x7001;
    private final static short SW_MSI_UNAVAILABLE                   = (short) 0x7002;
    private final static short OFFSET_DATA_UNAVAILABLE              = (short) 0x7003 ;

    byte[] ServerAID = {(byte)0x01, (byte)0x02, (byte)0x03, (byte)0x04, (byte)0x01, (byte)0x01};
    
    byte[] ClientAID = {(byte)0x01, (byte)0x02, (byte)0x03, (byte)0x04, (byte)0x02, (byte)0x01};
    
    short byteArrayAddress;
    short APDUBufferAddress;

    protected ClientApplet(){
          register();
    }

    protected ClientApplet(byte[] buffer, short offset, byte length){
          register();
    }

    public static void install(byte[] bArray, short bOffset, byte bLength) throws ISOException{
        new ClientApplet(bArray, bOffset, bLength );
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
            switch (apduBuffer[ISO7816.OFFSET_INS] )
            {
                case INS_FIND_BUFFER_ADDRESS: {
                    findBufferAddress(apdu);
                    break;
                }
                
                case INS_RETURN_MEMORY_OFFSET: {
                    returnMemoryAtOffset(apdu);
                    break;
                }
                
                case INS_RETURN_MEMORY_ARRAY: {
                    returnMemoryAsArray(apdu);
                    break;
                }
                
                case INS_RETURN_READABLE_MEMORY_ADDRESSES: {
                    returnMemoryAddresses(apdu);
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
     * Method to find the memory address of the parameter passed
     * 
     * @param apdu, whose memory reference is found illegally
     * 
     * @return Sends the memory address of apdu buffer 
     */
    void findBufferAddress(APDU apdu){

        byte[] apduBuffer = apdu.getBuffer();
        
        /* Get the AID of the ServerApplet */
        AID aid = JCSystem.lookupAID(ServerAID, (short) 0, (byte) ServerAID.length);
        
        if (aid == null) 
            ISOException.throwIt(SW_SERVER_UNAVAILABLE);

        /* Get the Shareable object from ServerApplet */
        MSI ServerObject = (MSI)(JCSystem.getAppletShareableInterfaceObject(aid, (byte) 0));
        
        if (ServerObject == null) 
            ISOException.throwIt(SW_MSI_UNAVAILABLE);

        APDUBufferAddress = ServerObject.getByteArrayAddress(apduBuffer);

        short length = (short)2;
                
        apdu.setOutgoing();
        apdu.setOutgoingLength(length);
        
        Util.setShort(apduBuffer, (short)0, APDUBufferAddress);
        apdu.sendBytes((short)0 , length);
    }
    
    
    /** 
     * Method to return the content of memory at an offset
     * 
     * @param apdu 
     * Passes offset as OFFSET_P1 and OFFSET_P2
     * Passes memory address as OFFSET_CDATA and (OFFSET_CDATA + 1)
     * 
     * @return 
     * Sends memory address value
     * Sends length of the short[] in memory
     * Sends the offset
     * Sends content of memory at offset passed
     */
    void returnMemoryAtOffset(APDU apdu){

        byte[] apduBuffer = apdu.getBuffer();
        
        /* Get the AID of the ServerApplet */
        AID aid = JCSystem.lookupAID(ServerAID, (short) 0, (byte) ServerAID.length);
        
        if (aid == null) 
            ISOException.throwIt(SW_SERVER_UNAVAILABLE);

        /* Get the Shareable object from ServerApplet */
        MSI ServerObject = (MSI)(JCSystem.getAppletShareableInterfaceObject(aid, (byte) 0));
        
        if (ServerObject == null) 
            ISOException.throwIt(SW_MSI_UNAVAILABLE);
        
        /* Get the offset from OFFSET_P1 and OFFSET_P2 */
        byte b1 = apduBuffer[ISO7816.OFFSET_P1];
        byte b2 = apduBuffer[ISO7816.OFFSET_P2];
        
        short offset = Util.makeShort(b1, b2);
        
        /* Get the memory address from OFFSET_CDATA and (OFFSET_CDATA+1) */
        if(apduBuffer[ISO7816.OFFSET_LC] == 0)
            ISOException.throwIt(OFFSET_DATA_UNAVAILABLE);
        
        byte m1 = apduBuffer[ISO7816.OFFSET_CDATA];
        byte m2 = apduBuffer[ISO7816.OFFSET_CDATA+1];
        
        byteArrayAddress = Util.makeShort(m1, m2);
        
        /* Retrieve the short[] from the memory address */
        short[] shortArray = ServerObject.castShortToShortArray(byteArrayAddress);
        
        short length = (short)8;
                
        apdu.setOutgoing();
        apdu.setOutgoingLength(length);
        
        Util.setShort(apduBuffer, (short)0, byteArrayAddress);
        Util.setShort(apduBuffer, (short)2, (short)shortArray.length);
        Util.setShort(apduBuffer, (short)4, offset);
        Util.setShort(apduBuffer, (short)6, shortArray[offset]);
        
        apdu.sendBytes((short)0 , length);
    }
    
    /**
     * Method to return content of memory as short[] starting from offset
     * 
     * @param apdu 
     * Passes offset as OFFSET_P1 and OFFSET_P2
     * Passes memory address as OFFSET_CDATA and (OFFSET_CDATA + 1)
     * Passes length of short[] to be read as (OFFSET_CDATA + 2) and (OFFSET_CDATA + 3)
     * 
     * @return 
     * Sends content of memory as short[] starting from offset of size length 
     */
    void returnMemoryAsArray(APDU apdu){

        byte[] apduBuffer = apdu.getBuffer();
        
        /* Get the AID of the ServerApplet */
        AID aid = JCSystem.lookupAID(ServerAID, (short) 0, (byte) ServerAID.length);
        
        if (aid == null) 
            ISOException.throwIt(SW_SERVER_UNAVAILABLE);

        /* Get the Shareable object from ServerApplet */
        MSI ServerObject = (MSI)(JCSystem.getAppletShareableInterfaceObject(aid, (byte) 0));
        
        if (ServerObject == null) 
            ISOException.throwIt(SW_MSI_UNAVAILABLE);

        /* Get the offset from OFFSET_P1 and OFFSET_P2 */
        byte b1 = apduBuffer[ISO7816.OFFSET_P1];
        byte b2 = apduBuffer[ISO7816.OFFSET_P2];
        
        short offset = Util.makeShort(b1, b2);
              
        /* Get the memory address from OFFSET_CDATA and (OFFSET_CDATA+1) */
        if(apduBuffer[ISO7816.OFFSET_LC] == 0)
            ISOException.throwIt(OFFSET_DATA_UNAVAILABLE);
        
        byte m1 = apduBuffer[ISO7816.OFFSET_CDATA];
        byte m2 = apduBuffer[ISO7816.OFFSET_CDATA + 1];
                
        byteArrayAddress = Util.makeShort(m1, m2);

        /* Retrieve the short[] from the memory address */
        short[] shortArray = ServerObject.castShortToShortArray(byteArrayAddress);
        
        byte l1 = apduBuffer[ISO7816.OFFSET_CDATA + 2];
        byte l2 = apduBuffer[ISO7816.OFFSET_CDATA + 3];
                
        short length = Util.makeShort(l1, l2);
      
        apdu.setOutgoing();
        apdu.setOutgoingLength(length);
        
        /* Return the contents of memory as short[] */  
        for (short i=0; i<length; i++){
            Util.setShort(apduBuffer, (short)(2*i), shortArray[offset]);
            offset = (short)(offset+1);
        }
        apdu.sendBytes((short)0 ,length);
    }
    
    /**
     * Method to determine the readable memory locations
     * 
     * @param apdu
     * Passes memory address as OFFSET_CDATA and (OFFSET_CDATA + 1)
     * Passes number of iterations as (OFFSET_CDATA + 2) and (OFFSET_CDATA + 3)
     * 
     * @return
     * Sends the memory address if successful read 
     * Sends 00 00 if memory read unsuccessful
     * 
     */
    void returnMemoryAddresses(APDU apdu){

        byte[] apduBuffer = apdu.getBuffer();
        
        /* Get the AID of the ServerApplet */
        AID aid = JCSystem.lookupAID(ServerAID, (short) 0, (byte) ServerAID.length);
        
        if (aid == null) 
            ISOException.throwIt(SW_SERVER_UNAVAILABLE);

        /* Get the Shareable object from ServerApplet */
        MSI ServerObject = (MSI)(JCSystem.getAppletShareableInterfaceObject(aid, (byte) 0));
        
        if (ServerObject == null) 
            ISOException.throwIt(SW_MSI_UNAVAILABLE);

        /* Get the memory address from OFFSET_CDATA and (OFFSET_CDATA+1) */
        if(apduBuffer[ISO7816.OFFSET_LC] == 0)
            ISOException.throwIt(OFFSET_DATA_UNAVAILABLE);
        
        byte mem1 = apduBuffer[ISO7816.OFFSET_CDATA];
        byte mem2 = apduBuffer[ISO7816.OFFSET_CDATA + 1];
        
        byteArrayAddress = Util.makeShort(mem1, mem2);
        
        /* Get the number of iterations from (OFFSET_CDATA+2) and (OFFSET_CDATA+3) */
        byte len1 = apduBuffer[ISO7816.OFFSET_CDATA + 2];
        byte len2 = apduBuffer[ISO7816.OFFSET_CDATA + 3];

        short max = Util.makeShort(len1, len2);
        
        short[] successfulAddress = new short[max];
        short index = (short)0;
             
        /* Iterate for max number to determine readable memory locations */
        for(short i = 0; i< max; i++){
            try{
                short[] shortArray = ServerObject.castShortToShortArray(byteArrayAddress);
                short dummyTestLength = (short)shortArray.length;
                short dummyTestValue = shortArray[0];
                successfulAddress[index] = byteArrayAddress;
                index++;
                byteArrayAddress = (short) (byteArrayAddress + 2);
            }catch(Exception e){
                byteArrayAddress = (short) (byteArrayAddress + 2);
                successfulAddress[index] = (short)0;
            }
        }
        
        short length = max;
                
        apdu.setOutgoing();
        apdu.setOutgoingLength(length);
   
        /* Return the readable memory locations or 00 00 if unsuccessful */
        index = (short)0;
        for (short i=0; i<length; i++){
            Util.setShort(apduBuffer, (short)(2*i), successfulAddress[index]);
            index++;
        }
        apdu.sendBytes((short)0 , length);
    }
}
