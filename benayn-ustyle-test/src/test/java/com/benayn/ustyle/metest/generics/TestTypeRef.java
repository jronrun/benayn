package com.benayn.ustyle.metest.generics;

public class TestTypeRef {
	
	private int level;
	private boolean isPair;
	private Class<?> rawType;
	private TestTypeRef childType;
	private TestTypeRef childType2;
	private String describe;
	
	public static TestTypeRef newTypeRef(int level) {
		TestTypeRef tree = new TestTypeRef();
		tree.level = level;
		return tree;
	}
	
	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public boolean isPair() {
		return isPair;
	}

	public void setPair(boolean isPair) {
		this.isPair = isPair;
	}

	public Class<?> getRawType() {
		return rawType;
	}

	public void setRawType(Class<?> rawType) {
		this.rawType = rawType;
	}

	public TestTypeRef getChildType() {
		return childType;
	}

	public void setChildType(TestTypeRef childType) {
		this.childType = childType;
	}

	public TestTypeRef getChildType2() {
		return childType2;
	}

	public void setChildType2(TestTypeRef childType2) {
		this.childType2 = childType2;
	}

	public String getDescribe() {
		return describe;
	}

	public void setDescribe(String describe) {
		this.describe = describe;
	}

	@Override public String toString() {
		return this.getDescribe();
	}
}
