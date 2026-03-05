package Qanairy.deepthought;

import static org.testng.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.Test;

import Qanairy.deepthought.resourceClasses.SelfContainedTestObject;
import com.deepthought.models.Feature;
import com.qanairy.db.DataDecomposer;

@Test(groups = "Regression")
public class DataDecomposerTests {

    public void decomposeJsonObject_handlesNestedObjectsAndArrays() throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("title", "hello world");
        jsonObject.put("labels", new JSONArray(Arrays.asList("buy", "sell")));

        JSONObject nested = new JSONObject();
        nested.put("description", "nested value");
        jsonObject.put("nested", nested);

        List<Feature> features = DataDecomposer.decompose(jsonObject);
        List<String> values = toValues(features);

        assertTrue(values.contains("hello"));
        assertTrue(values.contains("world"));
        assertTrue(values.contains("buy"));
        assertTrue(values.contains("sell"));
        assertTrue(values.contains("nested"));
        assertTrue(values.contains("value"));
    }

    public void decomposeString_splitsByWhitespace() throws Exception {
        List<Feature> features = DataDecomposer.decompose("alpha beta   gamma");
        assertEquals(features.size(), 3);
        assertEquals(features.get(0).getValue(), "alpha");
        assertEquals(features.get(2).getValue(), "gamma");
    }

    public void decomposeMap_handlesStringsArraysAndLists() throws Exception {
        HashMap<String, Object> map = new HashMap<>();
        map.put("phrase", "quick fox");
        map.put("array", new String[] {"jumped", "over"});
        map.put("list", new ArrayList<>(Arrays.asList("lazy dog", "again")));

        List<Feature> features = DataDecomposer.decompose(map);
        List<String> values = toValues(features);

        assertTrue(values.contains("quick"));
        assertTrue(values.contains("fox"));
        assertTrue(values.contains("jumped"));
        assertTrue(values.contains("over"));
        assertTrue(values.contains("lazy"));
        assertTrue(values.contains("dog"));
        assertTrue(values.contains("again"));
    }

    public void decomposeObject_handlesPublicFields() throws Exception {
        SelfContainedTestObject object = new SelfContainedTestObject();
        object.string_value = "sample text";
        object.int_primitive_value = 42;
        object.string_list = Arrays.asList("one two", "three");
        object.string_map = new HashMap<>();
        object.string_map.put("k", "v");

        List<Feature> features = DataDecomposer.decompose(object);
        List<String> values = toValues(features);

        assertTrue(values.contains("sample"));
        assertTrue(values.contains("text"));
        assertTrue(values.contains("42"));
        assertTrue(values.contains("one"));
        assertTrue(values.contains("two"));
        assertTrue(values.contains("three"));
    }

    private List<String> toValues(List<Feature> features) {
        List<String> values = new ArrayList<>();
        for (Feature feature : features) {
            values.add(feature.getValue());
        }
        return values;
    }
}
