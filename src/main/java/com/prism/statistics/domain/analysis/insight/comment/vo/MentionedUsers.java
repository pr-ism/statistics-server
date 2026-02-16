package com.prism.statistics.domain.analysis.insight.comment.vo;

import jakarta.persistence.Embeddable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@EqualsAndHashCode
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MentionedUsers {

    private static final Pattern MENTION_PATTERN = Pattern.compile("@([a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?)");
    private static final String SEPARATOR = ",";

    private String userNames;

    private int count;

    public static MentionedUsers fromBody(String body) {
        if (body == null || body.isBlank()) {
            return empty();
        }

        List<String> mentions = extractMentions(body);
        if (mentions.isEmpty()) {
            return empty();
        }

        String userNames = String.join(SEPARATOR, mentions);
        return new MentionedUsers(userNames, mentions.size());
    }

    public static MentionedUsers of(List<String> userNames) {
        if (userNames == null || userNames.isEmpty()) {
            return empty();
        }

        List<String> validNames = userNames.stream()
                .filter(name -> name != null && !name.isBlank())
                .distinct()
                .collect(Collectors.toList());

        if (validNames.isEmpty()) {
            return empty();
        }

        return new MentionedUsers(String.join(SEPARATOR, validNames), validNames.size());
    }

    public static MentionedUsers empty() {
        return new MentionedUsers(null, 0);
    }

    private static List<String> extractMentions(String body) {
        Matcher matcher = MENTION_PATTERN.matcher(body);
        return matcher.results()
                .map(result -> result.group(1))
                .distinct()
                .collect(Collectors.toList());
    }

    private MentionedUsers(String userNames, int count) {
        this.userNames = userNames;
        this.count = count;
    }

    public List<String> toList() {
        if (userNames == null || userNames.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.asList(userNames.split(SEPARATOR));
    }

    public boolean isEmpty() {
        return count == 0;
    }

    public boolean contains(String userName) {
        if (isEmpty() || userName == null) {
            return false;
        }
        return toList().contains(userName);
    }

    public MentionedUsers merge(MentionedUsers other) {
        if (other == null) {
            throw new IllegalArgumentException("병합 대상은 null일 수 없습니다.");
        }
        if (other.isEmpty()) {
            return this;
        }
        if (this.isEmpty()) {
            return other;
        }

        List<String> merged = new ArrayList<>(toList());
        merged.addAll(other.toList());
        return of(merged.stream().distinct().collect(Collectors.toList()));
    }
}
