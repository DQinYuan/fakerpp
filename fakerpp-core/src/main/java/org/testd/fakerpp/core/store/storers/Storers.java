package org.testd.fakerpp.core.store.storers;

import lombok.RequiredArgsConstructor;
import org.javatuples.Triplet;
import org.reflections.Reflections;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.testd.fakerpp.core.ERMLException;
import org.testd.fakerpp.core.parser.ast.DataSourceInfo;
import org.testd.fakerpp.core.util.MyReflectUtil;
import org.testd.fakerpp.core.util.MyStringUtil;

import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class Storers {

    @Cacheable("getInitedStorer")
    public Storer getInitedStorer(DataSourceInfo dsi) throws ERMLException {
        Storer storer = storers().get(dsi.getType())
                .get(dsi.getStorer()).get();
        storer.init(dsi);
        return storer;
    }


    private Pattern storerExtrator =
            Pattern.compile("org\\.testd\\.fakerpp\\.core\\.store\\.storers\\.(.+)\\.(.+)Storer");

    /**
     *
     * @return type -> storer -> storer supplier
     */
    @Cacheable("storers")
    public Map<String, Map<String, Supplier<Storer>>> storers() {
        String pack = "org.testd.fakerpp.core.store.storers";
        return MyReflectUtil.subtypes(pack, Storer.class)
                .filter(s -> s.getSimpleName().endsWith("Storer") && !Modifier.isAbstract(s.getModifiers()))
                .filter(s -> !s.getPackage().getName().equals(pack))
                .map(s -> {
                    Matcher matcher = storerExtrator.matcher(s.getName());
                    if (matcher.find()) {
                        String type = matcher.group(1);
                        String storer = matcher.group(2);
                        return new Triplet<String, String, Class<? extends Storer>>(type,
                                MyStringUtil.camelToDelimit(storer), s);
                    }

                    throw new RuntimeException("Invalid storter");
                }).collect(Collectors.toMap(Triplet::getValue0, t -> new LinkedHashMap<String, Supplier<Storer>>() {
                            {

                                put(t.getValue1(), () -> {
                                    try {
                                        return t.getValue2().newInstance();
                                    } catch (InstantiationException | IllegalAccessException e) {
                                        throw new RuntimeException(e);
                                    }
                                });

                            }
                        },
                        (s1, s2) -> {
                            s1.putAll(s2);
                            return s1;
                        }
                        , LinkedHashMap::new));
    }


}
