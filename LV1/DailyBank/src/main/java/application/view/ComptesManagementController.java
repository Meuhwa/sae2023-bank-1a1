package application.view;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import application.DailyBankApp;
import application.DailyBankState;
import application.control.ComptesManagement;
import application.tools.ConstantesIHM;
import application.tools.RelevesBancaire;
import application.tools.StageManagement;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import model.data.AgenceBancaire;
import model.data.Client;
import model.data.CompteCourant;
import model.data.Employe;
import model.orm.Access_BD_AgenceBancaire;

public class ComptesManagementController {

	// Etat courant de l'application
	private DailyBankState dailyBankState;

	// Contrôleur de Dialogue associé à ComptesManagementController
	private ComptesManagement cmDialogController;

	// Fenêtre physique ou est la scène contenant le fichier xml contrôlé par this
	private Stage primaryStage;


	// Données de la fenêtre
	private Client clientDesComptes;
	private ObservableList<CompteCourant> oListCompteCourant;
	public AgenceBancaire unAg;
	public Employe unEmploye;
	
	
	

	// Manipulation de la fenêtre
	public void initContext(Stage _containingStage, ComptesManagement _cm, DailyBankState _dbstate, Client client) {
		this.cmDialogController = _cm;
		this.primaryStage = _containingStage;
		this.dailyBankState = _dbstate;
		this.clientDesComptes = client;
		this.configure();
	}

	private void configure() {
		String info;

		this.primaryStage.setOnCloseRequest(e -> this.closeWindow(e));

		this.oListCompteCourant = FXCollections.observableArrayList();
		this.lvComptes.setItems(this.oListCompteCourant);
		this.lvComptes.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		this.lvComptes.getFocusModel().focus(-1);
		this.lvComptes.getSelectionModel().selectedItemProperty().addListener(e -> this.validateComponentState());

		info = this.clientDesComptes.nom + "  " + this.clientDesComptes.prenom + "  (id : "
				+ this.clientDesComptes.idNumCli + ")";
		this.lblInfosClient.setText(info);

		this.loadList();
		this.validateComponentState();
	}

	public void displayDialog() {
		this.primaryStage.showAndWait();
	}

	// Gestion du stage
	private Object closeWindow(WindowEvent e) {
		this.doCancel();
		e.consume();
		return null;
	}

	// Attributs de la scene + actions

	@FXML
	private Label lblInfosClient;
	@FXML
	private ListView<CompteCourant> lvComptes;
	@FXML
	private Button btnVoirOpes;
	@FXML
	private Button btnGenererReleve;
	@FXML
	private Button btnModifierCompte;
	@FXML
	private Button btnSupprCompte;
	@FXML
	private Button btnClôtureCompte;
	@FXML
	private Button btnNouveauCompte;


	@FXML
	private void doCancel() {
		this.primaryStage.close();
	}

	@FXML
	private void doVoirOperations() {
		int selectedIndice = this.lvComptes.getSelectionModel().getSelectedIndex();
		if (selectedIndice >= 0) {
			CompteCourant cpt = this.oListCompteCourant.get(selectedIndice);
			this.cmDialogController.gererOperationsDUnCompte(cpt);

		}
		this.loadList();
		this.validateComponentState();
	}

	@FXML
	/**
	 * Permet de faire générer le relevé bancaire du compte selectionné pour des dates choisies dans une page de dialogue
	 * 
	 * @author illan
	 */
	private void doGenererReleve() {

		int selectedIndice = this.lvComptes.getSelectionModel().getSelectedIndex();
		CompteCourant compteSelected = this.oListCompteCourant.get(selectedIndice);
		
		
		try {
			FXMLLoader loader = new FXMLLoader(
					OperationEditorPaneController.class.getResource("releveeditorpane.fxml"));
			BorderPane root = loader.load();

			Scene scene = new Scene(root,900,500);
			scene.getStylesheets().add(DailyBankApp.class.getResource("application.css").toExternalForm());

			Stage releveStage = new Stage();
			releveStage.initModality(Modality.WINDOW_MODAL);
			releveStage.initOwner(this.primaryStage);
			StageManagement.manageCenteringStage(this.primaryStage, releveStage);
			releveStage.setScene(scene);
			releveStage.setTitle("Création d'un relevé bancaire");
			releveStage.setResizable(false);

			ReleveEditorPaneController repController = loader.getController();
			repController.initContext(this.dailyBankState, this.primaryStage, this.clientDesComptes , compteSelected);
			releveStage.showAndWait();

		} catch (Exception e) {
			e.printStackTrace();
		}
		
	
	}
	/**
	 * Permet de changer le text du bouton cloturer et reouvrir
	 *
	 * @author Bilon
	 */
	private void afficheText(CompteCourant cc) {
		
//		if(cc.estCloture.equals("O")) {
//			btnClôtureCompte.setText("ReOuvrir");
//		}else {

		if(cc.estCloture.equals("O")) {
			btnClôtureCompte.setText("ReOuvrir");
		}else {
			btnClôtureCompte.setText("Cloturer");
			}
	}
	
	/**
	 * Permet de cloturer un compte
	 *
	 * @author Bilon
	 */
	@FXML
	private void doClotureCompte() {
		int selectedIndice = this.lvComptes.getSelectionModel().getSelectedIndex();
		CompteCourant cpt = this.oListCompteCourant.get(selectedIndice);
		if (selectedIndice >= 0) {
			this.cmDialogController.cloture(cpt);
		}
		this.loadList();
		this.validateComponentState();
	}

	@FXML
	/**
	 * 
	 * @author illan
	 */
	private void doModifierCompte() {
		int selectedIndice = this.lvComptes.getSelectionModel().getSelectedIndex();
		CompteCourant compte = this.oListCompteCourant.get(selectedIndice);


		compte = this.cmDialogController.modifierCompte(compte);
		if (compte != null) {
			this.loadList();
			validateComponentState();
		}

	}

	@FXML
	/**
	 * Permet de supprimer le compte selectionné
	 *
	 * @author illan
	 */
	private void doSupprimerCompte() {

		int selectedIndice = this.lvComptes.getSelectionModel().getSelectedIndex();
		this.cmDialogController.suppressionCompte(this.oListCompteCourant.get(selectedIndice));
		this.loadList();
		validateComponentState();
	}

	@FXML
	/**
	 * Permet de crée un nouveau compte
	 *
	 * @author Bilon
	 */
	private void doNouveauCompte() {
		CompteCourant compte;
		compte = this.cmDialogController.creerNouveauCompte();
		if (compte != null) {
			this.oListCompteCourant.add(compte);
		}
	}


	private void loadList() {
		ArrayList<CompteCourant> listeCpt;
		listeCpt = this.cmDialogController.getComptesDunClient();
		this.oListCompteCourant.clear();
		this.oListCompteCourant.addAll(listeCpt);
	}

	private void validateComponentState() {
		// Non implémenté => désactivé


		int selectedIndice = this.lvComptes.getSelectionModel().getSelectedIndex();

		if(this.clientDesComptes.estInactif.equals(ConstantesIHM.CLIENT_INACTIF)){
			this.btnNouveauCompte.setDisable(true);
			this.btnModifierCompte.setDisable(true);
			this.btnClôtureCompte.setDisable(true);
			this.btnSupprCompte.setDisable(true);
			if (selectedIndice >= 0) {
				CompteCourant cpt = this.oListCompteCourant.get(selectedIndice);
				this.btnVoirOpes.setDisable(false);	
				this.btnGenererReleve.setDisable(false);
			}
			else {
				this.btnVoirOpes.setDisable(true);	
			}
		}
		else {
			this.btnNouveauCompte.setDisable(false);

			if (selectedIndice >= 0) {
				CompteCourant cpt = this.oListCompteCourant.get(selectedIndice);
				afficheText(cpt);

				this.btnVoirOpes.setDisable(false);
				this.btnGenererReleve.setDisable(false);


				//si le compte est cloturé
				if (cpt.estCloture.equals("O")) {
					this.btnSupprCompte.setDisable(false);
					this.btnClôtureCompte.setDisable(true);
					this.btnModifierCompte.setDisable(true);
					//si le compte est cloturer
					if (cpt.estCloture.equals("O")) {
						this.btnClôtureCompte.setDisable(true);
					}
				}
					
				
					//si la liste est vide et le compte cloturer
					if (cpt.estCloture.equals("O")) {
						this.btnSupprCompte.setDisable(false);
						this.btnClôtureCompte.setDisable(true);
						this.btnModifierCompte.setDisable(true);
						
					}else {
						this.btnSupprCompte.setDisable(true);
						if(ClientsManagementController.estInactif.equals("N")){
							this.btnModifierCompte.setDisable(false);
							this.btnClôtureCompte.setDisable(false);
						}
					}

				}else {
					this.btnSupprCompte.setDisable(true);
					this.btnClôtureCompte.setDisable(false);
					this.btnModifierCompte.setDisable(false);

				}

			
		}
	}
}