package com.benayn.ustyle.multipos;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * https://github.com/jronrun/benayn
 */
public abstract class MultiPos<E, L, R> {
		
		protected L left = null;
		protected R right = null;
		protected Integer position = null;
		protected Character pos = 'X';
		
		protected MultiPos(L left, R right) {
			this.left = left;
			this.right = right;
		}
		
		/**
		 * The first result will be involved in the operation
		 * 
		 * @return
		 */
		public E first() {
			return pos(1, 'F');
		}
		
		/**
		 * The last result will be involved in the operation
		 * 
		 * @return
		 */
		public E last() {
			return pos(-1, 'L');
		}
		
		/**
		 * All of the results will be involved in the operation
		 * 
		 * @return
		 */
		public E all() {
			return pos(null, 'A');
		}
		
		/**
		 * Delay evaluation the position value.
		 * The specified position result that use the last operation specified position 
		 * 
		 * @return
		 */
		public E late() {
			return pos(null, 'X');
		}
		
		/**
		 * The specified position result will be involved in the operation
		 * 
		 * @param position
		 * @return
		 */
		public E pos(Integer position) {
			return pos(checkNotNull(position), 'C');
		}
		
		/**
		 * Returns the operation result
		 * 
		 * @return
		 */
		protected abstract E result();
		
		private E pos(Integer position, Character pos) {
			this.pos = pos;
			this.position = position;
			return result();
		}
		
	}