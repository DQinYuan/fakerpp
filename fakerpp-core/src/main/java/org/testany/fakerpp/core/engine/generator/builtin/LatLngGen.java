package org.testany.fakerpp.core.engine.generator.builtin;

import org.testany.fakerpp.core.ERMLException;
import org.testany.fakerpp.core.engine.generator.Generator;
import org.testany.fakerpp.core.engine.generator.builtin.base.GeoBase;
import org.testany.fakerpp.core.util.SeedableThreadLocalRandom;

import java.util.List;

public class LatLngGen implements Generator {

    public String formatter = "${Lat} ${Lng}";

    public String input = "geojson";

    private GeoBase geoBase;
    private int colNum;

    @Override
    public void init(int colNum) throws ERMLException {
        geoBase = new GeoBase(formatter, input);
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
