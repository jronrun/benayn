/**
 * 
 */
package com.benayn.ustyle.string;

import static com.benayn.ustyle.string.Strs.EMPTY;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.benayn.ustyle.Decisional;
import com.benayn.ustyle.Gather;
import com.benayn.ustyle.Mapper;
import com.benayn.ustyle.Pair;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Doubles;

/**
 * https://github.com/jronrun/benayn
 */
public final class Betner extends AbstrStrMatcher<Betner> {
	
	/**
	 * Returns a new {@link Betner} instance with given string
	 * 
	 * @param target
	 * @return
	 */
	public static Betner of(String target) {
		return new Betner().update(target);
	}
	
	/**
	 * Reset the range from two instances of the same String, Adjacent tag matches.
	 * 
	 * @param leftSameWithRight
	 * @return
	 */
	public Betner betweenNext(String leftSameWithRight) {
		return inrange(leftSameWithRight, leftSameWithRight, 'N');
	}
			
	/**
	 * Reset the given target String as delegate String
	 * 
	 * @param target
	 * @return
	 */
	public Betner reset(String target) {
		this.rebuild = !target4Betn.equals(target);
		this.target4Betn = target;
		return this;
	}

	/**
	 * Reset the range from two instances of the same String, The first and last one matches.
	 * 
	 * @param leftSameWithRight
	 * @return
	 */
	public Betner betweenLast(String leftSameWithRight) {
		return inrange(leftSameWithRight, leftSameWithRight, 'E');
	}
	
	/**
	 * Reset the range from the given left and right
	 * 
	 * @param left
	 * @param right
	 * @return
	 */
	public Betner between(String left, String right) {
		return inrange(left, right, 'N');
	}
	
	/**
	 * Reset the range from the given right
	 * 
	 * @param right
	 * @return
	 */
	public Betner before(String right) {
		return inrange(right, right, 'R');
	}
	
	/**
	 * Reset the range from the given left
	 * 
	 * @param left
	 * @return
	 */
	public Betner after(String left) {
		return inrange(left, left, 'L');
	}
	
	/**
	 * Set whether the results contain the left and right borders
	 * 
	 * @param leftInclusive
	 * @param rightInclusive
	 * @return
	 */
	public Betner inclusive(boolean leftInclusive, boolean rightInclusive) {
		inclusive[0] = leftInclusive;
		inclusive[1] = rightInclusive;
		initializePosition();
		return this;
	}
	
	/**
	 * Set whether the results does not contain the left and right borders
	 * 
	 * @return
	 */
	public Betner inclusReset() {
		return inclusive(false, false);
	}
	
	/**
	 * Set whether the results contain the left and right borders
	 * 
	 * @return
	 */
	public Betner inclusBoth() {
		return inclusive(true, true);
	}
	
	/**
	 * Set whether the results contain the left borders
	 * 
	 * @return
	 */
	public Betner inclusL() {
		return inclusive(true, inclusive[1]);
	}
	
	/**
	 * Set whether the results contain the right borders
	 * 
	 * @return
	 */
	public Betner inclusR() {
		return inclusive(inclusive[0], true);
	}
	
	protected Betner inclusive(boolean[] inclusives) {
		inclusive(inclusives[0], inclusives[1]);
		return this;
	}
	
	/**
	 * Returns the number of matching string found in delegate string
	 * 
	 * @return
	 */
	public int countMatches() {
		if (null != this.positions && this.positions.isPresent()) {
			return this.positions.get().size();
		}
		
		if (Strs.isEmpty(target4Betn)) {
			return 0;
		}
		
		if (!region.isPresent()) {
			return 0;
		}
		
		if (isEqual(region.get()[0], region.get()[1])) {
			int matchCount = count(target4Betn, region.get()[1]);
			
			if (this.matchWay.get() == 'R' 
					|| this.matchWay.get() == 'L') {
				return matchCount;
			}
			
			matchCount = (matchCount % 2 == 0) ? matchCount : (matchCount - 1);
			return matchCount / 2;
		}
		
		return Math.min(count(target4Betn, region.get()[0]), count(target4Betn, region.get()[1]));
	}

	/**
	 * Returns the substring in given range but only the first match is returned.
	 * 
	 * @return
	 */
	public String first() {
		return result(1);
	}
	
	/**
	 * Returns the substring in given range but only the last match is returned.
	 * 
	 * @return
	 */
	public String last() {
		return result(countMatches());
	}
	
	/**
	 * Returns the substring in given range with given left position number
	 * 
	 * @param positionNum
	 * @return
	 */
	public String result(int positionNum) {
		Pair<Integer, Integer> kv = null;
		if (null != (kv = position(positionNum, false))) {
			return doSubstring(kv);
		}
		
		return EMPTY;
	}
	
	protected Pair<Integer, Integer> position(int positionNum, boolean fixInclus) {
		int c = countMatches();
		positionNum = positionNum < 0 ? (positionNum + c + 1) : positionNum;
		initializePosition();
		if (positionNum > c) {
			return null;
		}
		
		if (fixInclus) {
			return renderInclus(this.positions.get().get(positionNum));
		}
		
		return this.positions.get().get(positionNum);
	}
	
	protected boolean isInclusL() {
		return inclusive[0];
	}
	
	protected boolean isInclusR() {
		return inclusive[1];
	}
	
	/**
	 * Returns the substring results in given range as a map gather, left position number as key, substring as value
	 * 
	 * @return
	 */
	public Mapper<Integer, String> asMapper() {
		return Mapper.from(asMap());
	}
	
	/**
	 * Returns the substring results in given range as a map, left position number as key, substring as value
	 * 
	 * @return
	 */
	public Map<Integer, String> asMap() {
		initializePosition();
		final Map<Integer, String> results = Maps.newHashMap();
		
		Mapper.from(positions.get()).entryLoop(new Decisional<Map.Entry<Integer, Pair<Integer,Integer>>>() {

			@Override protected void decision(Entry<Integer, Pair<Integer, Integer>> input) {
				results.put(input.getKey(), doSubstring(input.getValue()));
			}
		});
		
		return results;
	}
	
	/**
	 * Returns the relationship in given range with given left position number, zero as root node position number.
	 * 
	 * @param positionNum
	 * @return
	 */
	public List<String> relation(int positionNum) {
		initializeRelationship();
		return this.subordinates.get().get(positionNum);
	}
	
	/**
	 * Returns the relationship map in given range, left position number as key, relationship as value
	 * 
	 * @return
	 */
	public Map<Integer, List<String>> relation() {
		initializeRelationship();
		return this.subordinates.get();
	}
	
	/**
	 * Returns the substring in given left position and right position when the left right tag is asymmetric
	 * 
	 * @param leftPos
	 * @param rightPos
	 * @return
	 */
	public String asymmetric(int leftPos, int rightPos) {
		boolean lm = false, rm = false, isSameLAR = isEqual(this.region.get()[0], this.region.get()[1]);
		int l = 0, r = 0, lr = 0, lp = 0, rp = 0;

		for (Pair<Integer, Integer> kv : indexes.get()) {
			if (isSameLAR) {
				++lr;
				if (lr == leftPos) { lp = kv.getR(); lm = true; }
				if (lr == rightPos) { rp = kv.getR(); rm = true; }
			} else {
				switch (kv.getL().intValue()) {
				case 0: ++l; if (l == leftPos) { lp = kv.getR(); lm = true; } break;
				case 1: ++r; if (r == rightPos) { rp = kv.getR(); rm = true; } break;
				}
			}
			
			if (lm && rm) { break; }
		}

		if (!(lm && rm)) {
			return EMPTY;
		}
		
		return this.doSubstring(Pair.of(lp, rp));
	}
	
	/**
	 * Reset the range from the given left and right with same tag match way
	 * 'N': NEXT, 'E': LAST, 'R': NOLEFT, 'L': NORIGHT
	 * 
	 * @param left
	 * @param right
	 * @param sameTagMatch
	 * @return
	 */
	protected Betner between(String left, String right, Character sameTagMatch) {
		return inrange(left, right, sameTagMatch);
	}
	
	/**
	 * 
	 */
	private void positionBuild(final List<Pair<Integer, Integer>> idxs) {
		if (matchWay.isPresent() && 'E' == matchWay.get()) {
			positionBuildSame(idxs);
			return;
		}
		
		if (matchWay.isPresent() && ('R' == matchWay.get() || 'L' == matchWay.get())) {
			positionBuildNoLeftOrRight(idxs);
			return;
		}
		
		positionBuildDifferent(idxs);
	}
	
	/**
	 * @param idxs
	 */
	private void positionBuildNoLeftOrRight(List<Pair<Integer, Integer>> idxs) {
		// <left position, right position>
		Map<Integer, Pair<Integer, Integer>> poses = Maps.newHashMap();
		int matcheCount = countMatches();
		int delegateLen = target4Betn.length();
		
		for (int i = 1; i <= matcheCount; i++) {
			switch (this.matchWay.get()) {
			case 'R': poses.put(i, Pair.of(0, idxs.get(i - 1).getR())); break;
			case 'L': poses.put(i, Pair.of(idxs.get(i - 1).getR(), delegateLen)); break;
			default: break;
			}
		}
		
		this.positions = Optional.of(poses);
	}

	/**
	 * 
	 */
	private void positionBuildSame(List<Pair<Integer, Integer>> idxs) {
		// <left position, right position>
		Map<Integer, Pair<Integer, Integer>> poses = Maps.newHashMap();
		int matcheCount = countMatches();
		int matches = idxs.size();
				
		for (int i = 1; i <= matcheCount; i++) {
			//first, last pair
			poses.put(i, Pair.of(idxs.get(i - 1).getR(), idxs.get(--matches).getR()));
		}
		
		this.positions = Optional.of(poses);
	}

	/**
	 * 
	 */
	private void positionBuildDifferent(final List<Pair<Integer, Integer>> idxs) {
		// <left position, right position>
		final Map<Integer, Pair<Integer, Integer>> poses = Maps.newHashMap();

		// Match by left position
		Gather.from(idxs).loop(new Decisional<Pair<Integer,Integer>>() {
			
			int leftNo = 0;

			@Override protected void decision(Pair<Integer, Integer> input) {
				// If left
				if (0 == input.getL().intValue()) {
					int mark = 0;
					int startPos = input.getR();

					for (Pair<Integer, Integer> idx : idxs) {
						// Start mark after startPos
						if (idx.getR() < startPos) {
							continue;
						}

						switch (idx.getL().intValue()) {
							// If left
							case 0: ++mark; break;
							// If right
							case 1: --mark; break;
						}

						if (0 == mark) {
							poses.put(++leftNo, Pair.of(startPos, idx.getR()));
							break;
						}
					}
				}
			}
		});
		
		this.positions = Optional.of(poses);
	}
	
	/**
	 * 
	 */
	private int count(String str, String sub) {
		int count = 0, idx = 0;
		while ((idx = str.indexOf(sub, idx)) != -1) {
			count++; idx += sub.length();
		}
		return count;
	}
	
	/**
	 * 
	 */
	private void initializePosition() {
		if (this.region.isPresent() && !this.positions.isPresent()) {
			positionBuild(this.indexes.get());
			subordinates = Optional.absent();
		}
	}
	
	/**
	 * 
	 */
	private void initializeRelationship() {
		initializePosition();
		if (!this.subordinates.isPresent()) {
			relationshipBuild();
		}
	}
	
	/**
	 * 
	 */
	private void relationshipBuild() {
		final Map<Integer, List<String>> subordinates = Maps.newHashMap();
		
		Mapper.from(positions.get()).entryLoop(
				new Decisional<Map.Entry<Integer,Pair<Integer,Integer>>>() {

			@Override protected void decision(Entry<Integer, Pair<Integer, Integer>> input) {
				Pair<Integer, Integer> target = input.getValue();
				Integer belongToThis = 0;
				
				// {1 {1.1 {1.1.1} {1.1.2}} {1.2}} {2}
				for (Entry<Integer, Pair<Integer, Integer>> position : positions.get().entrySet()) {
					Pair<Integer, Integer> pos = position.getValue();
					
					// target left > loop left && target right < loop right
					if (target.getL().intValue() > pos.getL().intValue()
							&& target.getR().intValue() < pos.getR().intValue()) {
						belongToThis = Math.max(belongToThis, position.getKey());
					}
				}
				
				List<String> subordinate = subordinates.get(belongToThis);
				if (null == subordinate) {
					subordinate = Lists.newLinkedList();
				}
						
				subordinate.add(doSubstring(target));
				subordinates.put(belongToThis, subordinate);
			}
		});
		
		
		this.subordinates = Optional.of(subordinates);
	}

	/**
	 * 
	 */
	private void indexBuild(String left, String right, Character sameTagMatch) {
		//<left or right, index pos>
		List<Pair<Integer, Integer>> idxs = Lists.newArrayList();
		this.region = Optional.of(new String[]{checkNotNull(left), checkNotNull(right)});
		this.positions = Optional.absent();
		this.matchWay = Optional.of(sameTagMatch);

		// Mark left and right occur position
		//left and right is same
		if (isEqual(left, right)) {
			switch (sameTagMatch) {
			case 'N':
				int nextIdx = 0, nextNo = 0;
				while ((nextIdx = target4Betn.indexOf(region.get()[0], nextIdx)) != -1) {
					idxs.add(Pair.of(nextNo % 2, nextIdx));
					nextIdx += region.get()[0].length();
					nextNo++;
				}
				break;

			case 'E':
				int lastIdx = 0;
				while ((lastIdx = target4Betn.indexOf(region.get()[0], lastIdx)) != -1) {
					idxs.add(Pair.of(0, lastIdx));
					lastIdx += region.get()[0].length();
				}
				break;
				
			case 'R':
			case 'L':
				int noleftIdx = 0;
				int leftOrRight = sameTagMatch == 'R' ? 1 : 0;
				while ((noleftIdx = target4Betn.indexOf(region.get()[leftOrRight], noleftIdx)) != -1) {
					idxs.add(Pair.of(leftOrRight, noleftIdx));
					noleftIdx += region.get()[leftOrRight].length();
				}
				break;
			}
		}
		//left and right are different
		else {
			matchWay = Optional.absent();
			for (int i = 0; i < region.get().length; i++) {
				int idx = 0;
				while ((idx = target4Betn.indexOf(region.get()[i], idx)) != -1) {
					idxs.add(Pair.of(i, idx));
					idx += region.get()[i].length();
				}
			}
		}

		// Sort by position
		this.indexes = Optional.of(Gather.from(idxs).orderList(new Ordering<Pair<Integer, Integer>>() {
			public int compare(Pair<Integer, Integer> left, Pair<Integer, Integer> right) {
				return Doubles.compare(left.getR(), right.getR());
			}
		}));
	}
	
	@Override protected void updateHandle() {
		this.target4Betn = delegate.get();
	}

	private Optional<String[]> region = Optional.absent();
	private boolean[] inclusive = new boolean[] {false, false};
	
	private Optional<List<Pair<Integer, Integer>>> indexes = null;
	private Optional<Map<Integer, Pair<Integer, Integer>>> positions = null;
	
	//'N': NEXT, 'E': LAST, 'R': NOLEFT, 'L': NORIGHT
	private Optional<Character> matchWay = Optional.absent();
	private Optional<Map<Integer, List<String>>> subordinates = Optional.absent();
	private String target4Betn = null;
	private boolean rebuild = false;
	
	/**
	 * 
	 */
	private Betner inrange(String left, String right, Character sameTagMatch) {
		if (this.region.isPresent() 
				&& !rebuild
				&& isEqual(this.region.get()[0], left) 
				&& isEqual(this.region.get()[1], right)
				&& (!this.matchWay.isPresent() || (this.matchWay.isPresent() && this.matchWay.get() == sameTagMatch))) {
			return this;
		}
		
		indexBuild(left, right, sameTagMatch);
		return this;
	}
	
	private String doSubstring(Pair<Integer, Integer> pos) {
		Pair<Integer, Integer> pair = renderInclus(pos);
		return target4Betn.substring(pair.getL(), pair.getR());
	}
	
	private Pair<Integer, Integer> renderInclus(Pair<Integer, Integer> pos) {
		boolean isBefore = this.matchWay.isPresent() && ('R' == this.matchWay.get());
		boolean isAfter = this.matchWay.isPresent() && ('L' == this.matchWay.get());
		if (isBefore) {
			inclusive[0] = false;
		}
		if (isAfter) {
			inclusive[1] = false;
		}
		return Pair.of(
				(inclusive[0] || isBefore) ? pos.getL() : (pos.getL() + region.get()[0].length()),
				(inclusive[1] ? (pos.getR() + region.get()[1].length()) : pos.getR()));
	}

	/* (non-Javadoc)
	 * @see com.funcity.me.string.AbstrStrMatcher#THIS()
	 */
	@Override protected Betner THIS() {
		return this;
	}

}
