package net.talaatharb.explainer.service;

import org.springframework.data.couchbase.core.CouchbaseTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class CBConnectionServiceImpl implements CBConnectionService {

	@Override
	public CouchbaseTemplate connect() {
		log.info("Connecting....");
		return null;
	}

}
