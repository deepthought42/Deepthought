package com.deepthought.models.edges;

import org.testng.annotations.Test;

import com.deepthought.models.ImageMatrixNode;

import static org.testng.Assert.*;

/**
 * Unit tests for PartOf relationship entity.
 */
@Test(groups = "Regression")
public class PartOfTests {

	@Test
	public void defaultConstructor_createsEmptyPartOf() {
		PartOf partOf = new PartOf();
		assertNull(partOf.getId());
		assertNull(partOf.getPart());
		assertNull(partOf.getWhole());
	}

	@Test
	public void constructorWithParams_setsPartAndWhole() {
		ImageMatrixNode part = new ImageMatrixNode(ImageMatrixNode.Type.OUTLINE, 10, 10, new int[10][10][3]);
		ImageMatrixNode whole = new ImageMatrixNode(ImageMatrixNode.Type.ORIGINAL, 10, 10, new int[10][10][3]);
		PartOf partOf = new PartOf(part, whole);
		assertSame(partOf.getPart(), part);
		assertSame(partOf.getWhole(), whole);
	}

	@Test
	public void getId_returnsNullWhenNotSet() {
		PartOf partOf = new PartOf();
		assertNull(partOf.getId());
	}

	@Test
	public void setPart_andGetPart() {
		PartOf partOf = new PartOf();
		ImageMatrixNode part = new ImageMatrixNode(ImageMatrixNode.Type.CROPPED_OBJECT, 5, 5, new int[5][5][3]);
		partOf.setPart(part);
		assertSame(partOf.getPart(), part);
	}

	@Test
	public void setWhole_andGetWhole() {
		PartOf partOf = new PartOf();
		ImageMatrixNode whole = new ImageMatrixNode(ImageMatrixNode.Type.ORIGINAL, 20, 20, new int[20][20][3]);
		partOf.setWhole(whole);
		assertSame(partOf.getWhole(), whole);
	}
}
