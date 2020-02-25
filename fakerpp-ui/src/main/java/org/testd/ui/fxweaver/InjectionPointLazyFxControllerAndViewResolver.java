package org.testd.ui.fxweaver;

import javafx.scene.Node;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.core.ResolvableType;
import org.testd.ui.fxweaver.core.FxControllerAndView;
import org.testd.ui.fxweaver.core.FxWeaver;
import org.testd.ui.fxweaver.core.LazyFxControllerAndView;

import java.util.Optional;

public class InjectionPointLazyFxControllerAndViewResolver {

    private final FxWeaver fxWeaver;

    public InjectionPointLazyFxControllerAndViewResolver(FxWeaver fxWeaver) {
        this.fxWeaver = fxWeaver;
    }

    /**
     * Resolve generic type classes of a {@link FxControllerAndView} {@link InjectionPoint} and return a
     * {@link LazyFxControllerAndView} embedding the {@link FxWeaver#load(Class)} method for instance creation.
     *
     * @param injectionPoint the actual injection point for the {@link FxControllerAndView} to inject
     * @throws IllegalArgumentException when types could not be resolved from the given injection point
     * @noinspection unchecked
     */
    public <C, V extends Node> FxControllerAndView<C, V> resolve(InjectionPoint injectionPoint) {
        ResolvableType resolvableType = findResolvableType(injectionPoint);
        if (resolvableType == null) {
            throw new IllegalArgumentException("No ResolvableType found");
        }
        try {
            Class<C> controllerClass = (Class<C>) resolvableType.getGenerics()[0].resolve();
            return new LazyFxControllerAndView<>(() -> fxWeaver.load(controllerClass));
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Generic controller type not resolvable for injection point " + injectionPoint, e);
        }
    }

    private ResolvableType findResolvableType(InjectionPoint injectionPoint) {
        return Optional.ofNullable(injectionPoint.getMethodParameter())
                .map(ResolvableType::forMethodParameter)
                // TODO: Refactor the following to use .or() when dropping Java 8 support
                .orElse(
                        Optional.ofNullable(injectionPoint.getField())
                                .map(ResolvableType::forField)
                                .orElse(null)
                );
    }

}

