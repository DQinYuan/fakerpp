package org.testd.fakerpp.core.store.storers;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.testd.fakerpp.core.ERMLException;
import org.testd.fakerpp.core.FakerppProperties;
import org.testd.fakerpp.core.parser.ast.DataSourceInfo;
import org.testd.fakerpp.core.util.MyReflectUtil;
import org.testd.fakerpp.core.util.MyStringUtil;
import org.testd.fakerpp.core.parser.ast.DataSourceInfo;
import org.testd.fakerpp.core.util.MyReflectUtil;
import org.testd.fakerpp.core.util.MyStringUtil;

import java.lang.invoke.MethodHandle;

@Component
@RequiredArgsConstructor
public class Storers {

    private String pathTemplate = "org.testd.fakerpp.core.store.storers.%s.%sStorer";
    private final FakerppProperties fakerppProperties;

    @Cacheable("getInitedStorer")
    public Storer getInitedStorer(DataSourceInfo dsi) throws ERMLException {
        String qulifiedName = String.format(pathTemplate, dsi.getType(), MyStringUtil.delimit2Camel(dsi.getStorer(),
                true));
        try {
            MethodHandle constructor = MyReflectUtil.getNoArgConstructor(qulifiedName, Storer.class);
            Storer storer = (Storer) constructor.invokeExact();
            storer.init(dsi, fakerppProperties.getStore().getBatchSize());
            return storer;
        } catch (ClassNotFoundException e) {
            throw new ERMLException(
                    String.format("can not found storer %s with type %s\n",
                            dsi.getStorer(), dsi.getType())
            );
        } catch (ERMLException e) {
            throw e;
        } catch (Throwable throwable) {
            throw new AssertionError(throwable);
        }
    }
}
