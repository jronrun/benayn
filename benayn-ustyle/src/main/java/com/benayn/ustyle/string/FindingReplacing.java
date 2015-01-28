/**
 * 
 */
package com.benayn.ustyle.string;

import static com.benayn.ustyle.string.Strs.EMPTY;
import static com.benayn.ustyle.string.Strs.INDEX_NONE_EXISTS;
import static com.benayn.ustyle.string.Strs.WHITE_SPACE;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.Map;

import com.benayn.ustyle.Comparer;
import com.benayn.ustyle.Decisional;
import com.benayn.ustyle.Gather;
import com.benayn.ustyle.Objects2;
import com.benayn.ustyle.Pair;
import com.benayn.ustyle.multipos.AsymmMultiPos;
import com.benayn.ustyle.multipos.InclusMultiPos;
import com.benayn.ustyle.multipos.NegateMultiPos;
import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 *
 */
public abstract class FindingReplacing<S> extends AbstrStrMatcher<S> {
	
	/**
	 * 
	 */
	//C: Previous look result as next operands string, all operate as one search string mode
	//S: Previous replaced result as next operate string mode
	protected Character searchMode = 'S';
	
	//<type(I:index, 'S':substring, 'B':before, 'A':after, 'N':betweenNext, 'L':betweenLast, 'K':lookup), <left, right>>
	protected List<Object[]> coordinates = null;
	
	{
		reset();
	}
	
	/**
	 * Sets the substring in given left index and right index as the search string
	 * <B>May multiple substring matched.</B>
	 * 
	 * @see Indexer#between(int, int)
	 * @param leftIndex
	 * @param rightIndex
	 * @return
	 */
	public S betns(int leftIndex, int rightIndex) {
		return betn(leftIndex, rightIndex).late();
	}
	
	/**
	 * Sets the substring in given left index and right index as the search string
	 * 
	 * @see Indexer#between(int, int)
	 * @param leftIndex
	 * @param rightIndex
	 * @return
	 */
	public NegateMultiPos<S, Integer, Integer> betn(int leftIndex, int rightIndex) {
		return new NegateMultiPos<S, Integer, Integer>(leftIndex, rightIndex) {

			@Override protected S result() {
				return aQueue('I', left, right, pos, position, null, plusminus, filltgt);
			}
		};
	}
	
	/**
	 * Sets the substring in the beginning and given right index as the search string
	 * <B>May multiple substring matched.</B>
	 * 
	 * @see Indexer#before(int)
	 * @param rightIndex
	 * @return
	 */
	public S befores(int rightIndex) {
		return before(rightIndex).late();
	}
	
	/**
	 * Sets the substring in the beginning and given right index as the search string
	 * 
	 * @see Indexer#before(int)
	 * @param rightIndex
	 * @return
	 */
	public NegateMultiPos<S, Integer, Integer> before(int rightIndex) {
		return new NegateMultiPos<S, Integer, Integer>(0, rightIndex) {

			@Override protected S result() {
				return aQueue('J', left, right, pos, position, null, plusminus, filltgt);
			}
		};
	}
	
	/**
	 * Sets the substring in given left index and the ending as the search string
	 * <B>May multiple substring matched.</B>
	 * 
	 * @see Indexer#after(int)
	 * @param leftIndex
	 * @return
	 */
	public S afters(int leftIndex) {
		return after(leftIndex).late();
	}
	
	/**
	 * Sets the substring in given left index and the ending as the search string
	 * 
	 * @see Indexer#after(int)
	 * @param rightIndex
	 * @return
	 */
	public NegateMultiPos<S, Integer, Character> after(int leftIndex) {
		return new NegateMultiPos<S, Integer, Character>(leftIndex, null) {

			@Override protected S result() {
				return aQueue('K', left, right, pos, position, null, plusminus, filltgt);
			}
		};
	}
	
	/**
	 * Sets the substrings in given same left tag and right tag as the search string, Adjacent tag matches
	 * <B>May multiple substring matched.</B>
	 * 
	 * @see Betner#betweenNext(String)
	 * @param leftSameWithRight
	 * @return
	 */
	public S betnNexts(String leftSameWithRight) {
		return betnNext(leftSameWithRight).late();
	}
	
	/**
	 * Sets the substrings in given same left tag and right tag as the search string, Adjacent tag matches
	 * 
	 * @see Betner#betweenNext(String)
	 * @param leftSameWithRight
	 * @return
	 */
	public AsymmMultiPos<S, String, String> betnNext(String leftSameWithRight) {
		return new AsymmMultiPos<S, String, String>(leftSameWithRight, null) {

			@Override protected S result() {
				if (Strs.isEmpty(asymmLR)) {
					return aQueue('N', left, right, pos, position, inclusive, plusminus, filltgt);
				} else { return aQueue('Z', left, left, pos, asymmLR, inclusive, plusminus, filltgt); }
			}
		};
	}
	
	/**
	 * Sets the substrings in given same left tag and right tag as the search string, 
	 * The first and last one matches. <B>May multiple substring matched.</B>
	 * 
	 * @see Betner#betweenLast(String)
	 * @param leftSameWithRight
	 * @return
	 */
	public S betnLasts(String leftSameWithRight) {
		return betnLast(leftSameWithRight).late();
	}

	/**
	 * Sets the substrings in given same left tag and right tag as the search string
	 * The first and last one matches.
	 * 
	 * @see Betner#betweenLast(String)
	 * @param leftSameWithRight
	 * @return
	 */
	public AsymmMultiPos<S, String, String> betnLast(String leftSameWithRight) {
		return new AsymmMultiPos<S, String, String>(leftSameWithRight, null) {

			@Override protected S result() {
				if (Strs.isEmpty(asymmLR)) {
					return aQueue('L', left, right, pos, position, inclusive, plusminus, filltgt);
				} else { return aQueue('Z', left, left, pos, asymmLR, inclusive, plusminus, filltgt); }
			}
		};
	}
	
	/**
	 * Sets the substrings in given left tag and right tag as the search string
	 * <B>May multiple substring matched.</B>
	 * 
	 * @see Betner#between(String, String)
	 * @param leftSameWithRight
	 * @return
	 */
	public S betns(String left, String right) {
		return betn(left, right).late();
	}
	
	/**
	 * Sets the substrings in given left tag and right tag as the search string
	 * 
	 * @see Betner#between(String, String)
	 * @param leftSameWithRight
	 * @return
	 */
	public AsymmMultiPos<S, String, String> betn(String left, String right) {
		return new AsymmMultiPos<S, String, String>(left, right) {

			@Override protected S result() {
				if (Strs.isEmpty(asymmLR)) {
					return aQueue('S', left, right, pos, position, inclusive, plusminus, filltgt);
				} else { return aQueue('Z', left, right, pos, asymmLR, inclusive, plusminus, filltgt); }
			}
		};
	}
	
	/**
	 * Sets the substrings in given left tag and ending as the search string
	 * <B>May multiple substring matched.</B>
	 * 
	 * @see Betner#before(String)
	 * @param right
	 * @return
	 */
	public S befores(String right) {
		return before(right).late();
	}
	
	/**
	 * Sets the substrings in given left tag and ending as the search string
	 * 
	 * @see Betner#before(String)
	 * @param right
	 * @return
	 */
	public InclusMultiPos<S, String, String> before(String right) {
		return new InclusMultiPos<S, String, String>(right, null) {

			@Override protected S result() {
				return aQueue('B', left, right, pos, position, inclusive, plusminus, filltgt);
			}
		};
	}
	
	/**
	 * Sets the substrings in the beginning and given right tag as the search string
	 * <B>May multiple substring matched.</B>
	 * 
	 * @see Betner#after(String)
	 * @param left
	 * @return
	 */
	public S afters(String left) {
		return after(left).late();
	}
	
	/**
	 * Sets the substrings in the beginning and given right tag as the search string
	 * 
	 * @see Betner#after(String)
	 * @param left
	 * @return
	 */
	public InclusMultiPos<S, String, String> after(String left) {
		return new InclusMultiPos<S, String, String>(left, null) {

			@Override protected S result() {
				return aQueue('A', left, right, pos, position, inclusive, plusminus, filltgt);
			}
		};
	}
	
	/**
	 * Sets the given lookups string as the search string
	 * <B>May multiple substring matched.</B>
	 * 
	 * @param left
	 * @return
	 */
	public S lookups(String... lookups) {
		for (String lookup : checkNotNull(lookups)) { lookup(lookup).late(); }
		return THIS();
	}
	
	/**
	 * Sets the given lookups string as the search string
	 * 
	 * @param left
	 * @return
	 */
	public NegateMultiPos<S, String, String> lookup(String lookups) {
		return new NegateMultiPos<S, String, String>(lookups, null) {
			
			@Override protected S result() {
				return aQueue('U', left, right, pos, position, null, plusminus, filltgt);
			}
		};
	}
	
	/**
	 * Sets the substring in given left index and right index as the delegate string
	 * 
	 * @param leftIndex
	 * @param rightIndex
	 * @return
	 */
	public S setBetn(int leftIndex, int rightIndex) {
		return set(Indexer.of(delegate.get()).between(leftIndex, rightIndex));
	}
	
	/**
	 * Sets the substring in given left index and right index as the delegate string
	 * <p><b>The look result same as {@link StrMatcher#finder()}'s behavior</b>
	 * 
	 * @see StrMatcher#finder()
	 * @param leftIndex
	 * @param rightIndex
	 * @return
	 */
	public NegateMultiPos<S, Integer, Integer> setBetns(int leftIndex, int rightIndex) {
		return new NegateMultiPos<S, Integer, Integer>(leftIndex, rightIndex) {

			@Override protected S result() {
				return delegateQueue('I', left, right, pos, position, null, plusminus, filltgt);
			}
		};
	}
	
	/**
	 * Sets the substring in beginning and the given right index as the delegate string
	 * 
	 * @param rightIndex
	 * @return
	 */
	public S setBefore(int rightIndex) {
		return set(Indexer.of(delegate.get()).before(rightIndex));
	}
	
	/**
	 * Sets the substring in beginning and the given right index as the delegate string
	 * <p><b>The look result same as {@link StrMatcher#finder()}'s behavior</b>
	 * 
	 * @see StrMatcher#finder()
	 * @param rightIndex
	 * @return
	 */
	public NegateMultiPos<S, Integer, Integer> setBefores(int rightIndex) {
		return new NegateMultiPos<S, Integer, Integer>(0, rightIndex) {

			@Override protected S result() {
				return delegateQueue('J', left, right, pos, position, null, plusminus, filltgt);
			}
		};
	}
	
	/**
	 * Sets the substring in given left index and ending as the delegate string
	 * 
	 * @param leftIndex
	 * @return
	 */
	public S setAfter(int leftIndex) {
		return set(Indexer.of(delegate.get()).after(leftIndex));
	}
	
	/**
	 * Sets the substring in given left index and ending as the delegate string
	 * <p><b>The look result same as {@link StrMatcher#finder()}'s behavior</b>
	 * 
	 * @see StrMatcher#finder()
	 * @param leftIndex
	 * @return
	 */
	public NegateMultiPos<S, Integer, Integer> setAfters(int leftIndex) {
		return new NegateMultiPos<S, Integer, Integer>(leftIndex, null) {

			@Override protected S result() {
				return delegateQueue('K', left, right, pos, position, null, plusminus, filltgt);
			}
		};
	}
	
	/**
	 * Sets the specified position substring before given right tag as the delegate string
	 * <p><b>The look result same as {@link StrMatcher#finder()}'s behavior</b>
	 * 
	 * @see StrMatcher#finder()
	 * @param left
	 * @param right
	 * @return
	 */
	public InclusMultiPos<S, String, String> setBefore(String right) {
		return new InclusMultiPos<S, String, String>(right, right) {
			
			@Override protected S result() {
				return delegateQueue('B', left, right, pos, position, inclusive, plusminus, filltgt);
			}
		};
	}
	
	/**
	 * Sets the specified position substring after given left tag as the delegate string
	 * <p><b>The look result same as {@link StrMatcher#finder()}'s behavior</b>
	 * 
	 * @see StrMatcher#finder()
	 * @param left
	 * @param right
	 * @return
	 */
	public InclusMultiPos<S, String, String> setAfter(String left) {
		return new InclusMultiPos<S, String, String>(left, left) {
			
			@Override protected S result() {
				return delegateQueue('A', left, right, pos, position, inclusive, plusminus, filltgt);
			}
		};
	}

	/**
	 * Sets the specified position substring in given left tag and right tag as the delegate string
	 * <p><b>The look result same as {@link StrMatcher#finder()}'s behavior</b>
	 * 
	 * @see StrMatcher#finder()
	 * @param left
	 * @param right
	 * @return
	 */
	public AsymmMultiPos<S, String, String> setBetn(String left, String right) {
		return new AsymmMultiPos<S, String, String>(left, right) {
			
			@Override protected S result() {
				if (Strs.isEmpty(asymmLR)) {
					return delegateQueue('S', left, right, pos, position, inclusive, plusminus, filltgt);
				} else { return delegateQueue('Z', left, right, pos, asymmLR, inclusive, plusminus, filltgt); }
			}
		};
	}
	
	/**
	 * Sets the substrings in given same left tag and right tag as the result string, Adjacent tag matches
	 * <p><b>The look result same as {@link StrMatcher#finder()}'s behavior</b>
	 * 
	 * @see StrMatcher#finder()
	 * @param leftSameWithRight
	 * @return
	 */
	public AsymmMultiPos<S, String, String> setBetnNext(String leftSameWithRight) {
		return new AsymmMultiPos<S, String, String>(leftSameWithRight, leftSameWithRight) {
			
			@Override protected S result() {
				if (Strs.isEmpty(asymmLR)) {
					return delegateQueue('N', left, right, pos, position, inclusive, plusminus, filltgt);
				} else { return delegateQueue('Z', left, left, pos, asymmLR, inclusive, plusminus, filltgt); }
			}
		};
	}
	
	/**
	 * Sets the substrings in given same left tag and right tag as the result string, 
	 * The first and last one matches.
	 * <p><b>The look result same as {@link StrMatcher#finder()}'s behavior</b>
	 * 
	 * @see StrMatcher#finder()
	 * @param leftSameWithRight
	 * @return
	 */
	public AsymmMultiPos<S, String, String> setBetnLast(String leftSameWithRight) {
		return new AsymmMultiPos<S, String, String>(leftSameWithRight, leftSameWithRight) {
			
			@Override protected S result() {
				if (Strs.isEmpty(asymmLR)) {
					return delegateQueue('L', left, right, pos, position, inclusive, plusminus, filltgt);
				} else { return delegateQueue('Z', left, left, pos, asymmLR, inclusive, plusminus, filltgt); }
			}
		};
	}

	/**
	 * Sets the given string as the delegate string
	 * 
	 * @param target
	 * @return
	 */
	public S set(String target) {
		operands = Optional.of(target);
		return THIS();
	}
	
	/**
	 * 
	 */
	protected String findingReplacing(String rRepmnt, char rMode, char rPosVar, Integer rPos) {
		String result = delegate.get();
		if (operands.isPresent()) { result = operands.get(); operands = Optional.absent(); }
		if (null == rRepmnt) { reset(); return result; } if ('X' == rPosVar) { rPosVar = 'F'; rPos = 1; }
		FindingReplacingDecision replaceDecision = new FindingReplacingDecision(result, rRepmnt, rMode, rPos, rPosVar);
		Gather.from(coordinates).loop(replaceDecision); reset();
		return replaceDecision.result();
	}

	/**
	 * 
	 */
	class FindingReplacingDecision extends Decisional<Object[]> {

		boolean finding, replacing, isRep2Find, isCalculated;
		Betner betn; Integer rPos; FarAction act;
		//rMode L: lookup, S: replace string, C: replace character
		//rPosVar F: first position, L: last position, A: all positions, C: specified position
		Character rMode, rPosVar; String rStr, result, intlResult, rRepmnt;
		
		public FindingReplacingDecision(String rResult, String rRepmnt, char rMode, Integer rPos, char rPosVar) {
			this.result = rResult; this.intlResult = rResult; this.rRepmnt = rRepmnt; this.rMode = rMode;
			this.rPos = rPos; this.rPosVar = rPosVar; this.betn = Betner.of(rResult);
			this.finding = Comparer.expect(rMode, 'L'); this.replacing = Comparer.expect(rMode, 'S', 'C');
			if (this.replacing && 'C' == searchMode) { 
				allAsOne = true; replacing = false; finding = true; isRep2Find = true;
			} if (log.isDebugEnabled()) { log.debug(toString()); }
		}

		public String result() {
			if (isRep2Find && !isCalculated) { asResult(replace(intlResult, result, fillRep(result.length()))); }
			return result;
		}
		
		@Override protected void decision(Object[] input) {
			switch ((act = asAction(input)).action) {
			case 'I':
			case 'J':
			case 'K': asLook(idxer.resetMode().quietly().reset(result).between(act.idxL, act.idxR)); break;
			case 'Z': asLook(betn.reset(result).between(act.strL, act.strR).asymmetric(act.asymmL, act.asymmR)); break;
			case 'U': asLook(act.strL); break;
				
			case 'S': betn.reset(result).between(act.strL, act.strR); break;
			case 'N': betn.reset(result).betweenNext(act.strL); break;
			case 'L': betn.reset(result).betweenLast(act.strL); break;
			case 'B': betn.reset(result).before(act.strL); break;
			case 'A': betn.reset(result).after(act.strL); break;
			} actionResult();
		}

		private void actionResult() {
			StringBuilder b = null;
			switch (act.forw) {
			case 'L':
				if (finding) {
					if (act.isAllPos) {
						if (isRep2Find) {
							if (act.areSames) { asResult(act.lookfor); }
							//else { /* using the first position */ act.forwIndexing(); asResult(str(act.idxForw.get(1))); }
							else { 
								if (act.is('A')) { asResult(str(minForwL(), result.length())); } 
								else if (act.is('B')) { asResult(str(0, maxForwR())); } 
								else if (act.is('L')) { act.forwIndexing(); asResult(str(act.idxForw.get(1))); }
								else {
									if (act.forwIndexing().size() <= 1) { asResult(str(act.idxForw.get(1))); }
									else { isCalculated = true; allAsOneNegateElse(false); }
								}
							}
						} else {
							act.forwIndexing(); b = new StringBuilder();
							for (int i = 1, ifl = act.idxForw.size(); i <= ifl; i++) {
								b.append(str(act.idxForw.get(i))); if (i < ifl) { b.append(separator.toString()); }
							} asResult(b);
						}
					} else {
						if (act.areSames) { asResult(act.lookfor);
						} else { act.forwIndexing(); asResult(str(act.idxForw.get(act.loc(1)))); }
					}
				} else if (replacing) {
					Pair<Integer, Integer> p, p2 = null;
					if (act.isAllPos) {
						if (act.is('A')) { strBuilderResultRep(minForwL(), result.length()); }
						else if (act.is('B')) { strBuilderResultRep(0, maxForwR()); }
						else if (act.is('L')) { act.forwIndexing(); strBuilderResultRep(act.idxForw.get(1)); }
						else {
							act.versaIndexing(); if (act.idxForw.size() == 1) { strBuilderResultRep(act.idxForw.get(1)); }
							else { b = new StringBuilder();
								for (int i = 1, ifl = act.idxVersa.size(), rl = result.length(); i <= ifl; i++) {
									p = act.idxVersa.get(i); if (i == 1 && p.getL() > 0) { b.append(fillRep(p.getL())); }
									b.append(str(p)); if (null != (p2 = act.idxVersa.get(i + 1))) { b.append(fillRep(p2.getL() - p.getR())); }
									if (i == ifl && p.getR() < rl) { b.append(fillRep(rl - p.getR())); }
								} asResult(b);
							}
						}
					} else { act.forwIndexing(); strBuilderResultRep(act.idxForw.get(act.loc(1))); }
				}
				break;
			case 'M':
			case 'N':
				if (finding) {
					act.forwIndexing(); b = new StringBuilder(); Pair<Integer, Integer> p;
					if (act.isAllPos) {
						if (allAsOne) {
							if ('S' == act.mode4Repmnt) { 
								if (act.is('A')) {
									asResult(new StringBuilder(str(0, minForwL())).append(act.filler).toString());
								}  else if (act.is('B')) {
									asResult(new StringBuilder(act.filler).append(str(maxForwR(), result.length())).toString());
								} else if (act.is('L')) {
									p = act.idxForw.get(1);
									asResult(new StringBuilder(str(0, p.getL())).append(act.filler).append(str(p.getR(), result.length())).toString());
								} else { allAsOneNegateElse(true); }
							} 
							else {
								for (int i = 1, ifl = act.idxForw.size(); i <= ifl; i++) {
									asResult(new StringBuilder(result).replace((p = act.idxForw.get(i)).getL(), p.getR(), fill(p)).toString());
								}
							}
						} else {
							for (int i = 1, ifl = act.idxForw.size(); i <= ifl; i++) {
								b.append(new StringBuilder(result).replace((p = act.idxForw.get(i)).getL(), p.getR(), fill(p)).toString());
								if (i < ifl) { b.append(separator.toString()); }
							} asResult(b);
						}
					} else {
						if (null != (p = act.idxForw.get(act.loc(1)))) {
							b.append(str(0, p.getL())).append(fill(p)).append(str(p.getR(), result.length())); asResult(b);
						}
					}
				} else if (replacing) {
					if (act.isAllPos) {
						if (act.is('A')) { strBuilderResultRep(0, minForwL()); }
						else if (act.is('B')) { strBuilderResultRep(maxForwR(), result.length()); }
						else if (act.is('L')) { negateResultRep(act.forwIndexing().size()); }
						else {
							act.forwIndexing(); Pair<Integer, Integer> p; b = new StringBuilder();
							for (int i = 1, ivl = act.idxForw.size(), rl = result.length(); i <= ivl; i++) {
								p = act.idxForw.get(i); if (i == 1 && p.getL() > 0) { b.append(fillRep(p.getL())); }
								b.append(str(p)); if (i == ivl && p.getR() < rl) { b.append(fillRep(rl - p.getR())); }
							} asResult(b);
						}
					} else { negateResultRep(act.loc(1)); }
				}
				break;
			}
		}
		
		private void negateResultRep(Integer pos) {
			Pair<Integer, Integer> p; StringBuilder b = new StringBuilder(); act.forwIndexing();
			if (null == (p = act.idxForw.get(pos == -1 ? act.idxForw.size() : pos))) { return; }
			if (act.is('J')) { b.append(str(0, p.getL())).append(fillRep(result.length() - p.getL())); }
			else if (act.is('K')) { b.append(fillRep(p.getR())).append(str(p.getR(), result.length())); }
			else { if (p.getL() > 0) { b.append(fillRep(p.getL())); } b.append(str(p));
			if (p.getR() < result.length()) { b.append(fillRep(result.length() - p.getR())); } } asResult(b);
		}

		private void strBuilderResultRep(Pair<Integer, Integer> p) { if (null != p) { strBuilderResultRep(p.getL(), p.getR()); } }
		private void strBuilderResultRep(int l, int r) {
			if (0 == l && 0 == r) { return; } asResult(new StringBuilder(result).replace(l, r, fillRep(r - l)).toString());
		}
		
		private Integer minForwL() {
			int min = result.length(); act.forwIndexing();
			for (Pair<Integer, Integer> pe : act.idxForw.values()) { min = Math.min(min, pe.getL()); }
			return min;
		}
		private Integer maxForwR() {
			int max = 0;  act.forwIndexing();
			for (Pair<Integer, Integer> pe : act.idxForw.values()) { max = Math.max(max, pe.getR()); }
			return max;
		}

		private void allAsOneNegateElse(boolean isAct) {
			boolean isChar = isRep2Find && 'C' == rMode; act.versaIndexing();
			StringBuilder b = new StringBuilder(); Pair<Integer, Integer> p, p2 = null;
			for (int i = 1, ifl = act.idxVersa.size(); i <= ifl; i++) {
				p = act.idxVersa.get(i); if (isChar) { p2 = act.idxVersa.get(i + 1); }
				if (i == 1 && p.getL() > 0) { allAsOneNegateElseFill(b, isAct, isChar, p, p2); }
				b.append(str(p)); if (i < ifl || (i == ifl && p.getR() < result.length())) {
					allAsOneNegateElseFill(b, isAct, isChar, p, p2); } } asResult(b);
		}
		
		private void allAsOneNegateElseFill(StringBuilder b, boolean isAct, 
				boolean isChar, Pair<Integer, Integer> p, Pair<Integer, Integer> p2) {
			if (isAct) { b.append(fill(p)); }
			else { if (isChar) { if (null != p2) { b.append(fillRep(p2.getL() - p.getR())); } }
			else { b.append(fillRep(p)); } }
		}

		//action I: betn index, J: before index, K: after index
		//		 N: betnNext string, L: betnLast string, S: betn string, B: before string, A: after string
		//		 Z: betn string asymmetric, U: search string
		private class FarAction {
			Object[] input; boolean isIdx, isStr, isAllPos, isLastPos, areSames;
			Integer idxL, idxR, pos, asymmL, asymmR; String strL, strR, filler, lookfor;
			Character action, /** forward or reverse */ forw, mode4Repmnt = 'S', posVar;
			Map<Integer, Pair<Integer, Integer>> idxForw, idxVersa;
			
			FarAction(Object[] input) {
				int iidx = -1; this.input = input; this.action = assist(++iidx);
				this.isIdx = is('I', 'J', 'K');
				this.isStr = is('N', 'L', 'S', 'Z', 'B', 'A', 'U');
				this.areSames = this.isIdx || is('Z', 'U');
				if (this.isIdx) { this.idxL = assist(++iidx); this.idxR = assist(++iidx);
					if (null == this.idxR) { this.idxR = result.length(); }
				} if (this.isStr) { this.strL = assist(++iidx); this.strR = assist(++iidx); }
				this.posVar = assist(++iidx); if ('X' == this.posVar) { this.pos = rPos; this.posVar = rPosVar; }
				this.isAllPos = 'A' == this.posVar; this.isLastPos = 'L' == this.posVar;
				Object tmpo = assist(++iidx); if (is('Z')) { String[] lr = ((String) tmpo).split(WHITE_SPACE);
					this.asymmL = Integer.parseInt(lr[0]); this.asymmR = Integer.parseInt(lr[1]);
				} else if (null != tmpo) { this.pos = (Integer) tmpo; }
				if (null != (tmpo = assist(++iidx))) { betn.inclusive((boolean[]) tmpo); }
				this.forw = assist(++iidx); this.filler = assist(++iidx);
				if (null == this.forw) { this.forw = 'L'; }
				if ('M' == this.forw) { this.mode4Repmnt = 'C'; }
				if (log.isDebugEnabled()) { log.debug(toString()); }
			}
			
			//1 forward, 2 reverse
			Integer loc(int whichDirect) {
				if ('C' == this.posVar && this.pos < 0) {
					return (1 == whichDirect ? this.idxForw.size() : this.idxVersa.size()) + this.pos + 1;
				} return this.pos;
			}
			
			Map<Integer, Pair<Integer, Integer>> versaIndexing() {
				if (null != this.idxVersa) { return this.idxVersa; } forwIndexing();
				Pair<Integer, Integer> p1 = null, p2 = null;
				Map<Integer, Pair<Integer, Integer>> posesVersa = Maps.newHashMap();
				for (int l = this.idxForw.size(), rl = result.length(), no = 0, i = 1; i <= l; i++) {
					p1 = this.idxForw.get(i);
					if (is('B')) { posesVersa.put(++no, Pair.of(p1.getR(), result.length())); }
					else if (is('A')) { posesVersa.put(++no, Pair.of(0, p1.getL())); }
					else if (is('L')) {
						posesVersa.put(++no, Pair.of(0, p1.getL()));
						posesVersa.put(++no, Pair.of(p1.getR(), result.length()));
					} else { p2 = this.idxForw.get(i + 1);
						if (i == 1 && p1.getL() > 0) { posesVersa.put(++no, Pair.of(0, p1.getL())); }
						if (null != p1 && null != p2) { posesVersa.put(++no, Pair.of(p1.getR(), p2.getL())); }
						if (i == l && p1.getR() < rl) { posesVersa.put(++no, Pair.of(p1.getR(), rl)); }
					}
				} return (this.idxVersa = posesVersa);
			}
			
			Map<Integer, Pair<Integer, Integer>> forwIndexing() {
				if (null != this.idxForw) { return this.idxForw; }
				Map<Integer, Pair<Integer, Integer>> posesForw = Maps.newHashMap();
				if (Strs.isEmpty(result)) { return detectLastPos(posesForw); }
				if (this.areSames) {
					if (!Strs.isEmpty(this.lookfor)) { int theIdx = 0, count = 0, ll = this.lookfor.length();
				        while ((theIdx = result.indexOf(this.lookfor, theIdx)) != INDEX_NONE_EXISTS) {
				        	posesForw.put(++count, Pair.of(theIdx, ll + theIdx)); theIdx += ll;
				        }
					} return detectLastPos(posesForw);
				} int c = betn.countMatches();
				Pair<Integer, Integer> pair = null;
				for (int i = 1; i <= c; i++) { 
					if (null != (pair = betn.position(i, true))) { posesForw.put(i, pair); }
				} return detectLastPos(posesForw);
			}
			
			Map<Integer, Pair<Integer, Integer>> detectLastPos(Map<Integer, Pair<Integer, Integer>> m) {
				if (this.isLastPos) { this.pos = m.size() < 1 ? 1 : m.size(); } return (this.idxForw = m);
			}
			boolean is(Character... acts) { for (Character a : acts) { if (a == action) { return true; } } return false; }
			@SuppressWarnings("unchecked")
			private <T> T assist(int idx) { return (T) ((input.length >= (idx + 1)) ? input[idx] : null); }
			@Override public String toString() { return Objects2.TO_STRING.apply(this); }
		}
		
		private String replace(String target, String search, String replacement) {
			if (log.isDebugEnabled()) { log.debug(String.format("Replace '%s' with '%s' in '%s'", search, replacement, target)); }
			if (Strs.isEmpty(search) || Strs.isEmpty(target) || !target.contains(search)) { return target; }
			if ('A' == rPosVar) { return target.replace(search, replacement); }
			
			List<String> l = Lists.newArrayList(Splitter.on(search).split(target));
			int idx = 0, len = l.size();
			if (2 == len) { return target.replace(search, replacement); }
			
			StringBuilder builder = new StringBuilder(); Integer pos = null;
			switch (rPosVar) { case 'F': pos = 1; break; case 'L': pos = len - 1; break; case 'C': pos = rPos; break; }
			for (String s : l) { builder.append(s).append((pos == ++idx) ? replacement : (idx == len ? EMPTY : search)); }
			return builder.toString();
		}
		
		private String fill(int len) {
			switch (act.mode4Repmnt) {
			case 'S': return act.filler;
			case 'C': return Strings.repeat(act.filler, len);
			} return EMPTY;
		}
		private String fillRep(int len) {
			switch (rMode) {
			case 'S': return rRepmnt;
			case 'C': return Strings.repeat(rRepmnt, len);
			} return EMPTY;
		}
		
		@Override public String toString() { return Objects2.TO_STRING.apply(this); }
		private String fill(Pair<Integer, Integer> p) { if (null == p) { return EMPTY; } return fill(p.getR() - p.getL()); }
		private String fillRep(Pair<Integer, Integer> p) { if (null == p) { return EMPTY; } return fillRep(p.getR() - p.getL()); }
		private String asResult(String target) { return (result = target); }
		private String asResult(StringBuilder b) { if (b.length() > 0) { return asResult(b.toString()); } return b.toString(); }
		private String asLook(String target) { return (act.lookfor = target); }
		private FarAction asAction(Object[] target) { return new FarAction(target); }
		private String str(Pair<Integer, Integer> pair) { if (null == pair) { return EMPTY; } return str(pair.getL(), pair.getR()); }
		private String str(Integer l, Integer r) { return idxer.resetMode().reset(result).between(l, r); }
	}
	
	private S delegateQueue(Object... args) {
		Finder l = Finder.of(delegate.get());
		l.coordinates.add(args);
		l.separator = separator;
		l.allAsOne = allAsOne;
		operands = Optional.of(('Z' == (Character) args[0]) ? l.get() : 
			((null == args[4]) ? l.find().all() : l.find().pos((Integer) args[4])));
		return THIS();
	}
	
	protected void reset() {
		separator = ",";
		allAsOne = Boolean.TRUE;
		operands = Optional.absent();
		coordinates = Lists.newLinkedList();
	}
	
	@Override protected void updateHandle() {
		reset();
	}

	protected Object separator = null;
	protected Boolean allAsOne = null;
	protected Optional<String> operands = null;
	protected Indexer idxer = Indexer.of(EMPTY);
	
	private S aQueue(Object... coordinate) {
		//Character action, Object left, Object right, Character direct, Object position, boolean[] inclusive
		//Character plus-minus, String fillTaget
		coordinates.add(coordinate);
		return THIS();
	}

}
