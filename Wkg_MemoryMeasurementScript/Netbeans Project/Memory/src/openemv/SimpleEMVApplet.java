/* 
 * Copyright (C) 2011  Digital Security group, Radboud University
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
 */

package openemv;

import javacard.framework.APDU;
import javacard.framework.Applet;
import javacard.framework.ISOException;
import javacard.framework.JCSystem;
import javacard.framework.OwnerPIN;
import javacard.framework.Util;
import javacard.security.RandomData;
 

/* A very basic EMV applet supporting only SDA and plaintext offline PIN.
 * This applet does not offer personalisation support - everything is hard-coded.
 * 
 * The code is optimised for readability, and not for performance or memory use.
 * 
 * This class does the central processing of APDUs. Handling of all crypto-related
 * stuff is outsourced to EMVCrypro, handling of the static card data to EMVStaticData,
 * and handling of the EMV protocol and session state to EMVProtocolState.
 *
 * @author joeri (joeri@cs.ru.nl)
 * @author erikpoll (erikpoll@cs.ru.nl)
 *
 */
public class SimpleEMVApplet extends Applet implements EMVConstants {

	final OwnerPIN pin;
	final RandomData randomData;
	final EMVCrypto theCrypto;
	final EMVProtocolState protocolState;
	final EMVStaticData staticData;
	
	/* Transient byte array for constructing APDU responses. 
	 * We could have used the APDU buffer for this, but then we have to be careful not to 
	 * overwrite any info in the instruction APDU that we still need.
	 */
	private final byte[] response;
	

	private SimpleEMVApplet() {

 /* Persistent : 8 bytes,  Transient(Deselect) : 256 bytes,  Transient(Reset) : 256 bytes */
		response = JCSystem.makeTransientByteArray((short)256, JCSystem.CLEAR_ON_DESELECT);


 /* Persistent : 44 bytes,  Transient(Deselect) : 1 bytes,  Transient(Reset) : 1 bytes */
		pin = new OwnerPIN((byte) 3, (byte) 2);

 /* Persistent : 8 bytes,  Transient(Deselect) : 0 bytes,  Transient(Reset) : 0 bytes */
		pin.update(new byte[] { (byte) 0x12, (byte) 0x34 }, (short) 0, (byte) 2);

 /* Persistent : 12 bytes,  Transient(Deselect) : 0 bytes,  Transient(Reset) : 0 bytes */
		randomData = RandomData.getInstance(RandomData.ALG_PSEUDO_RANDOM);
		

 /* Persistent : 32 bytes,  Transient(Deselect) : 5 bytes,  Transient(Reset) : 5 bytes */
		protocolState = new EMVProtocolState(this);

 /* Persistent : 208 bytes,  Transient(Deselect) : 0 bytes,  Transient(Reset) : 0 bytes */
		staticData = new EMVStaticData();

 /* Persistent : 208 bytes,  Transient(Deselect) : 317 bytes,  Transient(Reset) : 317 bytes */
		theCrypto = new EMVCrypto(this);
	} 

	/**
	 * Installs an instance of the applet.
	 * 
	 * @see javacard.framework.Applet#install(byte[], byte, byte)
	 */
	public static void install(byte[] buffer, short offset, byte length) {
		(new SimpleEMVApplet()).register();
	}

	/**
	 * Processes incoming APDUs.
	 * 
	 * @see javacard.framework.Applet#process(javacard.framework.APDU)
	 */
	public void process(APDU apdu) {
		byte[] apduBuffer = apdu.getBuffer();
		byte cla = apduBuffer[OFFSET_CLA];
		byte ins = apduBuffer[OFFSET_INS];

		if (selectingApplet()) {
			protocolState.startNewSession();
			
			apdu.setOutgoing();
			apdu.setOutgoingLength(staticData.getFCILength());
			apdu.sendBytesLong(staticData.getFCI(), (short)0, staticData.getFCILength());
			return;
		}

		switch (ins) {

		case INS_EXTERNAL_AUTHENTICATE: // 0x82
			break;

		case INS_GET_CHALLENGE: // 0x84
			getChallenge(apdu, apduBuffer);
			break;

		case INS_INTERNAL_AUTHENTICATE:
			break;

		case INS_READ_RECORD: // 0xB2
			readRecord(apdu, apduBuffer);
			break;

		case INS_GET_PROCESSING_OPTIONS: // 0xA8
			getProcessingOptions(apdu, apduBuffer);
			break;

		case INS_GET_DATA: // 0xCA
			getData(apdu, apduBuffer);
			break;

		case INS_VERIFY: // 0x20
			verifyPIN(apdu, apduBuffer);
			break;

		case INS_GENERATE_AC: // 0xAE
			short len = (short) (apduBuffer[OFFSET_LC] & 0xFF);
			if (len != apdu.setIncomingAndReceive()) {
				ISOException.throwIt(SW_WRONG_LENGTH);
			}
			if ((apduBuffer[OFFSET_P1] & 0x10) == 0x10) {
				ISOException.throwIt(SW_WRONG_P1P2);
			}
			if (protocolState.getFirstACGenerated() == NONE) {
				generateFirstAC(apdu, apduBuffer);
			} else if (protocolState.getSecondACGenerated() == NONE) {
				generateSecondAC(apdu, apduBuffer);
			} else
				ISOException.throwIt(SW_INS_NOT_SUPPORTED);
			break;

		case INS_APPLICATION_BLOCK:
		case INS_APPLICATION_UNBLOCK:
		case INS_CARD_BLOCK:
		case INS_PIN_CHANGE_UNBLOCK:
		default:
			ISOException.throwIt(SW_INS_NOT_SUPPORTED);
			break;
		}
	}
 
	/*
	 * The VERIFY command checks the pin. This implementation only supports
	 * transaction_data PIN.
	 */	
	private void verifyPIN(APDU apdu, byte[] apduBuffer) {
		if (apduBuffer[OFFSET_P2] != (byte) (0x80)) {
			ISOException.throwIt(SW_WRONG_P1P2); // we only support transaction_data PIN
		}
		if (pin.getTriesRemaining() == 0) {
			ISOException.throwIt((short) 0x6983); // PIN blocked
			return;
		}

		/* EP: For the code below to be correct, digits in the PIN object need
		 * to be coded in the same way as in the APDU, ie. using 4 bit words.
		 */

		if (pin.check(apduBuffer, (short) (OFFSET_CDATA + 1), (byte) 2)) {
			protocolState.setCVMPerformed(PLAINTEXT_PIN);
			apdu.setOutgoingAndSend((short) 0, (short) 0); // return 9000
		} else {
			ISOException.throwIt((short) ((short) (0x63C0) + (short) pin
					.getTriesRemaining()));
		}
	}

	/*
	 * The GET CHALLENGE command generates an 8 byte unpredictable number.
	 */
	private void getChallenge(APDU apdu, byte[] apduBuffer) {
		randomData.generateData(apduBuffer, (short) 0, (short) 8);
		apdu.setOutgoingAndSend((short) 0, (short) 8);
	}

	/*
	 * The GET DATA command is used to retrieve a primitive data object not
	 * encapsulated in a record within the current application.
	 * 
	 * The usage of GET DATA in this implementation is limited to the ATC,
	 * the PIN Try Counter, and the last online ATC.
	 */
	private void getData(APDU apdu, byte[] apduBuffer) {
		/*
		 * buffer[OFFSET_P1..OFFSET_P2] should contains of the following tags
		 *  9F36 - ATC 
		 *  9F17 - PIN Try Counter 
		 *  9F13 - Last online ATC 
		 *  9F4F - Log Format
		 */
		if (apduBuffer[OFFSET_P1] == (byte) 0x9F) {
			apduBuffer[0] = (byte) 0x9F;
			apduBuffer[1] = apduBuffer[OFFSET_P2];
			switch (apduBuffer[OFFSET_P2]) {
			case 0x36: // ATC
				apduBuffer[OFFSET_P2 + 1] = (byte) 0x02; // length 2 bytes
				Util.setShort(apduBuffer, (short) (OFFSET_P2 + 2), protocolState.getATC()); // value
				apdu.setOutgoingAndSend(OFFSET_P1, (short) 5); 
				break;

			case 0x17: // PIN Try Counter
				apduBuffer[OFFSET_P2 + 1] = (byte) 0x01; // length 1 byte
				apduBuffer[OFFSET_P2 + 2] = pin.getTriesRemaining(); // value
				apdu.setOutgoingAndSend(OFFSET_P1, (short) 4); 
				break;

			case 0x13: // Last online ATC
				apduBuffer[OFFSET_P2 + 1] = (byte) 0x02; // length 2 bytes
				Util.setShort(apduBuffer, (short) (OFFSET_P2 + 2), protocolState.getLastOnlineATC()); // value
				apdu.setOutgoingAndSend(OFFSET_P1, (short) 5);  
				break;
			case 0x4F: // Log Format - not supported yet
			default:
				ISOException.throwIt(SW_WRONG_P1P2);
				break;
			}
		}
	}

	private void readRecord(APDU apdu, byte[] apduBuffer) {
		staticData.readRecord(apduBuffer, response);
		
		apdu.setOutgoing();
		apdu.setOutgoingLength((short)(response[1]+2));
		apdu.sendBytesLong(response, (short)0, (short)(response[1]+2));
	}

	private void getProcessingOptions(APDU apdu, byte[] apduBuffer) {
		
		response[0] = (byte) 0x80; // Tag
		response[1] = (byte) 0x06; // Length
		
		Util.setShort(response, (short)2, staticData.getAIP()); 
		
		Util.arrayCopyNonAtomic(staticData.getAFL(), (short)0, response, (short)4, (short)4);
		
		apdu.setOutgoing();
		apdu.setOutgoingLength((short)8);
		apdu.sendBytesLong(response, (short)0, (short)8);		
	}

	public void generateFirstAC(APDU apdu, byte[] apduBuffer) {
		byte cid = (byte) (apduBuffer[OFFSET_P1] & 0xC0);
		if (cid == RFU_CODE || cid == AAC_CODE) {
			ISOException.throwIt(SW_WRONG_P1P2);
		}
		
		theCrypto.generateFirstACReponse(cid, apduBuffer, staticData.getCDOL1DataLength(), null, (short)0, response, (short)0);
		protocolState.setFirstACGenerated(cid);
		
		apdu.setOutgoing();
		apdu.setOutgoingLength((short)(response[1]+2));
		apdu.sendBytesLong(response, (short)0, (short)(response[1]+2));		
	}
 
	public void generateSecondAC(APDU apdu, byte[] apduBuffer) {
		byte cid = (byte) (apduBuffer[OFFSET_P1] & 0xC0);
		if (cid == RFU_CODE || cid == ARQC_CODE) {
			ISOException.throwIt(SW_WRONG_P1P2);
		}	

		theCrypto.generateSecondACReponse(cid, apduBuffer, staticData.getCDOL2DataLength(), null, (short)0, response, (short)0);
		protocolState.setSecondACGenerated(cid);
		
		apdu.setOutgoing();
		apdu.setOutgoingLength((short)(response[1]+2));
		apdu.sendBytesLong(response, (short)0, (short)(response[1]+2));		
	}

}
