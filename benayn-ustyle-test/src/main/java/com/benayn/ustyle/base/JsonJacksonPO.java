package com.benayn.ustyle.base;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JsonJacksonPO {
    
    private Set<Float> sFloats;
    private List<String> lStrs;
    private List<Map<String, Set<Integer>>> complex;
    private Map<String, Integer> mStrInt;
    private Map<Long, Boolean> mLongBool;
    
    private Set<Domain> sDomains;
    
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
    
    private Byte bytea;
    private Byte[] byteaArr;
    private byte bytep;
    
    //byte[] not support
//    private byte[] bytepArr;
//    public byte[] getBytepArr() {
//        return bytepArr;
//    }
//
//    public void setBytepArr(byte[] bytepArr) {
//        this.bytepArr = bytepArr;
//    }
    
    //nested not support
//    private Map<List<Map<String, Set<Integer>>>, Set<List<Map<String, Set<Integer>>>>> prop;
//
//    public Map<List<Map<String, Set<Integer>>>, Set<List<Map<String, Set<Integer>>>>> getProp() {
//        return prop;
//    }
//
//    public void setProp(Map<List<Map<String, Set<Integer>>>, Set<List<Map<String, Set<Integer>>>>> prop) {
//        this.prop = prop;
//    }

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

    public Set<Domain> getsDomains() {
        return sDomains;
    }

    public void setsDomains(Set<Domain> sDomains) {
        this.sDomains = sDomains;
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

    public Long[] getLongaArr() {
        return longaArr;
    }

    public void setLongaArr(Long[] longaArr) {
        this.longaArr = longaArr;
    }

    public long getLongp() {
        return longp;
    }

    public void setLongp(long longp) {
        this.longp = longp;
    }

    public long[] getLongpArr() {
        return longpArr;
    }

    public void setLongpArr(long[] longpArr) {
        this.longpArr = longpArr;
    }

    public Integer getIntegera() {
        return integera;
    }

    public void setIntegera(Integer integera) {
        this.integera = integera;
    }

    public Integer[] getIntegeraArr() {
        return integeraArr;
    }

    public void setIntegeraArr(Integer[] integeraArr) {
        this.integeraArr = integeraArr;
    }

    public int getIntegerp() {
        return integerp;
    }

    public void setIntegerp(int integerp) {
        this.integerp = integerp;
    }

    public int[] getIntegerpArr() {
        return integerpArr;
    }

    public void setIntegerpArr(int[] integerpArr) {
        this.integerpArr = integerpArr;
    }

    public Short getShorta() {
        return shorta;
    }

    public void setShorta(Short shorta) {
        this.shorta = shorta;
    }

    public Short[] getShortaArr() {
        return shortaArr;
    }

    public void setShortaArr(Short[] shortaArr) {
        this.shortaArr = shortaArr;
    }

    public short getShortp() {
        return shortp;
    }

    public void setShortp(short shortp) {
        this.shortp = shortp;
    }

    public short[] getShortpArr() {
        return shortpArr;
    }

    public void setShortpArr(short[] shortpArr) {
        this.shortpArr = shortpArr;
    }

    public Double getDoublea() {
        return doublea;
    }

    public void setDoublea(Double doublea) {
        this.doublea = doublea;
    }

    public Double[] getDoubleaArr() {
        return doubleaArr;
    }

    public void setDoubleaArr(Double[] doubleaArr) {
        this.doubleaArr = doubleaArr;
    }

    public double getDoublep() {
        return doublep;
    }

    public void setDoublep(double doublep) {
        this.doublep = doublep;
    }

    public double[] getDoublepArr() {
        return doublepArr;
    }

    public void setDoublepArr(double[] doublepArr) {
        this.doublepArr = doublepArr;
    }

    public Float getFloata() {
        return floata;
    }

    public void setFloata(Float floata) {
        this.floata = floata;
    }

    public Float[] getFloataArr() {
        return floataArr;
    }

    public void setFloataArr(Float[] floataArr) {
        this.floataArr = floataArr;
    }

    public float getFloatp() {
        return floatp;
    }

    public void setFloatp(float floatp) {
        this.floatp = floatp;
    }

    public float[] getFloatpArr() {
        return floatpArr;
    }

    public void setFloatpArr(float[] floatpArr) {
        this.floatpArr = floatpArr;
    }

    public Boolean getBooleana() {
        return booleana;
    }

    public void setBooleana(Boolean booleana) {
        this.booleana = booleana;
    }

    public Boolean[] getBooleanaArr() {
        return booleanaArr;
    }

    public void setBooleanaArr(Boolean[] booleanaArr) {
        this.booleanaArr = booleanaArr;
    }

    public boolean isBooleanp() {
        return booleanp;
    }

    public void setBooleanp(boolean booleanp) {
        this.booleanp = booleanp;
    }

    public boolean[] getBooleanpArr() {
        return booleanpArr;
    }

    public void setBooleanpArr(boolean[] booleanpArr) {
        this.booleanpArr = booleanpArr;
    }

    public Character getCharactera() {
        return charactera;
    }

    public void setCharactera(Character charactera) {
        this.charactera = charactera;
    }

    public Character[] getCharacteraArr() {
        return characteraArr;
    }

    public void setCharacteraArr(Character[] characteraArr) {
        this.characteraArr = characteraArr;
    }

    public char getCharacterp() {
        return characterp;
    }

    public void setCharacterp(char characterp) {
        this.characterp = characterp;
    }

    public char getCharacterpArr() {
        return characterpArr;
    }

    public void setCharacterpArr(char characterpArr) {
        this.characterpArr = characterpArr;
    }

    
    public Byte getBytea() {
        return bytea;
    }

    public void setBytea(Byte bytea) {
        this.bytea = bytea;
    }

    public Byte[] getByteaArr() {
        return byteaArr;
    }

    public void setByteaArr(Byte[] byteaArr) {
        this.byteaArr = byteaArr;
    }

    public byte getBytep() {
        return bytep;
    }

    public void setBytep(byte bytep) {
        this.bytep = bytep;
    }
}
