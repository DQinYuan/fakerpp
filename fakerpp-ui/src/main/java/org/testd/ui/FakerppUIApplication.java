package org.testd.ui;

import javafx.application.Application;
import javafx.scene.Node;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.testd.ui.fxweaver.InjectionPointLazyFxControllerAndViewResolver;
import org.testd.ui.fxweaver.SpringFxWeaver;
import org.testd.ui.fxweaver.core.FxControllerAndView;
import org.testd.ui.fxweaver.core.FxWeaver;

@SpringBootApplication
public class FakerppUIApplication {

    public static void main(String[] args) {
        Application.launch(JavaFxApplication.class, args);
    }

    @Bean
    public FxWeaver fxWeaver(ConfigurableApplicationContext applicationContext) {
        return new SpringFxWeaver(applicationContext);
    }

    @Bean
    public InjectionPointLazyFxControllerAndViewResolver injectionPointLazyFxControllerAndViewResolver(
            FxWeaver fxWeaver) {
        return new InjectionPointLazyFxControllerAndViewResolver(fxWeaver);
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public <C, V extends Node> FxControllerAndView<C, V> fxControllerAndView(
            InjectionPointLazyFxControllerAndViewResolver injectionPointLazyFxControllerAndViewResolver,
            InjectionPoint injectionPoint) {
        return injectionPointLazyFxControllerAndViewResolver.resolve(injectionPoint);
    }

}
