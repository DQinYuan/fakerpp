package org.testd.ui;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.testd.ui.fxweaver.core.FxWeaver;
import org.testd.ui.util.Stages;
import org.testd.ui.view.OpenDialogView;

import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
public class PrimaryStageHolder implements ApplicationListener<StageReadyEvent> {

    private final FxWeaver fxWeaver;
    private Stage primaryStage;

    @Override
    public void onApplicationEvent(StageReadyEvent event) {
        Stage stage = event.getSource();
        Scene scene = new Scene(fxWeaver.loadView(OpenDialogView.class));
        stage.setScene(scene);
        stage.show();
        primaryStage = stage;
    }

    public void changeSceneFullScene(Class<?> target) {
        changeSceneFullSceneWithParam(target, null, o -> {});
    }

    public <T> void changeSceneFullSceneWithParam(Class<?> target, T param, Consumer<T> paramConsumer) {
        changeScene(target, s  -> s.setMaximized(true), param, paramConsumer);
    }

    public <T> void changeScene(Class<?> target, Consumer<Stage> otherSet,
                                T param, Consumer<T> paramConsumer) {
        primaryStage.getScene().setRoot(fxWeaver.loadView(target));
        paramConsumer.accept(param);
        otherSet.accept(primaryStage);
    }

    public Stage child() {
        return Stages.child(primaryStage.getScene().getWindow());
    }

    public void newSceneInChild(Class<?> target) {
        newSceneInChild(fxWeaver.loadView(target));
    }

    public void newSceneInChild(Parent content) {
        Stages.newSceneInChild(content, primaryStage.getScene().getWindow());
    }
}
