package org.cthul.org.model.io;

import org.commonmark.node.CustomNode;

/**
 *
 */
public class Token extends CustomNode {
    
    public static class Tag extends Token {
        
        private String literal;

        public Tag(String literal) {
            this.literal = literal;
        }

        public String getLiteral() {
            return literal;
        }

        public void setLiteral(String literal) {
            this.literal = literal;
        }
    }
    
    public static class Task extends Token {
        
        private char openChar;
        private char statusChar;
        
        public Task() {
            this('[', '\0');
        }

        public Task(char openChar, char statusChar) {
            this.openChar = openChar;
            this.statusChar = statusChar;
        }

        public void setOpenChar(char openChar) {
            this.openChar = openChar;
        }

        public char getOpenChar() {
            return openChar;
        }

        public void setStatusChar(char statusChar) {
            this.statusChar = statusChar;
        }

        public char getStatusChar() {
            return statusChar;
        }
    }
    
    public static class Temporal extends Token {
        
        private final boolean reference;
        private final String value;

        public Temporal(boolean reference, String value) {
            this.reference = reference;
            this.value = value;
        }

        public boolean isReference() {
            return reference;
        }

        public String getValue() {
            return value;
        }
    }
    
}
