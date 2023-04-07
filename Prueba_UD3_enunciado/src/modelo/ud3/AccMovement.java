package modelo.ud3;
// Generated 31 mar 2023 8:53:10 by Hibernate Tools 4.3.6.Final

import java.math.BigDecimal;
import java.time.LocalDateTime;
//import java.util.Date; //cambiado a LocalDateTime de java.time
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * AccMovement generated by hbm2java
 */
@Entity
@Table(name = "ACC_MOVEMENT")
public class AccMovement implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2352141146741415139L;
	private int accountMovId;
	private Account accountDestino; 
	private Account accountOrigen;
	private BigDecimal amount;
	private LocalDateTime datetime;

	
	
	public AccMovement() {
	}

	public AccMovement(int accountMovId, Account accountDestino, Account accountOrigen,
			BigDecimal amount, LocalDateTime datetime) {
		this.accountMovId = accountMovId;
		this.accountDestino = accountDestino;
		this.accountOrigen = accountOrigen;
		this.amount = amount;
		this.datetime = datetime;
	}

	@Id
	//@GeneratedValue(strategy = IDENTITY)
	@Column(name = "ACCOUNT_MOV_ID", unique = true, nullable = false)
	public int getAccountMovId() {
		return this.accountMovId;
	}

	public void setAccountMovId(int accountMovId) {
		this.accountMovId = accountMovId;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ACCOUNT_DEST_ID", nullable = false)
	public Account getAccountDestino() {
		return this.accountDestino;
	}

	public void setAccountDestino(Account accountDestino) {
		this.accountDestino = accountDestino;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ACCOUNT_ORIGIN_ID", nullable = false)
	public Account getAccountOrigen() {
		return this.accountOrigen;
	}

	public void setAccountOrigen(Account accountOrigen) {
		this.accountOrigen = accountOrigen;
	}

	@Column(name = "AMOUNT", nullable = false, scale = 4)
	public BigDecimal getAmount() {
		return this.amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	//@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "DATETIME", nullable = false, length = 23)
	public LocalDateTime getDatetime() {
		return this.datetime;
	}

	public void setDatetime(LocalDateTime datetime) {
		this.datetime = datetime;
	}

}
