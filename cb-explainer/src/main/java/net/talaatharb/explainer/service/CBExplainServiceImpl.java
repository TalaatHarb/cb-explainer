package net.talaatharb.explainer.service;

import org.springframework.data.couchbase.core.CouchbaseTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class CBExplainServiceImpl implements CBExplainService {

	@Override
	public String explain(CouchbaseTemplate template, String query) {
		log.info("Explaining...");
		return null;
	}

}
