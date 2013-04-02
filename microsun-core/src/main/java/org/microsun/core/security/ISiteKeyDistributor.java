package org.microsun.core.security;

import java.security.cert.Certificate;

public interface ISiteKeyDistributor {
	Certificate getSiteCertificate() throws Exception;
	String getSiteName() throws Exception;
	String getStationName() throws Exception;
	String getStationType() throws Exception;
	String getDomainName() throws Exception;
}
