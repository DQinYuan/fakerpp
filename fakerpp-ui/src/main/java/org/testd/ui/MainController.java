package org.testd.ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import lombok.RequiredArgsConstructor;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Component;
import org.testd.ui.service.WeatherService;


@Component
@FxmlView("main-scene.fxml")
@RequiredArgsConstructor
public class MyController {

    //------------ di service
    private final WeatherService weatherService;

    //------------ JavaFx Component

    @FXML
    private Label weatherLabel;

    public void loadWeatherForecast(ActionEvent actionEvent) {
        weatherLabel.setText(weatherService.getWeatherForecast());
    }

}
