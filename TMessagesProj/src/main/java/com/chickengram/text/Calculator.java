package com.chickengram.text;

import android.text.Editable;

import java.math.BigDecimal;
import java.math.MathContext;

public final class Calculator {

    private final String source;
    private int position;

    private Calculator(String source) {
        this.source = source;
    }

    public static void apply(Editable editable) {
        final int length = editable.length();
        if (length < 5 || editable.charAt(length - 1) != ' ' || editable.charAt(length - 2) != '=') {
            return;
        }
        final int end = length - 2;
        int start = end;
        while (start > 0 && isExpressionChar(editable.charAt(start - 1))) {
            start--;
        }
        final String expression = editable.subSequence(start, end).toString().trim();
        if (!looksLikeMath(expression)) {
            return;
        }
        final String result = evaluate(expression);
        if (result != null) {
            editable.insert(length - 1, result);
        }
    }

    public static String evaluate(String expression) {
        try {
            final Calculator calculator = new Calculator(expression.replace(',', '.').replace('×', '*').replace('÷', '/').replace(':', '/').replace(" ", ""));
            final double value = calculator.parseExpression();
            if (calculator.position != calculator.source.length() || Double.isNaN(value) || Double.isInfinite(value)) {
                return null;
            }
            return format(value);
        } catch (Exception e) {
            return null;
        }
    }

    private static String format(double value) {
        if (value == Math.rint(value) && Math.abs(value) < 1e15) {
            return Long.toString((long) value);
        }
        final BigDecimal decimal = new BigDecimal(value).round(new MathContext(12)).stripTrailingZeros();
        return decimal.toPlainString();
    }

    private static boolean isExpressionChar(char c) {
        return c >= '0' && c <= '9' || c == '+' || c == '-' || c == '*' || c == '/' || c == '×' || c == '÷' || c == ':' || c == '^' || c == '%' || c == '(' || c == ')' || c == '.' || c == ',' || c == ' ';
    }

    private static boolean looksLikeMath(String expression) {
        boolean digit = false;
        boolean operator = false;
        for (int i = 0; i < expression.length(); i++) {
            final char c = expression.charAt(i);
            if (c >= '0' && c <= '9') {
                digit = true;
            } else if (i > 0 && (c == '+' || c == '-' || c == '*' || c == '/' || c == '×' || c == '÷' || c == ':' || c == '^' || c == '%')) {
                operator = true;
            }
        }
        return digit && operator;
    }

    private double parseExpression() {
        double value = parseTerm();
        while (position < source.length()) {
            final char c = source.charAt(position);
            if (c == '+') {
                position++;
                value += parseTerm();
            } else if (c == '-') {
                position++;
                value -= parseTerm();
            } else {
                break;
            }
        }
        return value;
    }

    private double parseTerm() {
        double value = parsePower();
        while (position < source.length()) {
            final char c = source.charAt(position);
            if (c == '*') {
                position++;
                value *= parsePower();
            } else if (c == '/') {
                position++;
                value /= parsePower();
            } else if (c == '%') {
                position++;
                value = value / 100.0;
            } else {
                break;
            }
        }
        return value;
    }

    private double parsePower() {
        final double base = parseUnary();
        if (position < source.length() && source.charAt(position) == '^') {
            position++;
            return Math.pow(base, parsePower());
        }
        return base;
    }

    private double parseUnary() {
        if (position < source.length()) {
            final char c = source.charAt(position);
            if (c == '-') {
                position++;
                return -parseUnary();
            } else if (c == '+') {
                position++;
                return parseUnary();
            }
        }
        return parsePrimary();
    }

    private double parsePrimary() {
        if (position < source.length() && source.charAt(position) == '(') {
            position++;
            final double value = parseExpression();
            if (position >= source.length() || source.charAt(position) != ')') {
                throw new IllegalArgumentException();
            }
            position++;
            return value;
        }
        final int start = position;
        while (position < source.length() && (Character.isDigit(source.charAt(position)) || source.charAt(position) == '.')) {
            position++;
        }
        if (start == position) {
            throw new IllegalArgumentException();
        }
        return Double.parseDouble(source.substring(start, position));
    }
}
