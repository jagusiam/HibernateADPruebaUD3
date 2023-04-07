package modelo.ud3;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

//9. Crear una subclase TermDepositAccount.java
//con las anotaciones necesarias para que se implemente 
//con una tabla por cada entidad con sus atributos propios, 
//de forma que la columna account_id sea la clave primaria 
//de la tabla llamada TERM_DEPOSIT_ACCOUNT y a su vez, 
//sea clave foránea hacia la clave primaria de ACCOUNT.
//Añade la configuración necesaria para que se cree 
//esta última tabla sin eliminar las ya existentes.

@Entity
@Table(name = "TERM_DEPOSIT_ACCOUNT")
@PrimaryKeyJoinColumn(name = "account_id") //en caso de JOINED STRATEGY DE HERENCIA
public class TermDepositAccount extends Account {

	private float interes;
	private int plazo_meses;
	
	public TermDepositAccount() {
		super();
		// TODO Auto-generated constructor stub
	}

	public TermDepositAccount(float interes, int plazo_meses) {
		super();
		this.interes = interes;
		this.plazo_meses = plazo_meses;
	}

	@Column(name = "INTERES")
	public float getInteres() {
		return interes;
	}

	public void setInteres(float interes) {
		this.interes = interes;
	}

	@Column(name = "PLAZO")
	public int getPlazo_meses() {
		return plazo_meses;
	}

	public void setPlazo_meses(int plazo_meses) {
		this.plazo_meses = plazo_meses;
	}
	
	
	
	
	
	
}
