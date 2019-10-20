package com.vladhuk.l1compiler.lexical;

public enum Token {
    IDENTIFIER {
        public String getRegex() {
            return "[a-zA-Z]+\\w*";
        }
    },
    LITERAL {
        public String getRegex() {
            return "true|false|'.*'|\\d+((\\.\\d+)|((\\.\\d+)?e[+-]\\d+))?";
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
            return "[+-]";
        }
    },
    MULT_OP {
        public String getRegex() {
            return "[*/]";
        }
    },
    POW_OP {
        public String getRegex() {
            return "^";
        }
    },
    REL_OP {
        public String getRegex() {
            return ">|>=|<|<=|==|!=";
        }
    },
    BRACKET_OP {
        public String getRegex() {
            return "[()]";
        }
    },
    PUNCT {
        public String getRegex() {
            return "[.:]";
        }
    },
    UNKNOWN {
        public String getRegex() {
            return "";
        }
    };

    public abstract String getRegex();
}
