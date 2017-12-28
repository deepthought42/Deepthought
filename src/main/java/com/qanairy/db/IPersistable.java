package com.qanairy.db;

import java.util.List;

/**
 * 
 * @author brand
 *
 * @param <V>
 */
public interface IPersistable<V, Z> {
	/**
	 * @return string of hashCodes identifying unique fingerprint of object by the contents of the object
	 */
	String generateKey(V obj);

	/**
	 * 
	 * @param connection
	 * @param obj
	 * @return
	 */
	Z convertToRecord(OrientConnectionFactory connection, V obj);
	
	/**
	 * 
	 * @param connection
	 * @param obj
	 * @return
	 */
	V convertFromRecord(Z obj);
	
	/**
	 * Saves the given object by finding existing instances in the databases and making
	 * the appropriate updates, or creating a new Vertex for the object data, 
	 * then saving the data to the database
	 * 
	 * @param connection
	 * @param obj
	 * @return
	 */
	V save(OrientConnectionFactory connection, V obj);
	
	/**
	 * 
	 * @param connection
	 * @param key
	 * @return
	 */
	V find(OrientConnectionFactory connection, String key);
	
	/**
	 * 
	 * @param connection
	 * @return
	 */
	List<V> findAll(OrientConnectionFactory connection);
}
