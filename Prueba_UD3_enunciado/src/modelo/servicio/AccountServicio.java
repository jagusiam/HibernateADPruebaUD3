package modelo.servicio;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import exceptions.SaldoInsuficienteException;
//import modelo.Dept;
import modelo.ud3.AccMovement;
import modelo.ud3.Account;
import modelo.ud3.Departamento;
import modelo.ud3.Empleado;

import exceptions.InstanceNotFoundException;
import util.SessionFactoryUtil;

public class AccountServicio implements IAccountServicio {
	
	//Extraigo el metodo de crear sesion y transaccion una sola vez y reutilizarla en los metodos de operaciones CRUD sobre la BD
	//Atributos para abrir sesion y transaccion
	private Session session;
	//private Transaction transa;	
	private Transaction tx;
	//Crea una sesión y transacción en la BD - 1 por cada operación
	private void iniciaOperacion() {
			//throws HibernateException {
		session = SessionFactoryUtil.getSessionFactory().openSession();
		tx = session.beginTransaction();
	} 
	

	@Override
	public Account findAccountById(int accId) throws InstanceNotFoundException {
		SessionFactory sessionFactory = SessionFactoryUtil.getSessionFactory();
		Session session = sessionFactory.openSession();
		
		Account account = session.get(Account.class, accId);
		if (account == null) {
			throw new InstanceNotFoundException(Account.class.getName());
		}

		session.close();
		return account;
	}
	

	@Override
	public AccMovement transferir(int accOrigen, int accDestino, double cantidad)
			throws SaldoInsuficienteException, InstanceNotFoundException, UnsupportedOperationException {

		Transaction tx = null;
		Session session = null;
		AccMovement movement = null;

		try {

			if (cantidad <= 0) {
				throw new UnsupportedOperationException();
			}
			SessionFactory sessionFactory = SessionFactoryUtil.getSessionFactory();
			session = sessionFactory.openSession();

			Account accountOrigen = session.get(Account.class, accOrigen);
			if (accountOrigen == null) {
				throw new InstanceNotFoundException(Account.class.getName() + " origen id:" + accOrigen);
			}
			BigDecimal cantidadBD = new BigDecimal(cantidad);
			if (accountOrigen.getAmount().compareTo(cantidadBD) < 0) {
				throw new SaldoInsuficienteException("No hay saldo suficiente", accountOrigen.getAmount(), cantidadBD);
			}
			Account accountDestino = session.get(Account.class, accDestino);
			if (accountDestino == null) {
				throw new InstanceNotFoundException(Account.class.getName() + " destino id:" + accDestino);
			}

			tx = session.beginTransaction();

			accountOrigen.setAmount(accountOrigen.getAmount().subtract(cantidadBD));
			accountDestino.setAmount(accountDestino.getAmount().add(cantidadBD));

			movement = new AccMovement();
			movement.setAmount(cantidadBD);
			movement.setDatetime(LocalDateTime.now());

			// Relación bidireccional
			movement.setAccountOrigen(accountOrigen);
			movement.setAccountDestino(accountDestino);
			// Son prescindibles y no recomendables en navegación bidireccional porque una
			// Account puede tener numerosos movimientos
//					accountOrigen.getAccMovementsOrigen().add(movement);
//					accountDestino.getAccMovementsDest().add(movement);
//					session.saveOrUpdate(accountOrigen);
//					session.saveOrUpdate(accountDestino);
			session.save(movement);

			tx.commit();

		} catch (Exception ex) {
			System.out.println("Ha ocurrido una exception: " + ex.getMessage());
			if (tx != null) {
				tx.rollback();
			}
			throw ex;
		} finally {
			if (session != null) {
				session.close();
			}
		}
		return movement;
	}
	

	@Override
	public AccMovement autoTransferir(int accNo, double cantidad) throws InstanceNotFoundException {

		Transaction tx = null;
		Session session = null;
		AccMovement movement = null;

		try {
			SessionFactory sessionFactory = SessionFactoryUtil.getSessionFactory();
			session = sessionFactory.openSession();

			Account account = session.get(Account.class, accNo);
			if (account == null) {
				throw new InstanceNotFoundException(Account.class.getName() + " origen id:" + accNo);
			}
			BigDecimal cantidadBD = new BigDecimal(cantidad);

			tx = session.beginTransaction();

			account.setAmount(account.getAmount().add(cantidadBD));

			movement = new AccMovement();
			movement.setAmount(cantidadBD);
			movement.setDatetime(LocalDateTime.now());

			// Relación bidireccional
			movement.setAccountOrigen(account);
			movement.setAccountDestino(account);

			session.save(movement);

			tx.commit();

		} catch (Exception ex) {
			System.out.println("Ha ocurrido una exception: " + ex.getMessage());
			if (tx != null) {
				tx.rollback();
			}
			throw ex;
		} finally {
			if (session != null) {
				session.close();
			}
		}
		return movement;
	}


	public Account saveOrUpdate(Account d) {
		SessionFactory sessionFactory = SessionFactoryUtil.getSessionFactory();
		Session session = sessionFactory.openSession();
		Transaction tx = null;

		try {
			tx = session.beginTransaction();

			session.saveOrUpdate(d);
			tx.commit();
		} catch (Exception ex) {
			System.out.println("Ha ocurrido una excepción en saveOrUpdate Account: " + ex.getMessage());
			if (tx != null) {
				tx.rollback();
			}
			throw ex;
		} finally {
			session.close();
		}
		return d;
	}

	
	
	//8. Modifica la implementación del método delete(int accId) para que permita eliminar una cuenta y todos sus movimientos en una transacción. 	
	public boolean delete(int accId) throws InstanceNotFoundException {
		SessionFactory sessionFactory = SessionFactoryUtil.getSessionFactory();
		Session session = sessionFactory.openSession();
		Transaction tx = null;
		boolean exito = false;

		try {
			tx = session.beginTransaction();
			Account account = session.get(Account.class, accId);
			//Al existir dos metodos diferentes para cuentas origen/destino - creo 2 sets 
			Set<AccMovement> accOrgMovs = account.getAccMovementsForAccountOriginId();
			Set<AccMovement> accDestMovs = account.getAccMovementsForAccountDestId();
									
			if (account != null) {
				
				//alternativa: recuperar el set y eliminar toda la coleccion
				//account.getAccMovementsForAccountDestId().removeAll(accDestMovs);
				//account.getAccMovementsForAccountOriginId().removeAll(accOrgMovs);
				
				//alternativa usando metodos de remove mov cuenta creados en la clase Account
				if (accDestMovs != null ) {
					//Iteramos por el set de movimientos y mientras haya elementos, los eliminamos
					Iterator<AccMovement> it = account.getAccMovementsForAccountDestId().iterator();
					while(it.hasNext()) {
						AccMovement accDestMov = it.next();
						account.removeAccDestMov(accDestMov); //llamada a metodo eliminar movimiento cuenta
					}
				}
				if (accOrgMovs != null ) { 
					//Iteramos por el set de movimientos y mientras haya elementos, los eliminamos
					Iterator<AccMovement> it2 = account.getAccMovementsForAccountOriginId().iterator();
					while(it2.hasNext()) {
						AccMovement accOrgMov = it2.next();
						account.removeAccOrigMov(accOrgMov); //llamada a metodo eliminar movimiento cuenta
					}
				}
				session.remove(account); //eliminamos la cuenta
				
			} else {
				throw new InstanceNotFoundException(Account.class.getName() + " id: " + accId);
			}
			tx.commit();
			exito = true;
			
		} catch (Exception ex) {
			//corrijo comentario
			System.out.println("Ha ocurrido una excepción eliminando Dept: " + ex.getMessage());
			//System.out.println("Ha ocurrido una excepción en create Dept: " + ex.getMessage());
			if (tx != null) {
				tx.rollback();
			}

			throw ex;
		} finally {
			session.close();
		}
		return exito;
	}

	
	
	//5. Un método para que cree una consulta HQL que devuelva todas las cuentas que puede tener un empleado. 
	//Utiliza parámetros en la consulta.
	//TODO: Revisar porque repite la impresion de cada cuenta 5 veces ?
	@Override
	public List<Account> getAccountsByEmpno(int empno) {
		// TODO Auto-generated method stub
		//Crear una sesionFactory a partir de la clase SessionFactory Util y abrir la sesion
		//Sustituido por llamada al metodo privado de esta clase: iniciaOperacion() - llamado dentro del bloque try-catch abajo;
		//SessionFactory sessionFactory = SessionFactoryUtil.getSessionFactory();
		//Session session = sessionFactory.openSession();
		
		//declaracion de lista de tipo clase Account
		List<Account> accountsEmp = null;
		
		try {
			
			iniciaOperacion(); //llamada al metodo nuevo creado para inciar operacion con BD
	
			//Primero recuperamos el empleado por el id solicitado
			Empleado empleado = session.get(Empleado.class, empno);
			
			//ya que get() devuelve null si no existe, gestionamos este caso
			//imprimiendo un mensaje en caso de que este sea null
			if (empleado == null) {
				//1. Un sencillo mnsje de error
				System.out.println("No se ha encontrado empleado con este id: " + empno);
				//2. o que lance una nueva excepcion, para ello hay que añadir también throws InstanceNotFoundException en la firma del metodo
				//throw new InstanceNotFoundException(Empleado.class.getName());
		    } 
						
			
			//TODO: revisar la query - ok
			//Cadena de consulta con uso de parametros: El valor del parametro se asigna con el método setParameter("name", vble)
			//donde name es el identificador del parametro y vble su valor
			accountsEmp = session.createQuery("SELECT e.accounts from Empleado e inner join e.accounts WHERE e.empno = :empId")
					.setParameter("empId", empno)
					.list();		

			//imrpimir uno por uno los datos de cuentas aspociada a un empleado			
			//para comprobar - se imprime la consulta	
//			for (Account account : accountsEmp) {
//					System.out.println("Cuenta " + accountsEmp.indexOf(account) + account);
//			}
			
		} catch (Exception ex) {
			System.err.println("Ha ocurrido una excepcion " + ex.getMessage());
		}
				
		session.close();
		
		return accountsEmp; //devuelve la lista de cuentas de un Empleado solicitado
	}
	
	
	
	//6.Un metodo para que devuelva la lista de titulares de una cuenta por su id. 
	//Si no se encuentra la cuenta, lanzará una InstanceNotFoundException.
	//deberá mostrarse cuando se seleccione una cuenta en la lista de la interfaz gráfica. 
	@Override
	public List<Empleado> getTitularesByAccountId(int accId) throws InstanceNotFoundException {
		// TODO Auto-generated method stub
//		SessionFactory sessionFactory = SessionFactoryUtil.getSessionFactory();
//		Session session = sessionFactory.openSession();
		
		iniciaOperacion(); //llamo al metodo para iniciar operacion
		
		//gestionar el caso en el que el id de cuenta proporcionado no exista
		Account account = session.get(Account.class, accId);
		if (account == null) {
			throw new InstanceNotFoundException(Account.class.getName() + " id de cuenta: " + account);
		} 
		
		List<Empleado> titulares = session.createQuery("select a.employees from Account a where a.accountno = : accno").setParameter("accno", accId).list();
		
		//Impresion por consola de los resultados para comprobar
		for (Empleado empleado : titulares) {
			System.out.println("Empleado: " + empleado.getEname());
		}
		
		session.close();
		
		return titulares;
	}

	
	//TODO: REVISAR PORQUE NO ME GUARDA LA CUENTA - Error!!! por el importe/amount!!! - como pasar el dato
	//para que funcione ? Posible solucion - pasar el parametro importe/amount al metodo?
	
	//7.Implementa el método para que cree una nueva cuenta asociada a un empleado. 
	//Asegúrate de crear la relación de forma bidireccional antes de guardar los cambios. Utiliza una transacción.		
	@Override
	public Account addAccountToEmployee(int empno, Account acc) {
		
		// TODO Auto-generated method stub
//		SessionFactory sessionFactory = SessionFactoryUtil.getSessionFactory();
//		Session session = sessionFactory.openSession();
		iniciaOperacion(); //empiezo sesion
		
		Empleado emp = new Empleado(); //creo un objeto empleado en memoria
		emp.setEmpno(empno); //establezco el nif de empleado pasado por usuario
		
		Account newAccount = new Account(); //creo un objeto de cuenta en memoria
		//¿como recojo el importe pasado por usuario?
		//BigDecimal saldo = new BigDecimal(0);				
		//saldo = newAccount.getAmount();
		//newAccount.setAmount(saldo);	

		//Se crea la realcion bidireccional antes de guardar los cambios
		//llamo al metodo addEmpleado(Objeto Empleado) que guarda la relacion 
		//que he creado en la clase Account
		//Es suficiente llamar a uno de ellos
		newAccount.addEmpleado(emp); 
		//emp.addAccount(newAccount);
			
		Transaction tx = null; //instancia de Transaccion 

		try {
			tx = session.beginTransaction(); //empiezo transaccion
			
			//Guardo nueva info en ambas tablas llamando al metodo save() de session
			session.saveOrUpdate(emp);
			session.saveOrUpdate(newAccount);
			
			tx.commit(); //confirmo transaccion 
			
		//Captura de excepciones	
		} catch (Exception ex) {
			System.out.println("Ha ocurrido una excepción en saveOrUpdate new Account: " + ex.getMessage());
			if (tx != null) {
				tx.rollback(); //revierto la transaccion en caso de error
			}
			throw ex;
		} finally {
			
			session.close(); //cierre de session
		}
		return newAccount;
	}

}
