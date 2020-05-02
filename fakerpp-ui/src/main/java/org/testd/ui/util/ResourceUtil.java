package org.testd.ui.util;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.nio.file.Paths;

public class ResourceUtil {

    public static ImageView loadImageView(String image) {
        return new ImageView(loadImage(image));
    }

    public static Image loadImage(String image) {
        return new Image(ResourceUtil.class.getResource(Paths.get("/img", image).toString())
                .toString());
    }

}
