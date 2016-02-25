package org.cthul.org.model.io;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.commonmark.internal.ListBlockParser;
import org.commonmark.internal.ListItemParser;
import org.commonmark.node.BulletList;
import org.commonmark.node.ListBlock;
import org.commonmark.node.OrderedList;
import org.commonmark.parser.block.AbstractBlockParserFactory;
import org.commonmark.parser.block.BlockParser;
import org.commonmark.parser.block.BlockStart;
import org.commonmark.parser.block.MatchedBlockParser;
import org.commonmark.parser.block.ParserState;

/**
 *
 */
public class TaskListParser {
    
    private static final Pattern TASK_LIST_MARKER = Pattern.compile("^(?:\\[.?\\]|\\(.?\\))( +|$)");
    
//    private final ListBlock block;
//
//    public TaskListParser(ListBlock listBlock) {
//        this.block = listBlock;
//    }
//    
//    @Override
//    public boolean isContainer() {
//        return true;
//    }
//
//    @Override
//    public boolean canContain(Block block) {
//        return block instanceof ListItem;
//    }
//
//    @Override
//    public Block getBlock() {
//        return block;
//    }
//
//    @Override
//    public BlockContinue tryContinue(ParserState parserState) {
//        return BlockContinue.atIndex(parserState.getIndex());
//    }
//
//    @Override
//    public void addLine(CharSequence line) {
//    }
//
//    @Override
//    public void closeBlock() {
//    }
//
//    @Override
//    public void parseInlines(InlineParser inlineParser) {
//    }
//
//    public void setTight(boolean tight) {
//        block.setTight(tight);
//    }

    /**
     * Parse a list marker and return data on the marker or null.
     */
    private static ListData parseListMarker(CharSequence ln, int offset) {
        CharSequence rest = ln.subSequence(offset, ln.length());
        int spacesAfterMarker;
        ListBlock listBlock;

        Matcher match;
        if ((match = TASK_LIST_MARKER.matcher(rest)).find()) {
            BulletList bulletList = new BulletList();
            bulletList.setBulletMarker(match.group(0).charAt(0));
            listBlock = bulletList;
            spacesAfterMarker = match.group(1).length();
        } else {
            return null;
        }
        int padding = 2;
//        boolean blankItem = match.group(0).length() == rest.length();
//        if (spacesAfterMarker >= 5 || spacesAfterMarker < 1 || blankItem) {
//            padding = match.group(0).length() - spacesAfterMarker + 1;
//        } else {
//            padding = match.group(0).length();
//        }
        return new ListData(listBlock, padding);
    }

    /**
     * Returns true if the two list items are of the same type,
     * with the same delimiter and bullet character. This is used
     * in agglomerating list items into lists.
     */
    private static boolean listsMatch(ListBlock a, ListBlock b) {
        if (a instanceof BulletList && b instanceof BulletList) {
            return Objects.equals(((BulletList) a).getBulletMarker(), ((BulletList) b).getBulletMarker());
        } else if (a instanceof OrderedList && b instanceof OrderedList) {
            return Objects.equals(((OrderedList) a).getDelimiter(), ((OrderedList) b).getDelimiter());
        }
        return false;
    }

    public static class Factory extends AbstractBlockParserFactory {

        @Override
        public BlockStart tryStart(ParserState state, MatchedBlockParser matchedBlockParser) {
            BlockParser matched = matchedBlockParser.getMatchedBlockParser();

            if (state.getIndent() >= 4 && !(matched instanceof ListBlockParser)) {
                return BlockStart.none();
            }
            if (matched instanceof ListItemParser) {
                return BlockStart.none();
            }
            int nextNonSpace = state.getNextNonSpaceIndex();
            ListData listData = parseListMarker(state.getLine(), nextNonSpace);
            if (listData == null) {
                return BlockStart.none();
            }

            // list item
            int newIndex = nextNonSpace; //listData.padding; // nextNonSpace + 

            int itemIndent = state.getIndent() + listData.padding;
            ListItemParser listItemParser = new ListItemParser(itemIndent);

            // prepend the list block if needed
            if (!(matched instanceof ListBlockParser) ||
                    !(listsMatch((ListBlock) matched.getBlock(), listData.listBlock))) {

                ListBlockParser listBlockParser = new ListBlockParser(listData.listBlock);
                listBlockParser.setTight(true);

                return BlockStart.of(listBlockParser, listItemParser).atIndex(newIndex);
            } else {
                return BlockStart.of(listItemParser).atIndex(newIndex);
            }
        }
    }

    private static class ListData {
        final ListBlock listBlock;
        final int padding;

        public ListData(ListBlock listBlock, int padding) {
            this.listBlock = listBlock;
            this.padding = padding;
        }
    }

}
