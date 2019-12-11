package com.vladhuk.l1compiler.automat;

public enum State {
    ERROR,
    EXIT,
    EOL,
    START_DECLARATION_VAL,
    START_DECLARATION_VAR,
    VAL_OR_VAR,
    DECLARATION_IDENTIFIER,
    DECLARATION_OPTIONAL_PUNCT,
    TYPE,
    ASSIGN,
    OPTIONAL_ASSIGN,
    EXPRESSION_OR_IDENT,
    EXPRESSION,
    ARITHM_EXPRESSION,
    BOOL_EXPRESSION,
    STRING,
    ARITHM_OR_BOOL_SIGN,
    ARITHM_SIGN,
    BOOL_SIGN,
    CLOSE_BRACKET,
    CLOSE_BRACKET_ARITHM,
    CLOSE_BRACKET_BOOL
}
