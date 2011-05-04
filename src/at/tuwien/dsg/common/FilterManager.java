package at.tuwien.dsg.common;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import at.tuwien.dsg.entities.Filter;

public class FilterManager {

	private List<Filter> filters;
	private Pattern pattern;
	private Filter appliedFilter;
	
	public FilterManager() {
		filters = new ArrayList<Filter>();
	}
	
	public void setFilter(Filter filter) {
		appliedFilter = filter;
		pattern = Pattern.compile(filter.getPattern());
	}
	
	public boolean match(String text) {
		return pattern.matcher(text).matches();
	}
	
	public void addFilter(Filter filter) {
		filters.add(filter);
	}
	
	public void setFilters(List<Filter> filters) {
		this.filters = filters;
	}

	public List<Filter> getFilters() {
		return filters;
	}
	
	
}
