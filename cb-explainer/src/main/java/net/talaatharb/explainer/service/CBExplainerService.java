package net.talaatharb.explainer.service;

import org.springframework.data.couchbase.core.CouchbaseTemplate;

public interface CBExplainerService {

	String explain(CouchbaseTemplate template, String query);

}
