package com.stredm.android.object;


public class Mix {
	private String mId;
	private String mRadiomix;

	public Mix() {
		// default testing values
		this("-1",
			"radiomix");
	}
	
	public Mix(String id, String radiomix) {
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