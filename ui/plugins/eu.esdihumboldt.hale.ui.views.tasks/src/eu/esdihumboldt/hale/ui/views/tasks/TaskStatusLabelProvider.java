/*
 * HUMBOLDT: A Framework for Data Harmonisation and Service Integration.
 * EU Integrated Project #030962                 01.10.2006 - 30.09.2010
 * 
 * For more information on the project, please refer to the this web site:
 * http://www.esdi-humboldt.eu
 * 
 * LICENSE: For information on the license under which this program is 
 * available, please refer to http:/www.esdi-humboldt.eu/license.html#core
 * (c) the HUMBOLDT Consortium, 2007 to 2010.
 */

package eu.esdihumboldt.hale.ui.views.tasks;

import org.eclipse.jface.viewers.TreeNode;

import eu.esdihumboldt.hale.ui.util.tree.MultiColumnTreeNodeLabelProvider;
import eu.esdihumboldt.hale.ui.views.tasks.model.ResolvedTask;

/**
 * Task status labels 
 *
 * @author Simon Templer
 * @partner 01 / Fraunhofer Institute for Computer Graphics Research
 */
public class TaskStatusLabelProvider extends MultiColumnTreeNodeLabelProvider {

	/**
	 * @see MultiColumnTreeNodeLabelProvider#MultiColumnTreeNodeLabelProvider(int)
	 */
	public TaskStatusLabelProvider(int columnIndex) {
		super(columnIndex);
	}

	/**
	 * @see MultiColumnTreeNodeLabelProvider#getValueText(Object, TreeNode)
	 */
	@Override
	protected String getValueText(Object value, TreeNode node) {
		if (value instanceof ResolvedTask) {
			ResolvedTask task = (ResolvedTask) value;
			return task.getTaskStatus().toString();
		}
		else {
			return null;
		}
	}

}