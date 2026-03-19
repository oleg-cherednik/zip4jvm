/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package ru.olegcherednik.zip4jvm.view;

import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.view.out.Out;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireZeroOrPositive;

/**
 * @author Oleg Cherednik
 * @since 05.11.2019
 */
@Getter
public abstract class BaseView implements View {

    protected static final int ONE = 1;

    protected final int offs;
    protected final int columnWidth;
    protected final long totalDisks;
    protected final String format;
    protected final String prefix;

    protected BaseView(int offs, int columnWidth) {
        this(offs, columnWidth, 0);
    }

    protected BaseView(int offs, int columnWidth, long totalDisks) {
        this.offs = offs;
        this.columnWidth = columnWidth;
        this.totalDisks = totalDisks;
        format = "%-" + columnWidth + "s%s";
        prefix = StringUtils.repeat(" ", offs);

        requireZeroOrPositive(totalDisks, "BaseView.totalDisks");
    }

    public void printLine(Out out, Object one, Object two) {
        out.printLine(offs, format, one, two);
    }

    public void printLine(Out out, Object one) {
        out.printLine(offs, one);
    }

    public void printCrc32(Out out, String one, long crc32) {
        out.printLine(offs, format, one, String.format("0x%08X", crc32 & 0xFFFFFFFFL));
    }

    public void printSize(Out out, String one, long size) {
        out.printLine(offs, format, one, strSize(size));
    }

    public void printOffs(Out out, String one, long offs) {
        out.printLine(this.offs, format, one, strOffs(offs));
    }

    // ---------- Title ----------

    public void printTitle(Out out, int signature, String title) {
        printTitle(out, signature, title, null);
    }

    public void printTitle(Out out, int signature, String title, Block block) {
        printTitle(out, '(' + strSignature(signature) + ") " + title, block);
    }

    public void printTitle(Out out, String title, Block block) {
        out.println(title, '=');
        printLocationAndSize(out, block);
    }

    // ---------- SubTitle ----------

    public void printSubTitle(Out out, int signature, long pos, String title, Block block) {
        String str = String.format("#%d (%s) %s", pos + 1, strSignature(signature), title);
        printSubTitle(out, str, block);
    }

    public void printSubTitle(Out out, long pos, String title) {
        printSubTitle(out, pos, title, null);
    }

    public void printSubTitle(Out out, long pos, String title, Block block) {
        String str = String.format("#%d %s", pos + 1, title);
        printSubTitle(out, str, block);
    }

    private void printSubTitle(Out out, String str, Block block) {
        out.println(str, '-');
        printLocationAndSize(out, block);
    }

    // ---------- Value & Location ----------

    public void printValueWithLocation(Out out, String valueName, Block block) {
        printValueWithLocation(out, valueName, block, 0);
    }

    public void printValueWithLocation(Out out, String valueName, Block block, int total) {
        out.printLine(offs, format, valueName, strDiskOffs(block));

        if (totalDisks > ONE)
            out.printLine(offs + 2, format, strDiskNo(block), block.getFileName());

        if (total > 0)
            printLine(out, "  - size:", strSize(block.getSize()) + " (" + strTotalRecords(total) + ')');
        else
            printSize(out, "  - size:", block.getSize());
    }

    // ----------

    protected void printLocationAndSize(Out out, Block block) {
        if (block == null)
            return;

        if (totalDisks > ONE)
            out.printLine(offs, format, strDiskNo(block), block.getFileName());

        printLocation(out, block);
        printSize(out, "- size:", block.getSize());
    }

    protected void printLocation(Out out, Block block) {
        out.printLine(offs, format, "- location:", strDiskOffs(block));
    }

    protected static String strTotalRecords(long total) {
        return String.format("%d record%s", total, total == 1 ? "" : "s");
    }

    protected static String strDiskOffs(Block block) {
        return strOffs(block.getDiskOffs());
    }

    private static String strOffs(long offs) {
        return String.format("%1$d (0x%1$08X) bytes", offs);
    }

    private static String strSize(long size) {
        return String.format("%d byte%s", size, size == 1 ? "" : "s");
    }

    protected static String strDiskNo(Block block) {
        return String.format("- disk (%04X):", block.getDiskNo());
    }

    public static String strSignature(int signature) {
        StringBuilder buf = new StringBuilder();

        for (int i = 0; i < 4; i++, signature >>= 8) {
            byte code = (byte) signature;
            char ch = (char) code;
            buf.append(Character.isLetterOrDigit(ch) ? ch : String.format("%02d", code));
        }

        return buf.toString();
    }

}
