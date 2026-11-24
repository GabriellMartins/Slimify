package com.gabrielmartins.slimify.api.format;

import lombok.experimental.UtilityClass;

@UtilityClass
public class SlimeFormat {

    public final String EXTENSION = ".slime";
    public final byte MAGIC_FIRST = (byte) 0xB1;
    public final byte MAGIC_SECOND = 0x0B;
    public final int HEADER_LENGTH = 3;
}
