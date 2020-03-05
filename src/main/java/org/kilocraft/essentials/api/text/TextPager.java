package org.kilocraft.essentials.api.text;

import org.kilocraft.essentials.api.user.OnlineUser;

public class TextPager {
    private final transient IText input;
    private final boolean onePage;

    public TextPager(IText input) {
        this(input, false);
    }

    public TextPager(IText input, boolean onePage) {
        this.input = input;
        this.onePage = onePage;
    }

    public void sendPage(final OnlineUser user) {

    }

}
