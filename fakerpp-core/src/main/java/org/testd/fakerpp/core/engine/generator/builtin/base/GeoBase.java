/**
 * Alipay.com Inc. Copyright (c) 2004-2018 All Rights Reserved.
 */
package org.testd.fakerpp.core.engine.generator.builtin.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.testd.fakerpp.core.util.earclipping.Point;
import org.geojson.*;
import org.testd.fakerpp.core.util.MyStringUtil;

import java.io.IOException;
import java.util.*;

@RequiredArgsConstructor
public class GeoBase {

    private final String formatter;

    private final String input;

    public String format(Double lat, Double lng) {
        Map<String, String> param = new HashMap<>();
        param.put("Lat", lat.toString());
        param.put("Lng", lng.toString());

        return MyStringUtil.replace(formatter, param);
    }

    public List<String> result(Double lat, Double lng, int colNum) {
        /**
         * 只需要生成一列时则直接生成 格式化 的复合字符串
         */
        if (colNum == 1) {
            return Arrays.asList(format(lat, lng));
        }
        /**
         * 生成两列时则分别生成纬度和经度列
         */
        if (colNum == 2) {
            return Arrays.asList(lat.toString(), lng.toString());
        }
        /**
         * 生成三列时则 分别生成 纬度列 经度列 以及 格式化的复合列
         */
        if (colNum == 3) {
            return Arrays.asList(lat.toString(), lng.toString(), format(lat, lng));
        }

        throw new RuntimeException("不支持生成3列以上");

    }

    public List<List<org.testd.fakerpp.core.util.earclipping.Point>> points(String origin) {
        switch (input) {
            case "wkt":
                return wkt2Points(origin);
            case "geojson":
                return geojson2Points(origin);
            default:
                throw new RuntimeException("geo生成器不支持input属性设置为 " + input);
        }
    }

    /**
     * 将wkt表示的经纬度围栏转换为Point对象列表表示
     * <p>
     * 这里返回二维List是为了兼容geojson，geojson是允许同时配置多个多边形的
     *
     * 第一个维度代表不同的多边形
     * 第二个维度代表多边形围栏
     *
     * @param wkt
     * @return
     */
    public List<List<org.testd.fakerpp.core.util.earclipping.Point>> wkt2Points(String wkt) {
        String[] latLngs = wkt
                .trim().split(",");

        List<org.testd.fakerpp.core.util.earclipping.Point> points = new ArrayList<>();

        for (String latLng : latLngs) {
            double[] latLngDouble = toLatLng(latLng);
            points.add(new org.testd.fakerpp.core.util.earclipping.Point(latLngDouble[0], latLngDouble[1]));
        }

        return Arrays.asList(checkEnd2End(points));
    }

    /**
     * 将geojson表示的经纬度围栏转换为Point对象表示。
     *
     * 返回的是一个二维List:
     *      第一个维度代表不同的多边形
     *      第二个维度代表多边形围栏
     * @param geojson
     * @return
     */
    public List<List<org.testd.fakerpp.core.util.earclipping.Point>> geojson2Points(String geojson) {
        try {
            GeoJsonObject geoJsonObject = new ObjectMapper().readValue(geojson.trim(), GeoJsonObject.class);
            if (!(geoJsonObject instanceof FeatureCollection)) {
                throw new RuntimeException("geo生成器的输入geojson仅支持featureCollection格式的配置");
            }

            FeatureCollection fc = (FeatureCollection) geoJsonObject;
            List<List<org.testd.fakerpp.core.util.earclipping.Point>> result = new ArrayList<>();
            for (Feature feature : fc.getFeatures()) {
                GeoJsonObject geometry = feature.getGeometry();
                if ( !(geometry instanceof Polygon) ){
                    throw new RuntimeException("geo生成器的输入geojson的geometry仅仅支持Polygon类型");
                }

                Polygon polygon = (Polygon) geometry;
                List<org.testd.fakerpp.core.util.earclipping.Point> polygonPoints = new ArrayList<>();
                for (LngLatAlt lngLat : polygon.getExteriorRing()) {
                    polygonPoints.add(new org.testd.fakerpp.core.util.earclipping.Point(lngLat.getLatitude(), lngLat.getLongitude()));
                }
                result.add(checkEnd2End(polygonPoints));
            }

            return result;

        } catch (IOException e) {
            throw new RuntimeException("geojson解析出错");
        }
    }

    /**
     * 检查多边形围栏有没有首尾相接(即最后一个点等于第一个点)
     * 如果没有首尾相接则报错,
     * 如果相接的话则去除最后一个点
     * @param origin
     * @return 去除了重复的首尾相接点后的多边形围栏
     */
    public List<org.testd.fakerpp.core.util.earclipping.Point> checkEnd2End(List<org.testd.fakerpp.core.util.earclipping.Point> origin){

        org.testd.fakerpp.core.util.earclipping.Point start = origin.get(0);
        org.testd.fakerpp.core.util.earclipping.Point end = origin.get(origin.size() - 1);

        if ( !(start.equals(end)) ){
            throw new RuntimeException("多边形围栏的首尾必须相接,即围栏的最后一个点的位置要和第一个点相同");
        }

        return origin.subList(0, origin.size() - 1);
    }

    /**
     * 将字符串"纬度 经度" 转化为double数组[纬度, 经度]
     *
     * @param latLngStr "纬度 经度"
     * @return double[纬度, 经度]
     */
    public double[] toLatLng(String latLngStr) {
        String[] latLng = latLngStr.split(" ");
        if (latLng.length != 2) {
            throw new RuntimeException("每个端点必须给一对经纬度");
        }
        return new double[] {Double.parseDouble(latLng[0]),
                Double.parseDouble(latLng[1])};
    }
}