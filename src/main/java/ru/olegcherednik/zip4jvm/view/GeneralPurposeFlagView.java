package ru.olegcherednik.zip4jvm.view;

import lombok.Builder;
import org.apache.commons.lang.StringUtils;
import ru.olegcherednik.zip4jvm.model.GeneralPurposeFlag;

import java.io.PrintStream;

/**
 * @author Oleg Cherednik
 * @since 15.10.2019
 */
@Builder
public class GeneralPurposeFlagView {

    private final GeneralPurposeFlag generalPurposeFlag;
    private final String prefix;

    public void print(PrintStream out) {
        int val = generalPurposeFlag.getAsInt();
        out.format("%sunzip software version needed to extract (20):  2.0\n", prefix);
        out.format("%sgeneral purpose bit flag (0x%04X) (bit 15..0):  %s.%s %s.%s\n", prefix, val,
                StringUtils.leftPad(Integer.toBinaryString(val >> 12 & 0xF), 4, '0'),
                StringUtils.leftPad(Integer.toBinaryString(val >> 8 & 0xF), 4, '0'),
                StringUtils.leftPad(Integer.toBinaryString(val >> 4 & 0xF), 4, '0'),
                StringUtils.leftPad(Integer.toBinaryString(val & 0xF), 4, '0'));
        out.format("%s  file security status  (bit 0):                %s\n", prefix,
                generalPurposeFlag.isEncrypted() ? "encrypted" : "not encrypted");
        out.format("%s  extended local header (bit 3):                %b\n", prefix, generalPurposeFlag.isDataDescriptorAvailable());

        if (generalPurposeFlag.isStrongEncryption())
            out.format("%s  strong encryption     (bit 6):                %b\n", prefix, generalPurposeFlag.isStrongEncryption());
        if (generalPurposeFlag.isUtf8())
            out.format("%s  UTF-8 names          (bit 11):                %b\n", prefix, generalPurposeFlag.isUtf8());
        /*
        file security status  (bit 0):                not encrypted
file security status  (bit 0):                encrypted

extended local header (bit 3):                no
extended local header (bit 3):                yes

UTF-8 names          (bit 11):                yes

strong encryption     (bit 6):                yes
         */
    }

}
