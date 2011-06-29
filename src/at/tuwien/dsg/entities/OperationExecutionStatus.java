package at.tuwien.dsg.entities;

public enum OperationExecutionStatus {

	/**
	 * The Service has just been invoked and the operation has begun its execution.
	 */
	Begin,	
	/**
	 * The Service operation has completed its execution. This verb is optional and can also be omitted for brevity.
	 */
	Finish,
	/**
	 * The Service operation has been canceled by the Service provider. 
	 */
	Cancel
}
