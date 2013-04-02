package org.microsun.core.security;

import java.security.GeneralSecurityException;
import java.security.cert.Certificate;
import java.util.List;


public interface SiteKeyService extends ISiteKeyDistributor {
	Certificate getCertificate(String alias) throws Exception;
	Certificate[] getCertificateChain(String alias) throws Exception;
	void setCertificate(String alias,Certificate cert) throws Exception;
	String exportSiteCertificate() throws Exception;
	void importSiteIdentity(String encString,String storepass,String keypass) throws Exception;
	void importSiteAliasIdentity(String siteAlias,String encString,String storepass,String keypass) throws Exception;
	void importEnterpriseCACert(String encString) throws Exception;
	void importCertificate(String alias, String encString) throws Exception;
	void importStationCertificate(String stationId) throws Exception;
	String generateSignatureForMessage(String message) throws Exception;
	String generateSignatureForMessage(String message,String charsetName) throws Exception;
	String generateSignatureForMessage(byte[] message) throws Exception;
	boolean verifySignatureForMessageFromSite(String message,String siteName,String charsetName,String sign_code) throws Exception;
	boolean verifySignatureForMessageFromSite(String message,String siteName,String sign_code) throws Exception;
	boolean verifySignatureForMessageFromSite(byte[] message,String siteName,String sign_code) throws Exception;
	byte[] encrypt(byte[] data) throws GeneralSecurityException;
	byte[] decrypt(byte[] data) throws GeneralSecurityException;
	Certificate getSiteCertificate() throws GeneralSecurityException;
	List<String> getAllAlias() throws Exception;
	public void validateCertificate(Certificate cert) throws GeneralSecurityException;
}

