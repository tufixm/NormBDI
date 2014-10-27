package jadex.platform.service.security;

import jadex.commons.Base64;
import jadex.commons.SUtil;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.DSAKey;
import java.security.interfaces.RSAKey;
import java.util.Date;

import javax.crypto.interfaces.DHKey;
import javax.security.auth.x500.X500Principal;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.x509.X509V1CertificateGenerator;


/**
 * 
 */
public class SSecurity
{
	static
	{
		BouncyCastleProvider bc = new BouncyCastleProvider();
		java.security.Security.addProvider(bc);
	}
	
	/**
	 *  Get keystore from a given file.
	 */
	public static KeyStore getKeystore(String storepath, String storepass, String keypass, String alias)
	{
		try
		{
			KeyStore ks = KeyStore.getInstance("JKS");
			FileInputStream fis = null;
			boolean loaded = false;
			try
			{
				File f = new File(storepath);
				if(f.exists())
				{
					fis = new FileInputStream(storepath);
					ks.load(fis, storepass.toCharArray());
					loaded = true;
				}
			}
			catch(Exception e)
			{
			}
			finally
			{
				if(fis!=null)
					fis.close();
				if(!loaded || !ks.containsAlias(alias))
					initKeystore(ks, storepath, storepass, keypass, alias);
			}
			return ks;
		}
		catch(Exception e)
		{
//			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	/**
	 *  Get keystore from a given file.
	 */
	public static void saveKeystore(KeyStore keystore, String storepath, String storepass)
	{
		FileOutputStream fos = null;
		try
		{
			fos = new FileOutputStream(storepath);
			keystore.store(fos, storepass.toCharArray());
		}
		catch(Exception e)
		{
			throw new RuntimeException(e);
		}
		finally
		{
			if(fos!=null)
			{
				try
				{
					fos.close();
				}
				catch(Exception e)
				{
				}
			}
		}
	}
	
	/**
	 *  Init keystore with a self-signed certificate.
	 */
	public static void initKeystore(KeyStore ks, String storepath, String storepass, String keypass, String alias)
	{
		try
    	{
	    	ks.load(null, null); // Must be called. 
	    	
//	    	RSAKeyPairGenerator r = new RSAKeyPairGenerator();
//	    	r.init(new KeyGenerationParameters(new SecureRandom(), 1024));
//	    	AsymmetricCipherKeyPair keys = r.generateKeyPair();
	    	
	    	KeyPair keys = generateKeyPair("RSA", 1024);
		    Certificate c = generateCertificate("CN=CKS Self Signed Cert", keys, 1000, "MD5WithRSA");
		    
		    // Creates key entry (i.e. keypair with certificate)
		    ks.setKeyEntry(alias, keys.getPrivate(), keypass.toCharArray(),  
		    	new java.security.cert.Certificate[]{c});  
		    
		    saveKeystore(ks, storepath, storepass);
    	}
		catch(RuntimeException e)
		{
			throw e;
		}
		catch(Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
//	/**
//	 *  Generate a certificate.
//	 *  @param dn the X.509 Distinguished Name, eg "CN=Test, L=London, C=GB"
//	 *  @param pair
//	 *  @param days
//	 *  @param algorithm
//	 *  @throws GeneralSecurityException
//	 *  @throws IOException
//	 */
//	public static Certificate generateCertificate(String dn, KeyPair pair, int days, String algorithm) throws GeneralSecurityException, IOException
//	{
//		PrivateKey privkey = pair.getPrivate();
//		X509CertInfo info = new X509CertInfo();
//		Date from = new Date();
//		Date to = new Date(from.getTime() + days * 86400000l);
//		CertificateValidity interval = new CertificateValidity(from, to);
//		BigInteger sn = new BigInteger(64, new SecureRandom());
//		X500Name owner = new X500Name(dn);
// 
//		info.set(X509CertInfo.VALIDITY, interval);
//		info.set(X509CertInfo.SERIAL_NUMBER, new CertificateSerialNumber(sn));
//		info.set(X509CertInfo.SUBJECT, new CertificateSubjectName(owner));
//		info.set(X509CertInfo.ISSUER, new CertificateIssuerName(owner));
//		info.set(X509CertInfo.KEY, new CertificateX509Key(pair.getPublic()));
//		info.set(X509CertInfo.VERSION, new CertificateVersion(CertificateVersion.V3));
//		AlgorithmId algo = new AlgorithmId(AlgorithmId.md5WithRSAEncryption_oid);
//		info.set(X509CertInfo.ALGORITHM_ID, new CertificateAlgorithmId(algo));
// 
//		// Sign the cert to identify the algorithm that's used.
//		X509CertImpl cert = new X509CertImpl(info);
//		cert.sign(privkey, algorithm);
// 
//		// Update the algorith, and resign.
//		algo = (AlgorithmId)cert.get(X509CertImpl.SIG_ALG);
//		info.set(CertificateAlgorithmId.NAME + "." + CertificateAlgorithmId.ALGORITHM, algo);
//		cert = new X509CertImpl(info);
//		cert.sign(privkey, algorithm);
//		return cert;
//	}   
	
	/** 
	 * Create a self-signed X.509 Certificate
	 * @param dn the X.509 Distinguished Name, eg "CN=Test, L=London, C=GB"
	 * @param pair the KeyPair
	 * @param days how many days from now the Certificate is valid for
	 * @param algorithm the signing algorithm, eg "SHA1withRSA"
	 */ 
	public static Certificate generateCertificate(KeyPair pair, int days, String algorithm) 
		throws GeneralSecurityException, IOException
	{
		return generateCertificate(null, pair, days, algorithm);
	}
	
	/** 
	 * Create a self-signed X.509 Certificate
	 * @param dn the X.509 Distinguished Name, eg "CN=Test, L=London, C=GB"
	 * @param pair the KeyPair
	 * @param days how many days from now the Certificate is valid for
	 * @param algorithm the signing algorithm, eg "SHA1withRSA"
	 */ 
	public static Certificate generateCertificate(String dn, KeyPair pair, int days, String algorithm) 
		throws GeneralSecurityException, IOException
	{
		if(dn==null)
			dn = "CN=CKS Self Signed Cert";
		if(days<=0)
			days = 365;
		
		X509V1CertificateGenerator gen = new X509V1CertificateGenerator();
		X500Principal dnn = new X500Principal(dn); //"CN=Test CA Certificate"

		Date from = new Date();
		Date to = new Date(from.getTime() + days * 86400000l);
		BigInteger sn = new BigInteger(64, new SecureRandom());
		
		gen.setSerialNumber(sn);
		gen.setIssuerDN(dnn);
		gen.setNotBefore(from);
		gen.setNotAfter(to);
		gen.setSubjectDN(dnn);                       // note: same as issuer
		gen.setPublicKey(pair.getPublic());
		gen.setSignatureAlgorithm(algorithm);

		Certificate cert = gen.generate(pair.getPrivate());
		
		return cert;
	}  
	
	/** 
	 * Create a self-signed X.509 Certificate
	 * @param dn the X.509 Distinguished Name, eg "CN=Test, L=London, C=GB"
	 * @param pair the KeyPair
	 * @param days how many days from now the Certificate is valid for
	 * @param algorithm the signing algorithm, eg "SHA1withRSA"
	 */ 
	public static KeyPair generateKeyPair(String algorithm, int keysize) 
		throws GeneralSecurityException, IOException
	{
		KeyPairGenerator gen = KeyPairGenerator.getInstance(algorithm);  
 	    gen.initialize(keysize);  
 	    KeyPair keys = gen.generateKeyPair();
		return keys;
	}
	
	/**
     * 
     */
    public static byte[] signContent(PrivateKey key, Signature engine, byte[] content) 
    	throws InvalidKeyException, SignatureException 
    {
    	engine.initSign(key);
    	engine.update(content); // clone ?
    	byte[] sig = engine.sign();// clone() ?
    	return sig;
    }
	
	/**
	 *  
     */
    public static boolean verifyContent(PublicKey key, Signature engine, byte[] content, byte[] sig) 
    	throws InvalidKeyException, SignatureException 
    {
    	engine.initVerify(key);
    	engine.update(content); // clone() ?
    	return engine.verify(sig); // clone() ?
    }
    
    /**
	 * Get the digest of a message as a formatted String.
	 */
	public static String getHexMessageDigest(byte[] data, String type)
	{
		try
		{
			MessageDigest mdig = MessageDigest.getInstance(type);
			byte[] fp = mdig.digest(data);
			return SUtil.hex(fp, ":", 1);
		}
		catch(NoSuchAlgorithmException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	/**
	 *  Get the key length.
	 */
	public static int getKeyLength(Key key)
	{
		int ret = -1;
		
		if(key instanceof RSAKey)
		{
			ret = ((RSAKey)key).getModulus().bitLength();
		}
		else if(key instanceof DSAKey)
		{
			ret = ((DSAKey)key).getParams().getP().bitLength();
		}
		else if(key instanceof DHKey)
		{
			ret = ((DHKey)key).getParams().getP().bitLength();
		}
//		else if(key instanceof ECKey)
//		{
//		}

		return ret;
	}
	
	/**
	 *  Get the textual representation of a certificate.
	 */
	public static String getCertificateText(Certificate cert)
	{
		String ret = null;
		
		try
		{
			StringBuffer buf =  new StringBuffer("-----BEGIN CERTIFICATE-----").append(SUtil.LF);
			buf.append(new String(Base64.toCharArray(cert.getEncoded(), 64)));
			buf.append(SUtil.LF).append("-----END CERTIFICATE-----");
			ret = buf.toString();
		}
		catch(Exception e)
		{
			throw new RuntimeException(e);
		}
		
		return ret;
	}
	
	/**
	 *  Get the textual representation of a certificate.
	 */
	public static Certificate createCertificate(InputStream in)
	{
		Certificate ret = null;
		try 
		{
			CertificateFactory fac = CertificateFactory.getInstance("X.509");
			ret = fac.generateCertificate(in);
		}
		catch(Exception ex)
		{ 
		}
		finally
		{
			try
			{
				in.close();
			}
			catch(Exception exc)
			{
			}
		}
		return ret;
	}
	
	/**
	 *  Get the textual representation of a certificate.
	 */
	public static Certificate createCertificate(String text)
	{
		Certificate ret = null;
		
		try
		{
			CertificateFactory fac = CertificateFactory.getInstance("X.509");
			ByteArrayInputStream bas = new ByteArrayInputStream(text.getBytes());
			ret = fac.generateCertificate(bas);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return ret;
	}
	
	/**
	 *  Get the alogrithm name of a certificate.
	 */
	public static String getAlgorithm(Certificate cert)
	{
		String ret = "MD5WithRSA"; // todo: how to find out if not X509
		if(cert instanceof X509Certificate)
			ret = ((X509Certificate)cert).getSigAlgName();
		return ret;
	}
	
	/**
	 *  Main for testing.
	 */
	public static void main(String[] args)
	{
		KeyStore ks = getKeystore("c:\\temp\\keystore", "hans", "hans", "alias");
		System.out.println("ks: "+ks);
	}
}
