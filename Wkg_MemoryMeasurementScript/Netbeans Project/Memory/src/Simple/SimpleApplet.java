
/*******************************
 * Package : 01 02 03 04 01
 * 
 * Applet  : 01 02 03 04 01 03 
 * 
 ******************************/

package Simple;

import javacard.framework.*;
import javacard.security.*;
import javacardx.crypto.*;

public class SimpleApplet extends Applet{
    
    final static byte CLA_SIMPLEAPPLET                = (byte) 0xB0;

    final static byte INS_ENCRYPT                    = (byte) 0x50;
    final static byte INS_DECRYPT                    = (byte) 0x51;
    final static byte INS_SETKEY                     = (byte) 0x52;
    final static byte INS_HASH                       = (byte) 0x53;
    final static byte INS_RANDOM                     = (byte) 0x54;
    final static byte INS_VERIFYPIN                  = (byte) 0x55;
    final static byte INS_SETPIN                     = (byte) 0x56;
    final static byte INS_RETURNDATA                 = (byte) 0x57;
    final static byte INS_SIGNDATA                   = (byte) 0x58;
    final static byte INS_GETAPDUBUFF                = (byte) 0x59;

    final static short ARRAY_LENGTH                   = (short) 0xff;
    final static byte  AES_BLOCK_LENGTH               = (short) 0x16;

    final static short SW_BAD_TEST_DATA_LEN          = (short) 0x6680;
    final static short SW_KEY_LENGTH_BAD             = (short) 0x6715;
    final static short SW_CIPHER_DATA_LENGTH_BAD     = (short) 0x6710;
    final static short SW_OBJECT_NOT_AVAILABLE       = (short) 0x6711;
    final static short SW_BAD_PIN                    = (short) 0x6900;

    private   AESKey         m_aesKey = null;
    private   Cipher         m_encryptCipher = null;
    private   Cipher         m_decryptCipher = null;
    private   RandomData     m_secureRandom = null;
    private   MessageDigest  m_hash = null;
    private   OwnerPIN       m_pin = null;
    private   Signature      m_sign = null;
    private   KeyPair        m_keyPair = null;
    private   Key            m_privateKey = null;
    private   Key            m_publicKey = null;

    private   short          m_apduLogOffset = (short) 0;
    private   byte        m_ramArray[] = null;
    private   byte       m_dataArray[] = null;

    /**
     * LabakApplet default constructor
     * Only this class's install method should create the applet object.
     * @param buffer
     * @param offset
     * @param length
     */
    protected SimpleApplet()
    {

 /* Persistent : 260 bytes,  Transient(Deselect) : 0 bytes,  Transient(Reset) : 0 bytes */
            m_dataArray = new byte[ARRAY_LENGTH];
            Util.arrayFillNonAtomic(m_dataArray, (short) 0, ARRAY_LENGTH, (byte) 0);


 /* Persistent : 60 bytes,  Transient(Deselect) : 0 bytes,  Transient(Reset) : 0 bytes */
            m_aesKey = (AESKey) KeyBuilder.buildKey(KeyBuilder.TYPE_AES, KeyBuilder.LENGTH_AES_256, false);

 /* Persistent : 24 bytes,  Transient(Deselect) : 34 bytes,  Transient(Reset) : 34 bytes */
            m_encryptCipher = Cipher.getInstance(Cipher.ALG_AES_BLOCK_128_CBC_NOPAD, false);

 /* Persistent : 24 bytes,  Transient(Deselect) : 34 bytes,  Transient(Reset) : 34 bytes */
            m_decryptCipher = Cipher.getInstance(Cipher.ALG_AES_BLOCK_128_CBC_NOPAD, false);

             m_secureRandom = RandomData.getInstance(RandomData.ALG_SECURE_RANDOM);


 /* Persistent : 8 bytes,  Transient(Deselect) : 260 bytes,  Transient(Reset) : 260 bytes */
            m_ramArray = JCSystem.makeTransientByteArray((short) 260, JCSystem.CLEAR_ON_DESELECT);

            m_aesKey.setKey(m_dataArray, (short) 0);

            m_encryptCipher.init(m_aesKey, Cipher.MODE_ENCRYPT);
            m_decryptCipher.init(m_aesKey, Cipher.MODE_DECRYPT);


 /* Persistent : 44 bytes,  Transient(Deselect) : 1 bytes,  Transient(Reset) : 1 bytes */
            m_pin = new OwnerPIN((byte) 5, (byte) 4);
            m_pin.update(m_dataArray, (byte) 0, (byte) 4);


 /* Persistent : 680 bytes,  Transient(Deselect) : 0 bytes,  Transient(Reset) : 0 bytes */
            m_keyPair = new KeyPair(KeyPair.ALG_RSA_CRT, KeyBuilder.LENGTH_RSA_1024);
            
            try {

 /* Persistent : 20 bytes,  Transient(Deselect) : 95 bytes,  Transient(Reset) : 95 bytes */
                m_hash = MessageDigest.getInstance(MessageDigest.ALG_SHA, false);
            }
            catch (CryptoException e) {
            }

        register();
    }

    /**
     * Method installing the applet.
     * @param bArray the array constaining installation parameters
     * @param bOffset the starting offset in bArray
     * @param bLength the length in bytes of the data parameter in bArray
     */
    public static void install(byte[] bArray, short bOffset, byte bLength) throws ISOException
    {
        new SimpleApplet();
    }

    public boolean select(){
      return true;
    }

    public void deselect(){
        return;
    }
    
    public boolean select(boolean bln) {
        return true;
    }

    public void deselect(boolean bln) {
        
    }

    /**
     * Method processing an incoming APDU.
     * @see APDU
     * @param apdu the incoming APDU
     * @exception ISOException with the response bytes defined by ISO 7816-4
     */
    public void process(APDU apdu) throws ISOException
    {
        byte[] apduBuffer = apdu.getBuffer();

        if (selectingApplet())
            return;

        if (apduBuffer[ISO7816.OFFSET_CLA] == CLA_SIMPLEAPPLET) {
            switch ( apduBuffer[ISO7816.OFFSET_INS] )
            {
                case INS_SETKEY: SetKey(apdu); break;
                case INS_ENCRYPT: Encrypt(apdu); break;
                case INS_DECRYPT: Decrypt(apdu); break;
                case INS_HASH: Hash(apdu); break;
                case INS_RANDOM: Random(apdu); break;
                case INS_VERIFYPIN: VerifyPIN(apdu); break;
                case INS_SETPIN: SetPIN(apdu); break;
                case INS_RETURNDATA: ReturnData(apdu); break;
                case INS_SIGNDATA: Sign(apdu); break;
                case INS_GETAPDUBUFF: GetAPDUBuff(apdu); break;
                default :
                    ISOException.throwIt( ISO7816.SW_INS_NOT_SUPPORTED ) ;
                break ;

            }
        }
        else ISOException.throwIt( ISO7816.SW_CLA_NOT_SUPPORTED);
    }

    void SetKey(APDU apdu) {
      byte[]    apdubuf = apdu.getBuffer();
      short     dataLen = apdu.setIncomingAndReceive();

      if ((short) (dataLen * 8) != KeyBuilder.LENGTH_AES_256) ISOException.throwIt(SW_KEY_LENGTH_BAD);

      m_aesKey.setKey(apdubuf, ISO7816.OFFSET_CDATA);

      m_encryptCipher.init(m_aesKey, Cipher.MODE_ENCRYPT);
      m_decryptCipher.init(m_aesKey, Cipher.MODE_DECRYPT);
    }
    
     void Encrypt(APDU apdu) {
      byte[]    apdubuf = apdu.getBuffer();
      short     dataLen = apdu.setIncomingAndReceive();

      if ((dataLen % 16) != 0) ISOException.throwIt(SW_CIPHER_DATA_LENGTH_BAD);

      m_encryptCipher.doFinal(apdubuf, ISO7816.OFFSET_CDATA, dataLen, m_ramArray, (short) 0);

      Util.arrayCopyNonAtomic(m_ramArray, (short) 0, apdubuf, ISO7816.OFFSET_CDATA, dataLen);

      apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA, dataLen);
    }

    void Decrypt(APDU apdu) {
      byte[]    apdubuf = apdu.getBuffer();
      short     dataLen = apdu.setIncomingAndReceive();

      if ((dataLen % 16) != 0) ISOException.throwIt(SW_CIPHER_DATA_LENGTH_BAD);

      m_decryptCipher.doFinal(apdubuf, ISO7816.OFFSET_CDATA, dataLen, m_ramArray, (short) 0);

      Util.arrayCopyNonAtomic(m_ramArray, (short) 0, apdubuf, ISO7816.OFFSET_CDATA, dataLen);

      apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA, dataLen);
    }

     void Hash(APDU apdu) {
      byte[]    apdubuf = apdu.getBuffer();
      short     dataLen = apdu.setIncomingAndReceive();

      if (m_hash != null) {
          m_hash.doFinal(apdubuf, ISO7816.OFFSET_CDATA, dataLen, m_ramArray, (short) 0);
      }
      else ISOException.throwIt(SW_OBJECT_NOT_AVAILABLE);

      Util.arrayCopyNonAtomic(m_ramArray, (short) 0, apdubuf, ISO7816.OFFSET_CDATA, m_hash.getLength());

      apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA, m_hash.getLength());
    }

     void Random(APDU apdu) {
      byte[]    apdubuf = apdu.getBuffer();
      short     dataLen = apdu.setIncomingAndReceive();

      m_secureRandom.generateData(apdubuf, ISO7816.OFFSET_CDATA, apdubuf[ISO7816.OFFSET_P1]);

      apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA, apdubuf[ISO7816.OFFSET_P1]);
    }

     void VerifyPIN(APDU apdu) {
      byte[]    apdubuf = apdu.getBuffer();
      short     dataLen = apdu.setIncomingAndReceive();

      if (m_pin.check(apdubuf, ISO7816.OFFSET_CDATA, (byte) dataLen) == false)
      ISOException.throwIt(SW_BAD_PIN);
    }

     void SetPIN(APDU apdu) {
      byte[]    apdubuf = apdu.getBuffer();
      short     dataLen = apdu.setIncomingAndReceive();

      m_pin.update(apdubuf, ISO7816.OFFSET_CDATA, (byte) dataLen);
    }

     void ReturnData(APDU apdu) {
      byte[]    apdubuf = apdu.getBuffer();
      short     dataLen = apdu.setIncomingAndReceive();
      short     keyLength = 32;
      short     challengeLength = dataLen;
      byte[]    aesKey = JCSystem.makeTransientByteArray(keyLength, JCSystem.CLEAR_ON_RESET);
      byte[]    challengeBuff = new byte[challengeLength];
      
      m_aesKey.getKey(aesKey, (short) 0);
      
      
      
      Util.arrayCopyNonAtomic(apdubuf, ISO7816.OFFSET_CDATA, challengeBuff, (short) 0, challengeLength);
      
      /************************ Implementation of HMAC RFC2104 as specified in Wikipedia *******************/
      
      byte[]    keyXorOPad = new byte[challengeLength];
      byte[]    keyXorIPad = new byte[challengeLength];
      byte      OPad = (byte) 0x5c;
      byte      IPad = (byte) 0x36;
      
      for (short i = 0; i < challengeLength; i++){
          keyXorOPad[i] = (byte) (aesKey[i] ^ OPad);
          keyXorIPad[i] = (byte) (aesKey[i] ^ IPad);
      }
      
      short     concatLength;
      concatLength = (short) (2 * challengeLength);
      byte[]    IPadConcatChallenge = new byte[concatLength];
      
      Util.arrayCopyNonAtomic(keyXorIPad, (short) 0, IPadConcatChallenge, (short) 0, challengeLength);
      Util.arrayCopyNonAtomic(challengeBuff, (short) 0, IPadConcatChallenge, (short) challengeLength, challengeLength);
      
      byte[]    innerHash = new byte[(short) 20];
      
      if (m_hash != null) {
          m_hash.doFinal(IPadConcatChallenge, (short) 0, concatLength, innerHash, (short) 0);
      }
      else ISOException.throwIt(SW_OBJECT_NOT_AVAILABLE);
      
      byte[]    innerConcat = new byte[(short)(keyLength + 20)];
      
      Util.arrayCopyNonAtomic(keyXorOPad, (short) 0, innerConcat, (short) 0, challengeLength);
      Util.arrayCopyNonAtomic(innerHash, (short) 0, innerConcat, (short) challengeLength, (short) 20);
      
      byte[]    outerHash = new byte[(short) 20];
      
      if (m_hash != null) {
          m_hash.doFinal(innerConcat, (short) 0, (short) innerConcat.length, outerHash, (short) 0);
      }
      else ISOException.throwIt(SW_OBJECT_NOT_AVAILABLE);
      
      /****************** Implementation of HMAC based OTP RFC4226 as specified in Wikipedia **************/
      
      byte[]  hotp = new byte[(short) 4];
      hotp[0]      = (byte) (outerHash[3] & 0x7F);
      hotp[1]      = (byte) (outerHash[5] & 0xFF);
      hotp[2]      = (byte) (outerHash[8] & 0xFF);
      hotp[3]      = (byte) (outerHash[10] & 0xFF);

      Util.arrayCopyNonAtomic(hotp, (short) 0, apdubuf, ISO7816.OFFSET_CDATA, (short) 4);
      
      
      apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA, (short) 4);
    }

    void Sign(APDU apdu) {
     byte[]    apdubuf = apdu.getBuffer();
     short     dataLen = apdu.setIncomingAndReceive();
     short     signLen = 0;


     m_keyPair.genKeyPair();

     m_publicKey = m_keyPair.getPublic();
     m_privateKey = m_keyPair.getPrivate();


     m_sign.init(m_privateKey, Signature.MODE_SIGN);

     signLen = m_sign.sign(apdubuf, ISO7816.OFFSET_CDATA, (byte) dataLen, m_ramArray, (byte) 0);

     Util.arrayCopyNonAtomic(m_ramArray, (short) 0, apdubuf, ISO7816.OFFSET_CDATA, signLen);

     apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA, signLen);
   }

   void GetAPDUBuff(APDU apdu) {
    byte[]    apdubuf = apdu.getBuffer();

    Util.arrayCopyNonAtomic(m_dataArray, (short) 0, apdubuf, ISO7816.OFFSET_CDATA, m_apduLogOffset);
    short tempLength = m_apduLogOffset;
    m_apduLogOffset = 0;
    apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA, tempLength);
  }
}

