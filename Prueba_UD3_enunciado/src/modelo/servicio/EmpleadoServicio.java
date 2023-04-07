package modelo.servicio;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import exceptions.InstanceNotFoundException;
import modelo.ud3.Empleado;
import util.SessionFactoryUtil;

public class EmpleadoServicio implements IEmpleadoServicio{

	@Override
	public Empleado find(int id) throws InstanceNotFoundException {
		SessionFactory sessionFactory = SessionFactoryUtil.getSessionFactory();
		Session session = sessionFactory.openSession();
		Empleado empleado = session.get(Empleado.class, id);
		if (empleado == null) {
			throw new InstanceNotFoundException(Empleado.class.getName());
		}

		session.close();
		return empleado;
	}

}
