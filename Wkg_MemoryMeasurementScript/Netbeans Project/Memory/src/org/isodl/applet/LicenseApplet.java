/*
 * DrivingLicenseApplet - A reference implementation of the ISO18013 standards.
 * Based on the passport applet code developed by the JMRTD team, see
 * http://jmrtd.org
 *
 * Copyright (C) 2006  SoS group, Radboud University
 * Copyright (C) 2009  Wojciech Mostowski, Radboud University
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *
 */

package org.isodl.applet;

import javacard.framework.APDU;
import javacard.framework.Applet;
import javacard.framework.CardRuntimeException;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.JCSystem;
import javacard.framework.Util;
import javacard.security.KeyBuilder;
import javacard.security.RandomData;
import javacard.security.Signature;
import javacardx.crypto.Cipher;

/**
 * License Applet - the implementation of the ISO18013 standard. The AID of the
 * applet should be A0000002480200.
 * 
 * @author ceesb (ceeesb@gmail.com)
 * @author martijno (martijn.oostdijk@gmail.com)
 * @author Wojciech Mostowski <woj@cs.ru.nl>
 * 
 */
public class LicenseApplet extends Applet implements ISO7816 {
	final static byte INS_MEMORYMEASURE = (byte) 0x80;
	final static byte CLA_SIMPLEAPPLET = (byte) 0xB0;

	private short memoryMeasureLength = (short)11;
	private short j = (short)0;
	public MemoryMeasurement mm[] = new MemoryMeasurement[memoryMeasureLength];
	short tempArrayLength1 = (short)0x7fff;
	short tempArrayLength2 = (short)0x3fff;

    static byte volatileState[];

    static byte persistentState;

    /* values for volatile state */
    static final byte CHALLENGED = 1;

    static final byte MUTUAL_AUTHENTICATED = 2;

    static final byte FILE_SELECTED = 4;

    static final byte ACTIVE_AUTHENTICATED = 8;

    static final byte CHIP_AUTHENTICATED = 0x10;

    static final byte TERMINAL_AUTHENTICATED = 0x20;

    /* values for persistent state */
    static final byte HAS_MUTUALAUTHENTICATION_KEYS = 1;

    static final byte HAS_EXPONENT = 2;

    static final byte LOCKED = 4;

    static final byte HAS_MODULUS = 8;

    static final byte HAS_EC_KEY = 0x10;

    static final byte HAS_CVCERTIFICATE = 0x20;

    static final byte HAS_SICID = 0x40;

    static final byte CHAIN_CLA = 0x10;

    /* for authentication */
    static final byte INS_EXTERNAL_AUTHENTICATE = (byte) 0x82;

    static final byte INS_GET_CHALLENGE = (byte) 0x84;

    static final byte CLA_PROTECTED_APDU = 0x0c;
    static final byte CLA_PLAIN_APDU = 0x00;

    static final byte INS_INTERNAL_AUTHENTICATE = (byte) 0x88;

    /* for EAP */
    static final byte INS_PSO = (byte) 0x2A;

    static final byte INS_MSE = (byte) 0x22;

    static final byte P2_VERIFYCERT = (byte) 0xBE;

    static final byte P1_SETFORCOMPUTATION = (byte) 0x41;

    static final byte P1_SETFORVERIFICATION = (byte) 0x81;

    static final byte P2_KAT = (byte) 0xA6;

    static final byte P2_DST = (byte) 0xB6;

    static final byte P2_AT = (byte) 0xA4;

    /* for reading */
    static final byte INS_SELECT_FILE = (byte) 0xA4;

    static final byte INS_READ_BINARY = (byte) 0xB0;

    /* for writing */
    static final byte INS_UPDATE_BINARY = (byte) 0xd6;

    static final byte INS_CREATE_FILE = (byte) 0xe0;

    static final byte INS_PUT_DATA = (byte) 0xda;

    static final short KEY_LENGTH = 16;

    static final short KEYMATERIAL_LENGTH = 16;

    static final short RND_LENGTH = 32;

    static final short MAC_LENGTH = 8;

    private static final byte PRIVMODULUS_TAG = 0x60;

    private static final byte PRIVEXPONENT_TAG = 0x61;

    private static final byte KEYSEED_TAG = 0x62;

    private static final byte ECPRIVATEKEY_TAG = 0x63;

    private static final byte CVCERTIFICATE_TAG = 0x64;

    private static final byte SICID_TAG = 0x65;

    /* status words */
    private static final short SW_OK = (short) 0x9000;

    private static final short SW_REFERENCE_DATA_NOT_FOUND = (short) 0x6A88;

    static final short SW_INTERNAL_ERROR = (short) 0x6d66;

    static final short SW_SM_DO_MISSING = (short) 0x6987;
    
    static final short SW_SM_DO_INCORRECT = (short) 0x6988;

    private static final byte MASK_SFI = (byte)0x80; 

    private byte[] rnd;

    private short rndLength;

    private byte[] ssc;

    private byte[] sicId;

    private FileSystem fileSystem;

    private RandomData randomData;

    static CVCertificate certificate;

    private short selectedFile;

    private LicenseCrypto crypto;

    private byte[] lastINS;

    private short[] chainingOffset;

    private byte[] chainingTmp;

    // This is as long we suspect a card verifiable certifcate could be
    private static final short CHAINING_BUFFER_LENGTH = 400;

    KeyStore keyStore;

    /**
     * Creates a new driving license applet.
     */
    public LicenseApplet() {
	byte[] tempByteArray1 = new byte[tempArrayLength1];
	byte[] tempByteArray2 = new byte[tempArrayLength2];

	for(short i=0; i<memoryMeasureLength; i++){
		mm[i] = new MemoryMeasurement();
	}

mm[j].startMeasurement(); fileSystem = new FileSystem(); mm[j].endMeasurement(); j++;

mm[j].startMeasurement(); randomData = RandomData.getInstance(RandomData.ALG_PSEUDO_RANDOM); mm[j].endMeasurement(); j++;

mm[j].startMeasurement(); certificate = new CVCertificate(); mm[j].endMeasurement(); j++;

mm[j].startMeasurement(); keyStore = new KeyStore(); mm[j].endMeasurement(); j++;
mm[j].startMeasurement(); crypto = new LicenseCrypto(keyStore); mm[j].endMeasurement(); j++;

mm[j].startMeasurement(); rnd = JCSystem.makeTransientByteArray(RND_LENGTH, JCSystem.CLEAR_ON_RESET); mm[j].endMeasurement(); j++;
mm[j].startMeasurement(); ssc = JCSystem.makeTransientByteArray((byte) 8, JCSystem.CLEAR_ON_RESET); mm[j].endMeasurement(); j++;
mm[j].startMeasurement(); volatileState = JCSystem.makeTransientByteArray((byte) 1,JCSystem.CLEAR_ON_RESET); mm[j].endMeasurement(); j++;
mm[j].startMeasurement(); lastINS = JCSystem.makeTransientByteArray((short) 1,JCSystem.CLEAR_ON_DESELECT); mm[j].endMeasurement(); j++;
mm[j].startMeasurement(); chainingOffset = JCSystem.makeTransientShortArray((short) 1, JCSystem.CLEAR_ON_DESELECT); mm[j].endMeasurement(); j++;
mm[j].startMeasurement(); chainingTmp = JCSystem.makeTransientByteArray(CHAINING_BUFFER_LENGTH, JCSystem.CLEAR_ON_DESELECT); mm[j].endMeasurement(); j++;
    }

    /**
     * Installs an instance of the applet.
     * 
     * @param buffer
     * @param offset
     * @param length
     * @see javacard.framework.Applet#install(byte[], byte, byte)
     */
    public static void install(byte[] buffer, short offset, byte length) {
        (new LicenseApplet()).register();
    }

    private static boolean needLe(byte ins) {
        if(ins == INS_READ_BINARY) {
            return true;
        }
        return false;
    }
    
    /**
     * Processes incoming APDUs.
     * 
     * @param apdu
     * @see javacard.framework.Applet#process(javacard.framework.APDU)
     */
	public void process(APDU apdu) throws ISOException {
		byte[] apduBuffer = apdu.getBuffer();
		if (selectingApplet())
			return;
		if (apduBuffer[ISO7816.OFFSET_CLA] == CLA_SIMPLEAPPLET) {
    			switch (apduBuffer[ISO7816.OFFSET_INS] ){
				case INS_MEMORYMEASURE: memoryMeasure(apdu); break;
		        	default : ISOException.throwIt( ISO7816.SW_INS_NOT_SUPPORTED ) ; break ;
			}
		}
		else ISOException.throwIt( ISO7816.SW_CLA_NOT_SUPPORTED);
	}

	public void memoryMeasure(APDU apdu){
		byte[]    apdubuf = apdu.getBuffer();
		apdu.setOutgoing();
		apdu.setOutgoingLength((short) (6 * memoryMeasureLength));
		short index = (short)0;
		for(short i=0; i<memoryMeasureLength; i++){
			Util.setShort(apdubuf,(index), (mm[i].getPersistentConsumption()));
			Util.setShort(apdubuf,(short)(index+2), (mm[i].getDeselectConsumption()));
			Util.setShort(apdubuf,(short)(index+4), (mm[i].getResetConsumption()));
			index = (short)(index + 6);
		}
		apdu.sendBytes((short)0 , (short) (6 * memoryMeasureLength));
	}
}
