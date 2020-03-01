package org.kilocraft.essentials.api.text;

import org.kilocraft.essentials.api.user.OnlineUser;

import java.util.List;
import java.util.Map;

public class PagedText {
    private final transient IText text;
    private final transient boolean onePage;

    public PagedText(final IText text) {
        this(text, false);
    }

    public PagedText(final IText text, final boolean onePage) {
        this.text = text;
        this.onePage = onePage;
    }

    public void send(final OnlineUser user, final String pageStr, final String chapterPageStr, final String commandName) {
        final List<String> lines = this.text.getLines();
        final List<String> chapters = this.text.getChapters();
        final Map<String, Integer> bookmarks = this.text.getBookmarks();


        if (pageStr == null || pageStr.isEmpty() || pageStr.matches("[0-9]+")) {
            //If an info file starts with a chapter title, list the chapters
            //If not display the text up until the first chapter.
            if (!lines.isEmpty() && lines.get(0).startsWith("#")) {
                if (this.onePage) {
                    return;
                }
                
                //user.sendMessage(tl("infoChapter"));
                final StringBuilder sb = new StringBuilder();
                boolean first = true;
                for (final String string : chapters) {
                    if (!first) {
                        sb.append(", ");
                    }
                    first = false;
                    sb.append(string);
                }
                user.sendMessage(sb.toString());
                return;
            } else {
                int page = 1;
                try {
                    page = Integer.parseInt(pageStr);
                } catch (final NumberFormatException ex) {
                    page = 1;
                }
                if (page < 1) {
                    page = 1;
                }

                final int start = this.onePage ? 0 : (page - 1) * 9;
                int end;
                for (end = 0; end < lines.size(); end++) {
                    final String line = lines.get(end);
                    if (line.startsWith("#")) {
                        break;
                    }
                }

                final int pages = end / 9 + (end % 9 > 0 ? 1 : 0);
                if (page > pages) {
                    //user.sendMessage(tl("infoUnknownChapter"));
                    return;
                }
                if (!this.onePage && commandName != null) {

                    final StringBuilder content = new StringBuilder();
                    final String[] title = commandName.split(" ", 2);
                    if (title.length > 1) {
                        //content.append(I18n.capitalCase(title[0])).append(": ");
                        content.append(title[1]);
                    } else {
                        //content.append(I18n.capitalCase(commandName));
                    }
                    //user.sendMessage(tl("infoPages", page, pages, content));
                }
                for (int i = start; i < end && i < start + (this.onePage ? 20 : 9); i++) {
                    user.sendMessage("§r" + lines.get(i));
                }
                if (!this.onePage && page < pages && commandName != null) {
                    //user.sendMessage(tl("readNextPage", commandName, page + 1));
                }
                return;
            }
        }

        //If we have a chapter, check to see if we have a page number
        int chapterpage = 0;
        if (chapterPageStr != null) {
            try {
                chapterpage = Integer.parseInt(chapterPageStr) - 1;
            } catch (final NumberFormatException ex) {
                chapterpage = 0;
            }
            if (chapterpage < 0) {
                chapterpage = 0;
            }
        }
//
//        //This checks to see if we have the chapter in the index
//        if (!bookmarks.containsKey(pageStr.toLowerCase(Locale.ENGLISH))) {
//            user.sendMessage(tl("infoUnknownChapter"));
//            return;
//        }
//
//        //Since we have a valid chapter, count the number of lines in the chapter
//        final int chapterstart = bookmarks.get(pageStr.toLowerCase(Locale.ENGLISH)) + 1;
//        int chapterend;
//        for (chapterend = chapterstart; chapterend < lines.size(); chapterend++) {
//            final String line = lines.get(chapterend);
//            if (line.length() > 0 && line.charAt(0) == '#') {
//                break;
//            }
//        }
//
//        //Display the chapter from the starting position
//        final int start = chapterstart + (this.onePage ? 0 : chapterpage * 9);
//        final int page = chapterpage + 1;
//        final int pages = (chapterend - chapterstart) / 9 + ((chapterend - chapterstart) % 9 > 0 ? 1 : 0);
//        if (!this.onePage && commandName != null) {
//            final StringBuilder content = new StringBuilder();
//            content.append(I18n.capitalCase(commandName)).append(": ");
//            content.append(pageStr);
//            user.sendMessage(tl("infoChapterPages", content, page, pages));
//        }
//        for (int i = start; i < chapterend && i < start + (this.onePage ? 20 : 9); i++) {
//            user.sendMessage("§r" + lines.get(i));
//        }
//        if (!this.onePage && page < pages && commandName != null) {
//            user.sendMessage(tl("readNextPage", commandName, pageStr + " " + (page + 1)));
//        }
    }

}
