/*
 * HUMBOLDT: A Framework for Data Harmonisation and Service Integration.
 * EU Integrated Project #030962                 01.10.2006 - 30.09.2010
 * 
 * For more information on the project, please refer to the this web site:
 * http://www.esdi-humboldt.eu
 * 
 * LICENSE: For information on the license under which this program is 
 * available, please refer to http:/www.esdi-humboldt.eu/license.html#core
 * (c) the HUMBOLDT Consortium, 2007 to 2011.
 */

package eu.esdihumboldt.hale.ui.views.data.internal.explore;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.ViewerCell;

import eu.esdihumboldt.hale.common.instance.model.Group;
import eu.esdihumboldt.hale.common.instance.model.Instance;
import eu.esdihumboldt.util.Pair;

/**
 * Label provider for instances values in a tree based on a 
 * {@link InstanceContentProvider}.
 * @author Simon Templer
 */
public class InstanceValueLabelProvider extends StyledCellLabelProvider {

	/**
	 * @see CellLabelProvider#update(ViewerCell)
	 */
	@Override
	public void update(ViewerCell cell) {
		TreePath treePath = cell.getViewerRow().getTreePath();
		Object element = treePath.getLastSegment();
		
		Object value = ((Pair<?, ?>) element).getSecond();
		
		boolean hasValue = false;
		if (value instanceof Instance) {
			hasValue = ((Instance) value).getValue() != null;
		}
		
		StyledString styledString;
		if (value == null) {
			styledString = new StyledString("no value", StyledString.DECORATIONS_STYLER);
		}
		else if (value instanceof Group && !hasValue) {
			styledString = new StyledString("+", StyledString.QUALIFIER_STYLER);
		}
		else {
			if (value instanceof Instance) {
				value = ((Instance) value).getValue();
			}
			//TODO some kind of conversion?
			styledString = new StyledString(value.toString(), null);
		}
		
		cell.setText(styledString.toString());
		cell.setStyleRanges(styledString.getStyleRanges());
		
		super.update(cell);
	}
	
}