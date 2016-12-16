package com.mengcraft.antispam.filter;

import static com.mengcraft.antispam.AntiSpam.nil;

/**
 * Created on 16-11-13.
 */
public abstract class Filter {

    public static class SimpleFilter extends Filter {

        private final String text;

        private SimpleFilter(String text) {
            this.text = text;
        }

        @Override
        protected boolean valid(String input) {
            return input.contains(text);
        }

        @Override
        public String toString() {
            return "Basic -> " + text;
        }

    }

    public boolean check(String input) {
        if (nil(input)) return false;
        String r = input.trim().replace(" ", "");
        return !r.isEmpty() && valid(r);
    }

    protected abstract boolean valid(String input);

    public static Filter build(String input) {
        return new SimpleFilter(input);
    }

}
