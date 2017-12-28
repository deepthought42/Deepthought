package com.qanairy.models.repositories;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.apache.commons.codec.digest.DigestUtils;

import com.qanairy.db.IPersistable;
import com.qanairy.db.OrientConnectionFactory;
import com.qanairy.models.Group;
import com.qanairy.models.ObjectDefinition;
import com.qanairy.models.TestRecord;
import com.qanairy.models.dto.DomainRepository;
import com.qanairy.models.dto.GroupRepository;
import com.qanairy.models.dto.IObjectDefinition;
import com.qanairy.models.dto.PageRepository;
import com.qanairy.models.dto.PathRepository;
import com.qanairy.models.dto.TestRecordRepository;
import com.qanairy.persistence.DataAccessObject;
import com.qanairy.persistence.IDomain;
import com.qanairy.persistence.IGroup;
import com.qanairy.persistence.ITest;

public class ObjectDefinitionRepository implements IPersistable<ObjectDefinition, IObjectDefinition> {

	public String generateKey(ObjectDefinition obj) {
		return DigestUtils.sha256Hex(obj.getValue());
	}

	public IObjectDefinition convertToRecord(OrientConnectionFactory connection, ObjectDefinition obj) {
		@SuppressWarnings("unchecked")
		Iterable<IObjectDefinition> tests = (Iterable<IObjectDefinition>) com.qanairy.db.DataAccessObject.findByKey(generateKey(obj), connection, IObjectDefinition.class);
		Iterator<IObjectDefinition> iter = tests.iterator();
		IObjectDefinition test_record = null;
		if(iter.hasNext()){
			test_record = iter.next();
		}
		else{
			test_record = connection.getTransaction().addVertex("class:"+IObjectDefinition.class.getSimpleName()+","+UUID.randomUUID(), IObjectDefinition.class);
			test_record.setKey(generateKey(obj));
			test_record.setType(obj.getType());
			test_record.setValue(obj.getValue());
		}	
		
		return test_record;
	}

	public ObjectDefinition convertFromRecord(IObjectDefinition obj) {
		return new ObjectDefinition(obj.getKey(), obj.getValue(), obj.getType());
	}

	public ObjectDefinition save(OrientConnectionFactory connection, ObjectDefinition obj) {
		// TODO Auto-generated method stub
		return null;
	}

	public ObjectDefinition find(OrientConnectionFactory connection, String key) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<ObjectDefinition> findAll(OrientConnectionFactory connection) {
		// TODO Auto-generated method stub
		return null;
	}
}
