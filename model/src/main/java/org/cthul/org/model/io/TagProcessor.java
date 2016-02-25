package org.cthul.org.model.io;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.Emphasis;
import org.commonmark.node.Heading;
import org.commonmark.node.Node;
import org.commonmark.node.Paragraph;
import org.commonmark.node.StrongEmphasis;
import org.commonmark.node.Text;
import org.commonmark.parser.PostProcessor;

/**
 *
 */
public class TagProcessor extends AbstractVisitor implements PostProcessor {

    private static final Pattern P_TASK_ICON;

    static {
        P_TASK_ICON = Pattern.compile(
                "(\\[[- +>%?x]?\\]|\\([- +>%?]?\\))" // task icon: [] [+] ...
                
                + "|(![-!\\d+x>]" // priority, special tag: !! !1..!8 !- !+ !>
                    + "|[#!@§$][:/]?[\\w&&[^\\d]][\\w/]*)" // other tags
                + "|((?://|->)(?:[-+]\\d+[dwmy]?|[-'0-9A-Za-z]+))" // date
        );
    }
    
    @Override
    public Node process(Node node) {
        node.accept(this);
        return node;
    }
    
    boolean inHeading = false;
    Deque<Node> emphasisStack = new ArrayDeque<>();

    @Override
    public void visit(Heading heading) {
        inHeading = true;
        super.visit(heading);
        inHeading = false;
    }

    @Override
    public void visit(Paragraph paragraph) {
        super.visit(paragraph);
    }

    @Override
    public void visit(Emphasis emphasis) {
        emphasisStack.push(emphasis);
        super.visit(emphasis);
        emphasisStack.pop();
    }

    @Override
    public void visit(StrongEmphasis emphasis) {
        emphasisStack.push(emphasis);
        super.visit(emphasis);
        emphasisStack.pop();
    }
    
    protected Node copy(Node em) {
        if (em instanceof Emphasis) {
            Node c = new Emphasis();
            return c;
        } else if (em instanceof StrongEmphasis) {
            Node c = new StrongEmphasis();
            return c;
        }
        throw new IllegalArgumentException(String.valueOf(em));
    }
    
    protected Node splitStack(Text text) {
        if (emphasisStack.isEmpty()) {
            return text;
        }
        if (emphasisStack.peek().getFirstChild() == text) {
            return emphasisStack.peekLast();
        }
        Node last = text;
        for (Node em: emphasisStack) {
            Node c = copy(em);
            c.appendChild(last);
            last = c;
        }
        return last;
    }

    /*
     - No spaces at beginning of line, including after the last tag.
     - No spaces between tags.
     - Keep spaces around tags in text
    */
    
    
    @Override
    public void visit(Text text) {
        if (inHeading) return;
        String lit = text.getLiteral();
        int n = 0;
        Matcher m = P_TASK_ICON.matcher(lit);
        while (m.find()) {
            if (m.start() > n) {
                String s = lit.substring(n, m.start());
                Text t = new Text(s);
                text.insertBefore(t);
            }
            if (m.group(1) != null) {
                char status = m.group().charAt(1);
                if (status == ']' || status == ')') status = 0;
                Token.Task t = new Token.Task(m.group().charAt(0), status);
                Node last = splitStack(text);
                last.insertBefore(t);
            } else if (m.group(2) != null) {
                Token.Tag t = new Token.Tag(m.group(2));
                text.insertBefore(t);
            } else if (m.group(3) != null) {
                boolean ref = m.group(3).startsWith("->");
                String s = m.group(3).substring(2);
                Token.Temporal t = new Token.Temporal(ref, s);
                text.insertBefore(t);
            } else {
                throw new IllegalArgumentException(m.toString());
            }
            n = m.end();
        }
        if (n < lit.length()) {
            String s = lit.substring(n, lit.length());
            text.setLiteral(s);
        } else {
            text.unlink();
        }
    }
    
}
