package org.testd.ui;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.testd.ui.fxweaver.core.FxControllerAndView;
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

    public <T> void changeSceneFullScreenWithParam(Class<T> target, Consumer<T> initer) {
        changeScene(target, s  -> s.setMaximized(true), initer);
    }

    public <T> void changeScene(Class<T> target) {
        changeScene(target, s -> {}, t -> {});
    }

    public <T> void changeScene(Class<T> target, Consumer<Stage> otherSet,
                               Consumer<T> initer) {
        FxControllerAndView<T, Parent> loaded = fxWeaver.load(target);
        primaryStage.getScene().setRoot(loaded.getView().get());
        initer.accept(loaded.getController());
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

    public void newSceneInChild(Parent content, String title) {
        Stages.newSceneInChild(content, primaryStage.getScene().getWindow(), title);
    }
}
