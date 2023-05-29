package model.data;

import java.util.ArrayList;

import application.tools.ConstantesIHM;

/**
 * Classe permettant de stocker les informations concernant la simulation d'un emprunt ou d'une assurance
 * et de générer le tableau d'emprunt
 *
 *
 * @author illan
 *
 */
public class Simulation {

	public String typeSimulation;
	public String typePeriode;
	public int nbPeriodes;
	public double taux;
	public double montant;
	public ArrayList<LigneTableauEmprunt> alSimulation;
	public ArrayList<LigneTableauAssurance> alSimulationAs;

	public Simulation() {
		this.typeSimulation = "";
		this.typePeriode = "";
		this.nbPeriodes = -1;
		this.taux = -1;
		this.montant = -1;
		this.alSimulation = new ArrayList<>() ;
	}

	public Simulation(String typeSimulation, String typePeriode, int nbPeriodes, double taux,double montant) {
		this.typeSimulation = typeSimulation;
		this.typePeriode = typePeriode;
		this.nbPeriodes = nbPeriodes;

		this.taux = taux / 100;
		if (this.typePeriode == ConstantesIHM.TYPE_PERIODE_1) { // Mensualités
			this.taux = this.taux/12;
		}


		this.montant = montant;
		genererSimulation();
	}

	public void genererSimulation() {
		this.alSimulationAs = new ArrayList<>() ;
		this.alSimulation = new ArrayList<>() ;

		switch (typeSimulation) {
		case ConstantesIHM.TYPE_SIMUL_1: // Emprunt taux fixe

			double capitalRestantDebut, interets,  montantARembourser, principal , capitalRestantFin;

			capitalRestantDebut = this.montant;
			interets = capitalRestantDebut*this.taux;
			montantARembourser = (this.montant*this.taux) / (1 - Math.pow((1 + this.taux), -this.nbPeriodes));
			principal = montantARembourser - interets;
			capitalRestantFin = capitalRestantDebut - principal;

			this.alSimulation.add( new LigneTableauEmprunt(1, capitalRestantDebut, interets, principal, montantARembourser, capitalRestantFin) );

			for (int i = 2 ; i <= this.nbPeriodes ; i++  ) {

				capitalRestantDebut = capitalRestantFin;
				interets = capitalRestantDebut*this.taux;
				principal = montantARembourser - interets;
				capitalRestantFin = capitalRestantDebut - principal;

				this.alSimulation.add( new LigneTableauEmprunt(i, capitalRestantDebut, interets, principal, montantARembourser, capitalRestantFin) );
				if (i == this.nbPeriodes) {
					this.alSimulation.get(i-1).capitalRestantFin = 0;
				}

			}

			break;
		case ConstantesIHM.TYPE_SIMUL_2: // Assurance taux fixe
			double montantAssurance,mensualite,tauxAssurance,montantEmprunt;
			int nbTemps;
			
			montantEmprunt =this.montant;
			nbTemps=this.nbPeriodes;
			mensualite=montantEmprunt/nbTemps;
			tauxAssurance=this.taux;
			montantAssurance=taux*montant*nbPeriodes;
			
			System.out.println("le montant taux d'assurance de l'agence bancaire est de "+this.taux+ " votre assurance sera d'un total de "+montantAssurance+ " soit d'une mensualité de "+montantAssurance/12);
			this.alSimulationAs.add( new LigneTableauAssurance(nbTemps, montantEmprunt, tauxAssurance, mensualite, montantAssurance));
			break;

		default:
			throw new IllegalArgumentException();
		}

	}
}
