package modelo.servicio;

import exceptions.InstanceNotFoundException;
import modelo.ud3.Empleado;

public interface IEmpleadoServicio {
	public Empleado find(int id) throws InstanceNotFoundException;

}
