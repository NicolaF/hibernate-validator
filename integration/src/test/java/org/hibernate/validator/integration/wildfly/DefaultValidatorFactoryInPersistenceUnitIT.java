/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.integration.wildfly;

import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.validation.ValidatorFactory;

import org.apache.log4j.Logger;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.persistence20.PersistenceDescriptor;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests the integration of Hibernate Validator in Wildfly
 *
 * @author Hardy Ferentschik
 * @todo the test should execute an actual validation. It is not guaranteed that one can access the validator factory
 * under javax.persistence.validation.factory
 */
@RunWith(Arquillian.class)
public class DefaultValidatorFactoryInPersistenceUnitIT {
	private static final String WAR_FILE_NAME = DefaultValidatorFactoryInPersistenceUnitIT.class.getSimpleName() + ".war";
	private static final Logger log = Logger.getLogger( DefaultValidatorFactoryInPersistenceUnitIT.class );

	@Deployment
	public static Archive<?> createTestArchive() {
		return ShrinkWrap
				.create( WebArchive.class, WAR_FILE_NAME )
				.addClasses( User.class )
				.addAsResource( "log4j.properties" )
				.addAsResource( persistenceXml(), "META-INF/persistence.xml" )
				.addAsWebInfResource( EmptyAsset.INSTANCE, "beans.xml" );
	}

	private static Asset persistenceXml() {
		String persistenceXml = Descriptors.create( PersistenceDescriptor.class )
				.version( "2.0" )
				.createPersistenceUnit()
				.name( "default" )
				.jtaDataSource( "java:jboss/datasources/ExampleDS" )
				.getOrCreateProperties()
				.createProperty().name( "hibernate.hbm2ddl.auto" ).value( "create-drop" ).up()
				.up()
				.up()
				.exportAsString();
		return new StringAsset( persistenceXml );
	}

	@PersistenceContext
	EntityManager em;

	@Test
	public void testValidatorFactoryPassedToPersistenceUnit() throws Exception {
		log.debug( "Running testValidatorFactoryPassedToPersistenceUnit..." );
		Map<String, Object> properties = em.getEntityManagerFactory().getProperties();
		// TODO the test should also execute an actual validation. It is not guaranteed that one can access the validator factory
		// under javax.persistence.validation.factory. This works for the JBoss AS purposes, but not generically
		Object obj = properties.get( "javax.persistence.validation.factory" );
		assertTrue( "There should be an object under this property", obj != null );
		ValidatorFactory factory = (ValidatorFactory) obj;
		assertEquals(
				"The Hibernate Validator implementation should be used",
				"ValidatorImpl",
				factory.getValidator().getClass().getSimpleName()
		);
		log.debug( "testValidatorFactoryPassedToPersistenceUnit completed" );
	}
}
