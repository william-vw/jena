package org.apache.jena.util.iterator;

public class EmptyIterator<T> extends NiceIterator<T> implements ExtendedIterator<T> {

	/**
	 * Constructor
	 * 
	 * @param element the single value to be returned
	 */
	public EmptyIterator() {
	}

	/**
	 * Can return a single value
	 */
	@Override
	public boolean hasNext() {
		return false;
	}

	/**
	 * Return the value
	 */
	@Override
	public T next() {
		return noElements("no objects in this iterator");
	}

}