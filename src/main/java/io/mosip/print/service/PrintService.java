package io.mosip.print.service;

public interface PrintService<T> {
    
	/**
	 * Gets the documents.
	 *
	 * @param type
	 *            the type
	 * @param idValue
	 *            the id value
	 * @param cardType
	 *            the card type
	 * @param isPasswordProtected
	 *            the is password protected
	 * @return the documents
	 */
	public T getDocuments(String credentialSubject, String requestId, String sign, String cardType,
			boolean isPasswordProtected);

	// Map<String, byte[]> getDocuments(String credentialSubject, String sign,
	// String cardType,
	// boolean isPasswordProtected);
	
}