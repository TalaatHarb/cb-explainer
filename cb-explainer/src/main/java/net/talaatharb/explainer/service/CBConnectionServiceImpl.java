package net.talaatharb.explainer.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.springframework.data.couchbase.CouchbaseClientFactory;
import org.springframework.data.couchbase.SimpleCouchbaseClientFactory;
import org.springframework.data.couchbase.core.CouchbaseTemplate;
import org.springframework.data.couchbase.core.convert.CouchbaseConverter;
import org.springframework.data.couchbase.core.convert.MappingCouchbaseConverter;
import org.springframework.data.couchbase.core.mapping.CouchbaseMappingContext;

import com.couchbase.client.java.Cluster;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class CBConnectionServiceImpl implements CBConnectionService {

	private static final String CONNECTION_FILE = "./cb.properties";

	private CouchbaseTemplate couchbaseTemplate;

	@Override
	public CouchbaseTemplate connect() {
		log.info("Connecting....");

		try {
			final Properties properties = loadConnectionDetails();

			final String connectionString = properties.getProperty(CONNECTION);
			final String username = properties.getProperty(USER);
			final String password = properties.getProperty(PASS);
			final String bucketName = properties.getProperty(BUCKET);
			final String scope = properties.getProperty(SCOPE);

			// Connect to the cluster
			final Cluster cluster = Cluster.connect(connectionString, username, password);

			// Create a CouchbaseClientFactory
			final CouchbaseClientFactory clientFactory = new SimpleCouchbaseClientFactory(cluster, bucketName, scope);

			// Create the CouchbaseMappingContext and MappingCouchbaseConverter
			final CouchbaseMappingContext mappingContext = new CouchbaseMappingContext();
			final CouchbaseConverter converter = new MappingCouchbaseConverter(mappingContext);

			// Create the CouchbaseTemplate
			couchbaseTemplate = new CouchbaseTemplate(clientFactory, converter);

			log.info("Connection Successful...");

			return couchbaseTemplate;

		} catch (final IOException e) {
			log.error(e.getMessage());
			return null;
		}
	}

	@Override
	public Properties loadConnectionDetails() throws IOException {
		final Properties properties = new Properties();
		try (FileInputStream in = new FileInputStream(new File(CONNECTION_FILE))) {
			properties.load(in);
		} catch (final IOException e) {
			log.error(e.getMessage());
		}

		return properties;

	}

	@Override
	public void editConnectionDetails(final Properties properties) {
		try {
			final FileOutputStream fileOutputStream = new FileOutputStream(new File(CONNECTION_FILE));
			properties.store(fileOutputStream, "Couchbase connection properties");
		} catch (final IOException e) {
			log.error(e.getMessage());
		}

	}

}
