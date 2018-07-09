package Qanairy.deepthought;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.Test;

import com.deepthought.models.Feature;
import com.qanairy.db.DataDecomposer;

public class DataDecomposerTests {

		@Test
		public void decomposeGenericObject(){
			String[] keys = {"String", "value", "object", "key", "here"};
			JSONObject json_obj = new JSONObject();
			try {
				json_obj.put("string_val", "String value");
				JSONObject obj = new JSONObject();
				obj.put("object_key", "object key here");
				json_obj.put("obj", obj);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			try {
				List<Feature> feature_list = DataDecomposer.decompose(json_obj);
				
				Map<String, Feature> map = new HashMap<String, Feature>();
				for(Feature obj : feature_list){
					map.put(obj.getValue(), obj);
				}
				
				for(int idx=0; idx<keys.length; idx++){
					map.remove(keys[idx]);
				}
				
				assert(map.isEmpty());
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NullPointerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		@Test
		public void decomposeMap(){
			
		}
		
		@Test
		public void decomposeObjectList(){
			
		}
		
		@Test
		public void decomposeObjectArray(){
			
		}
}
