/**
 * 
 */
package com.benayn.ustyle.thirdparty;

import java.math.RoundingMode;
import java.util.Set;

import com.benayn.ustyle.Decision;
import com.google.common.collect.Sets;
import com.google.common.math.DoubleMath;
import com.google.common.math.LongMath;


/**
 * @see https://github.com/toonetown/guava-ext
 */
public enum SizeUnit implements EnumLookup.Keyed<String> {
    BYTE        ("B", true, 0),
    
    /* Decimal-based versions */
    KILOBYTE    ("KB", true, 1),
    MEGABYTE    ("MB", true, 2),
    GIGABYTE    ("GB", true, 3),
    TERABYTE    ("TB", true, 4),
    
    /* Plus binary versions */
    KIBIBYTE    ("KiB", false, 1),
    MEBIBYTE    ("MiB", false, 2),
    GIBIBYTE    ("GiB", false, 3),
    TEBIBYTE    ("TiB", false, 4);

    private static final long BASE_DECIMAL = 1000;
    private static final long BASE_BINARY = 1024;

    private final String value;
    private final long numBytes;
    private final boolean isDecimal;
    private final boolean isBinary;

    private SizeUnit(final String value, final boolean isDecimal, final int pow) {
        this.value = value;
        this.numBytes = LongMath.pow((isDecimal ? BASE_DECIMAL : BASE_BINARY), pow);
        this.isDecimal = (pow > 0) ? isDecimal : true;
        this.isBinary = (pow > 0) ? (!isDecimal) : true;
    }

    public long toBytes(final double size) {
        return DoubleMath.roundToLong(size * numBytes, RoundingMode.HALF_EVEN);
    }

    public double convert(final double sourceSize, final SizeUnit sourceUnit) {
        return ((double) sourceUnit.toBytes(sourceSize)) / ((double) numBytes);
    }

    private static final EnumLookup<SizeUnit, String> $ALL = EnumLookup.of(SizeUnit.class);

    /**
     * Finds a single SizeUnit by name, or ERROR if not found
     */
    public static SizeUnit find(final String name) { 
    	return $ALL.find(name); 
    }

	public static Set<SizeUnit> all() {
		return $ALL.keySet();
	}

	public static Set<SizeUnit> allDecimal() {
		return Sets.filter(all(), new Decision<SizeUnit>() {

			@Override public boolean apply(SizeUnit input) {
				return input == null ? false : input.isDecimal();
			}
		});
	}

	public static Set<SizeUnit> allBinary() {
		return Sets.filter(all(), new Decision<SizeUnit>() {

			@Override public boolean apply(SizeUnit input) {
				return input == null ? false : input.isBinary();
			}
		});
	}

	@Override public String getValue() {
		return value;
	}

	public long getNumBytes() {
		return numBytes;
	}

	public boolean isDecimal() {
		return isDecimal;
	}

	public boolean isBinary() {
		return isBinary;
	}
	
}
