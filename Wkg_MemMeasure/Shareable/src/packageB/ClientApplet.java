/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/***************************
 * Package : 01 02 03 04 02
 * 
 * Applet  : 01 02 03 04 02 02 
 * 
 **************************/

package packageB;

import packageA.ServerApplet;
import packageA.MSI;
import javacard.framework.AID;
import javacard.framework.APDU;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.JCSystem;
import javacard.framework.Util;

/**
 *
 * @author swatch
 */

public class ClientApplet {
    
    final static byte CLA_SIMPLEAPPLET               = (byte) 0xB0;
    final static byte INS_USERINPUT1                 = (byte) 0x71;
    
    byte[] AliceAID = {(byte)0x01, (byte)0x02, (byte)0x03, (byte)0x04, (byte)0x01, (byte)0x01};
    
    byte[] BobAID = {(byte)0x01, (byte)0x02, (byte)0x03, (byte)0x04, (byte)0x02, (byte)0x02};
    
    private static short creditMiles;

    /*
    protected ClientApplet(byte[] buffer, short offset, byte length){
        this.register();
    }
    
    public static void install(byte[] bArray, short bOffset, byte bLength) throws ISOException
    {
        new ClientApplet(bArray, bOffset, bLength);
    }*/
    
    /*public void process(APDU apdu) throws ISOException {
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
    }*/

    public void foo(APDU apdu){
                      
        byte[]    apdubuf = apdu.getBuffer();
        
        byte b1 = apdubuf[ISO7816.OFFSET_P1];
        byte b2 = apdubuf[ISO7816.OFFSET_P2];
        
        short max = Util.makeShort(b1, b2);
        short add = (short)(b1 + b2);
        
        apdu.setOutgoing();

        apdu.setOutgoingLength( (short) 6 );

        
        Util.setShort(apdubuf,(short)(0), (short)(b1));
        Util.setShort(apdubuf,(short)(2), creditMiles);
        Util.setShort(apdubuf,(short)(4), add);
        apdu.sendBytes((short)0 , (short) 6);
    }

    public void init() {
    }

}
