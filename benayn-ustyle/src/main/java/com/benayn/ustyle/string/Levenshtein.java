/**   
* @Title: Levenshtein.java 
* @Package com.benayn.ustyle.string 
* @author paulo.ye   
* @date May 30, 2015 11:45:12 AM 
* @version V1.0   
*/
package com.benayn.ustyle.string;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.benayn.ustyle.logger.Log;
import com.benayn.ustyle.logger.Loggers;
import com.google.common.collect.ForwardingObject;
import com.google.common.collect.Maps;

/**
 * https://github.com/jronrun/benayn
 * @see https://github.com/tdebatty/java-string-similarity
 */
public abstract class Levenshtein extends ForwardingObject {
	
	/**
	 * 
	 */
	protected final Log log = Loggers.from(getClass());
	
	/**
	 * default similarity coefficient
	 */
	protected static final double DEFAULT_SIMILARITY_COEFFICIENT = 0.6;
	
	/**
	 * Returns a new Levenshtein edit distance instance
	 * 
	 * @see LevenshteinEditDistance
	 * @param baseTarget
	 * @return
	 */
	public static <T extends Levenshtein> T of(String baseTarget) {
		return of(baseTarget, null);
	}
	
	/**
	 * Returns a new Levenshtein edit distance instance with compare target string
	 * 
	 * @see LevenshteinEditDistance
	 * @param baseTarget
	 * @param compareTarget
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Levenshtein> T of(String baseTarget, String compareTarget) {
		return (T) new LevenshteinEditDistance(baseTarget).update(compareTarget);
	}
	
	/**
	 * Returns a new Weighted Levenshtein edit distance instance
	 * 
	 * @see WeightedLevenshtein
	 * @param baseTarget
	 * @param characterSubstitution
	 * @return
	 */
	public static <T extends Levenshtein> T weightedLevenshtein(String baseTarget, CharacterSubstitution characterSubstitution) {
		return weightedLevenshtein(baseTarget, null, characterSubstitution);
	}
	
	/**
	 * Returns a new Weighted Levenshtein edit distance instance with compare target string
	 * 
	 * @see WeightedLevenshtein
	 * @param baseTarget
	 * @param compareTarget
	 * @param characterSubstitution
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Levenshtein> T weightedLevenshtein(
			String baseTarget, String compareTarget, CharacterSubstitution characterSubstitution) {
		return (T) new WeightedLevenshtein(baseTarget, characterSubstitution).update(compareTarget);
	}
	
	/**
	 * Returns a new Damerau-Levenshtein distance instance
	 * 
	 * @see Damerau
	 * @param baseTarget
	 * @return
	 */
	public static <T extends Levenshtein> T damerau(String baseTarget) {
		return damerau(baseTarget, null);
	}
	
	/**
	 * Returns a new Damerau-Levenshtein distance instance with compare target string
	 * 
	 * @see Damerau
	 * @param baseTarget
	 * @param compareTarget
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Levenshtein> T damerau(String baseTarget, String compareTarget) {
		return (T) new Damerau(baseTarget).update(compareTarget);
	}
	
	/**
	 * Returns a new Jaro-Winkler similarity instance
	 * 
	 * @see JaroWinkler
	 * @param baseTarget
	 * @return
	 */
	public static <T extends Levenshtein> T jaroWinkler(String baseTarget) {
		return jaroWinkler(baseTarget, null);
	}
	
	/**
	 * Returns a new Jaro-Winkler similarity instance with compare target string
	 * 
	 * @see JaroWinkler
	 * @param baseTarget
	 * @param compareTarget
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Levenshtein> T jaroWinkler(String baseTarget, String compareTarget) {
		return (T) new JaroWinkler(baseTarget).update(compareTarget);
	}
	
	/**
	 * Returns a new Longest Common Subsequence edit distance instance
	 * 
	 * @see LongestCommonSubsequence
	 * @param baseTarget
	 * @return
	 */
	public static <T extends Levenshtein> T longestCommonSubsequence(String baseTarget) {
		return longestCommonSubsequence(baseTarget, null);
	}
	
	/**
	 * Returns a new Longest Common Subsequence edit distance instance with compare target string
	 * 
	 * @see LongestCommonSubsequence
	 * @param baseTarget
	 * @param compareTarget
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Levenshtein> T longestCommonSubsequence(String baseTarget, String compareTarget) {
		return (T) new LongestCommonSubsequence(baseTarget).update(compareTarget);
	}
	
	/**
	 * Returns a new Q-Gram (Ukkonen) instance 
	 * 
	 * @see QGram
	 * @param baseTarget
	 * @return
	 */
	public static <T extends Levenshtein> T QGram(String baseTarget) {
		return QGram(baseTarget, null);
	}
	
	/**
	 * Returns a new Q-Gram (Ukkonen) instance with compare target string
	 * 
	 * @see QGram
	 * @param baseTarget
	 * @param compareTarget
	 * @return
	 */
	public static <T extends Levenshtein> T QGram(String baseTarget, String compareTarget) {
		return QGram(baseTarget, compareTarget, null);
	}
	
	/**
	 * Returns a new Q-Gram (Ukkonen) instance with compare target string and k-shingling
	 * 
	 * @see QGram
	 * @param baseTarget
	 * @param compareTarget
	 * @param k
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Levenshtein> T QGram(String baseTarget, String compareTarget, Integer k) {
		return (T) new QGram(baseTarget, k).update(compareTarget);
	}
	
	/**
	 * Returns an new n-Gram distance (Kondrak) instance
	 * 
	 * @see NGram
	 * @param baseTarget
	 * @return
	 */
	public static <T extends Levenshtein> T NGram(String baseTarget) {
		return NGram(baseTarget, null);
	}
	
	/**
	 * Returns an new n-Gram distance (Kondrak) instance with compare target string
	 * 
	 * @see NGram
	 * @param baseTarget
	 * @param compareTarget
	 * @return
	 */
	public static <T extends Levenshtein> T NGram(String baseTarget, String compareTarget) {
		return NGram(baseTarget, compareTarget, null);
	}
	
	/**
	 * Returns an new n-Gram distance (Kondrak) instance with compare target string and n
	 * 
	 * @see NGram
	 * @param baseTarget
	 * @param compareTarget
	 * @param n
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Levenshtein> T NGram(String baseTarget, String compareTarget, Integer n) {
		return (T) new NGram(baseTarget, n).update(compareTarget);
	}
	
	/**
	 * Returns a new Jaccard index instance 
	 * 
	 * @see Jaccard
	 * @param baseTarget
	 * @return
	 */
	public static <T extends Levenshtein> T jaccard(String baseTarget) {
		return jaccard(baseTarget, null);
	}
	
	/**
	 * Returns a new Jaccard index instance with compare target string
	 * 
	 * @see Jaccard
	 * @param baseTarget
	 * @param compareTarget
	 * @return
	 */
	public static <T extends Levenshtein> T jaccard(String baseTarget, String compareTarget) {
		return jaccard(baseTarget, compareTarget, null);
	}
	
	/**
	 * Returns a new Jaccard index instance with compare target string and k-shingling
	 * 
	 * @see Jaccard
	 * @param baseTarget
	 * @param compareTarget
	 * @param k
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Levenshtein> T jaccard(String baseTarget, String compareTarget, Integer k) {
		return (T) new Jaccard(baseTarget, k).update(compareTarget);
	}
	
	/**
	 * Returns a new Sorensen-Dice coefficient instance
	 * 
	 * @see SorensenDice
	 * @param baseTarget
	 * @return
	 */
	public static <T extends Levenshtein> T sorensenDice(String baseTarget) {
		return sorensenDice(baseTarget, null);
	}
	
	/**
	 * Returns a new Sorensen-Dice coefficient instance with compare target string
	 * 
	 * @see SorensenDice
	 * @param baseTarget
	 * @param compareTarget
	 * @return
	 */
	public static <T extends Levenshtein> T sorensenDice(String baseTarget, String compareTarget) {
		return sorensenDice(baseTarget, compareTarget, null);
	}
	
	/**
	 * Returns a new Sorensen-Dice coefficient instance with compare target string and k-shingling
	 * 
	 * @see SorensenDice
	 * @param baseTarget
	 * @param compareTarget
	 * @param k
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Levenshtein> T sorensenDice(String baseTarget, String compareTarget, Integer k) {
		return (T) new SorensenDice(baseTarget, k).update(compareTarget);
	}
	
	/**
	 * Returns a new Cosine similarity instance
	 * 
	 * @see Cosine
	 * @param baseTarget
	 * @return
	 */
	public static <T extends Levenshtein> T cosine(String baseTarget) {
		return cosine(baseTarget, null);
	}
	
	/**
	 * Returns a new Cosine similarity instance with compare target string
	 * 
	 * @see Cosine
	 * @param baseTarget
	 * @param compareTarget
	 * @return
	 */
	public static <T extends Levenshtein> T cosine(String baseTarget, String compareTarget) {
		return cosine(baseTarget, compareTarget, null);
	}
	
	/**
	 * Returns a new Cosine similarity instance with compare target string and k-shingling
	 * 
	 * @see Cosine
	 * @param baseTarget
	 * @param compareTarget
	 * @param k
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Levenshtein> T cosine(String baseTarget, String compareTarget, Integer k) {
		return (T) new Cosine(baseTarget, k).update(compareTarget);
	}
	
	/**
	 * Returns true if the delegate and the given compare target string is similarity
	 * 
	 * @see #isSimilarity()
	 * @param compareTarget
	 * @return
	 */
	public boolean isSimilarity(String compareTarget) {
		return update(compareTarget).isSimilarity();
	}
	
	/**
	 * Returns true if the delegate and compareTarget is similarity
	 * 
	 * @return
	 */
	public boolean isSimilarity() {
		return similarity() >= getSimilarityCoefficient();
	}
	
	/**
	 * Similarity between 0 (completely different) and 1 (delegate = the given compare target string)
	 * 
	 * @see #similarity()
	 * @param compareTarget
	 * @return
	 */
	public double similarity(String compareTarget) {
		return update(compareTarget).similarity();
	}
	
	/**
	 * Similarity between 0 (completely different) and 1 (delegate = compareTarget)
	 * 
	 * @return
	 */
    public double similarity() {
    	checkArgument(null != this.compareTarget, "The compare target string cannot be null");
    	return doSimilarity();
    }
    
    /**
     * Generally, distance = 1 - similarity. Some implementations can also provide a method distanceAbsolute
     * 
     * @see #distance()
     * @param compareTarget
     * @return
     */
    public double distance(String compareTarget) {
    	return update(compareTarget).distance();
    }
    
    /**
     * Generally, distance = 1 - similarity. Some implementations can also provide a method distanceAbsolute
     * 
     * @return distance between 0 (delegate = compareTarget) and 1 (completely different)
     */
    public double distance() {
    	checkArgument(null != this.compareTarget, "The compare target string cannot be null");
    	return doDistance();
    }
    
    /**
     * Levenshtein edit distance that allows to define different weights for different character substitutions.
     */
    public static class WeightedLevenshtein extends Levenshtein {
    	
    	private final CharacterSubstitution characterSubstitution;

		protected WeightedLevenshtein(String delegate, CharacterSubstitution characterSubstitution) {
			super(delegate);
			this.characterSubstitution = checkNotNull(characterSubstitution);
		}

		@Override protected double doDistance() {
			return (double) distanceAbsolute(this.delegate, this.compareTarget) 
					/ Math.max(this.delegate.length(), this.compareTarget.length());
		}

		@Override protected double doSimilarity() {
			return 1.0 - distance();
		}
		
		public double distanceAbsolute(String s1, String s2) {
	        if (s1.equals(s2)){
	            return 0;
	        }
	        
	        if (s1.length() == 0) {
	            return s2.length();
	        }
	        
	        if (s2.length() == 0) {
	            return s1.length();
	        }

	        // create two work vectors of integer distances
	        double[] v0 = new double[s2.length() + 1];
	        double[] v1 = new double[s2.length() + 1];
	        double[] vtemp;

	        // initialize v0 (the previous row of distances)
	        // this row is A[0][i]: edit distance for an empty s
	        // the distance is just the number of characters to delete from t
	        for (int i = 0; i < v0.length; i++) {
	            v0[i] = i;
	        }
	        
	        for (int i = 0; i < s1.length(); i++) {
	            // calculate v1 (current row distances) from the previous row v0
	            // first element of v1 is A[i+1][0]
	            //   edit distance is delete (i+1) chars from s to match empty t
	            v1[0] = i + 1;

	            // use formula to fill in the rest of the row
	            for (int j = 0; j < s2.length(); j++) {
	                double cost = (s1.charAt(i) == s2.charAt(j)) 
	                		? 0 : this.characterSubstitution.cost(s1.charAt(i), s2.charAt(j));
	                v1[j + 1] = Math.min(
	                        v1[j] + 1,              // Cost of insertion
	                        Math.min(
	                                v0[j + 1] + 1,  // Cost of remove
	                                v0[j] + cost)); // Cost of substitution
	            }
	            
	            // copy v1 (current row) to v0 (previous row) for next iteration
	            //System.arraycopy(v1, 0, v0, 0, v0.length);
	            
	            // Flip references to current and previous row
	            vtemp = v0;
	            v0 = v1;
	            v1 = vtemp;
	                
	        }

	        return v0[s2.length()];
	    }

		@Override protected WeightedLevenshtein delegate() {
			return this;
		}
    	
    }
    
    /**
     * Used to indicate the cost of character substitution.
     * 
     * Cost should always be in [0.0 .. 1.0]. For example, in an OCR application, cost('o', 'a') could be 0.4
     * In a checkspelling application, cost('u', 'i') could be 0.4 because these are next to each other on the keyboard...
     */
    public interface CharacterSubstitution {
    	
        public double cost(char c1, char c2);
        
    }
    
    /**
     * Cosine Similarity.
     * The strings are first transformed in vectors of occurrences of k-shingles (sequences of k characters). 
     * In this n-dimensional space, the similarity between the two strings is the cosine of their respective vectors.
     * 
     * The default value of k is 3.
     */
    public static class Cosine extends SetBasedStringSimilarity {

		protected Cosine(String delegate, Integer k) {
			super(delegate, null == k ? 3 : k);
		}

		@Override protected double similarity(int[] profile1, int[] profile2) {
			return dotProduct(profile1, profile2) / (norm(profile1) * norm(profile2));
		}
		
		/**
	     * Compute the norm L2 : sqrt(Sum_i( v_i^2))
	     * @param profile
	     * @return L2 norm
	     */
	    protected static double norm(int[] profile) {
	        double agg = 0;
	        
	        for (int v : profile) {
	            agg += v * v;
	        }
	        
	        return Math.sqrt(agg);
	    }
	    
	    protected static double dotProduct(int[] profile1, int[] profile2) {
	        int length = Math.max(profile1.length, profile2.length);
	        profile1 = java.util.Arrays.copyOf(profile1, length);
	        profile2 = java.util.Arrays.copyOf(profile2, length);
	        
	        double agg = 0;
	        for (int i = 0; i < length; i++) {
	            agg += profile1[i] * profile2[i];
	        }
	        return agg;
	    }

		@Override protected Cosine delegate() {
			return this;
		}
    	
    }
    
    /**
     * Sorensen-Dice coefficient, aka Sørensen index, Dice's coefficient or 
     * Czekanowski's binary (non-quantitative) index.
     * 
     * The strings are first converted to boolean sets of k-shingles (strings of k characters), 
     * then the similarity is computed as 2 * |A inter B| / (|A| + |B|).
     * Attention: Sorensen-Dice distance (and similarity) does not satisfy triangle inequality.
     * 
     * The default value of k is 3.
     */
    public static class SorensenDice extends SetBasedStringSimilarity {

		protected SorensenDice(String delegate, Integer k) {
			super(delegate, null == k ? 3 : k);
		}

		/**
	     * Compute Sorensen-Dice coefficient 2 * |A inter B| / (|A| + |B|).
	     * @param profile1
	     * @param profile2
	     * @return 
	     */
		@Override protected double similarity(int[] profile1, int[] profile2) {
	        int length = Math.max(profile1.length, profile2.length);
	        profile1 = java.util.Arrays.copyOf(profile1, length);
	        profile2 = java.util.Arrays.copyOf(profile2, length);
	        
	        int inter = 0;
	        int sum = 0;
	        for (int i = 0; i < length; i++) {
	            if (profile1[i] > 0 && profile2[i] > 0) {
	                inter++;
	            }
	            
	            if (profile1[i] > 0) {
	                sum++;
	            }
	            
	            if (profile2[i] > 0) {
	                sum++;
	            }
	        }
	        
	        return 2.0 * inter / sum;
	    }

		@Override protected SorensenDice delegate() {
			return this;
		}
    	
    }
    
    /**
     * Jaccard index. The strings are first transformed into sets of k-shingles (sequences of k
     * characters), then Jaccard index is computed as |A inter B| / |A union B|.
     * The default value of k is 3.
     */
    public static class Jaccard extends SetBasedStringSimilarity {

		protected Jaccard(String delegate, Integer k) {
			super(delegate, null == k ? 3 : k);
		}

		/**
	     * Compute and return the Jaccard index similarity between two string profiles.
	     * 
	     * E.g:
	     * ks = new KShingling(4)
	     * profile1 = ks.getProfile("My String")
	     * profile2 = ks.getProfile("My other string")
	     * similarity = jaccard.similarity(profile1, profile2)
	     * 
	     * @param profile1
	     * @param profile2
	     * @return 
	     */
		@Override protected double similarity(int[] profile1, int[] profile2) {
	        int length = Math.max(profile1.length, profile2.length);
	        profile1 = java.util.Arrays.copyOf(profile1, length);
	        profile2 = java.util.Arrays.copyOf(profile2, length);
	        
	        int inter = 0;
	        int union = 0;
	        
	        for (int i = 0; i < length; i++) {
	            if (profile1[i] > 0 || profile2[i] > 0) {
	                union++;
	                
	                if (profile1[i] > 0 && profile2[i] > 0) {
	                    inter++;
	                }
	            }
	        }
	    
	        return (double) inter / union;
	    }

		@Override
		protected Jaccard delegate() {
			return this;
		}
    	
    }
    
    /**
     * N-Gram Similarity as defined by Kondrak, "N-Gram Similarity and Distance",
     * String Processing and Information Retrieval, Lecture Notes in Computer Science Volume 3772, 2005, pp 115-126.
     * 
     * The algorithm uses affixing with special character '\n' two increase the weight of first characters. 
     * The normalization is achieved by dividing the total similarity score the original length of the longer word.
     * 
     * http://webdocs.cs.ualberta.ca/~kondrak/papers/spire05.pdf
     * 
     * The default value of n is 2.
     */
    public static class NGram extends Levenshtein {
    	
    	private final int n;

		protected NGram(String delegate, Integer n) {
			super(delegate);
			this.n = null == n ? 2 : n;
		}

		@Override protected double doDistance() {
			return distance(this.delegate, this.compareTarget);
		}

		@Override
		protected double doSimilarity() {
			return distance();
		}
		
		public double distance(String target1, String target2) {
	        final char special = '\n';
	        final int sl = target1.length();
	        final int tl = target2.length();

	        if (sl == 0 || tl == 0) {
	            if (sl == tl) {
	                return 1;
	            } else {
	                return 0;
	            }
	        }

	        int cost = 0;
	        if (sl < n || tl < n) {
	            for (int i = 0, ni = Math.min(sl, tl); i < ni; i++) {
	                if (target1.charAt(i) == target2.charAt(i)) {
	                    cost++;
	                }
	            }
	            return (float) cost / Math.max(sl, tl);
	        }

	        char[] sa = new char[sl + n - 1];
	        float p[]; //'previous' cost array, horizontally
	        float d[]; // cost array, horizontally
	        float _d[]; //placeholder to assist in swapping p and d

	        //construct sa with prefix
	        for (int i = 0; i < sa.length; i++) {
	            if (i < n - 1) {
	                sa[i] = special; //add prefix
	            } else {
	                sa[i] = target1.charAt(i - n + 1);
	            }
	        }
	        p = new float[sl + 1];
	        d = new float[sl + 1];

	        // indexes into strings s and t
	        int i; // iterates through source
	        int j; // iterates through target

	        char[] t_j = new char[n]; // jth n-gram of t

	        for (i = 0; i <= sl; i++) {
	            p[i] = i;
	        }

	        for (j = 1; j <= tl; j++) {
	            //construct t_j n-gram 
	            if (j < n) {
	                for (int ti = 0; ti < n - j; ti++) {
	                    t_j[ti] = special; //add prefix
	                }
	                for (int ti = n - j; ti < n; ti++) {
	                    t_j[ti] = target2.charAt(ti - (n - j));
	                }
	            } else {
	                t_j = target2.substring(j - n, j).toCharArray();
	            }
	            d[0] = j;
	            for (i = 1; i <= sl; i++) {
	                cost = 0;
	                int tn = n;
	                //compare sa to t_j
	                for (int ni = 0; ni < n; ni++) {
	                    if (sa[i - 1 + ni] != t_j[ni]) {
	                        cost++;
	                    } else if (sa[i - 1 + ni] == special) { //discount matches on prefix
	                        tn--;
	                    }
	                }
	                float ec = (float) cost / tn;
	                // minimum of cell to the left+1, to the top+1, diagonally left and up +cost
	                d[i] = Math.min(Math.min(d[i - 1] + 1, p[i] + 1), p[i - 1] + ec);
	            }
	            // copy current distance counts to 'previous row' distance counts
	            _d = p;
	            p = d;
	            d = _d;
	        }

	        // our last action in the above loop was to switch d and p, so p now
	        // actually has the most recent cost counts
	        return 1.0 - (p[sl] / Math.max(tl, sl));
	    }

		@Override protected NGram delegate() {
			return this;
		}
    	
    }
    
    /**
     * Q-gram similarity and distance.
     * Defined by Ukkonen in "Approximate string-matching with q-grams and maximal matches", 
     * http://www.sciencedirect.com/science/article/pii/0304397592901434
     * 
     * The distance between two strings is defined as the L1 norm of the difference of their profiles 
     * (the number of occurences of each k-shingle). Q-gram distance is a lower bound on Levenshtein distance, 
     * but can be computed in O(|A| + |B|), where Levenshtein requires O(|A|.|B|)
     * 
     * The default value of k is 3.
     */
    public static class QGram extends SetBasedStringSimilarity {

		protected QGram(String delegate, Integer k) {
			super(delegate, null == k ? 3 : k);
		}

		@Override protected double similarity(int[] profile1, int[] profile2) {
	        int length = Math.max(profile1.length, profile2.length);
	        profile1 = java.util.Arrays.copyOf(profile1, length);
	        profile2 = java.util.Arrays.copyOf(profile2, length);
	        
	        int d = 0;
	        for (int i = 0; i < length; i++) {
	            d += Math.abs(profile1[i] - profile2[i]);
	        }
	        
	        int sum = 0;
	        for (int i : profile1) {
	            sum += i;
	        }
	        for (int i : profile2) {
	            sum += i;
	        }
	        
	        return 1.0 - (double) d / sum;
	    }

		@Override protected QGram delegate() {
			return this;
		}
    	
    }
    
    /**
     * 
     */
    public static abstract class SetBasedStringSimilarity extends Levenshtein {
    	
    	protected final int k;

		protected SetBasedStringSimilarity(String delegate, int k) {
			super(delegate);
			this.k = k;
		}

		@Override protected double doDistance() {
			return 1.0 - doSimilarity();
		}

		@Override protected double doSimilarity() {
			if (this.delegate.equals(this.compareTarget)) {
	            return 1.0;
	        }
	        
	        if (this.delegate.equals(Strs.EMPTY) || this.compareTarget.equals(Strs.EMPTY)) {
	            return 0.0;
	        }
	        
	        KShingling ks = new KShingling(this.k);
	        return similarity(ks.getProfile(this.delegate), ks.getProfile(this.compareTarget));
		}
		
		protected abstract double similarity(int[] profile1, int[] profile2);
    	
    }
    
    /**
     * k-shingling is the operation of transforming a string (or text document) into
     * a set of n-grams, which can be used to measure the similarity between two strings or documents.
     * 
     * Generally speaking, a n-gram is any sequence of k tokens. We use here the definition from  Leskovec, Rajaraman & Ullman (2014),
     * "Mining of Massive Datasets", Cambridge University Press:
     * Multiple subsequent spaces are replaced by a single space, and a k-gram is a sequence of k characters.
     */
    public static class KShingling {
    	
    	protected int k;
        private Map<String, Integer> shingles = Maps.newHashMap();
        
        /**
         * Default value of k is 5 (recommended for emails).
         * A good rule of thumb is to imagine that there are only 20 characters 
         * and estimate the number of k-shingles as 20^k. For large documents, 
         * such as research articles, k = 9 is considered a safe choice.
         */
        public KShingling() {
            k = 5;
        }
        
        public KShingling(int k) {
        	checkArgument(k > 0, "k should be positive!");
            this.k = k;
        }
        
        public int getK() {
            return k;
        }
        
        /**
         * Pattern for finding multiple following spaces
         */
        private static final Pattern spaceReg = Pattern.compile("\\s+");
        
        /**
         * Compute and return the profile of s, as defined by Ukkonen "Approximate string-matching with q-grams and maximal matches".
         * https://www.cs.helsinki.fi/u/ukkonen/TCS92.pdf
         * The profile is the number of occurrences of k-shingles, and is used to compute q-gram similarity, Jaccard index, etc.
         * E.g. if s = ABCAB and k =2
         * This will return {AB=2, BC=1, CA=1}
         * 
         * Attention: the space requirement of a single profile can be as large as
         * k * n (where n is the size of the string)
         * Computation cost is O(n)
         * @param target
         * @return 
         */
        public int[] getProfile(String target) {
            ArrayList<Integer> r = new ArrayList<Integer>(shingles.size());
            for (int i = 0; i < shingles.size(); i++) {
                r.add(0);
            }
            
            target = spaceReg.matcher(target).replaceAll(" ");
            String shingle;
            for (int i = 0; i < (target.length() - k + 1); i++) {
                shingle = target.substring(i, i+k);
                int position;
                
                if (shingles.containsKey(shingle)) {
                    position = shingles.get(shingle);
                    r.set(position, r.get(position) + 1);
                    
                } else {
                    shingles.put(shingle, shingles.size());
                    r.add(1);
                }
                
            }
            
            return convertIntegers(r);
        }
     
        public static int[] convertIntegers(List<Integer> integers) {
            int[] ret = new int[integers.size()];
            Iterator<Integer> iterator = integers.iterator();
            for (int i = 0; i < ret.length; i++) {
                ret[i] = iterator.next().intValue();
            }
            return ret;
        }
    }
    
    /**
     * The longest common subsequence (LCS) problem consists in finding the longest subsequence common to two (or more) sequences. 
     * It differs from problems of finding common substrings: unlike substrings, subsequences are not required to occupy 
     * consecutive positions within the original sequences.
     * 
     * It is used by the diff utility, by Git for reconciling multiple changes, etc.
     * 
     * The LCS distance between Strings X (length n) and Y (length m) is
     * n + m - 2 |LCS(X, Y)|
     * min = 0
     * max = n + m
     * 
     * LCS distance is equivalent to Levenshtein distance, when only insertion and deletion is allowed (no substitution), 
     * or when the cost of the substitution is the double of the cost of an insertion or deletion.
     * 
     * ! This class currently implements the dynamic programming approach, which has a space requirement O(m * n)!
     */
    public static class LongestCommonSubsequence extends Levenshtein {

		protected LongestCommonSubsequence(String delegate) {
			super(delegate);
		}

		@Override protected double doDistance() {
			return ((double) distanceAbsolute(this.delegate, this.compareTarget)) 
					/ (this.delegate.length() + this.compareTarget.length());
		}

		@Override protected double doSimilarity() {
			return 1.0 - distance();
		}

		@Override protected LongestCommonSubsequence delegate() {
			return this;
		}
		
		public int distanceAbsolute(String target1, String target2) {
	        return target1.length() + target2.length() - 2 * length(target1, target2);
	    }
	    
	    public int length(String target1, String target2) {
	        /* function LCSLength(X[1..m], Y[1..n])
	            C = array(0..m, 0..n)
	        
	            for i := 0..m
	               C[i,0] = 0
	        
	            for j := 0..n
	               C[0,j] = 0
	        
	            for i := 1..m
	                for j := 1..n
	                    if X[i] = Y[j]
	                        C[i,j] := C[i-1,j-1] + 1
	                    else
	                        C[i,j] := max(C[i,j-1], C[i-1,j])
	            return C[m,n]
	        */
	        int m = target1.length();
	        int n = target2.length();
	        char[] X = target1.toCharArray();
	        char[] Y = target2.toCharArray();
	        
	        int[][] C = new int[m+1][n+1];
	        
	        for (int i = 0; i <= m; i++) {
	            C[i][0] = 0;
	        }
	        
	        for (int j = 0; j <= n; j++) {
	            C[0][j] = 0;
	        }
	        
	        for (int i = 1; i <=m ; i++) {
	            for (int j = 1; j <= n; j++) {
	                if (X[i-1] == Y[j-1]) {
	                    C[i][j] = C[i-1][j-1] + 1;
	                    
	                } else {
	                    C[i][j] = Math.max(C[i][j-1], C[i-1][j]);
	                }
	            }
	        }
	        
	        return C[m][n];
	    }
    	
    }
    
    /**
     * Jaro-Winkler is string edit distance that was developed in the area of 
     * record linkage (duplicate detection) (Winkler, 1990). 
     * 
     * The Jaro–Winkler distance metric is designed and best suited for short 
     * strings such as person names, and to detect typos.
     * 
     * http://en.wikipedia.org/wiki/Jaro-Winkler_distance
     */
    public static class JaroWinkler extends Levenshtein {

    	/**
         * Sets the threshold used to determine when Winkler bonus should be used.
         * Set to a negative value to get the Jaro distance.
         * Default value is 0.7
         *
         * @param threshold the new value of the threshold
         */
        public final void setThreshold(double threshold) {
            this.threshold = threshold;
        }

        /**
         * Returns the current value of the threshold used for adding the Winkler
         * bonus. The default value is 0.7.
         *
         * @return the current value of the threshold
         */
        public double getThreshold() {
            return threshold;
        }
        
        private double threshold = 0.7;
        
		protected JaroWinkler(String delegate) {
			super(delegate);
		}

		@Override protected double doDistance() {
			return 1.0 - similarity();
		}

		@Override protected double doSimilarity() {
			int[] mtp = matches(this.delegate, this.compareTarget);
	        float m = mtp[0];
	        if (m == 0) {
	            return 0f;
	        }
	        float j = ((m / this.delegate.length() + m / this.compareTarget.length() + (m - mtp[1]) / m)) / 3;
	        float jw = j < getThreshold() ? j : j + Math.min(0.1f, 1f / mtp[3]) * mtp[2] * (1 - j);
	        return jw;
		}

		@Override protected JaroWinkler delegate() {
			return this;
		}
		
		private int[] matches(String target1, String target2) {
	        String max, min;
	        if (target1.length() > target2.length()) {
	            max = target1;
	            min = target2;
	        } else {
	            max = target2;
	            min = target1;
	        }
	        int range = Math.max(max.length() / 2 - 1, 0);
	        int[] matchIndexes = new int[min.length()];
	        Arrays.fill(matchIndexes, -1);
	        boolean[] matchFlags = new boolean[max.length()];
	        int matches = 0;
	        for (int mi = 0; mi < min.length(); mi++) {
	            char c1 = min.charAt(mi);
	            for (int xi = Math.max(mi - range, 0),
	                    xn = Math.min(mi + range + 1, max.length()); xi < xn; xi++) {
	                if (!matchFlags[xi] && c1 == max.charAt(xi)) {
	                    matchIndexes[mi] = xi;
	                    matchFlags[xi] = true;
	                    matches++;
	                    break;
	                }
	            }
	        }
	        char[] ms1 = new char[matches];
	        char[] ms2 = new char[matches];
	        for (int i = 0, si = 0; i < min.length(); i++) {
	            if (matchIndexes[i] != -1) {
	                ms1[si] = min.charAt(i);
	                si++;
	            }
	        }
	        for (int i = 0, si = 0; i < max.length(); i++) {
	            if (matchFlags[i]) {
	                ms2[si] = max.charAt(i);
	                si++;
	            }
	        }
	        int transpositions = 0;
	        for (int mi = 0; mi < ms1.length; mi++) {
	            if (ms1[mi] != ms2[mi]) {
	                transpositions++;
	            }
	        }
	        int prefix = 0;
	        for (int mi = 0; mi < min.length(); mi++) {
	            if (target1.charAt(mi) == target2.charAt(mi)) {
	                prefix++;
	            } else {
	                break;
	            }
	        }
	        return new int[]{matches, transpositions / 2, prefix, max.length()};
	    }
    	
    }
    
    /**
     * Damerau-Levenshtein distance, computed as the minimum number of operations needed to 
     * transform one string into the other, where an operation is defined as an insertion, deletion, or substitution of a
	 * single character, or a transposition of two adjacent characters.
     */
    public static class Damerau extends Levenshtein {

		protected Damerau(String delegate) {
			super(delegate);
		}

		@Override protected double doDistance() {
			return (double) absoluteDistance(this.delegate, this.compareTarget) 
					/ Math.max(this.delegate.length(), this.compareTarget.length());
		}

		@Override protected double doSimilarity() {
			return 1.0 - distance();
		}
		
		public int absoluteDistance(String target1, String target2) {

	        // INFinite distance is the max possible distance
	        int INF = target1.length() + target2.length();
	        
	        // Create and initialize the character array indices
	        HashMap<Character, Integer> DA = new HashMap<Character, Integer>();
	        
	        for (int d = 0; d < target1.length(); d++) {
	            if (!DA.containsKey(target1.charAt(d))) {
	                DA.put(target1.charAt(d), 0);
	            }
	        }
	        
	        for (int d = 0; d < target2.length(); d++) {
	            if (!DA.containsKey(target2.charAt(d))) {
	                DA.put(target2.charAt(d), 0);
	            }
	        }
	        
	        // Create the distance matrix H[0 .. s1.length+1][0 .. s2.length+1]
	        int[][] H = new int[target1.length() + 2][target2.length() + 2];
	        
	        // initialize the left and top edges of H
	        for (int i = 0; i <= target1.length(); i++) {
	            H[i + 1][0] = INF;
	            H[i + 1][1] = i;
	        }
	        
	        for (int j = 0; j <= target2.length(); j++) {
	            H[0][j + 1] = INF;
	            H[1][j + 1] = j;
	            
	        }
	        
	        // fill in the distance matrix H
	        // look at each character in s1
	        for (int i = 1; i <= target1.length(); i++) {
	            int DB = 0;
	            
	            // look at each character in b
	            for (int j = 1; j <= target2.length(); j++) {
	                int i1 = DA.get(target2.charAt(j - 1));
	                int j1 = DB;
	                
	                int cost = 1;
	                if (target1.charAt(i - 1) == target2.charAt(j - 1)) {
	                    cost = 0;
	                    DB = j;
	                }
	                
	                H[i + 1][j + 1] = min(
	                        H[i][j] + cost,     // substitution
	                        H[i + 1][j] + 1,    // insertion
	                        H[i][j + 1] + 1,    // deletion
	                        H[i1][j1] + (i - i1 - 1) + 1 + (j - j1 - 1));
	            }
	            
	            DA.put(target1.charAt(i - 1), i);
	        }
	        
	        return H[target1.length() + 1][target2.length() + 1];
	    }

		protected static int min(int a, int b, int c, int d) {
	        return Math.min(a, Math.min(b, Math.min(c, d)));
	    }
		
		@Override protected Damerau delegate() {
			return this;
		}
    	
    }
    
    /**
     * The Levenshtein distance, or edit distance, between two words is the minimum number of single-character 
     * edits (insertions, deletions or substitutions) required to change one word into the other.
     * 
     * http://en.wikipedia.org/wiki/Levenshtein_distance
     * 
     * It is always at least the difference of the sizes of the two strings. It is at most the length of the longer string.
     * It is zero if and only if the strings are equal. 
     * If the strings are the same size, the Hamming distance is an upper bound on the Levenshtein distance.
     * The Levenshtein distance verifies the triangle inequality (the distance between two strings is no greater 
     * than the sum Levenshtein distances from a third string).
     * 
     * Implementation uses dynamic programming (Wagner–Fischer algorithm), with only 2 rows of data.
     * The space requirement is thus O(m) and the algorithm runs in O(mn).
     */
    public static class LevenshteinEditDistance extends Levenshtein {

		protected LevenshteinEditDistance(String delegate) {
			super(delegate);
		}

		@Override public double doSimilarity() {
			return 1.0 - distance();
		}

		@Override public double doDistance() {
			return (double) distanceAbsolute(this.delegate, this.compareTarget) 
					/ Math.max(this.delegate.length(), this.compareTarget.length());
		}
		
		@Override protected LevenshteinEditDistance delegate() {
			return this;
		}
		
	    public int distanceAbsolute(String target1, String target2) {
	        if (target1.equals(target2)){
	            return 0;
	        }
	        
	        if (target1.length() == 0) {
	            return target2.length();
	        }
	        
	        if (target2.length() == 0) {
	            return target1.length();
	        }

	        // create two work vectors of integer distances
	        int[] v0 = new int[target2.length() + 1];
	        int[] v1 = new int[target2.length() + 1];
	        int[] vtemp;

	        // initialize v0 (the previous row of distances)
	        // this row is A[0][i]: edit distance for an empty s
	        // the distance is just the number of characters to delete from t
	        for (int i = 0; i < v0.length; i++) {
	            v0[i] = i;
	        }
	        
	        for (int i = 0; i < target1.length(); i++) {
	            // calculate v1 (current row distances) from the previous row v0
	            // first element of v1 is A[i+1][0]
	            //   edit distance is delete (i+1) chars from s to match empty t
	            v1[0] = i + 1;

	            // use formula to fill in the rest of the row
	            for (int j = 0; j < target2.length(); j++) {
	                int cost = (target1.charAt(i) == target2.charAt(j)) ? 0 : 1;
	                v1[j + 1] = Math.min(
	                        v1[j] + 1,              // Cost of insertion
	                        Math.min(
	                                v0[j + 1] + 1,  // Cost of remove
	                                v0[j] + cost)); // Cost of substitution
	            }
	            
	            // copy v1 (current row) to v0 (previous row) for next iteration
	            //System.arraycopy(v1, 0, v0, 0, v0.length);
	            
	            // Flip references to current and previous row
	            vtemp = v0;
	            v0 = v1;
	            v1 = vtemp;
	                
	        }

	        return v0[target2.length()];
	    }

    }

	private double similarityCoefficient = DEFAULT_SIMILARITY_COEFFICIENT;
	protected String delegate = null, compareTarget = null;
	
	protected Levenshtein(String delegate) {
		this.delegate = checkNotNull(delegate);
	}
	
	protected abstract double doDistance();
    protected abstract double doSimilarity();
	
	/**
	 * Sets the similarity coefficient
	 * 
	 * @param similarityCoefficient
	 * @return
	 */
	public Levenshtein setSimilarityCoefficient(double similarityCoefficient) {
		this.similarityCoefficient = similarityCoefficient;
		return this;
	}
	
	/**
	 * Gets the similarity coefficient
	 * @return
	 */
	public double getSimilarityCoefficient() {
		return this.similarityCoefficient;
	}

	/**
     * Sets the compare target string
     * 
     * @param compareTarget
     * @return
     */
    protected Levenshtein update(String compareTarget) {
    	this.compareTarget = compareTarget;
    	return this;
    }
    
}
