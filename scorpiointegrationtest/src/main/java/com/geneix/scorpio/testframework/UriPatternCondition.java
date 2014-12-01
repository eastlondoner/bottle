package com.geneix.scorpio.testframework;

import com.google.common.base.Predicate;
import com.xebialabs.restito.semantics.Call;
import com.xebialabs.restito.semantics.Condition;

import javax.annotation.Nullable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Andrew on 20/11/2014.
 */
public class UriPatternCondition extends Condition {
    private final UriPatternPredicate predicate;

    protected UriPatternCondition(UriPatternPredicate predicate) {
        super(predicate);
        this.predicate = predicate;
    }

    public static Condition matchesPattern(Pattern pattern) {
        return new UriPatternCondition(new UriPatternPredicate(pattern));
    }

    public static Condition matchesPattern(String regex) {
        return new UriPatternCondition(new UriPatternPredicate(regex));
    }

    public String getGroup(String groupName) {
        return predicate.matcher.group(groupName);
    }

    public static class UriPatternPredicate implements Predicate<Call> {
        private final Pattern pattern;
        private Matcher matcher;

        public UriPatternPredicate(String regex) {
            this(Pattern.compile(regex));
        }

        public UriPatternPredicate(Pattern pattern) {
            this.pattern = pattern;
        }

        @Override
        public boolean apply(@Nullable Call input) {
            if (input == null) return false;
            matcher = pattern.matcher(input.getUri());
            return matcher.matches();
        }

    }
}
