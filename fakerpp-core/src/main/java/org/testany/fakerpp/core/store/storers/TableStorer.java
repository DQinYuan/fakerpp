package org.testany.fakerpp.core.store.storers;

import org.testany.fakerpp.core.ERMLException;

import java.util.List;
import java.util.Map;

/**
 * it's not thread-safe
 */
public interface TableStorer {

    void store(List<String> records) throws ERMLException;

    void flush() throws ERMLException;

    Map<String, List<String>> feedBackData(List<String> excludes) throws ERMLException ;

}
