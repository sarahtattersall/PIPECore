package uk.ac.imperial.pipe.tuple;

/**
 * Python-esque mechanism to simply create and return a pair of values 
 * @author stevedoubleday
 *
 * @param <T1>
 * @param <T2>
 */

public class Tuple <T1, T2> {

	public T1 tuple1;
	public T2 tuple2; 
	
	public Tuple(T1 tuple1, T2 tuple2) {
		this.tuple1 = tuple1; 
		this.tuple2 = tuple2; 
	}
	
}
