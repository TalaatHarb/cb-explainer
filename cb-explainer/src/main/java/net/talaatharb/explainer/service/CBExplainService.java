package net.talaatharb.explainer.service;

import org.springframework.data.couchbase.core.CouchbaseTemplate;

public interface CBExplainService {

	String explain(CouchbaseTemplate template, String query);

}
