package com.mengcraft.antispam.filter;

import com.google.common.base.Preconditions;

import java.util.regex.Pattern;

/**
 * Created on 16-11-13.
 */
public class RegularFilter extends Filter {

    private final Pattern pattern;

    private RegularFilter(Pattern pattern) {
        this.pattern = pattern;
    }

    @Override
    protected boolean valid(String input) {
        return pattern.matcher(input).matches();
    }

    public static Filter build(String input) {
        Preconditions.checkArgument(input.length() > 0);
        Pattern compiled = Pattern.compile(input);
        return new RegularFilter(compiled);
    }

}
