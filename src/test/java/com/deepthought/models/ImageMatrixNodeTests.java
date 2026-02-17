package com.deepthought.models;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Unit tests for ImageMatrixNode entity.
 */
@Test(groups = "Regression")
public class ImageMatrixNodeTests {

	@Test
	public void defaultConstructor_createsEmptyNode() {
		ImageMatrixNode node = new ImageMatrixNode();
		assertNull(node.getId());
		assertNull(node.getType());
		assertEquals(node.getWidth(), 0);
		assertEquals(node.getHeight(), 0);
		assertNotNull(node.getRgbMatrix());
		assertEquals(node.getRgbMatrix().length, 0);
	}

	@Test
	public void constructorWithParams_setsAllFields() {
		int[][][] rgb = new int[2][3][3];
		rgb[0][0] = new int[] { 255, 0, 0 };
		rgb[0][1] = new int[] { 0, 255, 0 };
		rgb[0][2] = new int[] { 0, 0, 255 };
		rgb[1][0] = new int[] { 128, 128, 128 };
		rgb[1][1] = new int[] { 64, 64, 64 };
		rgb[1][2] = new int[] { 192, 192, 192 };
		ImageMatrixNode node = new ImageMatrixNode(ImageMatrixNode.Type.ORIGINAL, 3, 2, rgb);
		assertEquals(node.getType(), "ORIGINAL");
		assertEquals(node.getWidth(), 3);
		assertEquals(node.getHeight(), 2);
		int[][][] retrieved = node.getRgbMatrix();
		assertEquals(retrieved.length, 2);
		assertEquals(retrieved[0].length, 3);
		assertEquals(retrieved[0][0][0], 255);
		assertEquals(retrieved[0][0][1], 0);
		assertEquals(retrieved[0][0][2], 0);
	}

	@Test
	public void typeEnum_hasAllExpectedValues() {
		assertEquals(ImageMatrixNode.Type.ORIGINAL.name(), "ORIGINAL");
		assertEquals(ImageMatrixNode.Type.OUTLINE.name(), "OUTLINE");
		assertEquals(ImageMatrixNode.Type.PCA.name(), "PCA");
		assertEquals(ImageMatrixNode.Type.BLACK_WHITE.name(), "BLACK_WHITE");
		assertEquals(ImageMatrixNode.Type.CROPPED_OBJECT.name(), "CROPPED_OBJECT");
	}

	@Test
	public void setType_withString() {
		ImageMatrixNode node = new ImageMatrixNode();
		node.setType("OUTLINE");
		assertEquals(node.getType(), "OUTLINE");
	}

	@Test
	public void setType_withEnum() {
		ImageMatrixNode node = new ImageMatrixNode();
		node.setType(ImageMatrixNode.Type.PCA);
		assertEquals(node.getType(), "PCA");
	}

	@Test
	public void setType_withNullEnum() {
		ImageMatrixNode node = new ImageMatrixNode();
		node.setType("ORIGINAL");
		node.setType((ImageMatrixNode.Type) null);
		assertNull(node.getType());
	}

	@Test
	public void setId_andGetId() {
		ImageMatrixNode node = new ImageMatrixNode();
		node.setId(42L);
		assertEquals(node.getId(), Long.valueOf(42L));
	}

	@Test
	public void setWidth_andGetWidth() {
		ImageMatrixNode node = new ImageMatrixNode();
		node.setWidth(100);
		assertEquals(node.getWidth(), 100);
	}

	@Test
	public void setHeight_andGetHeight() {
		ImageMatrixNode node = new ImageMatrixNode();
		node.setHeight(200);
		assertEquals(node.getHeight(), 200);
	}

	@Test
	public void setRgbMatrix_withNull_clearsMatrix() {
		ImageMatrixNode node = new ImageMatrixNode(ImageMatrixNode.Type.ORIGINAL, 1, 1, new int[][][] { { { 1, 2, 3 } } });
		node.setRgbMatrix(null);
		int[][][] result = node.getRgbMatrix();
		assertNotNull(result);
		assertEquals(result.length, 0);
	}

	@Test
	public void getRgbMatrix_whenEmpty_returnsEmptyArray() {
		ImageMatrixNode node = new ImageMatrixNode();
		int[][][] result = node.getRgbMatrix();
		assertNotNull(result);
		assertEquals(result.length, 0);
	}
}
