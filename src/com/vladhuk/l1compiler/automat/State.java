package com.vladhuk.l1compiler.automat;

public enum State {
    ERROR,
    EXIT,
    EOL,
    VAL,
    VAR,
    DECLARATION_IDENTIFIER,
    DECLARATION_OPTIONAL_PUNCT,
    TYPE,
    ASSIGN,
    EXPRESSION_OR_IDENT,
    IDENTIFIER,
    EXPRESSION
}
