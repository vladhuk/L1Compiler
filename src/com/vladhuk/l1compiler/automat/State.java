package com.vladhuk.l1compiler.automat;

public enum State {
    ERROR,
    EXIT,
    EOL,
    DECLARATION,
    DECLARATION_OPTIONAL_PUNCT,
    TYPE,
    ASSIGN,
    OPTIONAL_ASSIGN,
    EXPRESSION,
    EXPRESSION_OR_STRING,
    ARITHM_EXPRESSION,
    BOOL_EXPRESSION,
    STRING,
    ARITHM_OR_BOOL_SIGN,
    ARITHM_SIGN,
    BOOL_SIGN,
    CLOSE_BRACKET,
    CLOSE_BRACKET_ARITHM,
    CLOSE_BRACKET_BOOL,
    LABEL_MARK_OR_ASSIGNING,
    IF,
    THEN,
    IDENTIFIER,
    GOTO,
    LOOP,
    TO,
    DO,
    DECLARATION_OR_ASSIGNING,
    END
}
