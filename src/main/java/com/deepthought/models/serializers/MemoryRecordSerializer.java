package com.deepthought.models.serializers;

import java.io.IOException;

import com.deepthought.models.MemoryRecord;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class MemoryRecordSerializer extends StdSerializer<MemoryRecord>{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6005292549091414436L;

	public MemoryRecordSerializer() {
        this(null);
    }
   
    public MemoryRecordSerializer(Class<MemoryRecord> t) {
        super(t);
    }
 
    @Override
    public void serialize(
      MemoryRecord value, JsonGenerator jgen, SerializerProvider provider) 
      throws IOException, JsonProcessingException {
        jgen.writeStartObject();
        jgen.writeNumberField("id", value.getID());
        jgen.writeFieldName("prediction");
        jgen.writeArray(value.getPrediction(), 0, value.getPrediction().length);
        jgen.writeEndObject();
    }
}
