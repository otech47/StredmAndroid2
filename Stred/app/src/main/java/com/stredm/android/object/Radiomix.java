package com.stredm.flume.object;


public class Radiomix {
	private String mId;
	private String mRadiomix;

	public Radiomix() {
		// default testing values
		this("-1",
			"radiomix");
	}
	
	public Radiomix(String id, String radiomix) {
		setId(id);
		setRadiomix(radiomix);
	}
	
	public String getId() {
		return mId;
	}

	public void setId(String mId) {
		this.mId = mId;
	}

	public String getRadiomix() {
		return mRadiomix;
	}
	
	public void setRadiomix(String radiomix) {
		this.mRadiomix = radiomix;
	}

}