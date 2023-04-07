package modelo.servicio;

import exceptions.SaldoInsuficienteException;
import modelo.ud3.AccMovement;
import modelo.ud3.Account;
import modelo.ud3.Empleado;

import java.math.BigDecimal;
import java.util.List;

import exceptions.InstanceNotFoundException;

public interface IAccountServicio {
	public Account findAccountById(int accId) throws InstanceNotFoundException ;
	
	public AccMovement transferir(int accOrigen, int accDestino, double cantidad)
			throws SaldoInsuficienteException, InstanceNotFoundException, UnsupportedOperationException ;
		
	public List<Account> getAccountsByEmpno(int empno) ;
	public Account saveOrUpdate(Account d);
	
	public AccMovement autoTransferir(int accountNo, double cantidad)
			throws  InstanceNotFoundException;
	
	public boolean delete(int accId) throws InstanceNotFoundException;
	
	
	public Account addAccountToEmployee(int empno, Account acc);
	
	public List<Empleado> getTitularesByAccountId(int accId)throws InstanceNotFoundException;
}
