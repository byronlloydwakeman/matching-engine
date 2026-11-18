package org.finance;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MainTest
{
    @Test
    void shouldRunSuccessfully()
    {
        Assertions.assertDoesNotThrow(() -> Main.main( new String[]{} ));
    }
}
