package com.mengcraft.antispam.filter;

/**
 * Created on 16-11-13.
 */
public abstract class Filter {

    public static class SimpleFilter extends Filter {

        private final String str;

        private SimpleFilter(String str) {
            this.str = str;
        }

        @Override
        protected boolean valid(String input) {
            return input.contains(str);
        }

    }

    public boolean check(String input) {
        return valid(input.trim().replace(" ", ""));
    }

    protected abstract boolean valid(String input);

    public static Filter build(String input) {
        return new SimpleFilter(input);
    }

}
