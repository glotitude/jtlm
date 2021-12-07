package com.glotitude.jtlm;

enum TokenType {
    // Single-character tokens.
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACKET, RIGHT_BRACKET, LEFT_BRACE, RIGHT_BRACE,
    COMMA, DOT, MINUS, PLUS, SEMICOLON, SLASH, STAR, COLON,

    // One or two character tokens.
    BANG, BANG_EQUAL,
    EQUAL, EQUAL_EQUAL,
    GREATER, GREATER_EQUAL,
    LESS, LESS_EQUAL,
    BINDING, // ->
    ARROW,   // =>
    RANGE,   // ..

    // Literals.
    IDENTIFIER, STRING, NUMBER,

    // Keywords.
    AND, ELSE, FALSE, FOR, IF, NULL, OR, IN,
    EMIT, REPEAT, SCHEDULE, RETURN, TRUE, VAR, WHILE,

    EOF
}
