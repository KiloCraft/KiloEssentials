/*
 * MIT License
 *
 * Copyright (c) 2020 OnBlock/Bahar
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.kilocraft.essentials.api.text;

import org.jetbrains.annotations.NotNull;

public enum LoggerFormats {

    RESET("\u001B[0m"),
    BLACK("\u001B[30m"),
    BRIGHT_BLACK("\u001b[30;1m"),
    RED("\u001B[31m"),
    BRIGHT_RED("\u001b[31;1m"),
    GREEN("\u001B[32m"),
    BRIGHT_GREEN("\u001b[32;1m"),
    YELLOW("\u001B[33m"),
    BRIGHT_YELLOW("\u001b[33;1m"),
    BLUE("\u001B[34m"),
    BRIGHT_BLUE("\u001b[34;1m"),
    MAGENTA("\u001B[35m"),
    BRIGHT_MAGENTA("\u001b[35;1m"),
    CYAN("\u001B[36m"),
    BRIGHT_CYAN("\u001b[36;1m"),
    WHITE("\u001B[37m"),
    BRIGHT_WHITE("\u001b[37;1m"),
    BOLD("\u001B[1m"),
    ITALICS("\u001B[3m"),
    UNDERLINE("\u001B[4m"),
    STRIKETHROUGH("\u001B[9m");

    @NotNull
    private final String code;

    LoggerFormats(@NotNull final String cde) {
        this.code = cde;
    }

    @NotNull
    public String getCode() {
        return this.code;
    }
}
