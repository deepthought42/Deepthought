package com.deepthought.models.repositories;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.apache.commons.codec.digest.DigestUtils;

import com.deepthought.models.Action;
import com.deepthought.models.ObjectDefinition;
import com.deepthought.models.dto.IAction;
import com.deepthought.models.dto.IObjectDefinition;
import com.qanairy.db.IPersistable;
import com.qanairy.db.OrientConnectionFactory;

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
		
		ActionRepository action_repo = new ActionRepository();
		List<Action> actions = new ArrayList<Action>();
		for(IAction action : obj.getActions()){
			actions.add(action_repo.convertFromRecord(action));
		}
		
		return new ObjectDefinition(obj.getKey(), obj.getValue(), obj.getType(), actions);
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
