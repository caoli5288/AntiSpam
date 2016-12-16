package com.mengcraft.antispam.filter;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created on 16-11-13.
 */
public class FilterChain extends Filter {

    private final List<Filter> chain = new ArrayList<>();

    private FilterChain() {
    }

    @Override
    protected boolean valid(String input) {
        for (Filter filter : chain) {
            if (filter.valid(input)) return true;
        }
        return false;
    }

    public boolean add(String input) {
        if (input.charAt(0) == '~') {
            return chain.add(RegularFilter.build(input.substring(1)));
        }
        return chain.add(Filter.build(input));
    }

    public List<Filter> getChain() {
        return ImmutableList.copyOf(chain);
    }

    @Override
    public String toString() {
        return chain.toString();
    }

    public static FilterChain build(Collection<String> input) {
        FilterChain chain = new FilterChain();
        for (String i : input) {
            chain.add(i);
        }
        return chain;
    }

}
