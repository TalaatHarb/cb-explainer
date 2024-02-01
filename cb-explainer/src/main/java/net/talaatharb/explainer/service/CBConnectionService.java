package net.talaatharb.explainer.service;

import org.springframework.data.couchbase.core.CouchbaseTemplate;

public interface CBConnectionService {

	CouchbaseTemplate connect();

}
