package net.talaatharb.explainer.service;

import java.io.IOException;
import java.util.Properties;

import org.springframework.data.couchbase.core.CouchbaseTemplate;

public interface CBConnectionService {

	String SCOPE = "scope";
	String BUCKET = "bucket";
	String PASS = "pass";
	String USER = "user";
	String CONNECTION = "connection";

	CouchbaseTemplate connect();

	void editConnectionDetails(Properties properties);

	Properties loadConnectionDetails() throws IOException;

}
