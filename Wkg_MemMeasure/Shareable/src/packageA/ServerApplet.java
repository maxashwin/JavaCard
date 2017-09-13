
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
public class ServerApplet extends javacard.framework.Applet implements MultiSelectable{
    
    final static byte CLA_SIMPLEAPPLET               = (byte) 0xB0;
    final static byte INS_USERINPUT1                 = (byte) 0x71;
    
    byte[] AID_SERVER = {(byte)0x01, (byte)0x02, (byte)0x03, (byte)0x04, (byte)0x01, (byte)0x01};
    byte[] AID_MEMORY = {(byte)0x01, (byte)0x02, (byte)0x03, (byte)0x04, (byte)0x01, (byte)0x02};
    
    byte[] AID_CLIENT = {(byte)0x01, (byte)0x02, (byte)0x03, (byte)0x04, (byte)0x02, (byte)0x01};
    
    public static short miles;
    
    final static short ARRAY_LENGTH = (short) 0x2fff;
    
    /* Transient memory type arrays */
    private byte m_ramArray_Deselect[] = null;
    private byte m_ramArray_Reset[] = null;
    
    /* Persistent memory type arrays */
    private byte[] m_dataArray = null;
    
    public static void install() throws ISOException{
        new ServerApplet ();
    }
    
    public static void install(byte[] bArray, short bOffset, byte bLength) throws ISOException{
        new ServerApplet ();
    }
    
    public ServerApplet(){
        
        miles = (short) 0;
        
        /* Consumes Transient memory of CLEAR_ON_DESELECT type */
        m_ramArray_Deselect = JCSystem.makeTransientByteArray((short) 260, JCSystem.CLEAR_ON_DESELECT);
        
        /*  Consumes Transient memory of CLEAR_ON_RESET type */
        m_ramArray_Reset = JCSystem.makeTransientByteArray((short) 520, JCSystem.CLEAR_ON_RESET);

        /* Consumes Persistent memory */ 
        m_dataArray = new byte[ARRAY_LENGTH];
        Util.arrayFillNonAtomic(m_dataArray, (short) 0, ARRAY_LENGTH, (byte) 0);
        
        register();
    } 
    
    public boolean select(){
      return true;
    }

    public void deselect(){
        return;
    }
    
    /* Methods required for implementation of Multiselectable */
    public boolean select(boolean bln) {
        return true;
    }

    public void deselect(boolean bln) {
        
    }
    
    /*
    public Shareable getShareableInterfaceObject(AID client, byte param){
        if(client.equals(BobAID, (short)0, (byte) BobAID.length) == false)
            return null;
        return this;
    }*/
    
    public void init() {
        // TODO start asynchronous download of heavy resources
    }

    public void process(APDU apdu) throws ISOException {
        byte[] apduBuffer = apdu.getBuffer();

        if (selectingApplet())
            return;

        if (apduBuffer[ISO7816.OFFSET_CLA] == CLA_SIMPLEAPPLET) {
            switch (apduBuffer[ISO7816.OFFSET_INS] )
            {
                case INS_USERINPUT1: 
                    foo(apdu); 
                    break;
                default :
                    ISOException.throwIt( ISO7816.SW_INS_NOT_SUPPORTED ) ;
                break ;

            }
        }
        else ISOException.throwIt( ISO7816.SW_CLA_NOT_SUPPORTED);
    }
   
    public static void grantcredit(short amount){
        
        miles = (short)(miles + amount);
    }
    
    public void foo(APDU apdu){
                      
        byte[]    apdubuf = apdu.getBuffer();
        
        byte b1 = apdubuf[ISO7816.OFFSET_P1];
        byte b2 = apdubuf[ISO7816.OFFSET_P2];
        
        short max = Util.makeShort(b1, b2);
        short amount = (short)(b1 + b2);
        
        //grantcredit(amount);
        
        apdu.setOutgoing();

        apdu.setOutgoingLength( (short) 6 );

        
        Util.setShort(apdubuf,(short)(0), (short)(b1));
        Util.setShort(apdubuf,(short)(2), max);
        Util.setShort(apdubuf,(short)(4), amount);
        apdu.sendBytes((short)0 , (short) 6);

    }
    
}