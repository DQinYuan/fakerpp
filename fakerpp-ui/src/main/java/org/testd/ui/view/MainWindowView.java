package org.testd.ui.view;

import com.dlsc.formsfx.model.structure.Field;
import com.dlsc.formsfx.model.structure.Form;
import com.dlsc.formsfx.model.structure.Group;
import com.dlsc.formsfx.model.validators.CustomValidator;
import javafx.application.Platform;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.stage.DirectoryChooser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.testd.fakerpp.core.ERMLException;
import org.testd.fakerpp.core.ERMLExecutor;
import org.testd.fakerpp.core.parser.ast.ERML;
import org.testd.ui.PrimaryStageHolder;
import org.testd.ui.UiPreferences;
import org.testd.ui.controller.DrawBoardController;
import org.testd.ui.fxweaver.core.FxWeaver;
import org.testd.ui.fxweaver.core.FxmlView;
import org.testd.ui.model.DataSourceInfoProperty;
import org.testd.ui.model.ERMLProperty;
import org.testd.ui.model.MetaProperty;
import org.testd.ui.model.TableProperty;
import org.testd.ui.util.*;
import org.testd.ui.view.dynamic.TaskStateView;
import org.testd.ui.view.dynamic.WorkSpaceConfigView;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;


@Component
@FxmlView
@RequiredArgsConstructor
public class MainWindowView {

    //------------ di
    private final DrawBoardView drawBoardView;
    private final DrawBoardController drawBoardController;
    private final MetaView metaView;
    private final FxWeaver fxWeaver;
    private final ERMLExecutor ermlExecutor;
    private final UiPreferences uiPreferences;
    private final PrimaryStageHolder primaryStageHolder;

    //------------ JavaFx Component

    @FXML
    private ScrollPane boardScroll;
    @FXML
    private SplitPane mainSplit;
    @FXML
    private Button runButton;
    @FXML
    private Tab mainTab;

    private ERMLProperty ermlProperty;
    private StringProperty modelName = new SimpleStringProperty();

    private TaskStateView waitStateView;
    private TaskStateView completeStateView;
    private TaskStateView failureStateView;

    @FXML
    private void initialize() {
        // init meta
        mainSplit.getItems().set(0, fxWeaver.loadView(MetaView.class));

        // init drawBoard
        boardScroll.setContent(drawBoardView);


        // init state views
        waitStateView = new TaskStateView(
                ResourceUtil.loadImageView("wait.png"),
                "Generating data into data sources, please wait"
        );
        completeStateView = new TaskStateView(
                ResourceUtil.loadImageView("complete.png"),
                "Successfully"
        );
        failureStateView = new TaskStateView(
                ResourceUtil.loadImageView("failure.png"),
                "Failure with exception"
        );

        // init tab title
        StringBinding modelNameBinding = new StringBinding() {
            {
                super.bind(modelName);
            }

            @Override
            protected String computeValue() {
                return modelName.get() == null ? "Undefined" : modelName.get();
            }
        };
        mainTab.textProperty().bind(modelNameBinding);
    }

    @FXML
    private void handleExit() {
        Platform.exit();
    }

    public void initFromErml(ERMLProperty ermlProperty, String title) {
        this.ermlProperty = ermlProperty;

        // bind meta info
        MetaProperty metaProperty = ermlProperty.getMeta();
        metaView.initFromMetaProperty(metaProperty);

        drawBoardView.init(ermlProperty.getTables());

        modelName.set(title);
    }

    private static Executor ermlExecPool = new ThreadPoolExecutor(
            Runtime.getRuntime().availableProcessors(),
            Runtime.getRuntime().availableProcessors(),
            0L, TimeUnit.SECONDS,
            new SynchronousQueue<>(),
            new NamedThreadFactory("erml-exec"),
            new ThreadPoolExecutor.AbortPolicy()
    );

    @FXML
    private void handleRun() {
        Task<Void> execTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                ermlExecutor.memoryExec(ermlProperty.unmap());
                return null;
            }
        };

        execTask.setOnSucceeded(event -> drawBoardController.translateState(completeStateView));

        execTask.exceptionProperty().addListener((observable, oldValue, newValue) -> {
            drawBoardController.translateState(failureStateView);
            FxDialogs.showException(
                    "Run error",
                    "There may be a configuration error",
                    String.format("Exec model exception %s", newValue.getMessage()),
                    newValue
            );
        });

        try {
            ermlExecPool.execute(execTask);
            drawBoardController.translateState(waitStateView);
        } catch (RejectedExecutionException reject) {
            FxDialogs.showError(
                    "Run error",
                    "not enough thread",
                    String.format(
                            "There are two many task in background, shouldn't more than %d",
                            Runtime.getRuntime().availableProcessors()
                    )
            );
        }

    }

    @FXML
    private void handleSave() {
        while (uiPreferences.get(UiPreferences.workSpaceKey) == null) {
            WorkSpaceConfigView workSpaceConfigView =
                    fxWeaver.loadControl(WorkSpaceConfigView.class);
            primaryStageHolder.newSceneInChild(workSpaceConfigView,
                    "There is no workspace now, please select one first");
        }

        // set model name
        String workSpacePath = uiPreferences.get(UiPreferences.workSpaceKey);
        while (modelName.get() == null) {
            Set<String> conflictNames;
            try {
                conflictNames =
                        Files.list(Paths.get(workSpacePath))
                                .map(Path::getFileName)
                                .map(Path::toString)
                                .collect(Collectors.toSet());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            primaryStageHolder.newSceneInChild(Forms.renderForm("config model name",
                    Collections.singletonList(Field.ofStringType(modelName)
                            .label("model name")
                            .validate(
                                    CustomValidator.forPredicate(name ->
                                                    !conflictNames.contains(name),
                                            "conflict with exist model in workspace")
                            ))
                    )
            );
        }

        Path modelBase = Paths.get(workSpacePath, modelName.get());
        FilesUtil.createDirectoriesIfNotExist(modelBase);

        ermlProperty.serial(
                (path, content) -> {
                    Path filePath = modelBase.resolve(path);
                    try {
                        FilesUtil.createDirectoriesIfNotExist(filePath.getParent());
                        Files.write(filePath, content.getBytes());
                    } catch (IOException e) {
                        FxDialogs.showException("Model Save Status",
                                "Model Save Fail",
                                String.format("exception when save file '%s'",
                                        filePath.toAbsolutePath().toString()),
                                e);
                        throw new RuntimeException(e);
                    }
                }
        );

        FxDialogs.showInformation("Model Save Status", "Model Save Successfully",
                String.format("model %s have been successfully in %s", modelName.get(),
                        modelBase.toAbsolutePath().toString()));
    }

    @FXML
    private void handleWorkspace() {
        WorkSpaceConfigView workSpaceConfigView =
                fxWeaver.loadControl(WorkSpaceConfigView.class);
        primaryStageHolder.newSceneInChild(workSpaceConfigView);
    }

}
