package de.croesch.git_history;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.DepthWalk.Commit;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class Viewer extends Application {

	private final TableView<CommitRep> table = new TableView<>();
	private final ObservableList<CommitRep> data = FXCollections
			.observableArrayList();

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage stage) throws Exception {
		DirectoryChooser chooser = new DirectoryChooser();
		chooser.setTitle("Open git repo");
		File gitDir = chooser.showDialog(stage);
		if (gitDir == null) {
			Platform.exit();
			return;
		}

		FileRepositoryBuilder builder = new FileRepositoryBuilder();
		Repository repository = builder.setGitDir(gitDir).readEnvironment()
				.findGitDir().build();

		RevWalk walk = new RevWalk(repository);
		RevCommit root = walk.parseCommit(repository.resolve("HEAD"));
		walk.markStart(root);

		data.add(new CommitRep(root));
		Task populateDataTask = new Task<Object>() {

			@Override
			protected Object call() throws Exception {
				RevCommit current;
				int i = 0;
				while ((current = walk.next()) != null) {
					data.add(new CommitRep(current));
				}
				return null;
			}
		};
		new Thread(populateDataTask).start();

		Scene scene = new Scene(new Group());
		stage.setTitle("git History");
		stage.setWidth(450);
		stage.setHeight(500);

		TableColumn firstNameCol = new TableColumn("ID");
		firstNameCol.setMinWidth(100);
		firstNameCol.setCellValueFactory(new PropertyValueFactory<>("id"));

		TableColumn lastNameCol = new TableColumn("Comment");
		lastNameCol.setMinWidth(100);
		lastNameCol.setCellValueFactory(new PropertyValueFactory<>("comment"));

		table.setItems(data);
		table.getColumns().addAll(firstNameCol, lastNameCol);

		FlowPane flowPane = new FlowPane(table);
		((Group) scene.getRoot()).getChildren().addAll(flowPane);

		stage.setScene(scene);
		stage.show();
	}

	public static class CommitRep {

		private final SimpleStringProperty id;
		private final SimpleStringProperty comment;

		private CommitRep(String id, String comment) {
			this.comment = new SimpleStringProperty(comment);
			this.id = new SimpleStringProperty(id);
		}

		public CommitRep(RevCommit commit) {
			this(commit.getId().name(), commit.getShortMessage());
		}

		public String getId() {
			return id.get();
		}

		public void setId(String newValue) {
			id.set(newValue);
		}

		public String getComment() {
			return comment.get();
		}

		public void setComment(String newValue) {
			comment.set(newValue);
		}
	}
}