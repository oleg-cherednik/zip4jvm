package com.cop.zip4j.core.writers;

import com.cop.zip4j.io.DataOutputStream;
import lombok.NonNull;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 02.08.2019
 */
public interface DataOutput {

    void write(@NonNull DataOutputStream out) throws IOException;
}
