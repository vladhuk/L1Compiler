package com.vladhuk.l1compiler.lexical;

public enum Token {
    CONSTANT {
        public String getRegex() {
            return "true|false|'[^']*'|\\d+(((\\.\\d+)?e[+-]\\d+)|(\\.\\d+))?";
        }
    },
    DECLARATION {
        public String getRegex() {
            return "var|val";
        }
    },
    LOOP {
        public String getRegex() {
            return "for|while|to|do|end";
        }
    },
    CONDITION {
        public String getRegex() {
            return "if|then";
        }
    },
    JUMP {
        public String getRegex() {
            return "goto";
        }
    },
    IO {
        public String getRegex() {
            return "in|out";
        }
    },
    TYPE {
        public String getRegex() {
            return "number|boolean|string";
        }
    },
    ASSIGN {
        public String getRegex() {
            return "=";
        }
    },
    ADD_OP {
        public String getRegex() {
            return "\\+|-";
        }
    },
    MULT_OP {
        public String getRegex() {
            return "\\*|/";
        }
    },
    POW_OP {
        public String getRegex() {
            return "\\^";
        }
    },
    REL_OP {
        public String getRegex() {
            return ">=|<=|==|!=|>|<";
        }
    },
    BRACKET_OP {
        public String getRegex() {
            return "\\(|\\)";
        }
    },
    PUNCT {
        public String getRegex() {
            return ":";
        }
    },
    IDENTIFIER {
        public String getRegex() {
            return "[a-zA-Z]+\\w*";
        }
    },
    UNKNOWN {
        public String getRegex() {
            return "";
        }
    };

    public abstract String getRegex();

    public static Token getToken(String lexem) {
        for (Token token : Token.values()) {
            if (lexem.matches(token.getRegex())) {
                return token;
            }
        }
        return UNKNOWN;
    }
}
