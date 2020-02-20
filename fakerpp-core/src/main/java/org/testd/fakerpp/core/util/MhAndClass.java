package org.testd.fakerpp.core.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.lang.invoke.MethodHandle;

@RequiredArgsConstructor
@Getter
public class MhAndClass {
    private final MethodHandle mh;
    private final Class clazz;
}
