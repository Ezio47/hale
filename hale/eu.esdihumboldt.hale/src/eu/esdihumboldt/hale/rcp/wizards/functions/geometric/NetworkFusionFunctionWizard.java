package eu.esdihumboldt.hale.rcp.wizards.functions.geometric;

import org.eclipse.jface.wizard.Wizard;

import eu.esdihumboldt.hale.rcp.wizards.functions.AbstractSingleCellWizard;
import eu.esdihumboldt.hale.rcp.wizards.functions.AlignmentInfo;

/**
 * Network fusion wizard
 * 
 * @author ?, Simon Templer
 * @partner 01 / Fraunhofer Institute for Computer Graphics Research
 * @version $Id$ 
 */
public class NetworkFusionFunctionWizard extends AbstractSingleCellWizard {

	/**
	 * @see AbstractSingleCellWizard#AbstractSingleCellWizard(AlignmentInfo)
	 */
	public NetworkFusionFunctionWizard(AlignmentInfo selection) {
		super(selection);
	}

	/**
	 * @see AbstractSingleCellWizard#init()
	 */
	@Override
	protected void init() {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * @see Wizard#performFinish()
	 */
	@Override
	public boolean performFinish() {
		// TODO Auto-generated method stub
		return false;
	}

}
