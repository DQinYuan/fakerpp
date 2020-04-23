package org.testd.fakerpp.core.engine.generator.builtin;

import org.testd.fakerpp.core.ERMLException;
import org.testd.fakerpp.core.engine.generator.Generator;
import org.testd.fakerpp.core.engine.generator.builtin.base.DefaultString;
import org.testd.fakerpp.core.engine.generator.builtin.base.GeoBase;
import org.testd.fakerpp.core.util.SeedableThreadLocalRandom;
import org.testd.fakerpp.core.ERMLException;
import org.testd.fakerpp.core.engine.generator.Generator;
import org.testd.fakerpp.core.engine.generator.builtin.base.DefaultString;
import org.testd.fakerpp.core.engine.generator.builtin.base.GeoBase;
import org.testd.fakerpp.core.util.SeedableThreadLocalRandom;

import java.util.List;

public class LatLngGen implements Generator {

    @DefaultString("${Lat} ${Lng}")
    public String formatter = "${Lat} ${Lng}";

    private GeoBase geoBase;
    private int colNum;

    @Override
    public void init(int colNum) throws ERMLException {
        geoBase = new GeoBase(formatter, "geojson");
        this.colNum = colNum;
    }

    @Override
    public List<String> nextData() throws ERMLException {
        return geoBase.result(SeedableThreadLocalRandom.nextDouble(-90, 90),
                SeedableThreadLocalRandom.nextDouble(-180, 180), colNum);
    }

    @Override
    public long dataNum() {
        return 0;
    }
}
