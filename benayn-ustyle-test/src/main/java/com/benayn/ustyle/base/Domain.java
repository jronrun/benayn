package com.benayn.ustyle.base;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.benayn.ustyle.Randoms;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class Domain {
	
	private Map<String, Integer> mStrInt;
	private Map<Long, Boolean> mLongBool;
	
	private Set<Domain> sDomains;
	private Set<Float> sFloats;
	
	private List<String> lStrs;
	private List<Map<String, Set<Integer>>> complex;
	
	private EnumType enum4Test;
	private BigDecimal bigDecimal;
	private BigInteger bigInteger;
	
	private Date date;
	private String string;
	
	private Long longa;
	private Long[] longaArr;
	private long longp;
	private long[] longpArr;
	
	private Integer integera;
	private Integer[] integeraArr;
	private int integerp;
	private int[] integerpArr;
	
	private Short shorta;
	private Short[] shortaArr;
	private short shortp;
	private short[] shortpArr;
	
	private Byte bytea;
	private Byte[] byteaArr;
	private byte bytep;
	private byte[] bytepArr;
	
	private Double doublea;
	private Double[] doubleaArr;
	private double doublep;
	private double[] doublepArr;
	
	private Float floata;
	private Float[] floataArr;
	private float floatp;
	private float[] floatpArr;
	
	private Boolean booleana;
	private Boolean[] booleanaArr;
	private boolean booleanp;
	private boolean[] booleanpArr;
	
	private Character charactera;
	private Character[] characteraArr;
	private char characterp;
	private char characterpArr;
	
	private Map<List<Map<String, Set<Integer>>>, Set<List<Map<String, Set<Integer>>>>> prop;
	public Map<List<Map<String, Set<Integer>>>, Set<List<Map<String, Set<Integer>>>>> getProp() {
		return prop;
	}
	public void setProp(
			Map<List<Map<String, Set<Integer>>>, Set<List<Map<String, Set<Integer>>>>> prop) {
		this.prop = prop;
	}

	public static Domain getDomain() {
		Domain d = Randoms.get(Domain.class);
		
		Set<Domain> sDomains = Sets.newHashSet();
		for (int i = 0; i < 3; i++) {
			sDomains.add(Randoms.<Domain>get(Domain.class));
		}
		d.setsDomains(sDomains);
		
		List<Map<String, Set<Integer>>> l = Lists.newLinkedList();
		
		Set<Integer> s1 = Sets.newHashSet();
		for (int i = 0; i < 5; i++) {
			s1.add((Integer) Randoms.get(Integer.class));
		}
		
		Map<String, Set<Integer>> m = Maps.newLinkedHashMap();
		m.put("m1", s1);
		
		l.add(m);
		
		d.setComplex(l);
		return d;
	}
	
	public static Map<String, Object> getProps() {
		Map<String, Object> m = Maps.newLinkedHashMap();
		m.put("date", "2012-3-18 11:11:11");
		m.put("string", 11);
		
		m.put("longa", "33");
		m.put("longp", "3");
		
		m.put("integera", "22");
		m.put("integerp", 22);
		
		m.put("shorta", "11");
		m.put("shortp", "11");
		
		m.put("bytea", 10);
		m.put("bytep", 10);
		
		m.put("doublea", "88.88");
		m.put("doublep", 88.8);
		
		m.put("floata", 99.33);
		m.put("floatp", "99.9");
		
		m.put("booleana", "false");
		m.put("booleanp", Boolean.TRUE);
		
		m.put("charactera", "a");
		m.put("characterp", 3);
		
		return m;
	}
	
	
	public Set<Domain> getsDomains() {
		return sDomains;
	}


	public void setsDomains(Set<Domain> sDomains) {
		this.sDomains = sDomains;
	}


	public Set<Float> getsFloats() {
		return sFloats;
	}


	public void setsFloats(Set<Float> sFloats) {
		this.sFloats = sFloats;
	}

	public List<String> getlStrs() {
		return lStrs;
	}


	public void setlStrs(List<String> lStrs) {
		this.lStrs = lStrs;
	}


	public List<Map<String, Set<Integer>>> getComplex() {
		return complex;
	}


	public void setComplex(List<Map<String, Set<Integer>>> complex) {
		this.complex = complex;
	}


	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public String getString() {
		return string;
	}
	public void setString(String string) {
		this.string = string;
	}
	public Long getLonga() {
		return longa;
	}
	public void setLonga(Long longa) {
		this.longa = longa;
	}
	public long getLongp() {
		return longp;
	}
	public void setLongp(long longp) {
		this.longp = longp;
	}
	public Integer getIntegera() {
		return integera;
	}
	public void setIntegera(Integer integera) {
		this.integera = integera;
	}
	public int getIntegerp() {
		return integerp;
	}
	public void setIntegerp(int integerp) {
		this.integerp = integerp;
	}
	public Short getShorta() {
		return shorta;
	}
	public void setShorta(Short shorta) {
		this.shorta = shorta;
	}
	public short getShortp() {
		return shortp;
	}
	public void setShortp(short shortp) {
		this.shortp = shortp;
	}
	public Byte getBytea() {
		return bytea;
	}
	public void setBytea(Byte bytea) {
		this.bytea = bytea;
	}
	public byte getBytep() {
		return bytep;
	}
	public void setBytep(byte bytep) {
		this.bytep = bytep;
	}
	public Double getDoublea() {
		return doublea;
	}
	public void setDoublea(Double doublea) {
		this.doublea = doublea;
	}
	public double getDoublep() {
		return doublep;
	}
	public void setDoublep(double doublep) {
		this.doublep = doublep;
	}
	public Float getFloata() {
		return floata;
	}
	public void setFloata(Float floata) {
		this.floata = floata;
	}
	public float getFloatp() {
		return floatp;
	}
	public void setFloatp(float floatp) {
		this.floatp = floatp;
	}
	public Boolean getBooleana() {
		return booleana;
	}
	public void setBooleana(Boolean booleana) {
		this.booleana = booleana;
	}
	public boolean isBooleanp() {
		return booleanp;
	}
	public void setBooleanp(boolean booleanp) {
		this.booleanp = booleanp;
	}
	public Character getCharactera() {
		return charactera;
	}
	public void setCharactera(Character charactera) {
		this.charactera = charactera;
	}
	public char getCharacterp() {
		return characterp;
	}
	public void setCharacterp(char characterp) {
		this.characterp = characterp;
	}


	public Map<String, Integer> getmStrInt() {
		return mStrInt;
	}


	public void setmStrInt(Map<String, Integer> mStrInt) {
		this.mStrInt = mStrInt;
	}


	public Map<Long, Boolean> getmLongBool() {
		return mLongBool;
	}


	public void setmLongBool(Map<Long, Boolean> mLongBool) {
		this.mLongBool = mLongBool;
	}


	public EnumType getEnum4Test() {
		return enum4Test;
	}


	public void setEnum4Test(EnumType enum4Test) {
		this.enum4Test = enum4Test;
	}


	public BigDecimal getBigDecimal() {
		return bigDecimal;
	}


	public void setBigDecimal(BigDecimal bigDecimal) {
		this.bigDecimal = bigDecimal;
	}


	public BigInteger getBigInteger() {
		return bigInteger;
	}


	public void setBigInteger(BigInteger bigInteger) {
		this.bigInteger = bigInteger;
	}


	public Long[] getLongaArr() {
		return longaArr;
	}


	public void setLongaArr(Long[] longaArr) {
		this.longaArr = longaArr;
	}


	public long[] getLongpArr() {
		return longpArr;
	}


	public void setLongpArr(long[] longpArr) {
		this.longpArr = longpArr;
	}


	public Integer[] getIntegeraArr() {
		return integeraArr;
	}


	public void setIntegeraArr(Integer[] integeraArr) {
		this.integeraArr = integeraArr;
	}


	public int[] getIntegerpArr() {
		return integerpArr;
	}


	public void setIntegerpArr(int[] integerpArr) {
		this.integerpArr = integerpArr;
	}


	public Short[] getShortaArr() {
		return shortaArr;
	}


	public void setShortaArr(Short[] shortaArr) {
		this.shortaArr = shortaArr;
	}


	public short[] getShortpArr() {
		return shortpArr;
	}


	public void setShortpArr(short[] shortpArr) {
		this.shortpArr = shortpArr;
	}


	public Byte[] getByteaArr() {
		return byteaArr;
	}


	public void setByteaArr(Byte[] byteaArr) {
		this.byteaArr = byteaArr;
	}


	public byte[] getBytepArr() {
		return bytepArr;
	}


	public void setBytepArr(byte[] bytepArr) {
		this.bytepArr = bytepArr;
	}


	public Double[] getDoubleaArr() {
		return doubleaArr;
	}


	public void setDoubleaArr(Double[] doubleaArr) {
		this.doubleaArr = doubleaArr;
	}


	public double[] getDoublepArr() {
		return doublepArr;
	}


	public void setDoublepArr(double[] doublepArr) {
		this.doublepArr = doublepArr;
	}


	public Float[] getFloataArr() {
		return floataArr;
	}


	public void setFloataArr(Float[] floataArr) {
		this.floataArr = floataArr;
	}


	public float[] getFloatpArr() {
		return floatpArr;
	}


	public void setFloatpArr(float[] floatpArr) {
		this.floatpArr = floatpArr;
	}


	public Boolean[] getBooleanaArr() {
		return booleanaArr;
	}


	public void setBooleanaArr(Boolean[] booleanaArr) {
		this.booleanaArr = booleanaArr;
	}


	public boolean[] getBooleanpArr() {
		return booleanpArr;
	}


	public void setBooleanpArr(boolean[] booleanpArr) {
		this.booleanpArr = booleanpArr;
	}


	public Character[] getCharacteraArr() {
		return characteraArr;
	}


	public void setCharacteraArr(Character[] characteraArr) {
		this.characteraArr = characteraArr;
	}


	public char getCharacterpArr() {
		return characterpArr;
	}


	public void setCharacterpArr(char characterpArr) {
		this.characterpArr = characterpArr;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((booleana == null) ? 0 : booleana.hashCode());
		result = prime * result + (booleanp ? 1231 : 1237);
		result = prime * result + ((bytea == null) ? 0 : bytea.hashCode());
		result = prime * result + bytep;
		result = prime * result
				+ ((charactera == null) ? 0 : charactera.hashCode());
		result = prime * result + characterp;
		result = prime * result + ((date == null) ? 0 : date.hashCode());
		result = prime * result + ((doublea == null) ? 0 : doublea.hashCode());
		long temp;
		temp = Double.doubleToLongBits(doublep);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((floata == null) ? 0 : floata.hashCode());
		result = prime * result + Float.floatToIntBits(floatp);
		result = prime * result
				+ ((integera == null) ? 0 : integera.hashCode());
		result = prime * result + integerp;
		result = prime * result + ((longa == null) ? 0 : longa.hashCode());
		result = prime * result + (int) (longp ^ (longp >>> 32));
		result = prime * result + ((shorta == null) ? 0 : shorta.hashCode());
		result = prime * result + shortp;
		result = prime * result + ((string == null) ? 0 : string.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Domain other = (Domain) obj;
		if (booleana == null) {
			if (other.booleana != null)
				return false;
		} else if (!booleana.equals(other.booleana))
			return false;
		if (booleanp != other.booleanp)
			return false;
		if (bytea == null) {
			if (other.bytea != null)
				return false;
		} else if (!bytea.equals(other.bytea))
			return false;
		if (bytep != other.bytep)
			return false;
		if (charactera == null) {
			if (other.charactera != null)
				return false;
		} else if (!charactera.equals(other.charactera))
			return false;
		if (characterp != other.characterp)
			return false;
		if (date == null) {
			if (other.date != null)
				return false;
		} else if (!date.equals(other.date))
			return false;
		if (doublea == null) {
			if (other.doublea != null)
				return false;
		} else if (!doublea.equals(other.doublea))
			return false;
		if (Double.doubleToLongBits(doublep) != Double
				.doubleToLongBits(other.doublep))
			return false;
		if (floata == null) {
			if (other.floata != null)
				return false;
		} else if (!floata.equals(other.floata))
			return false;
		if (Float.floatToIntBits(floatp) != Float.floatToIntBits(other.floatp))
			return false;
		if (integera == null) {
			if (other.integera != null)
				return false;
		} else if (!integera.equals(other.integera))
			return false;
		if (integerp != other.integerp)
			return false;
		if (longa == null) {
			if (other.longa != null)
				return false;
		} else if (!longa.equals(other.longa))
			return false;
		if (longp != other.longp)
			return false;
		if (shorta == null) {
			if (other.shorta != null)
				return false;
		} else if (!shorta.equals(other.shorta))
			return false;
		if (shortp != other.shortp)
			return false;
		if (string == null) {
			if (other.string != null)
				return false;
		} else if (!string.equals(other.string))
			return false;
		return true;
	}
	

}
