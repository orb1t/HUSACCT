package domain.direct.violating;

import technology.direct.dao.CallInstanceOuterClassDAO;

public class CallConstructorInnerClass{
	
	public CallConstructorInnerClass() {
		CallInstanceOuterClassDAO.CallInstanceInnerClassDAO v = new CallInstanceOuterClassDAO.CallInstanceInnerClassDAO("test");
	}
	
}