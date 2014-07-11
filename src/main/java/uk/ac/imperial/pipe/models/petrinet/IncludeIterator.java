package uk.ac.imperial.pipe.models.petrinet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Stack;


public class IncludeIterator implements Iterator<IncludeHierarchy> {

	private boolean done;
	private Iterator<IncludeHierarchy> iterator;
	private IncludeHierarchy includeHierarchy;
	private boolean start = false;
	private IncludeHierarchy current;
	private Iterator<IncludeHierarchy> previousIterator;
	private Stack<IncludeHierarchy> stack;
	private Stack<Iterator<IncludeHierarchy>> stackIterator;
	private ArrayList<IncludeHierarchy> includes;
	public IncludeIterator(IncludeHierarchy includeHierarchy) {
		this.includeHierarchy = includeHierarchy; 
		buildIncludeList(); 
//		done = false; 
//		setInitialIterator();
//		iterator = includeHierarchy.includeMap().values().iterator();  
	}
	private void buildIncludeList() {
		includes = new ArrayList<IncludeHierarchy>(); 
		includes.add(includeHierarchy); 
		stack = new Stack<>();
		stackIterator = new Stack<Iterator<IncludeHierarchy>>();  
		pushIteratorForOneLevel(includeHierarchy); 
		IncludeHierarchy include = nextHierarchyFromCurrentIterator(); 
		while (include != null)
		{
			includes.add(include); 
			include = nextHierarchyFromCurrentIterator(); 
		}
		iterator = includes.iterator(); 
	}
	private IncludeHierarchy nextHierarchyFromCurrentIterator() {
		IncludeHierarchy include = null;
		if (!stackIterator.empty()) {
			Iterator<IncludeHierarchy> it = stackIterator.pop(); 
			if (it.hasNext()) {
				include = it.next(); 
				if (it.hasNext()) stackIterator.push(it);
				pushIteratorForOneLevel(include); 
			}
			else return nextHierarchyFromCurrentIterator(); 
		}
		return include;
	}
	private void pushIteratorForOneLevel(IncludeHierarchy includeHierarchy) {
		Collection<IncludeHierarchy> hierarchies = includeHierarchy.includeMap().values();
		Iterator<IncludeHierarchy> it = hierarchies.iterator(); 
		if (it.hasNext()) {
			stackIterator.push(it);
		}
	}
//	includes.add(it.next()); 
//	private void setInitialIterator() {
//		if (includeHierarchy.parent() == null) {
//			start = true; 
//		}
//	}
//	@Override
//	public boolean hasNext() {
//		if (start) 	return true; 
//		else {
//			if (iterator.hasNext()) return true; 
//			else {
//				if (previousIterator != null) {
////					iterator = previousIterator; 
////					return iterator.hasNext();  // how to keep popping back up?  
//					return previousIterator.hasNext(); 
//				}
//				else return false; 
//			}
//		}
//	}
//	@Override
//	public IncludeHierarchy next() {
//		if (hasNext()) {
//			if (start) {
//				current = includeHierarchy;  
//				start = false; 
//			}
//			else pushDownOneLevel(); 
//		}
//		else {
//			if (previousIterator != null) {
//				iterator = previousIterator; 
//				previousIterator = null; 
//				if (hasNext()) pushDownOneLevel(); 
//				else current = null; 
//			}
//			else current = null; 
//		}
//		return current; 
//	}
//
//	private void pushDownOneLevel() {
//		current = iterator.next(); 
//		previousIterator = iterator; 
//		iterator = current.iterator();  // 
//	}

	public IncludeHierarchy current() {
		return current;
	}
	@Override
	public void remove() {
		throw new UnsupportedOperationException("Include Iterator does not support remove method."); 
	}
//	public ParameterPoint next()
//	{
//		verifyState();
//		if (initialized) buildNextPoint();  
//		else  setUpParameterValueArray(); 
//		ParameterPoint point = new ParameterPoint(copyParameterValuesArray(), parameters); 
//		if (fitnessTracker != null) fitnessTracker.setCurrentParameterPoint(point); 
//		return point;
//	}
//	private ParameterValue<?>[] copyParameterValuesArray()
//	{
//		ParameterValue<?>[] parameterValuesCopy = new ParameterValue<?>[size]; 
//		for (int i = 0; i < size; i++)
//		{
//			parameterValuesCopy[i] = parameterValues[i];
//		}
//		return parameterValuesCopy;
//	}
//	private void buildNextPoint()
//	{
//		if (currentParameter().hasNext()) setNextParameterValue();
//		else 
//		{
//			bubbleUpUntilTop();  
//			if (atTop)
//			{
//				if (currentParameter().hasNext())	
//				{
//					rippleDownResetToFirstValue(); 
//				}
//				else throw new NoSuchElementException(PARAMETER_LIST_COMPLETELY_TRAVERSED); 
//			}
//		}
//	}
//	private Parameter<?> currentParameter()
//	{
//		return parameters.get(current);
//	}
//	private void setNextParameterValue()
//	{
//		parameterValues[current] = currentParameter().nextParameterValue();
//		last[current] = !currentParameter().hasNext(); 
//	}
//	private void bubbleUpUntilTop()
//	{
//		last[current] = true;
//		if	(current > 0) 
//		{
//			current--;
//			if (currentParameter().hasNext())	
//			{
//				rippleDownResetToFirstValue(); 
//			}
//			else bubbleUpUntilTop(); 
//		}
//		else atTop  = true; 
//	}
//	private void rippleDownResetToFirstValue()
//	{
//		setNextParameterValue();
//		if (current < size-1)
//		{
//			current++;
//			rippleDownResetToFirstValue(); 
//		}
//	}
//	private void setUpParameterValueArray()
//	{
//		parameterValues = new ParameterValue<?>[size];
//		for (int i = 0; i < size; i++)
//		{
//			current = i;
//			if (hasNext()) setNextParameterValue();
//		}
//		initialized = true; 
//	}
//  build list of IncludeHierarchies:  hasNext just looks at current, until there are no more
//  top
//    push	  
	protected ArrayList<IncludeHierarchy> getIncludes() {
		return includes;
	}
	@Override
	public boolean hasNext() {
		return iterator.hasNext();
	}
	@Override
	public IncludeHierarchy next() {
		current = iterator.next();
		return current;
	}
}
