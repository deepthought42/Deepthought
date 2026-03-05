package Qanairy.deepthought.resourceClasses;

import java.util.List;
import java.util.Map;

public class SelfContainedTestObject {
	public String string_value;
	public int	  int_primitive_value;
	public Integer int_object_value;
	public double  double_value;
	public Double  double_object_value;
	public float   float_primitive_value;
	
	public List<String> string_list;
	public Map<String, String> string_map;

	public SelfContainedTestObject() {
	}
	
	public SelfContainedTestObject(String string_value, int primitive_int, Integer int_object, double double_primitive, List<String> string_list, Map<String, String> string_map){
		this.string_value = string_value;
		this.int_primitive_value = primitive_int;
		this.int_object_value = int_object;
		this.double_value = double_primitive;
		this.string_list = string_list;
		this.string_map = string_map;
	}
}
